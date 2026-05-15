package com.airnexus.passenger_service.controller;

import com.airnexus.passenger_service.dto.PassengerDTO;
import com.airnexus.passenger_service.service.PassengerService;
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
@RequestMapping("/api/passengers")
@RequiredArgsConstructor
@Tag(name = "Passenger Management", description = "APIs for managing passenger records and check-in")
public class PassengerController {

    private final PassengerService passengerService;

    @Operation(summary = "Add a single passenger to a booking")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Passenger added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid passenger data")
    })
    @PostMapping
    public ResponseEntity<PassengerDTO> addPassenger(@RequestBody PassengerDTO passengerDTO) {
        return ResponseEntity.ok(passengerService.addPassenger(passengerDTO));
    }

    @Operation(summary = "Add multiple passengers in bulk",
            description = "Adds a list of passengers to a booking in a single request.")
    @ApiResponse(responseCode = "200", description = "All passengers added successfully")
    @PostMapping("/bulk")
    public ResponseEntity<List<PassengerDTO>> addPassengers(@RequestBody List<PassengerDTO> passengers) {
        return ResponseEntity.ok(passengerService.addPassengers(passengers));
    }

    @Operation(summary = "Get passenger by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Passenger found"),
            @ApiResponse(responseCode = "404", description = "Passenger not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PassengerDTO> getPassengerById(
            @Parameter(description = "Passenger ID") @PathVariable String id) {
        return ResponseEntity.ok(passengerService.getPassengerById(id));
    }

    @Operation(summary = "Get passenger by ticket number")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Passenger found"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @GetMapping("/ticket/{ticketNumber}")
    public ResponseEntity<PassengerDTO> getByTicketNumber(
            @Parameter(description = "Ticket number") @PathVariable String ticketNumber) {
        return ResponseEntity.ok(passengerService.getByTicketNumber(ticketNumber));
    }

    @Operation(summary = "Get all passengers for a booking")
    @ApiResponse(responseCode = "200", description = "List of passengers for the booking")
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PassengerDTO>> getPassengersByBooking(
            @Parameter(description = "Booking ID") @PathVariable String bookingId) {
        return ResponseEntity.ok(passengerService.getPassengersByBooking(bookingId));
    }

    @Operation(summary = "Get all passengers by PNR code")
    @ApiResponse(responseCode = "200", description = "List of passengers with this PNR")
    @GetMapping("/pnr/{pnrCode}")
    public ResponseEntity<List<PassengerDTO>> getPassengersByPnr(
            @Parameter(description = "PNR code") @PathVariable String pnrCode) {
        return ResponseEntity.ok(passengerService.getPassengersByPnr(pnrCode));
    }

    @Operation(summary = "Update passenger details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Passenger updated successfully"),
            @ApiResponse(responseCode = "404", description = "Passenger not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PassengerDTO> updatePassenger(
            @Parameter(description = "Passenger ID") @PathVariable String id,
            @RequestBody PassengerDTO passengerDTO) {
        return ResponseEntity.ok(passengerService.updatePassenger(id, passengerDTO));
    }

    @Operation(summary = "Assign a seat to a passenger")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seat assigned successfully"),
            @ApiResponse(responseCode = "404", description = "Passenger not found")
    })
    @PutMapping("/{id}/assign-seat")
    public ResponseEntity<PassengerDTO> assignSeat(
            @Parameter(description = "Passenger ID") @PathVariable String id,
            @Parameter(description = "Seat ID") @RequestParam String seatId,
            @Parameter(description = "Seat number e.g. 12A") @RequestParam String seatNumber) {
        return ResponseEntity.ok(passengerService.assignSeat(id, seatId, seatNumber));
    }

    @Operation(summary = "Check in a passenger")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Passenger checked in successfully"),
            @ApiResponse(responseCode = "404", description = "Passenger not found")
    })
    @PutMapping("/{id}/check-in")
    public ResponseEntity<PassengerDTO> checkIn(
            @Parameter(description = "Passenger ID") @PathVariable String id) {
        return ResponseEntity.ok(passengerService.checkIn(id));
    }

    @Operation(summary = "Get passenger count for a booking")
    @ApiResponse(responseCode = "200", description = "Count of passengers in the booking")
    @GetMapping("/booking/{bookingId}/count")
    public ResponseEntity<Integer> countByBooking(
            @Parameter(description = "Booking ID") @PathVariable String bookingId) {
        return ResponseEntity.ok(passengerService.countByBooking(bookingId));
    }

    @Operation(summary = "Delete a passenger by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Passenger deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Passenger not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePassenger(
            @Parameter(description = "Passenger ID") @PathVariable String id) {
        passengerService.deletePassenger(id);
        return ResponseEntity.ok("Passenger deleted successfully");
    }

    @Operation(summary = "Delete all passengers for a booking")
    @ApiResponse(responseCode = "200", description = "All passengers for the booking deleted")
    @DeleteMapping("/booking/{bookingId}")
    public ResponseEntity<String> deletePassengersByBooking(
            @Parameter(description = "Booking ID") @PathVariable String bookingId) {
        passengerService.deletePassengersByBooking(bookingId);
        return ResponseEntity.ok("All passengers for booking deleted");
    }
}
