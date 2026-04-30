package com.airnexus.airline_service.service;




import com.airnexus.airline_service.dto.AirlineDTO;
import com.airnexus.airline_service.dto.AirportDTO;

import java.util.List;

public interface AirlineService {
    // Airline operations
    AirlineDTO createAirline(AirlineDTO airlineDTO);
    AirlineDTO getAirlineById(String id);
    AirlineDTO getAirlineByIata(String iataCode);
    List<AirlineDTO> getAllAirlines();
    List<AirlineDTO> getActiveAirlines();
    AirlineDTO updateAirline(String id, AirlineDTO airlineDTO);
    void deactivateAirline(String id);

    // Airport operations
    AirportDTO createAirport(AirportDTO airportDTO);
    AirportDTO getAirportById(String id);
    AirportDTO getAirportByIata(String iataCode);
    List<AirportDTO> getAllAirports();
    List<AirportDTO> getAirportsByCity(String city);
    List<AirportDTO> searchAirports(String query);
    AirportDTO updateAirport(String id, AirportDTO airportDTO);
    void deleteAirport(String id);
}
