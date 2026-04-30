package com.airnexus.notification_service.repository;


import com.airnexus.notification_service.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByRecipientId(String recipientId);
    List<Notification> findByRecipientIdAndIsRead(String recipientId, Boolean isRead);
    Integer countByRecipientIdAndIsRead(String recipientId, Boolean isRead);
    List<Notification> findByType(Notification.NotificationType type);
    List<Notification> findByRelatedBookingId(String bookingId);
}