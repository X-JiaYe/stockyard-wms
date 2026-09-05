package com.wms.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 仓库。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("warehouse")
public class Warehouse extends BaseEntity {

    private String code;

    private String name;

    /** 状态：1启用 0停用 */
    private Integer status;
}
