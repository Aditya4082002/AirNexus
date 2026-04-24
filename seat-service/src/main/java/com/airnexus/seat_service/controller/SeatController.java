package com.airnexus.seat_service.controller;

import com.airnexus.seat_service.dto.SeatDTO;
import com.airnexus.seat_service.entity.Seat;
import com.airnexus.seat_service.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @PostMapping
    public ResponseEntity<SeatDTO> addSeat(@RequestBody SeatDTO seatDTO) {
        return ResponseEntity.ok(seatService.addSeat(seatDTO));
    }

    @PostMapping("/flight/{flightId}/bulk")
    public ResponseEntity<List<SeatDTO>> addSeatsForFlight(
            @PathVariable String flightId,
            @RequestBody List<SeatDTO> seats) {
        return ResponseEntity.ok(seatService.addSeatsForFlight(flightId, seats));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeatDTO> getSeatById(@PathVariable String id) {
        return ResponseEntity.ok(seatService.getSeatById(id));
    }

    @GetMapping("/flight/{flightId}/available")
    public ResponseEntity<List<SeatDTO>> getAvailableSeats(@PathVariable String flightId) {
        return ResponseEntity.ok(seatService.getAvailableSeats(flightId));
    }

    @GetMapping("/flight/{flightId}/class/{seatClass}")
    public ResponseEntity<List<SeatDTO>> getAvailableByClass(
            @PathVariable String flightId,
            @PathVariable Seat.SeatClass seatClass) {
        return ResponseEntity.ok(seatService.getAvailableByClass(flightId, seatClass));
    }

    @GetMapping("/flight/{flightId}/map")
    public ResponseEntity<List<SeatDTO>> getSeatMap(@PathVariable String flightId) {
        return ResponseEntity.ok(seatService.getSeatMap(flightId));
    }

    @PutMapping("/{seatId}/hold")
    public ResponseEntity<SeatDTO> holdSeat(
            @PathVariable String seatId,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(seatService.holdSeat(seatId, userId));
    }

    @PutMapping("/{seatId}/release")
    public ResponseEntity<SeatDTO> releaseSeat(@PathVariable String seatId) {
        return ResponseEntity.ok(seatService.releaseSeat(seatId));
    }

    @PutMapping("/{seatId}/confirm")
    public ResponseEntity<SeatDTO> confirmSeat(@PathVariable String seatId) {
        return ResponseEntity.ok(seatService.confirmSeat(seatId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SeatDTO> updateSeat(
            @PathVariable String id,
            @RequestBody SeatDTO seatDTO) {
        return ResponseEntity.ok(seatService.updateSeat(id, seatDTO));
    }

    @GetMapping("/flight/{flightId}/count/{seatClass}")
    public ResponseEntity<Integer> countAvailableByClass(
            @PathVariable String flightId,
            @PathVariable Seat.SeatClass seatClass) {
        return ResponseEntity.ok(seatService.countAvailableByClass(flightId, seatClass));
    }

    @DeleteMapping("/flight/{flightId}")
    public ResponseEntity<String> deleteSeatsForFlight(@PathVariable String flightId) {
        seatService.deleteSeatsForFlight(flightId);
        return ResponseEntity.ok("Seats deleted for flight");
    }
}