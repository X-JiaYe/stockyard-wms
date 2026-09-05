package com.wms.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 货位。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("location")
public class Location extends BaseEntity {

    private Long warehouseId;

    private Long zoneId;

    private String code;

    /** 类型：1拣货位 2存储位 3暂存位 */
    private Integer locType;

    /** 状态：1可用 0停用 */
    private Integer status;
}
