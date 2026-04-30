package com.airnexus.seat_service.service;

import com.airnexus.seat_service.dto.SeatDTO;
import com.airnexus.seat_service.entity.Seat;

import java.util.List;

public interface SeatService {
    SeatDTO addSeat(SeatDTO seatDTO);
    List<SeatDTO> addSeatsForFlight(String flightId, List<SeatDTO> seats);
    SeatDTO getSeatById(String id);
    List<SeatDTO> getAvailableSeats(String flightId);
    List<SeatDTO> getAvailableByClass(String flightId, Seat.SeatClass seatClass);
    List<SeatDTO> getSeatMap(String flightId);
    SeatDTO holdSeat(String seatId, String userId);
    SeatDTO releaseSeat(String seatId);
    SeatDTO confirmSeat(String seatId);
    SeatDTO updateSeat(String id, SeatDTO seatDTO);
    Integer countAvailableByClass(String flightId, Seat.SeatClass seatClass);
    void deleteSeatsForFlight(String flightId);
}