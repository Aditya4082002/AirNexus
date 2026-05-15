package com.airnexus.flight_service.publisher;

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

    // Called from updateFlightStatus() when status = DELAYED
    public void publishFlightDelay(String flightId, String flightNumber, String newDepartureTime) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "FLIGHT_DELAY");
        event.put("flightId", flightId);
        event.put("flightNumber", flightNumber);
        event.put("newTime", newDepartureTime);

        rabbitTemplate.convertAndSend(exchange, "notification.flight.delay", event);
        log.info("Published FLIGHT_DELAY event for flightNumber={}", flightNumber);
    }
}