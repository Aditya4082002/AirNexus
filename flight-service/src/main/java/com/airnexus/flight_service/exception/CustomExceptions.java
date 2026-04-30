package com.airnexus.flight_service.exception;

public class CustomExceptions {

    public static class FlightNotFoundException extends RuntimeException {
        public FlightNotFoundException(String message) {
            super(message);
        }
    }

    public static class DuplicateFlightNumberException extends RuntimeException {
        public DuplicateFlightNumberException(String message) {
            super(message);
        }
    }

    public static class InsufficientSeatsException extends RuntimeException {
        public InsufficientSeatsException(String message) {
            super(message);
        }
    }

    public static class InvalidFlightStatusException extends RuntimeException {
        public InvalidFlightStatusException(String message) {
            super(message);
        }
    }

    public static class FlightSearchException extends RuntimeException {
        public FlightSearchException(String message) {
            super(message);
        }
    }
}