package com.airnexus.booking_service.client;

import com.airnexus.booking_service.client.fallback.PassengerClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "passenger-service", fallback = PassengerClientFallback.class)
public interface PassengerClient {

    @PostMapping("/api/passengers/bulk")
    List<PassengerDTO> addPassengers(@RequestBody List<PassengerDTO> passengers);

    @PutMapping("/api/passengers/{id}/assign-seat")
    PassengerDTO assignSeat(@PathVariable String id,
                            @RequestParam String seatId,
                            @RequestParam String seatNumber);

    @DeleteMapping("/api/passengers/booking/{bookingId}")
    void deletePassengersByBooking(@PathVariable String bookingId);
}


