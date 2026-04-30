package com.airnexus.booking_service.repository;

import com.airnexus.booking_service.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    Optional<Booking> findByPnrCode(String pnrCode);
    List<Booking> findByUserId(String userId);
    List<Booking> findByFlightId(String flightId);
    List<Booking> findByStatus(Booking.BookingStatus status);
    List<Booking> findByUserIdAndStatus(String userId, Booking.BookingStatus status);
    Integer countByFlightIdAndStatus(String flightId, Booking.BookingStatus status);
    Boolean existsByPnrCode(String pnrCode);

    List<Booking> findByUserIdAndBookedAtAfter(String userId, LocalDateTime date);
}