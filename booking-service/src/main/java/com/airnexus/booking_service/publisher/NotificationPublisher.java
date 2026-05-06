package com.airnexus.booking_service.publisher;

import com.airnexus.booking_service.dto.NotificationEvent;
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

    // Called from confirmBooking()
    public void publishBookingConfirmed(String userId, String email, String phone,
                                        String bookingId, String pnr, String flightDetails) {

        NotificationEvent event = new NotificationEvent();
        event.setType("BOOKING_CONFIRMED");
        event.setUserId(userId);
        event.setEmail(email);
        event.setPhone(phone);
        event.setBookingId(bookingId);
        event.setPnr(pnr);
        event.setFlightDetails(flightDetails);

        rabbitTemplate.convertAndSend(
                exchange,
                "notification.booking.confirmed",
                event
        );

        log.info("Published BOOKING_CONFIRMED event for bookingId={}", bookingId);
    }

    // Called from cancelBooking()
    public void publishCancellation(String userId, String email,
                                    String bookingId, String pnr) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "CANCELLATION");
        event.put("userId", userId);
        event.put("email", email);
        event.put("bookingId", bookingId);
        event.put("pnr", pnr);

        rabbitTemplate.convertAndSend(exchange, "notification.booking.cancelled", event);
        log.info("Published CANCELLATION event for bookingId={}", bookingId);
    }
}