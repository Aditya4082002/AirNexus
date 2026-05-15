package com.airnexus.payment_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private String paymentId;
    private String bookingId;
    private String userId;
    private Double amount;
    private String currency;
    private String status;
    private String paymentMode;
    private String transactionId;
    private String razorpayOrderId;
    private String gatewayResponse;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private Double refundAmount;
    private LocalDateTime createdAt;
}