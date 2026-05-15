package com.airnexus.booking_service.client.fallback;

import com.airnexus.booking_service.client.PassengerClient;
import com.airnexus.booking_service.client.PassengerDTO;
import com.airnexus.booking_service.exception.CustomExceptions;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PassengerClientFallback implements PassengerClient {

    @Override
    public List<PassengerDTO> addPassengers(List<PassengerDTO> passengers) {
        throw new CustomExceptions.ServiceUnavailableException(
                "Passenger service is unavailable. Cannot add passengers.");
    }

    @Override
    public PassengerDTO assignSeat(String id, String seatId, String seatNumber) {
        throw new CustomExceptions.ServiceUnavailableException(
                "Passenger service is unavailable. Cannot assign seat.");
    }

    @Override
    public void deletePassengersByBooking(String bookingId) {
        throw new CustomExceptions.ServiceUnavailableException(
                "Passenger service is unavailable. Cannot delete passengers.");
    }
}