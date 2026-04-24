package com.airnexus.flight_service.service;


import com.airnexus.flight_service.dto.FlightDTO;
import com.airnexus.flight_service.dto.FlightSearchRequest;
import com.airnexus.flight_service.entity.Flight;

import java.util.List;

public interface FlightService {
    FlightDTO addFlight(FlightDTO flightDTO);
    FlightDTO getFlightById(String id);
    FlightDTO getFlightByNumber(String flightNumber);
    List<FlightDTO> searchFlights(FlightSearchRequest request);
    List<FlightDTO> getFlightsByAirline(String airlineId);
    FlightDTO updateFlight(String id, FlightDTO flightDTO);
    FlightDTO updateFlightStatus(String id, Flight.FlightStatus status);
    void decrementSeats(String flightId, Integer count);
    void incrementSeats(String flightId, Integer count);
    void deleteFlight(String id);
}