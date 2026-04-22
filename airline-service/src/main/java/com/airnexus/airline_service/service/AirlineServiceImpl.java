package com.airnexus.airline_service.service;


import com.airnexus.airline_service.dto.AirlineDTO;
import com.airnexus.airline_service.dto.AirportDTO;
import com.airnexus.airline_service.entity.Airline;
import com.airnexus.airline_service.entity.Airport;
import com.airnexus.airline_service.repository.AirlineRepository;
import com.airnexus.airline_service.repository.AirportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AirlineServiceImpl implements AirlineService {

    private final AirlineRepository airlineRepository;
    private final AirportRepository airportRepository;

    // ============ AIRLINE OPERATIONS ============

    @Override
    public AirlineDTO createAirline(AirlineDTO dto) {
        if (airlineRepository.existsByIataCode(dto.getIataCode())) {
            throw new RuntimeException("Airline with IATA code already exists");
        }

        Airline airline = new Airline();
        airline.setName(dto.getName());
        airline.setIataCode(dto.getIataCode());
        airline.setIcaoCode(dto.getIcaoCode());
        airline.setLogoUrl(dto.getLogoUrl());
        airline.setCountry(dto.getCountry());
        airline.setContactEmail(dto.getContactEmail());
        airline.setContactPhone(dto.getContactPhone());
        airline.setIsActive(true);

        airline = airlineRepository.save(airline);
        return mapAirlineToDTO(airline);
    }

    @Override
    public AirlineDTO getAirlineById(String id) {
        Airline airline = airlineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Airline not found"));
        return mapAirlineToDTO(airline);
    }

    @Override
    public AirlineDTO getAirlineByIata(String iataCode) {
        Airline airline = airlineRepository.findByIataCode(iataCode)
                .orElseThrow(() -> new RuntimeException("Airline not found"));
        return mapAirlineToDTO(airline);
    }

    @Override
    public List<AirlineDTO> getAllAirlines() {
        return airlineRepository.findAll().stream()
                .map(this::mapAirlineToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AirlineDTO> getActiveAirlines() {
        return airlineRepository.findByIsActive(true).stream()
                .map(this::mapAirlineToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AirlineDTO updateAirline(String id, AirlineDTO dto) {
        Airline airline = airlineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Airline not found"));

        airline.setName(dto.getName());
        airline.setLogoUrl(dto.getLogoUrl());
        airline.setCountry(dto.getCountry());
        airline.setContactEmail(dto.getContactEmail());
        airline.setContactPhone(dto.getContactPhone());

        airline = airlineRepository.save(airline);
        return mapAirlineToDTO(airline);
    }

    @Override
    public void deactivateAirline(String id) {
        Airline airline = airlineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Airline not found"));
        airline.setIsActive(false);
        airlineRepository.save(airline);
    }

    // ============ AIRPORT OPERATIONS ============

    @Override
    public AirportDTO createAirport(AirportDTO dto) {
        if (airportRepository.existsByIataCode(dto.getIataCode())) {
            throw new RuntimeException("Airport with IATA code already exists");
        }

        Airport airport = new Airport();
        airport.setName(dto.getName());
        airport.setIataCode(dto.getIataCode());
        airport.setIcaoCode(dto.getIcaoCode());
        airport.setCity(dto.getCity());
        airport.setCountry(dto.getCountry());
        airport.setLatitude(dto.getLatitude());
        airport.setLongitude(dto.getLongitude());
        airport.setTimezone(dto.getTimezone());

        airport = airportRepository.save(airport);
        return mapAirportToDTO(airport);
    }

    @Override
    public AirportDTO getAirportById(String id) {
        Airport airport = airportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Airport not found"));
        return mapAirportToDTO(airport);
    }

    @Override
    public AirportDTO getAirportByIata(String iataCode) {
        Airport airport = airportRepository.findByIataCode(iataCode)
                .orElseThrow(() -> new RuntimeException("Airport not found"));
        return mapAirportToDTO(airport);
    }

    @Override
    public List<AirportDTO> getAllAirports() {
        return airportRepository.findAll().stream()
                .map(this::mapAirportToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AirportDTO> getAirportsByCity(String city) {
        return airportRepository.findByCity(city).stream()
                .map(this::mapAirportToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AirportDTO> searchAirports(String query) {
        return airportRepository.searchAirports(query).stream()
                .map(this::mapAirportToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AirportDTO updateAirport(String id, AirportDTO dto) {
        Airport airport = airportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Airport not found"));

        airport.setName(dto.getName());
        airport.setCity(dto.getCity());
        airport.setCountry(dto.getCountry());
        airport.setLatitude(dto.getLatitude());
        airport.setLongitude(dto.getLongitude());
        airport.setTimezone(dto.getTimezone());

        airport = airportRepository.save(airport);
        return mapAirportToDTO(airport);
    }

    @Override
    public void deleteAirport(String id) {
        airportRepository.deleteById(id);
    }

    // ============ MAPPERS ============

    private AirlineDTO mapAirlineToDTO(Airline airline) {
        AirlineDTO dto = new AirlineDTO();
        dto.setAirlineId(airline.getAirlineId());
        dto.setName(airline.getName());
        dto.setIataCode(airline.getIataCode());
        dto.setIcaoCode(airline.getIcaoCode());
        dto.setLogoUrl(airline.getLogoUrl());
        dto.setCountry(airline.getCountry());
        dto.setContactEmail(airline.getContactEmail());
        dto.setContactPhone(airline.getContactPhone());
        dto.setIsActive(airline.getIsActive());
        return dto;
    }

    private AirportDTO mapAirportToDTO(Airport airport) {
        AirportDTO dto = new AirportDTO();
        dto.setAirportId(airport.getAirportId());
        dto.setName(airport.getName());
        dto.setIataCode(airport.getIataCode());
        dto.setIcaoCode(airport.getIcaoCode());
        dto.setCity(airport.getCity());
        dto.setCountry(airport.getCountry());
        dto.setLatitude(airport.getLatitude());
        dto.setLongitude(airport.getLongitude());
        dto.setTimezone(airport.getTimezone());
        return dto;
    }
}
