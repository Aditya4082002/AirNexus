package com.airnexus.notification_service.service;

import com.airnexus.notification_service.dto.NotificationDTO;
import com.airnexus.notification_service.entity.Notification;
import com.airnexus.notification_service.exception.CustomExceptions;
import com.airnexus.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    @Override
    @Transactional
    public NotificationDTO sendNotification(NotificationDTO dto) {
        if (dto.getRecipientId() == null || dto.getRecipientId().isEmpty()) {
            throw new CustomExceptions.InvalidRecipientException(
                    "Recipient ID is required"
            );
        }

        Notification notification = new Notification();
        notification.setRecipientId(dto.getRecipientId());
        notification.setType(Notification.NotificationType.valueOf(dto.getType()));
        notification.setTitle(dto.getTitle());
        notification.setMessage(dto.getMessage());
        notification.setChannel(Notification.NotificationChannel.valueOf(dto.getChannel()));
        notification.setRelatedBookingId(dto.getRelatedBookingId());
        notification.setRecipientEmail(dto.getRecipientEmail());
        notification.setRecipientPhone(dto.getRecipientPhone());

        notification = notificationRepository.save(notification);

        // Send based on channel
        Notification.NotificationChannel channel = notification.getChannel();
        if (channel == Notification.NotificationChannel.EMAIL || channel == Notification.NotificationChannel.ALL) {
            if (dto.getRecipientEmail() != null && !dto.getRecipientEmail().isEmpty()) {
                emailService.sendEmail(dto.getRecipientEmail(), dto.getTitle(), dto.getMessage());
            } else if (channel == Notification.NotificationChannel.EMAIL) {
                throw new CustomExceptions.InvalidRecipientException(
                        "Email address is required for EMAIL channel"
                );
            }
        }

        if (channel == Notification.NotificationChannel.SMS || channel == Notification.NotificationChannel.ALL) {
            if (dto.getRecipientPhone() != null && !dto.getRecipientPhone().isEmpty()) {
                smsService.sendSms(dto.getRecipientPhone(), dto.getMessage());
            } else if (channel == Notification.NotificationChannel.SMS) {
                throw new CustomExceptions.InvalidRecipientException(
                        "Phone number is required for SMS channel"
                );
            }
        }

        return mapToDTO(notification);
    }

    @Override
    public void sendBookingConfirmation(String userId, String email, String phone, String bookingId, String pnr, String flightDetails) {
        // In-app notification
        Notification notification = new Notification();
        notification.setRecipientId(userId);
        notification.setType(Notification.NotificationType.BOOKING_CONFIRMED);
        notification.setTitle("Booking Confirmed");
        notification.setMessage("Your booking " + pnr + " has been confirmed!");
        notification.setChannel(Notification.NotificationChannel.ALL);
        notification.setRelatedBookingId(bookingId);
        notification.setRecipientEmail(email);
        notification.setRecipientPhone(phone);
        notificationRepository.save(notification);

        // Email
        if (email != null && !email.isEmpty()) {
            System.out.println("📧 Sending email to: " + email);//debug
            emailService.sendBookingConfirmation(email, pnr, flightDetails);
        }

        // SMS
        if (phone != null && !phone.isEmpty()) {
            System.out.println("📱 Sending SMS to: " + phone);//debugs
            smsService.sendBookingConfirmationSms(phone, pnr);
        }
    }

    @Override
    public void sendPaymentSuccess(String userId, String email, String bookingId, Double amount) {
        Notification notification = new Notification();
        notification.setRecipientId(userId);
        notification.setType(Notification.NotificationType.PAYMENT_SUCCESS);
        notification.setTitle("Payment Successful");
        notification.setMessage("Payment of ₹" + amount + " received successfully!");
        notification.setChannel(Notification.NotificationChannel.ALL);
        notification.setRelatedBookingId(bookingId);
        notification.setRecipientEmail(email);
        notificationRepository.save(notification);

        if (email != null && !email.isEmpty()) {
            emailService.sendEmail(email, "Payment Successful",
                    "Your payment of ₹" + amount + " has been processed successfully.");
        }
    }

    @Override
    public void sendCheckInReminder(String userId, String email, String phone, String bookingId, String pnr, String flightNumber) {
        Notification notification = new Notification();
        notification.setRecipientId(userId);
        notification.setType(Notification.NotificationType.CHECKIN_REMINDER);
        notification.setTitle("Check-in Reminder");
        notification.setMessage("Your flight " + flightNumber + " departs in 24 hours. Check-in now!");
        notification.setChannel(Notification.NotificationChannel.ALL);
        notification.setRelatedBookingId(bookingId);
        notification.setRecipientEmail(email);
        notification.setRecipientPhone(phone);
        notificationRepository.save(notification);

        if (email != null && !email.isEmpty()) {
            emailService.sendCheckInReminder(email, pnr, flightNumber);
        }
        if (phone != null && !phone.isEmpty()) {
            smsService.sendCheckInReminderSms(phone, pnr);
        }
    }

    @Override
    public void sendFlightDelay(String userId, String email, String bookingId, String flightNumber, String newTime) {
        Notification notification = new Notification();
        notification.setRecipientId(userId);
        notification.setType(Notification.NotificationType.FLIGHT_DELAY);
        notification.setTitle("Flight Delayed");
        notification.setMessage("Flight " + flightNumber + " is delayed. New departure: " + newTime);
        notification.setChannel(Notification.NotificationChannel.ALL);
        notification.setRelatedBookingId(bookingId);
        notification.setRecipientEmail(email);
        notificationRepository.save(notification);

        if (email != null && !email.isEmpty()) {
            emailService.sendEmail(email, "Flight Delay Alert",
                    "Your flight " + flightNumber + " has been delayed. New departure time: " + newTime);
        }
    }

    @Override
    public void sendCancellationConfirmation(String userId, String email, String bookingId, String pnr) {
        Notification notification = new Notification();
        notification.setRecipientId(userId);
        notification.setType(Notification.NotificationType.CANCELLATION);
        notification.setTitle("Booking Cancelled");
        notification.setMessage("Your booking " + pnr + " has been cancelled. Refund will be processed in 5-7 days.");
        notification.setChannel(Notification.NotificationChannel.ALL);
        notification.setRelatedBookingId(bookingId);
        notification.setRecipientEmail(email);
        notificationRepository.save(notification);

        if (email != null && !email.isEmpty()) {
            emailService.sendEmail(email, "Booking Cancelled",
                    "Your booking " + pnr + " has been cancelled. Refund will be processed within 5-7 working days.");
        }
    }

    @Override
    public List<NotificationDTO> getNotificationsByUser(String userId) {
        return notificationRepository.findByRecipientId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getUnreadNotifications(String userId) {
        return notificationRepository.findByRecipientIdAndIsRead(userId, false).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Integer getUnreadCount(String userId) {
        return notificationRepository.countByRecipientIdAndIsRead(userId, false);
    }

    @Override
    @Transactional
    public NotificationDTO markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomExceptions.NotificationNotFoundException(
                        "Notification not found with ID: " + notificationId
                ));

        notification.setIsRead(true);
        notification = notificationRepository.save(notification);
        return mapToDTO(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String userId) {
        List<Notification> notifications = notificationRepository.findByRecipientIdAndIsRead(userId, false);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Override
    @Transactional
    public void deleteNotification(String notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new CustomExceptions.NotificationNotFoundException(
                    "Notification not found with ID: " + notificationId
            );
        }
        notificationRepository.deleteById(notificationId);
    }

    private NotificationDTO mapToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setNotificationId(notification.getNotificationId());
        dto.setRecipientId(notification.getRecipientId());
        dto.setType(notification.getType().name());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setChannel(notification.getChannel().name());
        dto.setRelatedBookingId(notification.getRelatedBookingId());
        dto.setIsRead(notification.getIsRead());
        dto.setSentAt(notification.getSentAt());
        dto.setRecipientEmail(notification.getRecipientEmail());
        dto.setRecipientPhone(notification.getRecipientPhone());
        return dto;
    }
}