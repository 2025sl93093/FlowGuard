package com.flowguard.pipeline.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String WEBHOOK_EXCHANGE = "flowguard.webhook.exchange";
    public static final String PIPELINE_QUEUE = "flowguard.pipeline.events";
    public static final String ROUTING_KEY = "pipeline.event";

    @Bean
    public DirectExchange webhookExchange() {
        return ExchangeBuilder.directExchange(WEBHOOK_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue pipelineQueue() {
        return QueueBuilder.durable(PIPELINE_QUEUE).build();
    }

    @Bean
    public Binding pipelineBinding(Queue pipelineQueue, DirectExchange webhookExchange) {
        return BindingBuilder.bind(pipelineQueue).to(webhookExchange).with(ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
