package com.wms.common.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 领域事件发布入口：业务 Service 在事务内调用 publish，由 {@link DomainEventRelay}
 * 在事务提交后（AFTER_COMMIT）统一投递 MQ，规避「MQ 已发但本地事务回滚」的不一致。
 */
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
