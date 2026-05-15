package com.airnexus.booking_service.client;

import com.airnexus.booking_service.client.fallback.SeatClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "seat-service",fallback = SeatClientFallback.class)
public interface SeatClient {

    @PutMapping("/api/seats/{seatId}/confirm")
    SeatDTO confirmSeat(@PathVariable String seatId);

    @PutMapping("/api/seats/{seatId}/release")
    SeatDTO releaseSeat(@PathVariable String seatId);

    @GetMapping("/api/seats/{id}")
    SeatDTO getSeatById(@PathVariable String id);
}


