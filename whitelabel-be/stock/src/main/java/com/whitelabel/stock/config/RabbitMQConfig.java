package com.whitelabel.stock.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public Queue stockQueue() {
        return new Queue("stock-queue", true);
    }

    @Bean
    public TopicExchange orderEventsExchange() {
        return new TopicExchange("order-events");
    }

    @Bean
    public Binding binding(Queue stockQueue, TopicExchange orderEventsExchange) {
        return BindingBuilder.bind(stockQueue).to(orderEventsExchange).with("order.placed");
    }
}
