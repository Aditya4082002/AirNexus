package com.airnexus.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String userId;
    private String email;
    private String phone;
    private String type;          // BOOKING_CONFIRMED, PAYMENT_SUCCESS, etc.
    private String bookingId;
    private String pnr;
    private String flightDetails;
    private Double amount;
    private String flightNumber;
    private String newTime;
}