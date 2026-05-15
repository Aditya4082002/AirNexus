package com.airnexus.payment_service.client;

public class BookingDTO {
    private String bookingId;
    private String status;

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
