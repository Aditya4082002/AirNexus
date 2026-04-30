package com.airnexus.airline_service.controller;

import com.airnexus.airline_service.dto.AirlineDTO;
import com.airnexus.airline_service.dto.AirportDTO;
import com.airnexus.airline_service.service.AirlineService;
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
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Airline & Airport Management", description = "APIs for managing airlines and airports")
public class AirlineController {

    private final AirlineService airlineService;

    // ============ AIRLINE ENDPOINTS ============

    @Operation(summary = "Create a new airline")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Airline created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/airlines")
    public ResponseEntity<AirlineDTO> createAirline(@RequestBody AirlineDTO airlineDTO) {
        return ResponseEntity.ok(airlineService.createAirline(airlineDTO));
    }

    @Operation(summary = "Get airline by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Airline found"),
            @ApiResponse(responseCode = "404", description = "Airline not found")
    })
    @GetMapping("/airlines/{id}")
    public ResponseEntity<AirlineDTO> getAirlineById(
            @Parameter(description = "Airline ID") @PathVariable String id) {
        return ResponseEntity.ok(airlineService.getAirlineById(id));
    }

    @Operation(summary = "Get airline by IATA code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Airline found"),
            @ApiResponse(responseCode = "404", description = "Airline not found")
    })
    @GetMapping("/airlines/iata/{iataCode}")
    public ResponseEntity<AirlineDTO> getAirlineByIata(
            @Parameter(description = "IATA code of the airline") @PathVariable String iataCode) {
        return ResponseEntity.ok(airlineService.getAirlineByIata(iataCode));
    }

    @Operation(summary = "Get all airlines")
    @ApiResponse(responseCode = "200", description = "List of all airlines")
    @GetMapping("/airlines")
    public ResponseEntity<List<AirlineDTO>> getAllAirlines() {
        return ResponseEntity.ok(airlineService.getAllAirlines());
    }

    @Operation(summary = "Get all active airlines")
    @ApiResponse(responseCode = "200", description = "List of active airlines")
    @GetMapping("/airlines/active")
    public ResponseEntity<List<AirlineDTO>> getActiveAirlines() {
        return ResponseEntity.ok(airlineService.getActiveAirlines());
    }

    @Operation(summary = "Update an existing airline")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Airline updated successfully"),
            @ApiResponse(responseCode = "404", description = "Airline not found")
    })
    @PutMapping("/airlines/{id}")
    public ResponseEntity<AirlineDTO> updateAirline(
            @Parameter(description = "Airline ID") @PathVariable String id,
            @RequestBody AirlineDTO airlineDTO) {
        return ResponseEntity.ok(airlineService.updateAirline(id, airlineDTO));
    }

    @Operation(summary = "Deactivate an airline")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Airline deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Airline not found")
    })
    @PutMapping("/airlines/{id}/deactivate")
    public ResponseEntity<String> deactivateAirline(
            @Parameter(description = "Airline ID") @PathVariable String id) {
        airlineService.deactivateAirline(id);
        return ResponseEntity.ok("Airline deactivated successfully");
    }

    // ============ AIRPORT ENDPOINTS ============

    @Operation(summary = "Create a new airport")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Airport created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/airports")
    public ResponseEntity<AirportDTO> createAirport(@RequestBody AirportDTO airportDTO) {
        return ResponseEntity.ok(airlineService.createAirport(airportDTO));
    }

    @Operation(summary = "Get airport by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Airport found"),
            @ApiResponse(responseCode = "404", description = "Airport not found")
    })
    @GetMapping("/airports/{id}")
    public ResponseEntity<AirportDTO> getAirportById(
            @Parameter(description = "Airport ID") @PathVariable String id) {
        return ResponseEntity.ok(airlineService.getAirportById(id));
    }

    @Operation(summary = "Get airport by IATA code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Airport found"),
            @ApiResponse(responseCode = "404", description = "Airport not found")
    })
    @GetMapping("/airports/iata/{iataCode}")
    public ResponseEntity<AirportDTO> getAirportByIata(
            @Parameter(description = "IATA code of the airport") @PathVariable String iataCode) {
        return ResponseEntity.ok(airlineService.getAirportByIata(iataCode));
    }

    @Operation(summary = "Get all airports")
    @ApiResponse(responseCode = "200", description = "List of all airports")
    @GetMapping("/airports")
    public ResponseEntity<List<AirportDTO>> getAllAirports() {
        return ResponseEntity.ok(airlineService.getAllAirports());
    }

    @Operation(summary = "Search airports by keyword")
    @ApiResponse(responseCode = "200", description = "List of matching airports")
    @GetMapping("/airports/search")
    public ResponseEntity<List<AirportDTO>> searchAirports(
            @Parameter(description = "Search query (name, city, IATA code)") @RequestParam String q) {
        return ResponseEntity.ok(airlineService.searchAirports(q));
    }

    @Operation(summary = "Get airports by city")
    @ApiResponse(responseCode = "200", description = "List of airports in the city")
    @GetMapping("/airports/city/{city}")
    public ResponseEntity<List<AirportDTO>> getAirportsByCity(
            @Parameter(description = "City name") @PathVariable String city) {
        return ResponseEntity.ok(airlineService.getAirportsByCity(city));
    }

    @Operation(summary = "Update an existing airport")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Airport updated successfully"),
            @ApiResponse(responseCode = "404", description = "Airport not found")
    })
    @PutMapping("/airports/{id}")
    public ResponseEntity<AirportDTO> updateAirport(
            @Parameter(description = "Airport ID") @PathVariable String id,
            @RequestBody AirportDTO airportDTO) {
        return ResponseEntity.ok(airlineService.updateAirport(id, airportDTO));
    }

    @Operation(summary = "Delete an airport")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Airport deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Airport not found")
    })
    @DeleteMapping("/airports/{id}")
    public ResponseEntity<String> deleteAirport(
            @Parameter(description = "Airport ID") @PathVariable String id) {
        airlineService.deleteAirport(id);
        return ResponseEntity.ok("Airport deleted successfully");
    }
}
