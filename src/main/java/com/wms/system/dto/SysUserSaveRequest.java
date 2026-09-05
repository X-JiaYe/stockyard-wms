package com.wms.system.dto;

import lombok.Data;

import java.util.List;

/**
 * 用户创建/更新请求（显式 DTO，避免直接绑定实体导致的 mass assignment）。
 */
@Data
public class SysUserSaveRequest {

    /** 用户名（仅创建时使用） */
    private String username;

    /** 密码（创建必填；更新时留空表示不修改） */
    private String password;

    private String nickname;

    /** 状态：1启用 0停用 */
    private Integer status;

    /** 所属仓库（仅 OPERATOR 必填） */
    private Long warehouseId;

    /** 角色编码集合 */
    private List<String> roleCodes;
}
