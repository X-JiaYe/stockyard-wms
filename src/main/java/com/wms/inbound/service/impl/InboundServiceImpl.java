package com.wms.inbound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.exception.BizException;
import com.wms.common.result.PageResult;
import com.wms.inbound.dto.AsnCreateRequest;
import com.wms.inbound.dto.AsnDetailVO;
import com.wms.inbound.dto.AsnLineItem;
import com.wms.inbound.dto.PutawayRequest;
import com.wms.inbound.dto.ReceiveRequest;
import com.wms.inbound.entity.InboundAsn;
import com.wms.inbound.entity.InboundAsnLine;
import com.wms.inbound.entity.InboundReceive;
import com.wms.inbound.enums.AsnStatus;
import com.wms.inbound.enums.QcResult;
import com.wms.inbound.mapper.InboundAsnLineMapper;
import com.wms.inbound.mapper.InboundAsnMapper;
import com.wms.inbound.mapper.InboundReceiveMapper;
import com.wms.inbound.service.InboundService;
import com.wms.stock.dto.StockChangeRequest;
import com.wms.stock.enums.RefType;
import com.wms.stock.enums.StockDirection;
import com.wms.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 入库实现：ASN → 收货（含质检）→ 上架（落库存）。
 */
@Service
@RequiredArgsConstructor
public class InboundServiceImpl implements InboundService {

    private final InboundAsnMapper asnMapper;
    private final InboundAsnLineMapper asnLineMapper;
    private final InboundReceiveMapper receiveMapper;
    private final StockService stockService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InboundAsn createAsn(AsnCreateRequest req) {
        InboundAsn asn = new InboundAsn();
        asn.setAsnNo(genAsnNo());
        asn.setWarehouseId(req.getWarehouseId());
        asn.setSupplierName(req.getSupplierName());
        asn.setRemark(req.getRemark());
        asn.setStatus(AsnStatus.PENDING.getValue());
        asnMapper.insert(asn);

        for (AsnLineItem item : req.getLines()) {
            InboundAsnLine line = new InboundAsnLine();
            line.setAsnId(asn.getId());
            line.setSkuId(item.getSkuId());
            line.setExpectedQty(item.getExpectedQty());
            line.setReceivedQty(BigDecimal.ZERO);
            line.setQualifiedQty(BigDecimal.ZERO);
            line.setPutawayQty(BigDecimal.ZERO);
            line.setLotNo(item.getLotNo() == null ? "" : item.getLotNo());
            asnLineMapper.insert(line);
        }
        return asn;
    }

