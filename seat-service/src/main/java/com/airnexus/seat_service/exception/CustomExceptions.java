package com.airnexus.seat_service.exception;

public class CustomExceptions {

    public static class SeatNotFoundException extends RuntimeException {
        public SeatNotFoundException(String message) {
            super(message);
        }
    }

    public static class SeatAlreadyBookedException extends RuntimeException {
        public SeatAlreadyBookedException(String message) {
            super(message);
        }
    }

    public static class SeatNotAvailableException extends RuntimeException {
        public SeatNotAvailableException(String message) {
            super(message);
        }
    }

    public static class SeatHoldExpiredException extends RuntimeException {
        public SeatHoldExpiredException(String message) {
            super(message);
        }
    }

    public static class InvalidSeatStatusException extends RuntimeException {
        public InvalidSeatStatusException(String message) {
            super(message);
        }
    }

    public static class DuplicateSeatException extends RuntimeException {
        public DuplicateSeatException(String message) {
            super(message);
        }
    }
}