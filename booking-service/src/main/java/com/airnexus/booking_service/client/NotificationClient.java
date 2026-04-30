package com.airnexus.booking_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/api/notifications/booking-confirmation")
    void sendBookingConfirmation(@RequestBody BookingNotificationRequest request);

    @PostMapping("/api/notifications/cancellation")
    void sendCancellationNotification(@RequestBody CancellationNotificationRequest request);
}

