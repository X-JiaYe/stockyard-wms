package com.wms.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {

    private String code;

    private String name;

    /** 类型：1目录 2菜单 3按钮/接口 */
    private Integer type;

    private Long parentId;
}
