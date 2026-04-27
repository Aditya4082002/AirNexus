package com.airnexus.booking_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingDTO {
    private String bookingId;
    private String userId;
    private String flightId;
    private String pnrCode;
    private String tripType;
    private String status;
    private Double totalFare;
    private Double baseFare;
    private Double taxes;
    private Double ancillaryCharges;
    private String mealPreference;
    private Integer luggageKg;
    private String contactEmail;
    private String contactPhone;
    private LocalDateTime bookedAt;
    private String paymentId;
    private Integer numberOfPassengers;
}