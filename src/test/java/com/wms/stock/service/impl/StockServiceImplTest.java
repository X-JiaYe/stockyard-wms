package com.wms.stock.service.impl;

import com.wms.common.exception.BizException;
import com.wms.stock.dto.RecalculateResult;
import com.wms.stock.dto.StockChangeRequest;
import com.wms.stock.dto.StockCountRequest;
import com.wms.stock.dto.StockMoveRequest;
import com.wms.stock.entity.StockBalance;
import com.wms.stock.entity.StockLedger;
import com.wms.stock.enums.StockDirection;
import com.wms.stock.mapper.StockBalanceMapper;
import com.wms.stock.mapper.StockLedgerMapper;
import com.wms.system.security.AuthContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 库存中枢单测：聚焦「先写账再聚合」、方向/符号一致性、防超卖、盘点与重算。
 */
@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    private static final Long WH = 1L;
    private static final Long SKU = 100L;
    private static final Long LOC = 200L;

    @Mock
    private StockLedgerMapper stockLedgerMapper;
    @Mock
    private StockBalanceMapper stockBalanceMapper;
    @Mock
    private AuthContext authContext;

    private StockServiceImpl stockService;

    @BeforeEach
    void setUp() {
        stockService = new StockServiceImpl(stockLedgerMapper, stockBalanceMapper, authContext);
    }

    private StockBalance balance(Long id, String qty) {
        StockBalance b = new StockBalance();
        b.setId(id);
        b.setWarehouseId(WH);
        b.setSkuId(SKU);
        b.setLotNo("");
        b.setLocationId(LOC);
        b.setQuantity(new BigDecimal(qty));
        return b;
    }

    private StockChangeRequest changeReq(String qty, int direction) {
        StockChangeRequest req = new StockChangeRequest();
        req.setWarehouseId(WH);
        req.setSkuId(SKU);
        req.setLotNo("");
        req.setLocationId(LOC);
        req.setQuantity(new BigDecimal(qty));
        req.setDirection(direction);
        req.setRefType("ADJUST");
        req.setRefNo("TEST-1");
        return req;
    }

    @Test
    @DisplayName("变动数量为 0 直接拒绝")
    void changeStock_zeroQuantity_rejected() {
        assertThatThrownBy(() -> stockService.changeStock(changeReq("0", StockDirection.IN.getValue())))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("变动数量不能为 0");
    }

    @Test
    @DisplayName("正数却标 OUT 拒绝")
    void changeStock_positiveQtyWithOutDirection_rejected() {
        assertThatThrownBy(() -> stockService.changeStock(changeReq("5", StockDirection.OUT.getValue())))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("变动方向与数量符号不一致");
    }

    @Test
    @DisplayName("负数却标 IN 拒绝")
    void changeStock_negativeQtyWithInDirection_rejected() {
        assertThatThrownBy(() -> stockService.changeStock(changeReq("-5", StockDirection.IN.getValue())))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("变动方向与数量符号不一致");
    }

    @Test
    @DisplayName("扣减导致结存为负时拒绝（防超卖）")
    void changeStock_insufficientStock_rejected() {
        when(stockBalanceMapper.selectForUpdate(WH, SKU, "", LOC)).thenReturn(balance(1L, "10"));
        assertThatThrownBy(() -> stockService.changeStock(changeReq("-15", StockDirection.OUT.getValue())))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("库存不足");
        verify(stockLedgerMapper, never()).insert(any(StockLedger.class));
        verify(stockBalanceMapper, never()).updateQuantity(anyLong(), any());
    }

    @Test
    @DisplayName("入库：先写流水账再聚合结存")
    void changeStock_inbound_writesLedgerThenAggregates() {
        when(stockBalanceMapper.selectForUpdate(WH, SKU, "", LOC)).thenReturn(balance(1L, "10"));

        StockBalance result = stockService.changeStock(changeReq("5", StockDirection.IN.getValue()));

        assertThat(result.getQuantity()).isEqualByComparingTo("15");
        verify(stockBalanceMapper).updateQuantity(1L, new BigDecimal("15"));
        ArgumentCaptor<StockLedger> cap = ArgumentCaptor.forClass(StockLedger.class);
        verify(stockLedgerMapper).insert(cap.capture());
        assertThat(cap.getValue().getQuantity()).isEqualByComparingTo("5");
        assertThat(cap.getValue().getBalance()).isEqualByComparingTo("15");
        assertThat(cap.getValue().getDirection()).isEqualTo(StockDirection.IN.getValue());
    }

    @Test
    @DisplayName("出库：负数变动正确落账")
    void changeStock_outbound_writesNegativeLedger() {
        when(stockBalanceMapper.selectForUpdate(WH, SKU, "", LOC)).thenReturn(balance(1L, "10"));

        StockBalance result = stockService.changeStock(changeReq("-5", StockDirection.OUT.getValue()));

        assertThat(result.getQuantity()).isEqualByComparingTo("5");
        verify(stockBalanceMapper).updateQuantity(1L, new BigDecimal("5"));
        ArgumentCaptor<StockLedger> cap = ArgumentCaptor.forClass(StockLedger.class);
        verify(stockLedgerMapper).insert(cap.capture());
        assertThat(cap.getValue().getQuantity()).isEqualByComparingTo("-5");
        assertThat(cap.getValue().getDirection()).isEqualTo(StockDirection.OUT.getValue());
    }

    @Test
    @DisplayName("首次记账：占位插入后再锁定")
    void changeStock_firstEntry_insertsPlaceholder() {
        when(stockBalanceMapper.selectForUpdate(WH, SKU, "", LOC))
                .thenReturn(null, balance(2L, "0"));

        stockService.changeStock(changeReq("8", StockDirection.IN.getValue()));

        verify(stockBalanceMapper).insertIfAbsent(any(StockBalance.class));
        verify(stockBalanceMapper).updateQuantity(2L, new BigDecimal("8"));
    }

    @Test
    @DisplayName("盘点：实盘与结存一致时不动账")
    void count_noDiff_returnsAsIs() {
        when(stockBalanceMapper.selectForUpdate(WH, SKU, "", LOC)).thenReturn(balance(1L, "10"));

        StockCountRequest req = new StockCountRequest();
        req.setWarehouseId(WH);
        req.setSkuId(SKU);
        req.setLotNo("");
        req.setLocationId(LOC);
        req.setActualQty(new BigDecimal("10"));
        req.setRefNo("COUNT-1");

        StockBalance result = stockService.count(req);

        assertThat(result.getQuantity()).isEqualByComparingTo("10");
        verify(stockLedgerMapper, never()).insert(any(StockLedger.class));
        verify(stockBalanceMapper, never()).updateQuantity(anyLong(), any());
    }

    @Test
    @DisplayName("盘点：实盘大于结存生成入库差异")
    void count_positiveDiff_generatesInboundAdjustment() {
        when(stockBalanceMapper.selectForUpdate(WH, SKU, "", LOC))
                .thenReturn(balance(1L, "10"), balance(2L, "10"));

        StockCountRequest req = new StockCountRequest();
        req.setWarehouseId(WH);
        req.setSkuId(SKU);
        req.setLotNo("");
        req.setLocationId(LOC);
        req.setActualQty(new BigDecimal("15"));
        req.setRefNo("COUNT-1");

        stockService.count(req);

        verify(stockBalanceMapper).updateQuantity(2L, new BigDecimal("15"));
        ArgumentCaptor<StockLedger> cap = ArgumentCaptor.forClass(StockLedger.class);
        verify(stockLedgerMapper).insert(cap.capture());
        assertThat(cap.getValue().getQuantity()).isEqualByComparingTo("5");
    }

    @Test
    @DisplayName("移动：源货位扣减、目标货位增加")
    void move_outFromSource_inToTarget() {
        when(stockBalanceMapper.selectForUpdate(eq(WH), eq(SKU), eq(""), anyLong()))
                .thenAnswer(inv -> {
                    Long loc = inv.getArgument(3);
                    StockBalance b = new StockBalance();
                    b.setId(loc.equals(200L) ? 10L : 20L);
                    b.setWarehouseId(WH);
                    b.setSkuId(SKU);
                    b.setLotNo("");
                    b.setLocationId(loc);
                    b.setQuantity(loc.equals(200L) ? new BigDecimal("10") : BigDecimal.ZERO);
                    return b;
                });

        StockMoveRequest req = new StockMoveRequest();
        req.setWarehouseId(WH);
        req.setSkuId(SKU);
        req.setLotNo(null);
        req.setFromLocationId(200L);
        req.setToLocationId(300L);
        req.setQuantity(new BigDecimal("5"));
        req.setRefNo("MOVE-1");

        stockService.move(req);

        verify(stockBalanceMapper).updateQuantity(10L, new BigDecimal("5"));
        verify(stockBalanceMapper).updateQuantity(20L, new BigDecimal("5"));
        verify(stockLedgerMapper, times(2)).insert(any(StockLedger.class));
    }

    @Test
    @DisplayName("重算：ledger 与 balance 一致")
    void recalculate_consistent() {
        when(stockLedgerMapper.sumQuantity(WH, SKU, "", LOC)).thenReturn(new BigDecimal("100"));
        when(stockBalanceMapper.selectOne(any())).thenReturn(balance(1L, "100"));

        RecalculateResult r = stockService.recalculate(WH, SKU, "", LOC);

        assertThat(r.isConsistent()).isTrue();
        assertThat(r.getLedgerQuantity()).isEqualByComparingTo("100");
        assertThat(r.getBalanceQuantity()).isEqualByComparingTo("100");
    }

    @Test
    @DisplayName("重算：无 balance 记录时视为 0 且不一致")
    void recalculate_noBalance_isInconsistent() {
        when(stockLedgerMapper.sumQuantity(WH, SKU, "", LOC)).thenReturn(new BigDecimal("100"));
        when(stockBalanceMapper.selectOne(any())).thenReturn(null);

        RecalculateResult r = stockService.recalculate(WH, SKU, "", LOC);

        assertThat(r.isConsistent()).isFalse();
        assertThat(r.getBalanceQuantity()).isEqualByComparingTo("0");
    }
}
