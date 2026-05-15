package com.airnexus.airline_service.dto;

import lombok.Data;

@Data
public class AirportDTO {
    private String airportId;
    private String name;
    private String iataCode;
    private String icaoCode;
    private String city;
    private String country;
    private Double latitude;
    private Double longitude;
    private String timezone;
}
