package com.airnexus.passenger_service.service;


import com.airnexus.passenger_service.dto.PassengerDTO;

import java.util.List;

public interface PassengerService {
    PassengerDTO addPassenger(PassengerDTO passengerDTO);
    List<PassengerDTO> addPassengers(List<PassengerDTO> passengers);
    PassengerDTO getPassengerById(String id);
    List<PassengerDTO> getPassengersByPnr(String pnrCode);
    PassengerDTO getByTicketNumber(String ticketNumber);
    List<PassengerDTO> getPassengersByBooking(String bookingId);
    PassengerDTO updatePassenger(String id, PassengerDTO passengerDTO);
    PassengerDTO assignSeat(String passengerId, String seatId, String seatNumber);
    PassengerDTO checkIn(String passengerId);
    void deletePassenger(String id);
    void deletePassengersByBooking(String bookingId);
    Integer countByBooking(String bookingId);
    String generateTicketNumber();
}
