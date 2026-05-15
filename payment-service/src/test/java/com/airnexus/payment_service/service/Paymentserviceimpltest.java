package com.airnexus.payment_service.service;

import com.airnexus.payment_service.client.BookingClient;
import com.airnexus.payment_service.client.BookingDTO;
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
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.razorpay.OrderClient;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImpl Tests")
class PaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private RazorpayClient razorpayClient;
    @Mock private BookingClient bookingClient;
    @Mock private NotificationClient notificationClient;

    @Mock private OrderClient orderClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private static final String TEST_KEY_ID     = "rzp_test_key_id";
    private static final String TEST_KEY_SECRET = "rzp_test_key_secret";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "razorpayKeyId",     TEST_KEY_ID);
        ReflectionTestUtils.setField(paymentService, "razorpayKeySecret", TEST_KEY_SECRET);

        razorpayClient.orders = orderClient;
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private Payment buildPayment(String id, Payment.PaymentStatus status) {
        Payment p = new Payment();
        p.setPaymentId(id);
        p.setBookingId("bk-1");
        p.setUserId("user-1");
        p.setAmount(6450.0);
        p.setCurrency("INR");
        p.setStatus(status);
        p.setRazorpayOrderId("order_abc123");
        p.setTransactionId("pay_xyz789");
        p.setGatewayResponse("{\"id\":\"order_abc123\"}");
        p.setCreatedAt(LocalDateTime.now());
        return p;
    }

    private PaymentRequest buildRequest(Double amount) {
        PaymentRequest req = new PaymentRequest();
        req.setBookingId("bk-1");
        req.setUserId("user-1");
        req.setAmount(amount);
        req.setCurrency("INR");
        return req;
    }

    /** Compute the correct HMAC-SHA256 signature for test data using TEST_KEY_SECRET. */
    private String computeValidSignature(String orderId, String paymentId) throws Exception {
        String payload = orderId + "|" + paymentId;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(TEST_KEY_SECRET.getBytes(), "HmacSHA256"));
        byte[] hash = mac.doFinal(payload.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  initiatePayment()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("initiatePayment()")
    class InitiatePayment {

        @Test
        @DisplayName("should create Razorpay order and return PaymentResponse with PENDING status")
        void initiatePayment_success() throws Exception {
            PaymentRequest req = buildRequest(6450.0);
            Payment saved = buildPayment("pay-1", Payment.PaymentStatus.PENDING);

            // Mock Razorpay order
            Order mockOrder = mock(Order.class);
            when(mockOrder.get("id")).thenReturn("order_abc123");
            when(mockOrder.toString()).thenReturn("{\"id\":\"order_abc123\"}");
            when(razorpayClient.orders.create(any(JSONObject.class))).thenReturn(mockOrder);
            when(paymentRepository.save(any(Payment.class))).thenReturn(saved);

            PaymentResponse response = paymentService.initiatePayment(req);

            assertThat(response).isNotNull();
            assertThat(response.getPaymentId()).isEqualTo("pay-1");
            assertThat(response.getRazorpayOrderId()).isEqualTo("order_abc123");
            assertThat(response.getAmount()).isEqualTo(6450.0);
            assertThat(response.getStatus()).isEqualTo("PENDING");
            assertThat(response.getRazorpayKeyId()).isEqualTo(TEST_KEY_ID);
            verify(paymentRepository).save(any(Payment.class));
        }

        @Test
        @DisplayName("should throw InvalidPaymentAmountException when amount is null")
        void initiatePayment_nullAmount() {
            PaymentRequest req = buildRequest(null);

            assertThatThrownBy(() -> paymentService.initiatePayment(req))
                    .isInstanceOf(CustomExceptions.InvalidPaymentAmountException.class)
                    .hasMessageContaining("greater than zero");

            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw InvalidPaymentAmountException when amount is zero")
        void initiatePayment_zeroAmount() {
            PaymentRequest req = buildRequest(0.0);

            assertThatThrownBy(() -> paymentService.initiatePayment(req))
                    .isInstanceOf(CustomExceptions.InvalidPaymentAmountException.class);
        }

        @Test
        @DisplayName("should throw InvalidPaymentAmountException when amount is negative")
        void initiatePayment_negativeAmount() {
            PaymentRequest req = buildRequest(-100.0);

            assertThatThrownBy(() -> paymentService.initiatePayment(req))
                    .isInstanceOf(CustomExceptions.InvalidPaymentAmountException.class);
        }

        @Test
        @DisplayName("should throw RazorpayException when Razorpay order creation fails")
        void initiatePayment_razorpayFails() throws Exception {
            PaymentRequest req = buildRequest(5000.0);
            when(razorpayClient.orders.create(any())).thenThrow(new RazorpayException("Connection refused"));

            assertThatThrownBy(() -> paymentService.initiatePayment(req))
                    .isInstanceOf(CustomExceptions.RazorpayException.class)
                    .hasMessageContaining("Razorpay order creation failed");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  processPayment()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("processPayment()")
    class ProcessPayment {

        @Test
        @DisplayName("should mark payment PAID, confirm booking, and send notification on valid signature")
        void processPayment_success() throws Exception {
            String orderId   = "order_abc123";
            String paymentId = "pay_xyz789";
            String signature = computeValidSignature(orderId, paymentId);

            Payment payment = buildPayment("pay-1", Payment.PaymentStatus.PENDING);
            when(paymentRepository.findByRazorpayOrderId(orderId)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
            when(bookingClient.confirmBooking(any(), any())).thenReturn(new BookingDTO());
            doNothing().when(notificationClient).sendPaymentSuccessNotification(any());

            PaymentDTO result = paymentService.processPayment(orderId, paymentId, signature);

            assertThat(result.getStatus()).isEqualTo("PAID");
            assertThat(payment.getTransactionId()).isEqualTo(paymentId);
            assertThat(payment.getPaidAt()).isNotNull();
            verify(bookingClient).confirmBooking("bk-1", "pay-1");
            verify(notificationClient).sendPaymentSuccessNotification(any(PaymentNotificationRequest.class));
        }

        @Test
        @DisplayName("should throw InvalidPaymentSignatureException on tampered signature")
        void processPayment_invalidSignature() {
            assertThatThrownBy(() -> paymentService.processPayment(
                    "order_abc", "pay_xyz", "bad_signature"))
                    .isInstanceOf(CustomExceptions.InvalidPaymentSignatureException.class)
                    .hasMessageContaining("Invalid payment signature");

            verify(paymentRepository, never()).findByRazorpayOrderId(any());
        }

        @Test
        @DisplayName("should throw PaymentNotFoundException when order ID not found")
        void processPayment_orderNotFound() throws Exception {
            String orderId   = "order_unknown";
            String paymentId = "pay_xyz";
            String signature = computeValidSignature(orderId, paymentId);

            when(paymentRepository.findByRazorpayOrderId(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.processPayment(orderId, paymentId, signature))
                    .isInstanceOf(CustomExceptions.PaymentNotFoundException.class)
                    .hasMessageContaining(orderId);
        }

        @Test
        @DisplayName("should throw PaymentAlreadyProcessedException when payment is already PAID")
        void processPayment_alreadyPaid() throws Exception {
            String orderId   = "order_abc123";
            String paymentId = "pay_xyz789";
            String signature = computeValidSignature(orderId, paymentId);

            Payment payment = buildPayment("pay-1", Payment.PaymentStatus.PAID);
            when(paymentRepository.findByRazorpayOrderId(orderId)).thenReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.processPayment(orderId, paymentId, signature))
                    .isInstanceOf(CustomExceptions.PaymentAlreadyProcessedException.class)
                    .hasMessageContaining("already processed");

            verify(paymentRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  getPaymentById()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getPaymentById()")
    class GetPaymentById {

        @Test
        @DisplayName("should return PaymentDTO when payment exists")
        void getPaymentById_found() {
            Payment payment = buildPayment("pay-1", Payment.PaymentStatus.PAID);
            when(paymentRepository.findById("pay-1")).thenReturn(Optional.of(payment));

            PaymentDTO result = paymentService.getPaymentById("pay-1");

            assertThat(result.getPaymentId()).isEqualTo("pay-1");
            assertThat(result.getStatus()).isEqualTo("PAID");
            assertThat(result.getAmount()).isEqualTo(6450.0);
        }

        @Test
        @DisplayName("should throw PaymentNotFoundException when payment not found")
        void getPaymentById_notFound() {
            when(paymentRepository.findById("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.getPaymentById("bad"))
                    .isInstanceOf(CustomExceptions.PaymentNotFoundException.class)
                    .hasMessageContaining("bad");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  getPaymentByBooking()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getPaymentByBooking()")
    class GetPaymentByBooking {

        @Test
        @DisplayName("should return PaymentDTO for given booking")
        void getPaymentByBooking_found() {
            Payment payment = buildPayment("pay-1", Payment.PaymentStatus.PAID);
            when(paymentRepository.findByBookingId("bk-1")).thenReturn(Optional.of(payment));

            PaymentDTO result = paymentService.getPaymentByBooking("bk-1");

            assertThat(result.getBookingId()).isEqualTo("bk-1");
        }

        @Test
        @DisplayName("should throw PaymentNotFoundException when no payment for booking")
        void getPaymentByBooking_notFound() {
            when(paymentRepository.findByBookingId("bk-99")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.getPaymentByBooking("bk-99"))
                    .isInstanceOf(CustomExceptions.PaymentNotFoundException.class)
                    .hasMessageContaining("bk-99");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  getPaymentsByUser()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getPaymentsByUser()")
    class GetPaymentsByUser {

        @Test
        @DisplayName("should return all payments for a user")
        void getPaymentsByUser_returnsList() {
            List<Payment> payments = List.of(
                    buildPayment("pay-1", Payment.PaymentStatus.PAID),
                    buildPayment("pay-2", Payment.PaymentStatus.REFUNDED)
            );
            when(paymentRepository.findByUserId("user-1")).thenReturn(payments);

            List<PaymentDTO> result = paymentService.getPaymentsByUser("user-1");

            assertThat(result).hasSize(2);
            assertThat(result).extracting(PaymentDTO::getUserId).containsOnly("user-1");
        }

        @Test
        @DisplayName("should return empty list when user has no payments")
        void getPaymentsByUser_empty() {
            when(paymentRepository.findByUserId("user-99")).thenReturn(List.of());

            assertThat(paymentService.getPaymentsByUser("user-99")).isEmpty();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  refundPayment()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("refundPayment()")
    class RefundPayment {

        @Test
        @DisplayName("should fully refund a PAID payment and update booking to CANCELLED")
        void refundPayment_fullRefund() {
            Payment payment = buildPayment("pay-1", Payment.PaymentStatus.PAID);
            when(paymentRepository.findById("pay-1")).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
            when(bookingClient.updateStatus(any(), any())).thenReturn(new BookingDTO());

            PaymentDTO result = paymentService.refundPayment("pay-1", 6450.0);

            assertThat(result.getStatus()).isEqualTo("REFUNDED");
            assertThat(payment.getRefundAmount()).isEqualTo(6450.0);
            assertThat(payment.getRefundedAt()).isNotNull();
            verify(bookingClient).updateStatus("bk-1", "CANCELLED");
        }

        @Test
        @DisplayName("should set PARTIALLY_REFUNDED when refund amount is less than total")
        void refundPayment_partialRefund() {
            Payment payment = buildPayment("pay-1", Payment.PaymentStatus.PAID);
            when(paymentRepository.findById("pay-1")).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any())).thenReturn(payment);
            when(bookingClient.updateStatus(any(), any())).thenReturn(new BookingDTO());

            PaymentDTO result = paymentService.refundPayment("pay-1", 3000.0);

            assertThat(result.getStatus()).isEqualTo("PARTIALLY_REFUNDED");
            assertThat(payment.getRefundAmount()).isEqualTo(3000.0);
        }

        @Test
        @DisplayName("should throw RefundNotAllowedException when payment is not PAID")
        void refundPayment_notPaid() {
            Payment payment = buildPayment("pay-1", Payment.PaymentStatus.PENDING);
            when(paymentRepository.findById("pay-1")).thenReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.refundPayment("pay-1", 5000.0))
                    .isInstanceOf(CustomExceptions.RefundNotAllowedException.class)
                    .hasMessageContaining("PENDING");

            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw InvalidPaymentAmountException when refund exceeds payment")
        void refundPayment_amountExceedsPayment() {
            Payment payment = buildPayment("pay-1", Payment.PaymentStatus.PAID);
            when(paymentRepository.findById("pay-1")).thenReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.refundPayment("pay-1", 99999.0))
                    .isInstanceOf(CustomExceptions.InvalidPaymentAmountException.class)
                    .hasMessageContaining("cannot exceed");

            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw PaymentNotFoundException when payment not found")
        void refundPayment_notFound() {
            when(paymentRepository.findById("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.refundPayment("bad", 100.0))
                    .isInstanceOf(CustomExceptions.PaymentNotFoundException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  verifySignature()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("verifySignature()")
    class VerifySignature {

        @Test
        @DisplayName("should return true for a valid HMAC-SHA256 signature")
        void verifySignature_valid() throws Exception {
            String orderId   = "order_test123";
            String paymentId = "pay_test456";
            String signature = computeValidSignature(orderId, paymentId);

            assertThat(paymentService.verifySignature(orderId, paymentId, signature)).isTrue();
        }

        @Test
        @DisplayName("should return false for a tampered signature")
        void verifySignature_tampered() {
            assertThat(paymentService.verifySignature("order_abc", "pay_xyz", "wrong_sig")).isFalse();
        }

        @Test
        @DisplayName("should return false for empty signature")
        void verifySignature_empty() {
            assertThat(paymentService.verifySignature("order_abc", "pay_xyz", "")).isFalse();
        }

        @Test
        @DisplayName("should return false when orderId and paymentId are swapped")
        void verifySignature_swappedParams() throws Exception {
            String orderId   = "order_abc";
            String paymentId = "pay_xyz";
            // Compute signature with correct order, then pass swapped
            String signature = computeValidSignature(orderId, paymentId);

            assertThat(paymentService.verifySignature(paymentId, orderId, signature)).isFalse();
        }
    }
}