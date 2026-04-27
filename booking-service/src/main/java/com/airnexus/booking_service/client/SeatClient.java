package com.airnexus.booking_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "seat-service")
public interface SeatClient {

    @PutMapping("/api/seats/{seatId}/confirm")
    SeatDTO confirmSeat(@PathVariable String seatId);

    @PutMapping("/api/seats/{seatId}/release")
    SeatDTO releaseSeat(@PathVariable String seatId);

    @GetMapping("/api/seats/{id}")
    SeatDTO getSeatById(@PathVariable String id);
}


