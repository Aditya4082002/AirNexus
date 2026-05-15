package com.airnexus.booking_service.client.fallback;

import com.airnexus.booking_service.client.SeatClient;
import com.airnexus.booking_service.client.SeatDTO;
import com.airnexus.booking_service.exception.CustomExceptions;
import org.springframework.stereotype.Component;

@Component
public class SeatClientFallback implements SeatClient {

    @Override
    public SeatDTO confirmSeat(String seatId) {
        throw new CustomExceptions.ServiceUnavailableException(
                "Seat service is unavailable. Cannot confirm seat.");
    }

    @Override
    public SeatDTO releaseSeat(String seatId) {
        throw new CustomExceptions.ServiceUnavailableException(
                "Seat service is unavailable. Cannot release seat.");
    }

    @Override
    public SeatDTO getSeatById(String id) {
        throw new CustomExceptions.ServiceUnavailableException(
                "Seat service is unavailable. Cannot fetch seat details.");
    }
}