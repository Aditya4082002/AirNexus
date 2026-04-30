package com.airnexus.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String paymentId;
    private String razorpayOrderId;
    private Double amount;
    private String currency;
    private String status;
    private String razorpayKeyId; // For frontend
}