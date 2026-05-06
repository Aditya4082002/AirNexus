//package com.airnexus.notification_service.consumer;
//
//import com.airnexus.notification_service.dto.NotificationEvent;
//import com.airnexus.notification_service.service.NotificationService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class NotificationConsumer {
//
//    private final NotificationService notificationService;
//
//    @RabbitListener(queues = "${rabbitmq.queue.notification}")
//    public void handleNotificationEvent(NotificationEvent event) {
//        log.info("Received notification event: type={}, userId={}", event.getType(), event.getUserId());
//        System.out.println("🔥 CONSUMER HIT");//debug
//        try {
//            switch (event.getType()) {
//                case "BOOKING_CONFIRMED" -> notificationService.sendBookingConfirmation(
//                        event.getUserId(), event.getEmail(), event.getPhone(),
//                        event.getBookingId(), event.getPnr(), event.getFlightDetails());
//
//                case "PAYMENT_SUCCESS" -> notificationService.sendPaymentSuccess(
//                        event.getUserId(), event.getEmail(),
//                        event.getBookingId(), event.getAmount());
//
//                case "FLIGHT_DELAY" -> notificationService.sendFlightDelay(
//                        event.getUserId(), event.getEmail(), event.getBookingId(),
//                        event.getFlightNumber(), event.getNewTime());
//
//                case "CHECKIN_REMINDER" -> notificationService.sendCheckInReminder(
//                        event.getUserId(), event.getEmail(), event.getPhone(),
//                        event.getBookingId(), event.getPnr(), event.getFlightNumber());
//
//                case "CANCELLATION" -> notificationService.sendCancellationConfirmation(
//                        event.getUserId(), event.getEmail(),
//                        event.getBookingId(), event.getPnr());
//
//                default -> log.warn("Unknown notification type: {}", event.getType());
//            }
//        } catch (Exception e) {
//            log.error("Failed to process notification event: {}", e.getMessage(), e);
//            throw e; // re-throw so RabbitMQ can dead-letter it
//        }
//    }
//}

package com.airnexus.notification_service.consumer;

import com.airnexus.notification_service.dto.NotificationEvent;
import com.airnexus.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${rabbitmq.queue.notification}")
    public void handleNotificationEvent(NotificationEvent event) {
        System.out.println("\n========================================");
        System.out.println("🔥🔥🔥 MESSAGE RECEIVED IN CONSUMER 🔥🔥🔥");
        System.out.println("Event Type: " + event.getType());
        System.out.println("User ID: " + event.getUserId());
        System.out.println("Email: " + event.getEmail());
        System.out.println("Booking ID: " + event.getBookingId());
        System.out.println("PNR: " + event.getPnr());
        System.out.println("========================================\n");

        log.info("Received notification event: type={}, userId={}", event.getType(), event.getUserId());

        try {
            switch (event.getType()) {
                case "BOOKING_CONFIRMED" -> {
                    System.out.println("📧 Calling sendBookingConfirmation...");
                    notificationService.sendBookingConfirmation(
                            event.getUserId(), event.getEmail(), event.getPhone(),
                            event.getBookingId(), event.getPnr(), event.getFlightDetails());
                    System.out.println("✅ sendBookingConfirmation completed");
                }

                case "PAYMENT_SUCCESS" -> notificationService.sendPaymentSuccess(
                        event.getUserId(), event.getEmail(),
                        event.getBookingId(), event.getAmount());

                case "FLIGHT_DELAY" -> notificationService.sendFlightDelay(
                        event.getUserId(), event.getEmail(), event.getBookingId(),
                        event.getFlightNumber(), event.getNewTime());

                case "CHECKIN_REMINDER" -> notificationService.sendCheckInReminder(
                        event.getUserId(), event.getEmail(), event.getPhone(),
                        event.getBookingId(), event.getPnr(), event.getFlightNumber());

                case "CANCELLATION" -> notificationService.sendCancellationConfirmation(
                        event.getUserId(), event.getEmail(),
                        event.getBookingId(), event.getPnr());

                default -> log.warn("Unknown notification type: {}", event.getType());
            }

            System.out.println("✅✅✅ MESSAGE PROCESSED SUCCESSFULLY ✅✅✅\n");

        } catch (Exception e) {
            System.out.println("❌❌❌ ERROR PROCESSING MESSAGE ❌❌❌");
            e.printStackTrace();
            log.error("Failed to process notification event: {}", e.getMessage(), e);
            throw e;
        }
    }
}