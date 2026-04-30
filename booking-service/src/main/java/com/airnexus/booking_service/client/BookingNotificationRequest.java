package com.airnexus.booking_service.client;

public class BookingNotificationRequest {
    private String userId;
    private String email;
    private String phone;
    private String bookingId;
    private String pnr;
    private String flightDetails;

    public BookingNotificationRequest() {}

    public BookingNotificationRequest(String userId, String email, String phone,
                                      String bookingId, String pnr, String flightDetails) {
        this.userId = userId;
        this.email = email;
        this.phone = phone;
        this.bookingId = bookingId;
        this.pnr = pnr;
        this.flightDetails = flightDetails;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public String getPnr() { return pnr; }
    public void setPnr(String pnr) { this.pnr = pnr; }
    public String getFlightDetails() { return flightDetails; }
    public void setFlightDetails(String flightDetails) { this.flightDetails = flightDetails; }
}

