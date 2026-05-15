package com.airnexus.passenger_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "seat-service")
public interface SeatClient {

    @PutMapping("/api/seats/{seatId}/hold")
    void holdSeat(@PathVariable String seatId);

    @PutMapping("/api/seats/{seatId}/confirm")
    void confirmSeat(@PathVariable String seatId);

    @PutMapping("/api/seats/{seatId}/release")
    void releaseSeat(@PathVariable String seatId);
}