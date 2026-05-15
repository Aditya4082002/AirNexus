package com.airnexus.flight_service.controller;

import com.airnexus.flight_service.dto.FlightDTO;
import com.airnexus.flight_service.dto.FlightSearchRequest;
import com.airnexus.flight_service.entity.Flight;
import com.airnexus.flight_service.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Tag(name = "Flight Management", description = "APIs for managing and searching flights")
public class FlightController {

    private final FlightService flightService;

    @Operation(summary = "Add a new flight")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Flight added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid flight data")
    })
    @PostMapping
    public ResponseEntity<FlightDTO> addFlight(@RequestBody FlightDTO flightDTO) {
        return ResponseEntity.ok(flightService.addFlight(flightDTO));
    }

    @Operation(summary = "Get flight by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Flight found"),
            @ApiResponse(responseCode = "404", description = "Flight not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FlightDTO> getFlightById(
            @Parameter(description = "Flight ID") @PathVariable String id) {
        return ResponseEntity.ok(flightService.getFlightById(id));
    }

    @Operation(summary = "Get flight by flight number")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Flight found"),
            @ApiResponse(responseCode = "404", description = "Flight not found")
    })
    @GetMapping("/number/{flightNumber}")
    public ResponseEntity<FlightDTO> getFlightByNumber(
            @Parameter(description = "Flight number e.g. AI-202") @PathVariable String flightNumber) {
        return ResponseEntity.ok(flightService.getFlightByNumber(flightNumber));
    }

    @Operation(summary = "Search available flights",
            description = "Search flights by origin, destination, date and passenger count.")
    @ApiResponse(responseCode = "200", description = "List of matching flights")
    @PostMapping("/search")
    public ResponseEntity<List<FlightDTO>> searchFlights(@RequestBody FlightSearchRequest request) {
        return ResponseEntity.ok(flightService.searchFlights(request));
    }

    @Operation(summary = "Get all flights by airline")
    @ApiResponse(responseCode = "200", description = "List of flights for the airline")
    @GetMapping("/airline/{airlineId}")
    public ResponseEntity<List<FlightDTO>> getFlightsByAirline(
            @Parameter(description = "Airline ID") @PathVariable String airlineId) {
        return ResponseEntity.ok(flightService.getFlightsByAirline(airlineId));
    }

    @Operation(summary = "Update an existing flight")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Flight updated successfully"),
            @ApiResponse(responseCode = "404", description = "Flight not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<FlightDTO> updateFlight(
            @Parameter(description = "Flight ID") @PathVariable String id,
            @RequestBody FlightDTO flightDTO) {
        return ResponseEntity.ok(flightService.updateFlight(id, flightDTO));
    }

    @Operation(summary = "Update flight status",
            description = "Updates the operational status of a flight (e.g. SCHEDULED, DELAYED, CANCELLED).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "404", description = "Flight not found")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<FlightDTO> updateFlightStatus(
            @Parameter(description = "Flight ID") @PathVariable String id,
            @Parameter(description = "New flight status") @RequestParam Flight.FlightStatus status) {
        return ResponseEntity.ok(flightService.updateFlightStatus(id, status));
    }

    @Operation(summary = "Decrement available seats after booking",
            description = "Reduces available seat count. Called internally by booking-service.")
    @ApiResponse(responseCode = "200", description = "Seats decremented")
    @PutMapping("/{id}/seats/decrement")
    public ResponseEntity<String> decrementSeats(
            @Parameter(description = "Flight ID") @PathVariable String id,
            @Parameter(description = "Number of seats to decrement") @RequestParam Integer count) {
        flightService.decrementSeats(id, count);
        return ResponseEntity.ok("Seats decremented");
    }

    @Operation(summary = "Increment available seats after cancellation",
            description = "Restores seat count when a booking is cancelled.")
    @ApiResponse(responseCode = "200", description = "Seats incremented")
    @PutMapping("/{id}/seats/increment")
    public ResponseEntity<String> incrementSeats(
            @Parameter(description = "Flight ID") @PathVariable String id,
            @Parameter(description = "Number of seats to restore") @RequestParam Integer count) {
        flightService.incrementSeats(id, count);
        return ResponseEntity.ok("Seats incremented");
    }

    @Operation(summary = "Delete a flight")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Flight deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Flight not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFlight(
            @Parameter(description = "Flight ID") @PathVariable String id) {
        flightService.deleteFlight(id);
        return ResponseEntity.ok("Flight deleted successfully");
    }
}
