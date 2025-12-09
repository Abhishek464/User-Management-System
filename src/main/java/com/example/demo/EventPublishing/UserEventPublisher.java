package com.example.demo.EventPublishing;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.events.user-registration-queue}")
    private String registrationQueue;

    @Value("${app.events.user-login-queue}")
    private String loginQueue;

    public void publishUserRegisteredEvent(Long userId, String email) {
        Map<String, Object> payload = Map.of(
                "type", "USER_REGISTERED",
                "userId", userId,
                "email", email,
                "timestamp", Instant.now().toString()
        );
        rabbitTemplate.convertAndSend(registrationQueue, payload);
    }

    public void publishUserLoginEvent(Long userId, String email) {
        Map<String, Object> payload = Map.of(
                "type", "USER_LOGGED_IN",
                "userId", userId,
                "email", email,
                "timestamp", Instant.now().toString()
        );
        rabbitTemplate.convertAndSend(loginQueue, payload);
    }
}
