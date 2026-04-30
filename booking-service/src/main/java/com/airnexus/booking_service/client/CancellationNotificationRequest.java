package com.airnexus.booking_service.client;

public class CancellationNotificationRequest {
    private String userId;
    private String email;
    private String bookingId;
    private String pnr;

    public CancellationNotificationRequest() {}

    public CancellationNotificationRequest(String userId, String email, String bookingId, String pnr) {
        this.userId = userId;
        this.email = email;
        this.bookingId = bookingId;
        this.pnr = pnr;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public String getPnr() { return pnr; }
    public void setPnr(String pnr) { this.pnr = pnr; }
}
