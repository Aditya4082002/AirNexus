package com.airnexus.flight_service.controller;

import com.airnexus.flight_service.dto.FlightDTO;
import com.airnexus.flight_service.dto.FlightSearchRequest;
import com.airnexus.flight_service.entity.Flight;
import com.airnexus.flight_service.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @PostMapping
    public ResponseEntity<FlightDTO> addFlight(@RequestBody FlightDTO flightDTO) {
        return ResponseEntity.ok(flightService.addFlight(flightDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightDTO> getFlightById(@PathVariable String id) {
        return ResponseEntity.ok(flightService.getFlightById(id));
    }

    @GetMapping("/number/{flightNumber}")
    public ResponseEntity<FlightDTO> getFlightByNumber(@PathVariable String flightNumber) {
        return ResponseEntity.ok(flightService.getFlightByNumber(flightNumber));
    }

    @PostMapping("/search")
    public ResponseEntity<List<FlightDTO>> searchFlights(@RequestBody FlightSearchRequest request) {
        return ResponseEntity.ok(flightService.searchFlights(request));
    }

    @GetMapping("/airline/{airlineId}")
    public ResponseEntity<List<FlightDTO>> getFlightsByAirline(@PathVariable String airlineId) {
        return ResponseEntity.ok(flightService.getFlightsByAirline(airlineId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlightDTO> updateFlight(
            @PathVariable String id,
            @RequestBody FlightDTO flightDTO) {
        return ResponseEntity.ok(flightService.updateFlight(id, flightDTO));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<FlightDTO> updateFlightStatus(
            @PathVariable String id,
            @RequestParam Flight.FlightStatus status) {
        return ResponseEntity.ok(flightService.updateFlightStatus(id, status));
    }

    @PutMapping("/{id}/seats/decrement")
    public ResponseEntity<String> decrementSeats(
            @PathVariable String id,
            @RequestParam Integer count) {
        flightService.decrementSeats(id, count);
        return ResponseEntity.ok("Seats decremented");
    }

    @PutMapping("/{id}/seats/increment")
    public ResponseEntity<String> incrementSeats(
            @PathVariable String id,
            @RequestParam Integer count) {
        flightService.incrementSeats(id, count);
        return ResponseEntity.ok("Seats incremented");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFlight(@PathVariable String id) {
        flightService.deleteFlight(id);
        return ResponseEntity.ok("Flight deleted successfully");
    }
}