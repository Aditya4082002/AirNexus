package com.airnexus.booking_service.client.fallback;

import com.airnexus.booking_service.client.FlightClient;
import com.airnexus.booking_service.client.FlightDTO;
import com.airnexus.booking_service.exception.CustomExceptions;
import org.springframework.stereotype.Component;

@Component
public class FlightClientFallback implements FlightClient {

    @Override
    public FlightDTO getFlightById(String id) {
        throw new CustomExceptions.ServiceUnavailableException(
                "Flight service is unavailable. Cannot fetch flight details.");
    }

    @Override
    public void decrementSeats(String id, Integer count) {
        throw new CustomExceptions.ServiceUnavailableException(
                "Flight service is unavailable. Cannot decrement seats.");
    }

    @Override
    public void incrementSeats(String id, Integer count) {
        throw new CustomExceptions.ServiceUnavailableException(
                "Flight service is unavailable. Cannot increment seats.");
    }
}