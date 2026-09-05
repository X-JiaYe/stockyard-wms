package com.wms.stock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.stock.entity.StockBalance;
import com.wms.stock.mapper.StockBalanceMapper;
import com.wms.stock.mapper.StockLedgerMapper;
import com.wms.stock.service.impl.StockServiceImpl;
import com.wms.support.WithMockLoginUser;
import com.wms.system.security.AuthContext;
import com.wms.system.security.JwtAuthenticationFilter;
import com.wms.system.security.JwtUtil;
import com.wms.system.security.SecurityConfig;
import com.wms.system.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 库存接口越权防护集成测试：验证方法级权限 + AuthContext 仓库隔离在真实链路中生效。
 * 使用真实 StockServiceImpl + AuthContext，仅 mock 数据访问层。
 */
@WebMvcTest(StockController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, StockServiceImpl.class, AuthContext.class})
class StockControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StockBalanceMapper stockBalanceMapper;

    @MockBean
    private StockLedgerMapper stockLedgerMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private String body(long warehouseId) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "warehouseId", warehouseId,
                "skuId", 100L,
                "locationId", 200L,
                "quantity", 5,
                "refNo", "ASN-1"));
    }

    @Test
    @DisplayName("OPERATOR 无 stock:manage 调入库接口返回 403")
    @WithMockLoginUser(authorities = "ROLE_OPERATOR")
    void operatorWithoutStockManage_returns403() throws Exception {
        mockMvc.perform(post("/stock/inbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(1L)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("OPERATOR 操作非本仓数据返回 403（越权拦截）")
    @WithMockLoginUser(warehouseId = 1L, authorities = "stock:manage")
    void operatorCrossWarehouse_returns403() throws Exception {
        mockMvc.perform(post("/stock/inbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(2L)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("OPERATOR 操作本仓数据返回 200")
    @WithMockLoginUser(warehouseId = 1L, authorities = "stock:manage")
    void operatorOwnWarehouse_returns200() throws Exception {
        StockBalance bal = new StockBalance();
        bal.setId(1L);
        bal.setWarehouseId(1L);
        bal.setSkuId(100L);
        bal.setLotNo("");
        bal.setLocationId(200L);
        bal.setQuantity(new BigDecimal("10"));
        when(stockBalanceMapper.selectForUpdate(1L, 100L, "", 200L)).thenReturn(bal);

        mockMvc.perform(post("/stock/inbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(1L)))
                .andExpect(status().isOk());
    }
}
