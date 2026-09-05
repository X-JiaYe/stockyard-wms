package com.wms.common.event;

import com.wms.common.config.RabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * ERP/WCS 适配器（示例消费端）：订阅出入库完成事件，异步推送外部系统。
 * 生产环境可替换为真实 ERP 接口调用，此处仅落日志体现事件流转。
 */
@Slf4j
@Component
public class ErpNotificationConsumer {

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onDomainEvent(String message) {
        log.info("[ERP适配器] 收到领域事件: {}", message);
    }
}
