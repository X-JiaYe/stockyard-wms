package com.wms.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 物料（SKU）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sku")
public class Sku extends BaseEntity {

    private String code;

    private String name;

    private String spec;

    private String unit;

    private String barcode;

    /** 是否批次管理：0否 1是 */
    private Integer trackLot;

    /** 是否序列号管理：0否 1是 */
    private Integer trackSerial;

    /** 状态：1启用 0停用 */
    private Integer status;
}
