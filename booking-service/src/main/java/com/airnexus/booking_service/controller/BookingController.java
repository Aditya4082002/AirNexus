package com.airnexus.booking_service.controller;

import com.airnexus.booking_service.dto.BookingDTO;
import com.airnexus.booking_service.dto.BookingRequest;
import com.airnexus.booking_service.dto.FareSummary;
import com.airnexus.booking_service.entity.Booking;
import com.airnexus.booking_service.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(@RequestBody BookingRequest request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable String id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @GetMapping("/pnr/{pnrCode}")
    public ResponseEntity<BookingDTO> getBookingByPnr(@PathVariable String pnrCode) {
        return ResponseEntity.ok(bookingService.getBookingByPnr(pnrCode));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUser(userId));
    }

    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<List<BookingDTO>> getUpcomingBookings(@PathVariable String userId) {
        return ResponseEntity.ok(bookingService.getUpcomingBookings(userId));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingDTO> cancelBooking(@PathVariable String id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<BookingDTO> updateStatus(
            @PathVariable String id,
            @RequestParam Booking.BookingStatus status) {
        return ResponseEntity.ok(bookingService.updateStatus(id, status));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<BookingDTO> confirmBooking(
            @PathVariable String id,
            @RequestParam String paymentId) {
        return ResponseEntity.ok(bookingService.confirmBooking(id, paymentId));
    }

    @PostMapping("/calculate-fare")
    public ResponseEntity<FareSummary> calculateFare(
            @RequestParam String flightId,
            @RequestParam Integer passengers,
            @RequestParam List<String> seatIds,
            @RequestParam(required = false) Integer luggageKg) {
        return ResponseEntity.ok(bookingService.calculateFare(flightId, passengers, seatIds, luggageKg));
    }
}
