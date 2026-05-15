package com.airnexus.payment_service.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    // Called from processPayment()
    public void publishPaymentSuccess(String userId, String bookingId, Double amount) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "PAYMENT_SUCCESS");
        event.put("userId", userId);
        event.put("bookingId", bookingId);
        event.put("amount", amount);
        rabbitTemplate.convertAndSend(exchange, "notification.payment.success", event);
        log.info("Published PAYMENT_SUCCESS event for bookingId={}", bookingId);
    }
}
