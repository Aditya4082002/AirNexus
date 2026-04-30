package com.airnexus.payment_service.exception;

public class CustomExceptions {

    public static class PaymentNotFoundException extends RuntimeException {
        public PaymentNotFoundException(String message) {
            super(message);
        }
    }

    public static class PaymentInitiationException extends RuntimeException {
        public PaymentInitiationException(String message) {
            super(message);
        }
    }

    public static class InvalidPaymentSignatureException extends RuntimeException {
        public InvalidPaymentSignatureException(String message) {
            super(message);
        }
    }

    public static class PaymentAlreadyProcessedException extends RuntimeException {
        public PaymentAlreadyProcessedException(String message) {
            super(message);
        }
    }

    public static class RefundNotAllowedException extends RuntimeException {
        public RefundNotAllowedException(String message) {
            super(message);
        }
    }

    public static class RazorpayException extends RuntimeException {
        public RazorpayException(String message) {
            super(message);
        }
    }

    public static class InvalidPaymentAmountException extends RuntimeException {
        public InvalidPaymentAmountException(String message) {
            super(message);
        }
    }
}