package com.airnexus.passenger_service.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingDTO {
    private String bookingId;
    private String pnrCode;
    private String flightId;
    private String flightNumber;
    private String status;
}
