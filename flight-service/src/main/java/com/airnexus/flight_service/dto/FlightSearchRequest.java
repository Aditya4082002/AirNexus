package com.airnexus.flight_service.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class FlightSearchRequest {
    private String origin;
    private String destination;
    private LocalDate departureDate;
    private LocalDate returnDate; // Optional, for round-trip
    private Integer passengers;
}