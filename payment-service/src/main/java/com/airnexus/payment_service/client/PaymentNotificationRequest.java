package com.airnexus.payment_service.client;

public class PaymentNotificationRequest {
    private String userId;
    private String email;
    private String bookingId;
    private Double amount;

    public PaymentNotificationRequest() {}

    public PaymentNotificationRequest(String userId, String email, String bookingId, Double amount) {
        this.userId = userId;
        this.email = email;
        this.bookingId = bookingId;
        this.amount = amount;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}