package com.wms.common.event;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 领域事件：出入库等业务动作完成后的不可变事实描述，供跨域/外部系统（ERP、WCS）订阅。
 * 仅作通知，不承载回滚语义。
 */
public record DomainEvent(String type, Long warehouseId, String refNo,
                          Map<String, Object> payload, LocalDateTime occurredAt) {

    public static DomainEvent of(String type, Long warehouseId, String refNo, Map<String, Object> payload) {
        return new DomainEvent(type, warehouseId, refNo, payload, LocalDateTime.now());
    }
}
