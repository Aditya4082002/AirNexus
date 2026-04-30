package com.airnexus.payment_service.service;

import com.airnexus.payment_service.client.BookingClient;
import com.airnexus.payment_service.client.NotificationClient;
import com.airnexus.payment_service.client.PaymentNotificationRequest;
import com.airnexus.payment_service.dto.PaymentDTO;
import com.airnexus.payment_service.dto.PaymentRequest;
import com.airnexus.payment_service.dto.PaymentResponse;
import com.airnexus.payment_service.entity.Payment;
import com.airnexus.payment_service.exception.CustomExceptions;
import com.airnexus.payment_service.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;
    private final BookingClient bookingClient;
    private final NotificationClient notificationClient;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Override
    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new CustomExceptions.InvalidPaymentAmountException(
                    "Payment amount must be greater than zero"
            );
        }

        try {
            // Create Razorpay Order
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int)(request.getAmount() * 100)); // Convert to paise
            orderRequest.put("currency", request.getCurrency());
            orderRequest.put("receipt", "rcpt_" + System.currentTimeMillis());

            Order razorpayOrder = razorpayClient.orders.create(orderRequest);

            // Create Payment record
            Payment payment = new Payment();
            payment.setBookingId(request.getBookingId());
            payment.setUserId(request.getUserId());
            payment.setAmount(request.getAmount());
            payment.setCurrency(request.getCurrency());
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setRazorpayOrderId(razorpayOrder.get("id"));
            payment.setGatewayResponse(razorpayOrder.toString());

            payment = paymentRepository.save(payment);

            return new PaymentResponse(
                    payment.getPaymentId(),
                    razorpayOrder.get("id"),
                    request.getAmount(),
                    request.getCurrency(),
                    "PENDING",
                    razorpayKeyId
            );

        } catch (RazorpayException e) {
            throw new CustomExceptions.RazorpayException(
                    "Razorpay order creation failed: " + e.getMessage()
            );
        }
    }

    @Override
    @Transactional
    public PaymentDTO processPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        // Verify signature
        if (!verifySignature(razorpayOrderId, razorpayPaymentId, razorpaySignature)) {
            throw new CustomExceptions.InvalidPaymentSignatureException(
                    "Invalid payment signature. Payment verification failed."
            );
        }

        // Find payment by order ID
        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new CustomExceptions.PaymentNotFoundException(
                        "Payment not found for Razorpay order ID: " + razorpayOrderId
                ));

        if (payment.getStatus() == Payment.PaymentStatus.PAID) {
            throw new CustomExceptions.PaymentAlreadyProcessedException(
                    "Payment already processed for this order"
            );
        }

        // Update payment status
        payment.setStatus(Payment.PaymentStatus.PAID);
        payment.setTransactionId(razorpayPaymentId);
        payment.setPaidAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        // Confirm booking - Let FeignException bubble up to GlobalExceptionHandler
        bookingClient.confirmBooking(payment.getBookingId(), payment.getPaymentId());

        // Send payment success notification
        PaymentNotificationRequest notificationRequest = new PaymentNotificationRequest(
                payment.getUserId(),
                null, // Email will be fetched from booking
                payment.getBookingId(),
                payment.getAmount()
        );
        notificationClient.sendPaymentSuccessNotification(notificationRequest);

        return mapToDTO(payment);
    }

    @Override
    public PaymentDTO getPaymentById(String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new CustomExceptions.PaymentNotFoundException(
                        "Payment not found with ID: " + id
                ));
        return mapToDTO(payment);
    }

    @Override
    public PaymentDTO getPaymentByBooking(String bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new CustomExceptions.PaymentNotFoundException(
                        "Payment not found for booking ID: " + bookingId
                ));
        return mapToDTO(payment);
    }

    @Override
    public List<PaymentDTO> getPaymentsByUser(String userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentDTO refundPayment(String paymentId, Double amount) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomExceptions.PaymentNotFoundException(
                        "Payment not found with ID: " + paymentId
                ));

        if (payment.getStatus() != Payment.PaymentStatus.PAID) {
            throw new CustomExceptions.RefundNotAllowedException(
                    "Only paid payments can be refunded. Current status: " + payment.getStatus()
            );
        }

        if (amount > payment.getAmount()) {
            throw new CustomExceptions.InvalidPaymentAmountException(
                    "Refund amount cannot exceed payment amount"
            );
        }

        try {
            // Create refund in Razorpay
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", (int)(amount * 100)); // Convert to paise

            // Actual Razorpay refund API call
            // razorpayClient.payments.refund(payment.getTransactionId(), refundRequest);

            // Update payment record
            if (amount.equals(payment.getAmount())) {
                payment.setStatus(Payment.PaymentStatus.REFUNDED);
            } else {
                payment.setStatus(Payment.PaymentStatus.PARTIALLY_REFUNDED);
            }
            payment.setRefundAmount(amount);
            payment.setRefundedAt(LocalDateTime.now());

            payment = paymentRepository.save(payment);

            // Update booking status
            bookingClient.updateStatus(payment.getBookingId(), "CANCELLED");

            return mapToDTO(payment);

        } catch (Exception e) {
            throw new CustomExceptions.RazorpayException(
                    "Refund processing failed: " + e.getMessage()
            );
        }
    }

    @Override
    public boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(razorpayKeySecret.getBytes(), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().equals(signature);

        } catch (Exception e) {
            throw new CustomExceptions.InvalidPaymentSignatureException(
                    "Signature verification error: " + e.getMessage()
            );
        }
    }

    private PaymentDTO mapToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentId(payment.getPaymentId());
        dto.setBookingId(payment.getBookingId());
        dto.setUserId(payment.getUserId());
        dto.setAmount(payment.getAmount());
        dto.setCurrency(payment.getCurrency());
        dto.setStatus(payment.getStatus().name());
        dto.setPaymentMode(payment.getPaymentMode() != null ? payment.getPaymentMode().name() : null);
        dto.setTransactionId(payment.getTransactionId());
        dto.setRazorpayOrderId(payment.getRazorpayOrderId());
        dto.setGatewayResponse(payment.getGatewayResponse());
        dto.setPaidAt(payment.getPaidAt());
        dto.setRefundedAt(payment.getRefundedAt());
        dto.setRefundAmount(payment.getRefundAmount());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }
}