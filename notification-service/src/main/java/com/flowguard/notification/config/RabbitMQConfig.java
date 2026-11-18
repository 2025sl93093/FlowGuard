package com.flowguard.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ALERT_EXCHANGE = "flowguard.alert.exchange";
    public static final String NOTIFICATIONS_QUEUE = "flowguard.notifications";

    @Bean
    public FanoutExchange alertExchange() {
        return ExchangeBuilder.fanoutExchange(ALERT_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(NOTIFICATIONS_QUEUE).build();
    }

    @Bean
    public Binding notificationsBinding(Queue notificationsQueue, FanoutExchange alertExchange) {
        return BindingBuilder.bind(notificationsQueue).to(alertExchange);
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
