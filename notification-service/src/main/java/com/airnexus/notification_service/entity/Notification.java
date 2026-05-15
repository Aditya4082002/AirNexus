package com.airnexus.notification_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String notificationId;

    @Column(nullable = false)
    private String recipientId; // User ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    private String relatedBookingId;

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    private String recipientEmail;

    private String recipientPhone;

    public enum NotificationType {
        BOOKING_CONFIRMED,
        PAYMENT_SUCCESS,
        FLIGHT_DELAY,
        GATE_CHANGE,
        CHECKIN_REMINDER,
        BOARDING,
        CANCELLATION,
        REFUND_PROCESSED
    }

    public enum NotificationChannel {
        APP, EMAIL, SMS, ALL
    }
}