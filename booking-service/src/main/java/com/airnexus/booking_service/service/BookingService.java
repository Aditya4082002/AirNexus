package com.airnexus.booking_service.service;

import com.airnexus.booking_service.dto.BookingDTO;
import com.airnexus.booking_service.dto.BookingRequest;
import com.airnexus.booking_service.dto.FareSummary;
import com.airnexus.booking_service.entity.Booking;

import java.util.List;

public interface BookingService {
    BookingDTO createBooking(BookingRequest request);
    BookingDTO getBookingById(String id);
    BookingDTO getBookingByPnr(String pnrCode);
    List<BookingDTO> getBookingsByUser(String userId);
    List<BookingDTO> getUpcomingBookings(String userId);
    BookingDTO cancelBooking(String id);
    BookingDTO updateStatus(String id, Booking.BookingStatus status);
    BookingDTO confirmBooking(String id, String paymentId);
    FareSummary calculateFare(String flightId, Integer passengers, List<String> seatIds, Integer luggageKg);
    String generatePnr();
}
