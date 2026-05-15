package com.airnexus.booking_service.dto;

import lombok.Data;

@Data
public class NotificationEvent {

    private String type;
    private String userId;
    private String email;
    private String phone;
    private String bookingId;
    private String pnr;
    private String flightDetails;
    private Double amount;
    private String flightNumber;
    private String newTime;
}
