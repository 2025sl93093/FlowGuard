package com.flowguard.webhook.service;

import com.flowguard.webhook.config.RabbitMQConfig;
import com.flowguard.webhook.dto.PipelineEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookPublisherService {

    private final RabbitTemplate rabbitTemplate;

    public void publishEvent(PipelineEventMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.WEBHOOK_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                message
        );
        log.info("Published event {} for repo {} run {}", message.getEventType(), message.getRepo(), message.getRunId());
    }
}
