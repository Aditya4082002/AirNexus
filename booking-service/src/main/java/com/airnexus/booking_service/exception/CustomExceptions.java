package com.airnexus.booking_service.exception;

public class CustomExceptions {

    public static class BookingNotFoundException extends RuntimeException {
        public BookingNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidPnrException extends RuntimeException {
        public InvalidPnrException(String message) {
            super(message);
        }
    }

    public static class BookingAlreadyCancelledException extends RuntimeException {
        public BookingAlreadyCancelledException(String message) {
            super(message);
        }
    }

    public static class BookingAlreadyConfirmedException extends RuntimeException {
        public BookingAlreadyConfirmedException(String message) {
            super(message);
        }
    }

    public static class InvalidBookingStatusException extends RuntimeException {
        public InvalidBookingStatusException(String message) {
            super(message);
        }
    }

    public static class FareCalculationException extends RuntimeException {
        public FareCalculationException(String message) {
            super(message);
        }
    }

    public static class SeatAllocationException extends RuntimeException {
        public SeatAllocationException(String message) {
            super(message);
        }
    }
}