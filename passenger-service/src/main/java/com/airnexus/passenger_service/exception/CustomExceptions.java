package com.airnexus.passenger_service.exception;

public class CustomExceptions {

    public static class PassengerNotFoundException extends RuntimeException {
        public PassengerNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidPassportException extends RuntimeException {
        public InvalidPassportException(String message) {
            super(message);
        }
    }

    public static class PassportExpiredException extends RuntimeException {
        public PassportExpiredException(String message) {
            super(message);
        }
    }

    public static class InvalidPassengerAgeException extends RuntimeException {
        public InvalidPassengerAgeException(String message) {
            super(message);
        }
    }

    public static class DuplicateTicketNumberException extends RuntimeException {
        public DuplicateTicketNumberException(String message) {
            super(message);
        }
    }

    public static class CheckInNotAllowedException extends RuntimeException {
        public CheckInNotAllowedException(String message) {
            super(message);
        }
    }
}