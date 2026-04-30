package com.airnexus.notification_service.service;

import com.airnexus.notification_service.dto.NotificationDTO;

import java.util.List;

public interface NotificationService {
    NotificationDTO sendNotification(NotificationDTO notificationDTO);
    void sendBookingConfirmation(String userId, String email, String phone, String bookingId, String pnr, String flightDetails);
    void sendPaymentSuccess(String userId, String email, String bookingId, Double amount);
    void sendCheckInReminder(String userId, String email, String phone, String bookingId, String pnr, String flightNumber);
    void sendFlightDelay(String userId, String email, String bookingId, String flightNumber, String newTime);
    void sendCancellationConfirmation(String userId, String email, String bookingId, String pnr);
    List<NotificationDTO> getNotificationsByUser(String userId);
    List<NotificationDTO> getUnreadNotifications(String userId);
    Integer getUnreadCount(String userId);
    NotificationDTO markAsRead(String notificationId);
    void markAllAsRead(String userId);
    void deleteNotification(String notificationId);
}