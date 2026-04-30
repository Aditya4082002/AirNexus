package com.airnexus.booking_service.controller;

import com.airnexus.booking_service.dto.BookingDTO;
import com.airnexus.booking_service.dto.BookingRequest;
import com.airnexus.booking_service.dto.FareSummary;
import com.airnexus.booking_service.entity.Booking;
import com.airnexus.booking_service.service.BookingService;
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
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Management", description = "APIs for creating and managing flight bookings")
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Create a new booking",
            description = "Books a flight for one or more passengers. Requires flight ID, passenger details and seat selection.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid booking request"),
            @ApiResponse(responseCode = "404", description = "Flight or seat not found")
    })
    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(@RequestBody BookingRequest request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    @Operation(summary = "Get booking by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking found"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(
            @Parameter(description = "Booking ID") @PathVariable String id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @Operation(summary = "Get booking by PNR code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking found"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @GetMapping("/pnr/{pnrCode}")
    public ResponseEntity<BookingDTO> getBookingByPnr(
            @Parameter(description = "PNR code of the booking") @PathVariable String pnrCode) {
        return ResponseEntity.ok(bookingService.getBookingByPnr(pnrCode));
    }

    @Operation(summary = "Get all bookings by user ID")
    @ApiResponse(responseCode = "200", description = "List of user's bookings")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByUser(
            @Parameter(description = "User ID") @PathVariable String userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUser(userId));
    }

    @Operation(summary = "Get upcoming bookings for a user")
    @ApiResponse(responseCode = "200", description = "List of upcoming bookings")
    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<List<BookingDTO>> getUpcomingBookings(
            @Parameter(description = "User ID") @PathVariable String userId) {
        return ResponseEntity.ok(bookingService.getUpcomingBookings(userId));
    }

    @Operation(summary = "Cancel a booking")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "400", description = "Booking cannot be cancelled")
    })
    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingDTO> cancelBooking(
            @Parameter(description = "Booking ID") @PathVariable String id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }

    @Operation(summary = "Update booking status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<BookingDTO> updateStatus(
            @Parameter(description = "Booking ID") @PathVariable String id,
            @Parameter(description = "New booking status") @RequestParam Booking.BookingStatus status) {
        return ResponseEntity.ok(bookingService.updateStatus(id, status));
    }

    @Operation(summary = "Confirm a booking after payment",
            description = "Marks a booking as confirmed once payment has been verified.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking confirmed"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @PutMapping("/{id}/confirm")
    public ResponseEntity<BookingDTO> confirmBooking(
            @Parameter(description = "Booking ID") @PathVariable String id,
            @Parameter(description = "Razorpay payment ID") @RequestParam String paymentId) {
        return ResponseEntity.ok(bookingService.confirmBooking(id, paymentId));
    }

    @Operation(summary = "Calculate fare before booking",
            description = "Returns a fare breakdown including base fare, seat charges and luggage charges.")
    @ApiResponse(responseCode = "200", description = "Fare summary returned")
    @PostMapping("/calculate-fare")
    public ResponseEntity<FareSummary> calculateFare(
            @Parameter(description = "Flight ID") @RequestParam String flightId,
            @Parameter(description = "Number of passengers") @RequestParam Integer passengers,
            @Parameter(description = "List of seat IDs selected") @RequestParam List<String> seatIds,
            @Parameter(description = "Luggage weight in KG (optional)") @RequestParam(required = false) Integer luggageKg) {
        return ResponseEntity.ok(bookingService.calculateFare(flightId, passengers, seatIds, luggageKg));
    }
}
