package com.airnexus.booking_service.client;

import com.airnexus.booking_service.client.fallback.FlightClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "flight-service", fallback = FlightClientFallback.class)
public interface FlightClient {

    @GetMapping("/api/flights/{id}")
    FlightDTO getFlightById(@PathVariable String id);

    @PutMapping("/api/flights/{id}/seats/decrement")
    void decrementSeats(@PathVariable String id, @RequestParam Integer count);

    @PutMapping("/api/flights/{id}/seats/increment")
    void incrementSeats(@PathVariable String id, @RequestParam Integer count);
}

