package com.airnexus.notification_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private String notificationId;
    private String recipientId;
    private String type;
    private String title;
    private String message;
    private String channel;
    private String relatedBookingId;
    private Boolean isRead;
    private LocalDateTime sentAt;
    private String recipientEmail;
    private String recipientPhone;
}
