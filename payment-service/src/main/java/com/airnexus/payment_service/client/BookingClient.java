package com.airnexus.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "booking-service")
public interface BookingClient {

    @PutMapping("/api/bookings/{id}/confirm")
    BookingDTO confirmBooking(@PathVariable String id, @RequestParam String paymentId);

    @PutMapping("/api/bookings/{id}/status")
    BookingDTO updateStatus(@PathVariable String id, @RequestParam String status);
}


