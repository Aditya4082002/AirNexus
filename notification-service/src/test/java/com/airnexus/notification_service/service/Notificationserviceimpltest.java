package com.airnexus.notification_service.service;

import com.airnexus.notification_service.dto.NotificationDTO;
import com.airnexus.notification_service.entity.Notification;
import com.airnexus.notification_service.exception.CustomExceptions;
import com.airnexus.notification_service.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationServiceImpl Tests")
class NotificationServiceImplTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private EmailService emailService;
    @Mock private SmsService smsService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    // ─── Helpers ────────────────────────────────────────────────────────────

    private Notification buildNotification(String id, String userId, boolean isRead) {
        Notification n = new Notification();
        n.setNotificationId(id);
        n.setRecipientId(userId);
        n.setType(Notification.NotificationType.BOOKING_CONFIRMED);
        n.setTitle("Test Title");
        n.setMessage("Test message body");
        n.setChannel(Notification.NotificationChannel.APP);
        n.setRelatedBookingId("bk-1");
        n.setIsRead(isRead);
        n.setSentAt(LocalDateTime.now());
        n.setRecipientEmail("user@test.com");
        n.setRecipientPhone("+91-9999999999");
        return n;
    }

    private NotificationDTO buildDTO(String recipientId, String type, String channel) {
        NotificationDTO dto = new NotificationDTO();
        dto.setRecipientId(recipientId);
        dto.setType(type);
        dto.setTitle("Test Title");
        dto.setMessage("Test message body");
        dto.setChannel(channel);
        dto.setRelatedBookingId("bk-1");
        dto.setRecipientEmail("user@test.com");
        dto.setRecipientPhone("+91-9999999999");
        return dto;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  sendNotification()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("sendNotification()")
    class SendNotification {

        @Test
        @DisplayName("should save notification and return DTO for APP channel")
        void sendNotification_appChannel_success() {
            NotificationDTO dto = buildDTO("user-1", "BOOKING_CONFIRMED", "APP");
            Notification saved = buildNotification("n-1", "user-1", false);

            when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

            NotificationDTO result = notificationService.sendNotification(dto);

            assertThat(result.getNotificationId()).isEqualTo("n-1");
            assertThat(result.getRecipientId()).isEqualTo("user-1");
            verify(notificationRepository).save(any(Notification.class));
            verifyNoInteractions(emailService, smsService);
        }

        @Test
        @DisplayName("should send email when channel is EMAIL")
        void sendNotification_emailChannel_sendsEmail() {
            NotificationDTO dto = buildDTO("user-1", "BOOKING_CONFIRMED", "EMAIL");
            Notification saved = buildNotification("n-1", "user-1", false);
            saved.setChannel(Notification.NotificationChannel.EMAIL);

            when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

            notificationService.sendNotification(dto);

            verify(emailService).sendEmail("user@test.com", "Test Title", "Test message body");
            verifyNoInteractions(smsService);
        }

        @Test
        @DisplayName("should send SMS when channel is SMS")
        void sendNotification_smsChannel_sendsSms() {
            NotificationDTO dto = buildDTO("user-1", "BOOKING_CONFIRMED", "SMS");
            Notification saved = buildNotification("n-1", "user-1", false);
            saved.setChannel(Notification.NotificationChannel.SMS);

            when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

            notificationService.sendNotification(dto);

            verify(smsService).sendSms("+91-9999999999", "Test message body");
            verifyNoInteractions(emailService);
        }

        @Test
        @DisplayName("should send both email and SMS when channel is ALL")
        void sendNotification_allChannel_sendsEmailAndSms() {
            NotificationDTO dto = buildDTO("user-1", "FLIGHT_DELAY", "ALL");
            Notification saved = buildNotification("n-1", "user-1", false);
            saved.setChannel(Notification.NotificationChannel.ALL);

            when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

            notificationService.sendNotification(dto);

            verify(emailService).sendEmail("user@test.com", "Test Title", "Test message body");
            verify(smsService).sendSms("+91-9999999999", "Test message body");
        }

        @Test
        @DisplayName("should throw InvalidRecipientException when recipientId is null")
        void sendNotification_nullRecipient_throws() {
            NotificationDTO dto = buildDTO(null, "BOOKING_CONFIRMED", "APP");

            assertThatThrownBy(() -> notificationService.sendNotification(dto))
                    .isInstanceOf(CustomExceptions.InvalidRecipientException.class)
                    .hasMessageContaining("Recipient ID is required");

            verify(notificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw InvalidRecipientException when recipientId is blank")
        void sendNotification_blankRecipient_throws() {
            NotificationDTO dto = buildDTO("", "BOOKING_CONFIRMED", "APP");

            assertThatThrownBy(() -> notificationService.sendNotification(dto))
                    .isInstanceOf(CustomExceptions.InvalidRecipientException.class);

            verify(notificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw InvalidRecipientException when EMAIL channel has no email")
        void sendNotification_emailChannelMissingEmail_throws() {
            NotificationDTO dto = buildDTO("user-1", "BOOKING_CONFIRMED", "EMAIL");
            dto.setRecipientEmail(null); // no email provided

            Notification saved = buildNotification("n-1", "user-1", false);
            saved.setChannel(Notification.NotificationChannel.EMAIL);
            saved.setRecipientEmail(null);

            when(notificationRepository.save(any())).thenReturn(saved);

            assertThatThrownBy(() -> notificationService.sendNotification(dto))
                    .isInstanceOf(CustomExceptions.InvalidRecipientException.class)
                    .hasMessageContaining("Email address is required");
        }

        @Test
        @DisplayName("should throw InvalidRecipientException when SMS channel has no phone")
        void sendNotification_smsChannelMissingPhone_throws() {
            NotificationDTO dto = buildDTO("user-1", "BOOKING_CONFIRMED", "SMS");
            dto.setRecipientPhone(null); // no phone provided

            Notification saved = buildNotification("n-1", "user-1", false);
            saved.setChannel(Notification.NotificationChannel.SMS);
            saved.setRecipientPhone(null);

            when(notificationRepository.save(any())).thenReturn(saved);

            assertThatThrownBy(() -> notificationService.sendNotification(dto))
                    .isInstanceOf(CustomExceptions.InvalidRecipientException.class)
                    .hasMessageContaining("Phone number is required");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  sendBookingConfirmation()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("sendBookingConfirmation()")
    class SendBookingConfirmation {

        @Test
        @DisplayName("should save APP notification, send email and SMS when both provided")
        void sendBookingConfirmation_allChannels() {
            Notification saved = buildNotification("n-1", "user-1", false);
            when(notificationRepository.save(any())).thenReturn(saved);

            notificationService.sendBookingConfirmation(
                    "user-1", "user@test.com", "+91-9999999999", "bk-1", "123456", "AI-202 DEL→BOM"
            );

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            Notification persisted = captor.getValue();
            assertThat(persisted.getType()).isEqualTo(Notification.NotificationType.BOOKING_CONFIRMED);
            assertThat(persisted.getChannel()).isEqualTo(Notification.NotificationChannel.APP);
            assertThat(persisted.getMessage()).contains("123456");

            verify(emailService).sendBookingConfirmation("user@test.com", "123456", "AI-202 DEL→BOM");
            verify(smsService).sendBookingConfirmationSms("+91-9999999999", "123456");
        }

        @Test
        @DisplayName("should NOT send email when email is null")
        void sendBookingConfirmation_noEmail() {
            when(notificationRepository.save(any())).thenReturn(buildNotification("n-1", "user-1", false));

            notificationService.sendBookingConfirmation(
                    "user-1", null, "+91-9999999999", "bk-1", "123456", "flight details"
            );

            verifyNoInteractions(emailService);
            verify(smsService).sendBookingConfirmationSms(any(), any());
        }

        @Test
        @DisplayName("should NOT send SMS when phone is null")
        void sendBookingConfirmation_noPhone() {
            when(notificationRepository.save(any())).thenReturn(buildNotification("n-1", "user-1", false));

            notificationService.sendBookingConfirmation(
                    "user-1", "user@test.com", null, "bk-1", "123456", "flight details"
            );

            verify(emailService).sendBookingConfirmation(any(), any(), any());
            verifyNoInteractions(smsService);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  sendPaymentSuccess()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("sendPaymentSuccess()")
    class SendPaymentSuccess {

        @Test
        @DisplayName("should save PAYMENT_SUCCESS notification and send email")
        void sendPaymentSuccess_withEmail() {
            when(notificationRepository.save(any())).thenReturn(buildNotification("n-1", "user-1", false));

            notificationService.sendPaymentSuccess("user-1", "user@test.com", "bk-1", 6450.0);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            assertThat(captor.getValue().getType()).isEqualTo(Notification.NotificationType.PAYMENT_SUCCESS);
            assertThat(captor.getValue().getMessage()).contains("6450.0");

            verify(emailService).sendEmail(eq("user@test.com"), eq("Payment Successful"), contains("6450.0"));
        }

        @Test
        @DisplayName("should NOT send email when email is null")
        void sendPaymentSuccess_noEmail() {
            when(notificationRepository.save(any())).thenReturn(buildNotification("n-1", "user-1", false));

            notificationService.sendPaymentSuccess("user-1", null, "bk-1", 6450.0);

            verifyNoInteractions(emailService);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  sendCheckInReminder()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("sendCheckInReminder()")
    class SendCheckInReminder {

        @Test
        @DisplayName("should save CHECKIN_REMINDER and send email and SMS")
        void sendCheckInReminder_allChannels() {
            when(notificationRepository.save(any())).thenReturn(buildNotification("n-1", "user-1", false));

            notificationService.sendCheckInReminder(
                    "user-1", "user@test.com", "+91-9999999999", "bk-1", "123456", "AI-202"
            );

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            assertThat(captor.getValue().getType()).isEqualTo(Notification.NotificationType.CHECKIN_REMINDER);
            assertThat(captor.getValue().getMessage()).contains("AI-202");

            verify(emailService).sendCheckInReminder("user@test.com", "123456", "AI-202");
            verify(smsService).sendCheckInReminderSms("+91-9999999999", "123456");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  sendFlightDelay()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("sendFlightDelay()")
    class SendFlightDelay {

        @Test
        @DisplayName("should save FLIGHT_DELAY notification and send email")
        void sendFlightDelay_withEmail() {
            when(notificationRepository.save(any())).thenReturn(buildNotification("n-1", "user-1", false));

            notificationService.sendFlightDelay("user-1", "user@test.com", "bk-1", "AI-202", "15:00");

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            Notification persisted = captor.getValue();
            assertThat(persisted.getType()).isEqualTo(Notification.NotificationType.FLIGHT_DELAY);
            assertThat(persisted.getChannel()).isEqualTo(Notification.NotificationChannel.ALL);
            assertThat(persisted.getMessage()).contains("AI-202").contains("15:00");

            verify(emailService).sendEmail(eq("user@test.com"), eq("Flight Delay Alert"), contains("AI-202"));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  sendCancellationConfirmation()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("sendCancellationConfirmation()")
    class SendCancellationConfirmation {

        @Test
        @DisplayName("should save CANCELLATION notification and send email")
        void sendCancellation_withEmail() {
            when(notificationRepository.save(any())).thenReturn(buildNotification("n-1", "user-1", false));

            notificationService.sendCancellationConfirmation("user-1", "user@test.com", "bk-1", "123456");

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            assertThat(captor.getValue().getType()).isEqualTo(Notification.NotificationType.CANCELLATION);
            assertThat(captor.getValue().getMessage()).contains("123456");

            verify(emailService).sendEmail(eq("user@test.com"), eq("Booking Cancelled"), contains("123456"));
        }

        @Test
        @DisplayName("should NOT send email when email is null")
        void sendCancellation_noEmail() {
            when(notificationRepository.save(any())).thenReturn(buildNotification("n-1", "user-1", false));

            notificationService.sendCancellationConfirmation("user-1", null, "bk-1", "123456");

            verifyNoInteractions(emailService);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  getNotificationsByUser()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getNotificationsByUser()")
    class GetNotificationsByUser {

        @Test
        @DisplayName("should return all notifications mapped to DTOs")
        void getNotificationsByUser_returnsList() {
            List<Notification> notifications = List.of(
                    buildNotification("n-1", "user-1", false),
                    buildNotification("n-2", "user-1", true)
            );
            when(notificationRepository.findByRecipientId("user-1")).thenReturn(notifications);

            List<NotificationDTO> result = notificationService.getNotificationsByUser("user-1");

            assertThat(result).hasSize(2);
            assertThat(result).extracting(NotificationDTO::getRecipientId).containsOnly("user-1");
        }

        @Test
        @DisplayName("should return empty list when user has no notifications")
        void getNotificationsByUser_empty() {
            when(notificationRepository.findByRecipientId("user-99")).thenReturn(List.of());

            assertThat(notificationService.getNotificationsByUser("user-99")).isEmpty();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  getUnreadNotifications()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getUnreadNotifications()")
    class GetUnreadNotifications {

        @Test
        @DisplayName("should return only unread notifications")
        void getUnreadNotifications_returnsUnread() {
            List<Notification> unread = List.of(buildNotification("n-1", "user-1", false));
            when(notificationRepository.findByRecipientIdAndIsRead("user-1", false)).thenReturn(unread);

            List<NotificationDTO> result = notificationService.getUnreadNotifications("user-1");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIsRead()).isFalse();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  getUnreadCount()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getUnreadCount()")
    class GetUnreadCount {

        @Test
        @DisplayName("should return correct unread count from repository")
        void getUnreadCount_returnsCount() {
            when(notificationRepository.countByRecipientIdAndIsRead("user-1", false)).thenReturn(5);

            assertThat(notificationService.getUnreadCount("user-1")).isEqualTo(5);
        }

        @Test
        @DisplayName("should return zero when no unread notifications")
        void getUnreadCount_zero() {
            when(notificationRepository.countByRecipientIdAndIsRead("user-1", false)).thenReturn(0);

            assertThat(notificationService.getUnreadCount("user-1")).isZero();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  markAsRead()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("markAsRead()")
    class MarkAsRead {

        @Test
        @DisplayName("should set isRead=true and return updated DTO")
        void markAsRead_success() {
            Notification notification = buildNotification("n-1", "user-1", false);
            when(notificationRepository.findById("n-1")).thenReturn(Optional.of(notification));
            when(notificationRepository.save(notification)).thenReturn(notification);

            NotificationDTO result = notificationService.markAsRead("n-1");

            assertThat(notification.getIsRead()).isTrue();
            assertThat(result.getIsRead()).isTrue();
            verify(notificationRepository).save(notification);
        }

        @Test
        @DisplayName("should throw NotificationNotFoundException when notification not found")
        void markAsRead_notFound() {
            when(notificationRepository.findById("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.markAsRead("bad"))
                    .isInstanceOf(CustomExceptions.NotificationNotFoundException.class)
                    .hasMessageContaining("bad");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  markAllAsRead()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("markAllAsRead()")
    class MarkAllAsRead {

        @Test
        @DisplayName("should set isRead=true on all unread notifications and saveAll")
        void markAllAsRead_success() {
            List<Notification> unread = List.of(
                    buildNotification("n-1", "user-1", false),
                    buildNotification("n-2", "user-1", false)
            );
            when(notificationRepository.findByRecipientIdAndIsRead("user-1", false)).thenReturn(unread);

            notificationService.markAllAsRead("user-1");

            assertThat(unread).allSatisfy(n -> assertThat(n.getIsRead()).isTrue());
            verify(notificationRepository).saveAll(unread);
        }

        @Test
        @DisplayName("should do nothing when there are no unread notifications")
        void markAllAsRead_noneUnread() {
            when(notificationRepository.findByRecipientIdAndIsRead("user-1", false)).thenReturn(List.of());

            notificationService.markAllAsRead("user-1");

            verify(notificationRepository).saveAll(List.of());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  deleteNotification()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteNotification()")
    class DeleteNotification {

        @Test
        @DisplayName("should delete notification when it exists")
        void deleteNotification_success() {
            when(notificationRepository.existsById("n-1")).thenReturn(true);

            notificationService.deleteNotification("n-1");

            verify(notificationRepository).deleteById("n-1");
        }

        @Test
        @DisplayName("should throw NotificationNotFoundException when notification not found")
        void deleteNotification_notFound() {
            when(notificationRepository.existsById("bad")).thenReturn(false);

            assertThatThrownBy(() -> notificationService.deleteNotification("bad"))
                    .isInstanceOf(CustomExceptions.NotificationNotFoundException.class)
                    .hasMessageContaining("bad");

            verify(notificationRepository, never()).deleteById(any());
        }
    }
}