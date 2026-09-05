package com.wms.system.security;

import com.wms.system.entity.SysUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AuthContext 鉴权分支单测：角色判定 / null / 匹配 / 不匹配 / 收敛。
 */
class AuthContextTest {

    private static final Long WH = 1L;

    private final AuthContext authContext = new AuthContext();

    @BeforeEach
    void clearBefore() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clearAfter() {
        SecurityContextHolder.clearContext();
    }

    private void login(Long warehouseId, boolean admin) {
        SysUser user = new SysUser();
        user.setWarehouseId(warehouseId);
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(admin ? "ROLE_ADMIN" : "ROLE_OPERATOR"));
        LoginUser loginUser = new LoginUser(user, authorities);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                loginUser, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("未登录 currentUser 抛 403")
    void currentUser_notAuthenticated_denied() {
        assertThatThrownBy(authContext::currentUser)
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("拥有 ROLE_ADMIN 即管理员（不依赖 warehouse_id）")
    void isAdmin_byRole() {
        login(null, true);
        assertThat(authContext.isAdmin()).isTrue();
    }

    @Test
    @DisplayName("OPERATOR 非管理员")
    void isAdmin_operator_false() {
        login(WH, false);
        assertThat(authContext.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("requireAdmin：管理员放行")
    void requireAdmin_admin_passes() {
        login(null, true);
        authContext.requireAdmin();
    }

    @Test
    @DisplayName("requireAdmin：普通用户拒绝")
    void requireAdmin_operator_denied() {
        login(WH, false);
        assertThatThrownBy(authContext::requireAdmin)
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("checkWarehouse：管理员对任意仓放行")
    void checkWarehouse_admin_passes() {
        login(null, true);
        authContext.checkWarehouse(null);
        authContext.checkWarehouse(999L);
    }

    @Test
    @DisplayName("checkWarehouse：普通用户命中本仓放行")
    void checkWarehouse_operator_ownWarehouse_passes() {
        login(WH, false);
        authContext.checkWarehouse(WH);
    }

    @Test
    @DisplayName("checkWarehouse：普通用户跨仓拒绝")
    void checkWarehouse_operator_otherWarehouse_denied() {
        login(WH, false);
        assertThatThrownBy(() -> authContext.checkWarehouse(2L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("checkWarehouse：普通用户目标仓为 null 拒绝")
    void checkWarehouse_operator_nullWarehouse_denied() {
        login(WH, false);
        assertThatThrownBy(() -> authContext.checkWarehouse(null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("scopeWarehouse：管理员保持原值（可 null）")
    void scopeWarehouse_admin_keepsValue() {
        login(null, true);
        assertThat(authContext.scopeWarehouse(null)).isNull();
        assertThat(authContext.scopeWarehouse(5L)).isEqualTo(5L);
    }

    @Test
    @DisplayName("scopeWarehouse：普通用户 null 收敛到本仓")
    void scopeWarehouse_operator_null_converges() {
        login(WH, false);
        assertThat(authContext.scopeWarehouse(null)).isEqualTo(WH);
    }

    @Test
    @DisplayName("scopeWarehouse：普通用户本仓保持")
    void scopeWarehouse_operator_ownWarehouse_keeps() {
        login(WH, false);
        assertThat(authContext.scopeWarehouse(WH)).isEqualTo(WH);
    }

    @Test
    @DisplayName("scopeWarehouse：普通用户跨仓拒绝")
    void scopeWarehouse_operator_otherWarehouse_denied() {
        login(WH, false);
        assertThatThrownBy(() -> authContext.scopeWarehouse(2L))
                .isInstanceOf(AccessDeniedException.class);
    }
}
