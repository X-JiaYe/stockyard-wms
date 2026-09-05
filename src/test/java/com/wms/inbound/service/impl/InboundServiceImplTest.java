package com.wms.inbound.service.impl;

import com.wms.common.exception.BizException;
import com.wms.inbound.dto.PutawayRequest;
import com.wms.inbound.dto.ReceiveRequest;
import com.wms.inbound.entity.InboundAsn;
import com.wms.inbound.entity.InboundAsnLine;
import com.wms.inbound.entity.InboundReceive;
import com.wms.inbound.enums.QcResult;
import com.wms.inbound.mapper.InboundAsnLineMapper;
import com.wms.inbound.mapper.InboundAsnMapper;
import com.wms.inbound.mapper.InboundReceiveMapper;
import com.wms.stock.dto.StockChangeRequest;
import com.wms.stock.enums.RefType;
import com.wms.stock.enums.StockDirection;
import com.wms.stock.service.StockService;
import com.wms.system.security.AuthContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 入库单测：收货（质检结论域校验、超收、合格量累加）与上架（可上架量校验、落库存）。
 */
@ExtendWith(MockitoExtension.class)
class InboundServiceImplTest {

    @Mock
    private InboundAsnMapper asnMapper;
    @Mock
    private InboundAsnLineMapper asnLineMapper;
    @Mock
    private InboundReceiveMapper receiveMapper;
    @Mock
    private StockService stockService;
    @Mock
    private AuthContext authContext;

    private InboundServiceImpl inboundService;

    @BeforeEach
    void setUp() {
        inboundService = new InboundServiceImpl(asnMapper, asnLineMapper, receiveMapper, stockService, authContext);
    }

    private InboundAsnLine line(Long id, Long asnId, String received, String qualified, String putaway) {
        InboundAsnLine l = new InboundAsnLine();
        l.setId(id);
        l.setAsnId(asnId);
        l.setSkuId(100L);
        l.setExpectedQty(new BigDecimal("20"));
        l.setReceivedQty(new BigDecimal(received));
        l.setQualifiedQty(new BigDecimal(qualified));
        l.setPutawayQty(new BigDecimal(putaway));
        l.setLotNo("");
        return l;
    }

    private InboundAsn asn(Long id) {
        InboundAsn a = new InboundAsn();
        a.setId(id);
        a.setWarehouseId(1L);
        a.setAsnNo("ASN001");
        return a;
    }

    @Test
    @DisplayName("收货：明细行不存在拒绝")
    void receive_lineNotFound_rejected() {
        when(asnLineMapper.selectForUpdateById(1L)).thenReturn(null);
        ReceiveRequest req = new ReceiveRequest();
        req.setAsnLineId(1L);
        req.setQty(new BigDecimal("5"));
        req.setQcResult(QcResult.PASS.getValue());
        assertThatThrownBy(() -> inboundService.receive(req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("ASN 明细行不存在");
    }

    @Test
    @DisplayName("收货：质检结论取值非法拒绝")
    void receive_invalidQcResult_rejected() {
        when(asnLineMapper.selectForUpdateById(1L)).thenReturn(line(1L, 10L, "0", "0", "0"));
        ReceiveRequest req = new ReceiveRequest();
        req.setAsnLineId(1L);
        req.setQty(new BigDecimal("5"));
        req.setQcResult(99);
        assertThatThrownBy(() -> inboundService.receive(req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("质检结论取值非法");
        verify(receiveMapper, never()).insert(any(InboundReceive.class));
    }

    @Test
    @DisplayName("收货：超收拒绝")
    void receive_overReceive_rejected() {
        when(asnLineMapper.selectForUpdateById(1L)).thenReturn(line(1L, 10L, "0", "0", "0"));
        ReceiveRequest req = new ReceiveRequest();
        req.setAsnLineId(1L);
        req.setQty(new BigDecimal("25"));
        req.setQcResult(QcResult.PASS.getValue());
        assertThatThrownBy(() -> inboundService.receive(req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("收货数量超出预期");
    }

    @Test
    @DisplayName("收货：合格累加收货量与合格量")
    void receive_pass_accumulatesBoth() {
        InboundAsnLine l = line(1L, 10L, "0", "0", "0");
        when(asnLineMapper.selectForUpdateById(1L)).thenReturn(l);
        when(asnMapper.selectById(10L)).thenReturn(asn(10L));
        when(asnLineMapper.selectList(any())).thenReturn(List.of(l));

        ReceiveRequest req = new ReceiveRequest();
        req.setAsnLineId(1L);
        req.setQty(new BigDecimal("5"));
        req.setQcResult(QcResult.PASS.getValue());

        InboundAsnLine result = inboundService.receive(req);

        assertThat(result.getReceivedQty()).isEqualByComparingTo("5");
        assertThat(result.getQualifiedQty()).isEqualByComparingTo("5");
        verify(receiveMapper).insert(any(InboundReceive.class));
    }

    @Test
    @DisplayName("收货：不合格只累加收货量，不累加合格量")
    void receive_fail_onlyAccumulatesReceived() {
        InboundAsnLine l = line(1L, 10L, "0", "0", "0");
        when(asnLineMapper.selectForUpdateById(1L)).thenReturn(l);
        when(asnMapper.selectById(10L)).thenReturn(asn(10L));
        when(asnLineMapper.selectList(any())).thenReturn(List.of(l));

        ReceiveRequest req = new ReceiveRequest();
        req.setAsnLineId(1L);
        req.setQty(new BigDecimal("5"));
        req.setQcResult(QcResult.FAIL.getValue());

        InboundAsnLine result = inboundService.receive(req);

        assertThat(result.getReceivedQty()).isEqualByComparingTo("5");
        assertThat(result.getQualifiedQty()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("上架：超出可上架量拒绝")
    void putaway_overAvailable_rejected() {
        when(asnLineMapper.selectForUpdateById(1L)).thenReturn(line(1L, 10L, "10", "10", "8"));
        PutawayRequest req = new PutawayRequest();
        req.setAsnLineId(1L);
        req.setLocationId(200L);
        req.setQty(new BigDecimal("5"));
        assertThatThrownBy(() -> inboundService.putaway(req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("上架数量超出");
        verify(stockService, never()).changeStock(any());
    }

    @Test
    @DisplayName("上架：落库存并累加上架量")
    void putaway_success() {
        InboundAsnLine l = line(1L, 10L, "10", "10", "0");
        when(asnLineMapper.selectForUpdateById(1L)).thenReturn(l);
        when(asnMapper.selectById(10L)).thenReturn(asn(10L));
        when(asnLineMapper.selectList(any())).thenReturn(List.of(l));

        PutawayRequest req = new PutawayRequest();
        req.setAsnLineId(1L);
        req.setLocationId(200L);
        req.setQty(new BigDecimal("5"));

        InboundAsnLine result = inboundService.putaway(req);

        assertThat(result.getPutawayQty()).isEqualByComparingTo("5");
        ArgumentCaptor<StockChangeRequest> cap = ArgumentCaptor.forClass(StockChangeRequest.class);
        verify(stockService).changeStock(cap.capture());
        assertThat(cap.getValue().getQuantity()).isEqualByComparingTo("5");
        assertThat(cap.getValue().getDirection()).isEqualTo(StockDirection.IN.getValue());
        assertThat(cap.getValue().getRefType()).isEqualTo(RefType.PUTAWAY.getValue());
        assertThat(cap.getValue().getLocationId()).isEqualTo(200L);
    }
}
