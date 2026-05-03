package org.raoamigos.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ===== Exchange =====
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    // ===== Queues =====
    public static final String OTP_QUEUE = "notification.otp.queue";
    public static final String ADMIN_CREDENTIALS_QUEUE = "notification.admin-credentials.queue";
    public static final String DELIVERY_BOOKED_QUEUE = "notification.delivery-booked.queue";
    public static final String DELIVERY_DELIVERED_QUEUE = "notification.delivery-delivered.queue";

    // ===== Routing Keys =====
    public static final String OTP_ROUTING_KEY = "notification.otp";
    public static final String ADMIN_CREDENTIALS_ROUTING_KEY = "notification.admin.credentials";
    public static final String DELIVERY_BOOKED_ROUTING_KEY = "notification.delivery.booked";
    public static final String DELIVERY_DELIVERED_ROUTING_KEY = "notification.delivery.delivered";

    // --- Exchange ---
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    // --- Queues ---
    @Bean
    public Queue otpQueue() {
        return QueueBuilder.durable(OTP_QUEUE).build();
    }

    @Bean
    public Queue adminCredentialsQueue() {
        return QueueBuilder.durable(ADMIN_CREDENTIALS_QUEUE).build();
    }

    @Bean
    public Queue deliveryBookedQueue() {
        return QueueBuilder.durable(DELIVERY_BOOKED_QUEUE).build();
    }

    @Bean
    public Queue deliveryDeliveredQueue() {
        return QueueBuilder.durable(DELIVERY_DELIVERED_QUEUE).build();
    }

    // --- Bindings ---
    @Bean
    public Binding otpBinding(Queue otpQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(otpQueue).to(notificationExchange).with(OTP_ROUTING_KEY);
    }

    @Bean
    public Binding adminCredentialsBinding(Queue adminCredentialsQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(adminCredentialsQueue).to(notificationExchange).with(ADMIN_CREDENTIALS_ROUTING_KEY);
    }

    @Bean
    public Binding deliveryBookedBinding(Queue deliveryBookedQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(deliveryBookedQueue).to(notificationExchange).with(DELIVERY_BOOKED_ROUTING_KEY);
    }

    @Bean
    public Binding deliveryDeliveredBinding(Queue deliveryDeliveredQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(deliveryDeliveredQueue).to(notificationExchange).with(DELIVERY_DELIVERED_ROUTING_KEY);
    }

    // --- JSON Message Converter ---
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
