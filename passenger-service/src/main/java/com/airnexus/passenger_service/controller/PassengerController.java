package com.airnexus.passenger_service.controller;

import com.airnexus.passenger_service.dto.PassengerDTO;
import com.airnexus.passenger_service.service.PassengerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passengers")
@RequiredArgsConstructor
public class PassengerController {

    private final PassengerService passengerService;

    @PostMapping
    public ResponseEntity<PassengerDTO> addPassenger(@RequestBody PassengerDTO passengerDTO) {
        return ResponseEntity.ok(passengerService.addPassenger(passengerDTO));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<PassengerDTO>> addPassengers(@RequestBody List<PassengerDTO> passengers) {
        return ResponseEntity.ok(passengerService.addPassengers(passengers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassengerDTO> getPassengerById(@PathVariable String id) {
        return ResponseEntity.ok(passengerService.getPassengerById(id));
    }

    @GetMapping("/ticket/{ticketNumber}")
    public ResponseEntity<PassengerDTO> getByTicketNumber(@PathVariable String ticketNumber) {
        return ResponseEntity.ok(passengerService.getByTicketNumber(ticketNumber));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PassengerDTO>> getPassengersByBooking(@PathVariable String bookingId) {
        return ResponseEntity.ok(passengerService.getPassengersByBooking(bookingId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PassengerDTO> updatePassenger(
            @PathVariable String id,
            @RequestBody PassengerDTO passengerDTO) {
        return ResponseEntity.ok(passengerService.updatePassenger(id, passengerDTO));
    }

    @PutMapping("/{id}/assign-seat")
    public ResponseEntity<PassengerDTO> assignSeat(
            @PathVariable String id,
            @RequestParam String seatId,
            @RequestParam String seatNumber) {
        return ResponseEntity.ok(passengerService.assignSeat(id, seatId, seatNumber));
    }

    @PutMapping("/{id}/check-in")
    public ResponseEntity<PassengerDTO> checkIn(@PathVariable String id) {
        return ResponseEntity.ok(passengerService.checkIn(id));
    }

    @GetMapping("/booking/{bookingId}/count")
    public ResponseEntity<Integer> countByBooking(@PathVariable String bookingId) {
        return ResponseEntity.ok(passengerService.countByBooking(bookingId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePassenger(@PathVariable String id) {
        passengerService.deletePassenger(id);
        return ResponseEntity.ok("Passenger deleted successfully");
    }

    @DeleteMapping("/booking/{bookingId}")
    public ResponseEntity<String> deletePassengersByBooking(@PathVariable String bookingId) {
        passengerService.deletePassengersByBooking(bookingId);
        return ResponseEntity.ok("All passengers for booking deleted");
    }
}