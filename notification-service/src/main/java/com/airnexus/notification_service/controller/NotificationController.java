package com.airnexus.notification_service.controller;

import com.airnexus.notification_service.dto.NotificationDTO;
import com.airnexus.notification_service.service.NotificationService;
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
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "APIs for sending and managing notifications via email and SMS")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Send a generic notification")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid notification data")
    })
    @PostMapping
    public ResponseEntity<NotificationDTO> sendNotification(@RequestBody NotificationDTO notificationDTO) {
        return ResponseEntity.ok(notificationService.sendNotification(notificationDTO));
    }

    @Operation(summary = "Send payment success notification",
            description = "Called internally by payment-service after a successful payment. Sends email to the user.")
    @ApiResponse(responseCode = "200", description = "Payment success notification sent")
    @PostMapping("/payment-success")
    public ResponseEntity<String> sendPaymentSuccessNotification(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payload with userId, email, bookingId and amount")
            @RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        String email = (String) request.get("email");
        String bookingId = (String) request.get("bookingId");
        Double amount = ((Number) request.get("amount")).doubleValue();
        notificationService.sendPaymentSuccess(userId, email, bookingId, amount);
        return ResponseEntity.ok("Payment success notification sent");
    }

    @Operation(summary = "Send booking confirmation notification",
            description = "Called internally by booking-service after a booking is confirmed. Sends email and SMS with PNR.")
    @ApiResponse(responseCode = "200", description = "Booking confirmation sent")
    @PostMapping("/booking-confirmation")
    public ResponseEntity<String> sendBookingConfirmation(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payload with userId, email, phone, bookingId, pnr and flightDetails")
            @RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        String email = (String) request.get("email");
        String phone = (String) request.get("phone");
        String bookingId = (String) request.get("bookingId");
        String pnr = (String) request.get("pnr");
        String flightDetails = (String) request.get("flightDetails");
        notificationService.sendBookingConfirmation(userId, email, phone, bookingId, pnr, flightDetails);
        return ResponseEntity.ok("Booking confirmation sent");
    }

    @Operation(summary = "Send cancellation notification",
            description = "Called internally by booking-service after a booking is cancelled. Sends email with cancellation details.")
    @ApiResponse(responseCode = "200", description = "Cancellation notification sent")
    @PostMapping("/cancellation")
    public ResponseEntity<String> sendCancellationNotification(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payload with userId, email, bookingId and pnr")
            @RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        String email = (String) request.get("email");
        String bookingId = (String) request.get("bookingId");
        String pnr = (String) request.get("pnr");
        notificationService.sendCancellationConfirmation(userId, email, bookingId, pnr);
        return ResponseEntity.ok("Cancellation notification sent");
    }

    @Operation(summary = "Get all notifications for a user")
    @ApiResponse(responseCode = "200", description = "List of all notifications for the user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByUser(
            @Parameter(description = "User ID") @PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getNotificationsByUser(userId));
    }

    @Operation(summary = "Get unread notifications for a user")
    @ApiResponse(responseCode = "200", description = "List of unread notifications")
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(
            @Parameter(description = "User ID") @PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }

    @Operation(summary = "Get unread notification count for a user")
    @ApiResponse(responseCode = "200", description = "Count of unread notifications")
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Integer> getUnreadCount(
            @Parameter(description = "User ID") @PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @Operation(summary = "Mark a notification as read")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification marked as read"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationDTO> markAsRead(
            @Parameter(description = "Notification ID") @PathVariable String id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @Operation(summary = "Mark all notifications as read for a user")
    @ApiResponse(responseCode = "200", description = "All notifications marked as read")
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<String> markAllAsRead(
            @Parameter(description = "User ID") @PathVariable String userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("All notifications marked as read");
    }

    @Operation(summary = "Delete a notification by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification deleted"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotification(
            @Parameter(description = "Notification ID") @PathVariable String id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok("Notification deleted");
    }
}
