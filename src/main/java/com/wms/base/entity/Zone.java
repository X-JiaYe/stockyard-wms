package com.wms.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库区。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("zone")
public class Zone extends BaseEntity {

    private Long warehouseId;

    private String code;

    private String name;

    /** 类型：1存储区 2暂存区 3退货区 4质检区 */
    private Integer zoneType;
}
