package com.airnexus.airline_service.dto;

import lombok.Data;

@Data
public class AirlineDTO {
    private String airlineId;
    private String name;
    private String iataCode;
    private String icaoCode;
    private String logoUrl;
    private String country;
    private String contactEmail;
    private String contactPhone;
    private Boolean isActive;
}
