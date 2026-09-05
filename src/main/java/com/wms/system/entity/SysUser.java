package com.wms.system.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 系统用户。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String nickname;

    /** 状态：1启用 0停用 */
    private Integer status;

    /** 所属仓库（仅 OPERATOR 必填，ADMIN 可空；更新时显式写入，支持清空为 NULL） */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long warehouseId;

    /** 角色编码集合（非表字段，创建/更新时用于绑定角色） */
    @TableField(exist = false)
    private List<String> roleCodes;
}
