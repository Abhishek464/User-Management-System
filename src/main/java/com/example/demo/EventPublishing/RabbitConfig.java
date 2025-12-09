package com.example.demo.EventPublishing;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitConfig {

    @Value("${app.events.user-registration-queue}")
    private String registrationQueue;

    @Value("${app.events.user-login-queue}")
    private String loginQueue;

    @Bean
    public Queue userRegistrationQueue() {
        return new Queue(registrationQueue, true);
    }

    @Bean
    public Queue userLoginQueue() {
        return new Queue(loginQueue, true);
    }
}
