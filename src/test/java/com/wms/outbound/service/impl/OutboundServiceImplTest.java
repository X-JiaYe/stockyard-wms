package com.wms.outbound.service.impl;

import com.wms.common.exception.BizException;
import com.wms.outbound.dto.PickRequest;
import com.wms.outbound.dto.ShipRequest;
import com.wms.outbound.entity.OutboundOrder;
import com.wms.outbound.entity.OutboundOrderLine;
import com.wms.outbound.enums.OutboundStatus;
import com.wms.outbound.mapper.OutboundOrderLineMapper;
import com.wms.outbound.mapper.OutboundOrderMapper;
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
 * 出库单测：拣货（超量校验、扣库存）与发运（未拣完拦截、置已发运）。
 */
@ExtendWith(MockitoExtension.class)
class OutboundServiceImplTest {

    @Mock
    private OutboundOrderMapper orderMapper;
    @Mock
    private OutboundOrderLineMapper orderLineMapper;
    @Mock
    private StockService stockService;
    @Mock
    private AuthContext authContext;

    private OutboundServiceImpl outboundService;

    @BeforeEach
    void setUp() {
        outboundService = new OutboundServiceImpl(orderMapper, orderLineMapper, stockService, authContext);
    }

    private OutboundOrderLine line(Long id, Long orderId, String orderQty, String pickedQty) {
        OutboundOrderLine l = new OutboundOrderLine();
        l.setId(id);
        l.setOrderId(orderId);
        l.setSkuId(100L);
        l.setOrderQty(new BigDecimal(orderQty));
        l.setPickedQty(new BigDecimal(pickedQty));
        l.setLotNo("");
        return l;
    }

    private OutboundOrder order(Long id) {
        OutboundOrder o = new OutboundOrder();
        o.setId(id);
        o.setWarehouseId(1L);
        o.setOrderNo("OUT001");
        o.setStatus(OutboundStatus.PENDING.getValue());
        return o;
    }

    @Test
    @DisplayName("拣货：明细行不存在拒绝")
    void pick_lineNotFound_rejected() {
        when(orderLineMapper.selectForUpdateById(1L)).thenReturn(null);
        PickRequest req = new PickRequest();
        req.setOrderLineId(1L);
        req.setLocationId(200L);
        req.setQty(new BigDecimal("5"));
        assertThatThrownBy(() -> outboundService.pick(req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("订单明细行不存在");
    }

    @Test
    @DisplayName("拣货：超量拒绝")
    void pick_overPick_rejected() {
        when(orderLineMapper.selectForUpdateById(1L)).thenReturn(line(1L, 10L, "20", "18"));
        PickRequest req = new PickRequest();
        req.setOrderLineId(1L);
        req.setLocationId(200L);
        req.setQty(new BigDecimal("5"));
        assertThatThrownBy(() -> outboundService.pick(req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("拣货数量超出订购量");
        verify(stockService, never()).changeStock(any());
    }

    @Test
    @DisplayName("拣货：扣库存并累加拣货量")
    void pick_success() {
        OutboundOrderLine l = line(1L, 10L, "20", "0");
        when(orderLineMapper.selectForUpdateById(1L)).thenReturn(l);
        when(orderMapper.selectById(10L)).thenReturn(order(10L));
        when(orderLineMapper.selectList(any())).thenReturn(List.of(l));

        PickRequest req = new PickRequest();
        req.setOrderLineId(1L);
        req.setLocationId(200L);
        req.setQty(new BigDecimal("5"));

        OutboundOrderLine result = outboundService.pick(req);

        assertThat(result.getPickedQty()).isEqualByComparingTo("5");
        ArgumentCaptor<StockChangeRequest> cap = ArgumentCaptor.forClass(StockChangeRequest.class);
        verify(stockService).changeStock(cap.capture());
        assertThat(cap.getValue().getQuantity()).isEqualByComparingTo("-5");
        assertThat(cap.getValue().getDirection()).isEqualTo(StockDirection.OUT.getValue());
        assertThat(cap.getValue().getRefType()).isEqualTo(RefType.PICK.getValue());
    }

    @Test
    @DisplayName("发运：订单不存在拒绝")
    void ship_orderNotFound_rejected() {
        when(orderMapper.selectById(10L)).thenReturn(null);
        ShipRequest req = new ShipRequest();
        req.setOrderId(10L);
        assertThatThrownBy(() -> outboundService.ship(req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("订单不存在");
    }

    @Test
    @DisplayName("发运：存在未拣完明细拒绝")
    void ship_partiallyPicked_rejected() {
        when(orderMapper.selectById(10L)).thenReturn(order(10L));
        when(orderLineMapper.selectList(any())).thenReturn(List.of(line(1L, 10L, "5", "3")));
        ShipRequest req = new ShipRequest();
        req.setOrderId(10L);
        assertThatThrownBy(() -> outboundService.ship(req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不能发运");
        verify(orderMapper, never()).updateById(any(OutboundOrder.class));
    }

    @Test
    @DisplayName("发运：全部拣完置为已发运")
    void ship_success() {
        when(orderMapper.selectById(10L)).thenReturn(order(10L));
        when(orderLineMapper.selectList(any())).thenReturn(List.of(line(1L, 10L, "5", "5")));
        ShipRequest req = new ShipRequest();
        req.setOrderId(10L);

        OutboundOrder result = outboundService.ship(req);

        assertThat(result.getStatus()).isEqualTo(OutboundStatus.SHIPPED.getValue());
        verify(orderMapper).updateById(any(OutboundOrder.class));
    }
}
