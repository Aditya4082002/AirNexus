package com.airnexus.passenger_service.client;

import com.airnexus.passenger_service.client.dto.BookingDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "booking-service")
public interface BookingClient {

    @GetMapping("/api/bookings/pnr/{pnrCode}")
    BookingDTO getBookingByPnr(@PathVariable String pnrCode);
}