    @Override
    public PageResult<InboundAsn> queryAsns(Long warehouseId, Integer status, long pageNum, long pageSize) {
        LambdaQueryWrapper<InboundAsn> qw = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            qw.eq(InboundAsn::getWarehouseId, warehouseId);
        }
        if (status != null) {
            qw.eq(InboundAsn::getStatus, status);
        }
        qw.orderByDesc(InboundAsn::getId);
        return PageResult.of(asnMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @Override
    public AsnDetailVO getAsn(Long id) {
        InboundAsn asn = asnMapper.selectById(id);
        if (asn == null) {
            throw new BizException("ASN 不存在");
        }
        List<InboundAsnLine> lines = asnLineMapper.selectList(
                new LambdaQueryWrapper<InboundAsnLine>()
                        .eq(InboundAsnLine::getAsnId, id)
                        .orderByAsc(InboundAsnLine::getId));
        AsnDetailVO vo = new AsnDetailVO();
        vo.setAsn(asn);
        vo.setLines(lines);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InboundAsnLine receive(ReceiveRequest req) {
        InboundAsnLine line = asnLineMapper.selectForUpdateById(req.getAsnLineId());
        if (line == null) {
            throw new BizException("ASN 明细行不存在");
        }
        if (req.getQcResult() == null
                || (req.getQcResult() != QcResult.PASS.getValue()
                    && req.getQcResult() != QcResult.FAIL.getValue())) {
            throw new BizException("质检结论取值非法");
        }
        BigDecimal qty = req.getQty();
        if (line.getReceivedQty().add(qty).compareTo(line.getExpectedQty()) > 0) {
            throw new BizException("收货数量超出预期，可收 " +
                    line.getExpectedQty().subtract(line.getReceivedQty()));
        }
        InboundAsn asn = asnMapper.selectById(line.getAsnId());
        if (asn == null) {
            throw new BizException("ASN 不存在");
        }

        // 记录收货明细
        InboundReceive receive = new InboundReceive();
        receive.setAsnId(line.getAsnId());
        receive.setAsnLineId(line.getId());
        receive.setSkuId(line.getSkuId());
        receive.setReceiveQty(qty);
        receive.setQcResult(req.getQcResult());
        receive.setRemark(req.getRemark());
        receiveMapper.insert(receive);

        // 累加收货量 / 合格量
        line.setReceivedQty(line.getReceivedQty().add(qty));
        if (req.getQcResult() != null && req.getQcResult() == QcResult.PASS.getValue()) {
            line.setQualifiedQty(line.getQualifiedQty().add(qty));
        }
        asnLineMapper.updateById(line);

        refreshAsnStatus(line.getAsnId());
        return line;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InboundAsnLine putaway(PutawayRequest req) {
        InboundAsnLine line = asnLineMapper.selectForUpdateById(req.getAsnLineId());
        if (line == null) {
            throw new BizException("ASN 明细行不存在");
        }
        BigDecimal qty = req.getQty();
        BigDecimal available = line.getQualifiedQty().subtract(line.getPutawayQty());
        if (qty.compareTo(available) > 0) {
            throw new BizException("上架数量超出合格可上架量，可上架 " + available);
        }
        InboundAsn asn = asnMapper.selectById(line.getAsnId());
        if (asn == null) {
            throw new BizException("ASN 不存在");
        }

        // 落库存（先记账后聚合，与入库单更新同事务）
        StockChangeRequest change = new StockChangeRequest();
        change.setWarehouseId(asn.getWarehouseId());
        change.setSkuId(line.getSkuId());
        change.setLotNo(line.getLotNo() == null ? "" : line.getLotNo());
        change.setLocationId(req.getLocationId());
        change.setQuantity(qty);
        change.setDirection(StockDirection.IN.getValue());
        change.setRefType(RefType.PUTAWAY.getValue());
        change.setRefNo(asn.getAsnNo());
        change.setRefLineId(line.getId());
        stockService.changeStock(change);

        line.setPutawayQty(line.getPutawayQty().add(qty));
        asnLineMapper.updateById(line);

        refreshAsnStatus(line.getAsnId());
        return line;
    }

    /** 刷新 ASN 头状态：全部上架完成→COMPLETED，全部收货→RECEIVED，部分→RECEIVING。 */
    private void refreshAsnStatus(Long asnId) {
        List<InboundAsnLine> lines = asnLineMapper.selectList(
                new LambdaQueryWrapper<InboundAsnLine>().eq(InboundAsnLine::getAsnId, asnId));
        boolean anyReceived = lines.stream()
                .anyMatch(l -> l.getReceivedQty().compareTo(BigDecimal.ZERO) > 0);
        boolean allReceived = lines.stream()
                .allMatch(l -> l.getReceivedQty().compareTo(l.getExpectedQty()) >= 0);
        // 全质检不合格（合格量全为 0）时不得判为完成
        boolean anyQualified = lines.stream()
                .anyMatch(l -> l.getQualifiedQty().compareTo(BigDecimal.ZERO) > 0);
        boolean allPutaway = anyQualified && lines.stream()
                .allMatch(l -> l.getPutawayQty().compareTo(l.getQualifiedQty()) >= 0);

        int status;
        if (allPutaway) {
            status = AsnStatus.COMPLETED.getValue();
        } else if (allReceived) {
            status = AsnStatus.RECEIVED.getValue();
        } else if (anyReceived) {
            status = AsnStatus.RECEIVING.getValue();
        } else {
            status = AsnStatus.PENDING.getValue();
        }
        InboundAsn update = new InboundAsn();
        update.setId(asnId);
        update.setStatus(status);
        asnMapper.updateById(update);
    }

    private String genAsnNo() {
        return "ASN" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
}
