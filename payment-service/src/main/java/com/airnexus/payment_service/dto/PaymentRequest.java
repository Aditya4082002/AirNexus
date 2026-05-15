package com.airnexus.payment_service.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private String bookingId;
    private String userId;
    private Double amount;
    private String currency = "INR";
}