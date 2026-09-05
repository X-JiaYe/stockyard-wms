package com.wms.system.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 当前登录用户上下文：提供「用户↔仓库」数据范围校验。
 *
 * <p>单仓绑定模型：{@code sys_user.warehouse_id} 为 NULL 表示管理员（全局），
 * 非 NULL 表示绑定单一仓库，只能操作本仓数据。</p>
 */
@Component
public class AuthContext {

    /** 当前登录用户（未登录抛 403）。 */
    public LoginUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        throw new AccessDeniedException("未登录");
    }

    /** 当前用户所属仓库；NULL 表示管理员（可访问全部）。 */
    public Long currentWarehouseId() {
        return currentUser().getSysUser().getWarehouseId();
    }

    public boolean isAdmin() {
        return currentWarehouseId() == null;
    }

    /** 管理员才放行，否则 403。 */
    public void requireAdmin() {
        if (!isAdmin()) {
            throw new AccessDeniedException("需要管理员权限");
        }
    }

    /** 校验目标仓库是否在用户可访问范围内（管理员放行）。 */
    public void checkWarehouse(Long warehouseId) {
        Long mine = currentWarehouseId();
        if (mine == null) {
            return;
        }
        if (warehouseId == null || !mine.equals(warehouseId)) {
            throw new AccessDeniedException("无权操作该仓库的数据");
        }
    }

    /** 查询场景：普通用户强制收敛到本仓；管理员保持原值（可 null 查全部）。 */
    public Long scopeWarehouse(Long warehouseId) {
        Long mine = currentWarehouseId();
        if (mine == null) {
            return warehouseId;
        }
        if (warehouseId != null && !mine.equals(warehouseId)) {
            throw new AccessDeniedException("无权访问该仓库的数据");
        }
        return mine;
    }
}
