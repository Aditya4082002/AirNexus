package com.airnexus.flight_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FlightDTO {
    private String flightId;
    private String flightNumber;
    private String airlineId;
    private String originAirportCode;
    private String destinationAirportCode;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer durationMinutes;
    private String status;
    private String aircraftType;
    private Integer totalSeats;
    private Integer availableSeats;
    private Double basePrice;
}