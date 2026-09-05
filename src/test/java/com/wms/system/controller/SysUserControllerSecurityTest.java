package com.wms.system.controller;

import com.wms.support.WithMockLoginUser;
import com.wms.system.security.JwtAuthenticationFilter;
import com.wms.system.security.JwtUtil;
import com.wms.system.security.SecurityConfig;
import com.wms.system.security.TokenBlacklistService;
import com.wms.system.security.UserDetailsServiceImpl;
import com.wms.system.service.SysUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 权限矩阵集成测试：验证方法级 @PreAuthorize 在真实请求链路中生效。
 */
@WebMvcTest(SysUserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class SysUserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SysUserService sysUserService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    @DisplayName("未认证访问系统管理接口返回 401")
    void unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/sys/users/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("OPERATOR 无 system:manage 访问用户详情返回 403")
    @WithMockLoginUser(authorities = "ROLE_OPERATOR")
    void operatorWithoutSystemManage_returns403() throws Exception {
        mockMvc.perform(get("/sys/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("管理员持有 system:manage 访问用户详情返回 200")
    @WithMockLoginUser(admin = true, authorities = {"ROLE_ADMIN", "system:manage"})
    void adminWithSystemManage_returns200() throws Exception {
        mockMvc.perform(get("/sys/users/1"))
                .andExpect(status().isOk());
    }
}
