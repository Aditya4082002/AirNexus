package com.airnexus.payment_service.controller;

import com.airnexus.payment_service.dto.PaymentDTO;
import com.airnexus.payment_service.dto.PaymentRequest;
import com.airnexus.payment_service.dto.PaymentResponse;
import com.airnexus.payment_service.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs for initiating, verifying and refunding payments via Razorpay")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Initiate a Razorpay payment order",
            description = "Creates a Razorpay order for the given booking. Returns order ID and key to launch Razorpay checkout on the frontend.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payment request"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(@RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.initiatePayment(request));
    }

    @Operation(summary = "Verify Razorpay payment and confirm booking",
            description = "Verifies the Razorpay payment signature. On success, marks booking as confirmed and triggers notifications.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment verified and booking confirmed"),
            @ApiResponse(responseCode = "400", description = "Payment verification failed - invalid signature")
    })
    @PostMapping("/verify")
    public ResponseEntity<PaymentDTO> verifyPayment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Razorpay callback payload with order_id, payment_id and signature")
            @RequestBody Map<String, String> payload) {
        String orderId = payload.get("razorpay_order_id");
        String paymentId = payload.get("razorpay_payment_id");
        String signature = payload.get("razorpay_signature");
        return ResponseEntity.ok(paymentService.processPayment(orderId, paymentId, signature));
    }

    @Operation(summary = "Get payment by payment ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PaymentDTO> getPaymentById(
            @Parameter(description = "Payment ID") @PathVariable String id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @Operation(summary = "Get payment by booking ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "No payment for this booking")
    })
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentDTO> getPaymentByBooking(
            @Parameter(description = "Booking ID") @PathVariable String bookingId) {
        return ResponseEntity.ok(paymentService.getPaymentByBooking(bookingId));
    }

    @Operation(summary = "Get all payments by user ID")
    @ApiResponse(responseCode = "200", description = "List of payments for the user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByUser(
            @Parameter(description = "User ID") @PathVariable String userId) {
        return ResponseEntity.ok(paymentService.getPaymentsByUser(userId));
    }

    @Operation(summary = "Refund a payment",
            description = "Issues a partial or full refund via Razorpay for a completed payment.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refund initiated successfully"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "400", description = "Refund amount exceeds original payment")
    })
    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentDTO> refundPayment(
            @Parameter(description = "Payment ID") @PathVariable String id,
            @Parameter(description = "Amount to refund in INR") @RequestParam Double amount) {
        return ResponseEntity.ok(paymentService.refundPayment(id, amount));
    }
}
