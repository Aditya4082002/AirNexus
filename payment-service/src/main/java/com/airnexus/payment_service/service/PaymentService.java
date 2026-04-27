package com.airnexus.payment_service.service;

import com.airnexus.payment_service.dto.PaymentDTO;
import com.airnexus.payment_service.dto.PaymentRequest;
import com.airnexus.payment_service.dto.PaymentResponse;

import java.util.List;
import java.util.Map;

public interface PaymentService {
    PaymentResponse initiatePayment(PaymentRequest request);
    PaymentDTO processPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature);
    PaymentDTO getPaymentById(String id);
    PaymentDTO getPaymentByBooking(String bookingId);
    List<PaymentDTO> getPaymentsByUser(String userId);
    PaymentDTO refundPayment(String paymentId, Double amount);
    boolean verifySignature(String orderId, String paymentId, String signature);
}