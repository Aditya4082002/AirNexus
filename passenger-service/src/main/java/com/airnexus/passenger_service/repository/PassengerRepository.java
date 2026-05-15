package com.airnexus.passenger_service.repository;

import com.airnexus.passenger_service.entity.PassengerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PassengerRepository extends JpaRepository<PassengerInfo, String> {
    List<PassengerInfo> findByBookingId(String bookingId);
    Optional<PassengerInfo> findByTicketNumber(String ticketNumber);
    Optional<PassengerInfo> findByPassportNumber(String passportNumber);
    List<PassengerInfo> findBySeatId(String seatId);
    Integer countByBookingId(String bookingId);
    void deleteByBookingId(String bookingId);
    Boolean existsByTicketNumber(String ticketNumber);
}