package com.wms.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户-角色关联（纯关联表，无主键/时间戳/逻辑删除）。
 */
@Data
@TableName("sys_user_role")
public class SysUserRole {

    private Long userId;

    private Long roleId;
}
