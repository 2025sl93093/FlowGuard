package com.flowguard.analytics.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String WEBHOOK_EXCHANGE = "flowguard.webhook.exchange";
    public static final String ANALYTICS_QUEUE = "flowguard.analytics.events";
    public static final String ROUTING_KEY = "pipeline.event";
    public static final String ALERT_EXCHANGE = "flowguard.alert.exchange";

    @Bean
    public DirectExchange webhookExchange() {
        return ExchangeBuilder.directExchange(WEBHOOK_EXCHANGE).durable(true).build();
    }

    @Bean
    public FanoutExchange alertExchange() {
        return ExchangeBuilder.fanoutExchange(ALERT_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue analyticsQueue() {
        return QueueBuilder.durable(ANALYTICS_QUEUE).build();
    }

    @Bean
    public Binding analyticsBinding(Queue analyticsQueue, DirectExchange webhookExchange) {
        return BindingBuilder.bind(analyticsQueue).to(webhookExchange).with(ROUTING_KEY);
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
