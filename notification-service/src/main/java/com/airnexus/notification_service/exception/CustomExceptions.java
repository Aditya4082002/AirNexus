package com.airnexus.notification_service.exception;

public class CustomExceptions {

    public static class NotificationNotFoundException extends RuntimeException {
        public NotificationNotFoundException(String message) {
            super(message);
        }
    }

    public static class EmailSendException extends RuntimeException {
        public EmailSendException(String message) {
            super(message);
        }
    }

    public static class SmsSendException extends RuntimeException {
        public SmsSendException(String message) {
            super(message);
        }
    }

    public static class InvalidNotificationChannelException extends RuntimeException {
        public InvalidNotificationChannelException(String message) {
            super(message);
        }
    }

    public static class InvalidRecipientException extends RuntimeException {
        public InvalidRecipientException(String message) {
            super(message);
        }
    }
}
