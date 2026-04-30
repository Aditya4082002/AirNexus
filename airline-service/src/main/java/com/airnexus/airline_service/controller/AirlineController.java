package com.airnexus.airline_service.controller;

import com.airnexus.airline_service.dto.AirlineDTO;
import com.airnexus.airline_service.dto.AirportDTO;
import com.airnexus.airline_service.service.AirlineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AirlineController {

    private final AirlineService airlineService;

    // ============ AIRLINE ENDPOINTS ============
    @PostMapping("/airlines")
    public ResponseEntity<AirlineDTO> createAirline(@RequestBody AirlineDTO airlineDTO) {
        return ResponseEntity.ok(airlineService.createAirline(airlineDTO));
    }

    @GetMapping("/airlines/{id}")
    public ResponseEntity<AirlineDTO> getAirlineById(@PathVariable String id) {
        return ResponseEntity.ok(airlineService.getAirlineById(id));
    }

    @GetMapping("/airlines/iata/{iataCode}")
    public ResponseEntity<AirlineDTO> getAirlineByIata(@PathVariable String iataCode) {
        return ResponseEntity.ok(airlineService.getAirlineByIata(iataCode));
    }

    @GetMapping("/airlines")
    public ResponseEntity<List<AirlineDTO>> getAllAirlines() {
        return ResponseEntity.ok(airlineService.getAllAirlines());
    }

    @GetMapping("/airlines/active")
    public ResponseEntity<List<AirlineDTO>> getActiveAirlines() {
        return ResponseEntity.ok(airlineService.getActiveAirlines());
    }

    @PutMapping("/airlines/{id}")
    public ResponseEntity<AirlineDTO> updateAirline(
            @PathVariable String id,
            @RequestBody AirlineDTO airlineDTO) {
        return ResponseEntity.ok(airlineService.updateAirline(id, airlineDTO));
    }

    @PutMapping("/airlines/{id}/deactivate")
    public ResponseEntity<String> deactivateAirline(@PathVariable String id) {
        airlineService.deactivateAirline(id);
        return ResponseEntity.ok("Airline deactivated successfully");
    }

    // ============ AIRPORT ENDPOINTS ============

    @PostMapping("/airports")
    public ResponseEntity<AirportDTO> createAirport(@RequestBody AirportDTO airportDTO) {
        return ResponseEntity.ok(airlineService.createAirport(airportDTO));
    }

    @GetMapping("/airports/{id}")
    public ResponseEntity<AirportDTO> getAirportById(@PathVariable String id) {
        return ResponseEntity.ok(airlineService.getAirportById(id));
    }

    @GetMapping("/airports/iata/{iataCode}")
    public ResponseEntity<AirportDTO> getAirportByIata(@PathVariable String iataCode) {
        return ResponseEntity.ok(airlineService.getAirportByIata(iataCode));
    }

    @GetMapping("/airports")
    public ResponseEntity<List<AirportDTO>> getAllAirports() {
        return ResponseEntity.ok(airlineService.getAllAirports());
    }

    @GetMapping("/airports/search")
    public ResponseEntity<List<AirportDTO>> searchAirports(@RequestParam String q) {
        return ResponseEntity.ok(airlineService.searchAirports(q));
    }

    @GetMapping("/airports/city/{city}")
    public ResponseEntity<List<AirportDTO>> getAirportsByCity(@PathVariable String city) {
        return ResponseEntity.ok(airlineService.getAirportsByCity(city));
    }

    @PutMapping("/airports/{id}")
    public ResponseEntity<AirportDTO> updateAirport(
            @PathVariable String id,
            @RequestBody AirportDTO airportDTO) {
        return ResponseEntity.ok(airlineService.updateAirport(id, airportDTO));
    }

    @DeleteMapping("/airports/{id}")
    public ResponseEntity<String> deleteAirport(@PathVariable String id) {
        airlineService.deleteAirport(id);
        return ResponseEntity.ok("Airport deleted successfully");
    }
}
