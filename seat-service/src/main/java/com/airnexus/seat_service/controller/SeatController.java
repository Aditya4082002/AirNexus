package com.airnexus.seat_service.controller;

import com.airnexus.seat_service.dto.SeatDTO;
import com.airnexus.seat_service.entity.Seat;
import com.airnexus.seat_service.service.SeatService;
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
@RequestMapping("/api/seats")
@RequiredArgsConstructor
@Tag(name = "Seat Management", description = "APIs for managing seat inventory, holds and seat maps")
public class SeatController {

    private final SeatService seatService;

    @Operation(summary = "Add a single seat")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seat added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid seat data")
    })
    @PostMapping
    public ResponseEntity<SeatDTO> addSeat(@RequestBody SeatDTO seatDTO) {
        return ResponseEntity.ok(seatService.addSeat(seatDTO));
    }

    @Operation(summary = "Add multiple seats for a flight in bulk",
            description = "Used by airline staff to add all seats when creating a new flight.")
    @ApiResponse(responseCode = "200", description = "All seats added successfully")
    @PostMapping("/flight/{flightId}/bulk")
    public ResponseEntity<List<SeatDTO>> addSeatsForFlight(
            @Parameter(description = "Flight ID") @PathVariable String flightId,
            @RequestBody List<SeatDTO> seats) {
        return ResponseEntity.ok(seatService.addSeatsForFlight(flightId, seats));
    }

    @Operation(summary = "Get seat by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seat found"),
            @ApiResponse(responseCode = "404", description = "Seat not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SeatDTO> getSeatById(
            @Parameter(description = "Seat ID") @PathVariable String id) {
        return ResponseEntity.ok(seatService.getSeatById(id));
    }

    @Operation(summary = "Get available seats for a flight")
    @ApiResponse(responseCode = "200", description = "List of available seats")
    @GetMapping("/flight/{flightId}/available")
    public ResponseEntity<List<SeatDTO>> getAvailableSeats(
            @Parameter(description = "Flight ID") @PathVariable String flightId) {
        return ResponseEntity.ok(seatService.getAvailableSeats(flightId));
    }

    @Operation(summary = "Get available seats by class for a flight",
            description = "Filters available seats by class: ECONOMY, BUSINESS, or FIRST.")
    @ApiResponse(responseCode = "200", description = "List of available seats in the requested class")
    @GetMapping("/flight/{flightId}/class/{seatClass}")
    public ResponseEntity<List<SeatDTO>> getAvailableByClass(
            @Parameter(description = "Flight ID") @PathVariable String flightId,
            @Parameter(description = "Seat class (ECONOMY, BUSINESS, FIRST)") @PathVariable Seat.SeatClass seatClass) {
        return ResponseEntity.ok(seatService.getAvailableByClass(flightId, seatClass));
    }

    @Operation(summary = "Get complete seat map for a flight",
            description = "Returns all seats (available, held and booked) for displaying the seat selection UI.")
    @ApiResponse(responseCode = "200", description = "Full seat map for the flight")
    @GetMapping("/flight/{flightId}/map")
    public ResponseEntity<List<SeatDTO>> getSeatMap(
            @Parameter(description = "Flight ID") @PathVariable String flightId) {
        return ResponseEntity.ok(seatService.getSeatMap(flightId));
    }

    @Operation(summary = "Hold a seat temporarily for a user",
            description = "Puts a seat on hold for the requesting user. Hold expires after a set duration.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seat held successfully"),
            @ApiResponse(responseCode = "409", description = "Seat already held or booked")
    })
    @PutMapping("/{seatId}/hold")
    public ResponseEntity<SeatDTO> holdSeat(
            @Parameter(description = "Seat ID") @PathVariable String seatId,
            @Parameter(description = "User ID from API Gateway") @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(seatService.holdSeat(seatId, userId));
    }

    @Operation(summary = "Release a seat hold")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seat released successfully"),
            @ApiResponse(responseCode = "404", description = "Seat not found")
    })
    @PutMapping("/{seatId}/release")
    public ResponseEntity<SeatDTO> releaseSeat(
            @Parameter(description = "Seat ID") @PathVariable String seatId) {
        return ResponseEntity.ok(seatService.releaseSeat(seatId));
    }

    @Operation(summary = "Confirm a seat as booked",
            description = "Marks a seat as permanently booked after successful payment.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seat confirmed"),
            @ApiResponse(responseCode = "404", description = "Seat not found")
    })
    @PutMapping("/{seatId}/confirm")
    public ResponseEntity<SeatDTO> confirmSeat(
            @Parameter(description = "Seat ID") @PathVariable String seatId) {
        return ResponseEntity.ok(seatService.confirmSeat(seatId));
    }

    @Operation(summary = "Update seat details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seat updated successfully"),
            @ApiResponse(responseCode = "404", description = "Seat not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SeatDTO> updateSeat(
            @Parameter(description = "Seat ID") @PathVariable String id,
            @RequestBody SeatDTO seatDTO) {
        return ResponseEntity.ok(seatService.updateSeat(id, seatDTO));
    }

    @Operation(summary = "Count available seats by class for a flight")
    @ApiResponse(responseCode = "200", description = "Count of available seats in the class")
    @GetMapping("/flight/{flightId}/count/{seatClass}")
    public ResponseEntity<Integer> countAvailableByClass(
            @Parameter(description = "Flight ID") @PathVariable String flightId,
            @Parameter(description = "Seat class") @PathVariable Seat.SeatClass seatClass) {
        return ResponseEntity.ok(seatService.countAvailableByClass(flightId, seatClass));
    }

    @Operation(summary = "Delete all seats for a flight",
            description = "Removes all seat records for a flight. Used when deleting a flight.")
    @ApiResponse(responseCode = "200", description = "All seats deleted for the flight")
    @DeleteMapping("/flight/{flightId}")
    public ResponseEntity<String> deleteSeatsForFlight(
            @Parameter(description = "Flight ID") @PathVariable String flightId) {
        seatService.deleteSeatsForFlight(flightId);
        return ResponseEntity.ok("Seats deleted for flight");
    }
}
