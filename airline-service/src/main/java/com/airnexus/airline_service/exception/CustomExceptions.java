package com.airnexus.airline_service.exception;

public class CustomExceptions {

    public static class AirlineNotFoundException extends RuntimeException {
        public AirlineNotFoundException(String message) {
            super(message);
        }
    }

    public static class AirportNotFoundException extends RuntimeException {
        public AirportNotFoundException(String message) {
            super(message);
        }
    }

    public static class DuplicateIataCodeException extends RuntimeException {
        public DuplicateIataCodeException(String message) {
            super(message);
        }
    }

    public static class AirlineInactiveException extends RuntimeException {
        public AirlineInactiveException(String message) {
            super(message);
        }
    }
}