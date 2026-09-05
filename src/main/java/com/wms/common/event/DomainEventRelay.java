package com.wms.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.common.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 领域事件中继：在业务事务提交后把事件序列化并投递到 RabbitMQ。
 * 投递失败仅告警、不阻断主流程（通知型事件，可靠性由 MQ 持久化 + 消费重试兜底）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventRelay {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void relay(DomainEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE,
                    RabbitConfig.ROUTING_PREFIX + event.type(), json);
            log.info("领域事件已投递 MQ: type={}, refNo={}", event.type(), event.refNo());
        } catch (Exception e) {
            log.warn("领域事件投递 MQ 失败（不阻断主流程）: type={}, refNo={}",
                    event.type(), event.refNo(), e);
        }
    }
}
