package com.airnexus.flight_service.service;

import com.airnexus.flight_service.dto.FlightDTO;
import com.airnexus.flight_service.dto.FlightSearchRequest;
import com.airnexus.flight_service.entity.Flight;
import com.airnexus.flight_service.exception.CustomExceptions;
import com.airnexus.flight_service.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;

    @Override
    @CacheEvict(value = "flights", allEntries = true)
    public FlightDTO addFlight(FlightDTO dto) {
        if (flightRepository.findByFlightNumber(dto.getFlightNumber()).isPresent()) {
            throw new CustomExceptions.DuplicateFlightNumberException(
                    "Flight number already exists: " + dto.getFlightNumber()
            );
        }

        Flight flight = new Flight();
        flight.setFlightNumber(dto.getFlightNumber());
        flight.setAirlineId(dto.getAirlineId());
        flight.setOriginAirportCode(dto.getOriginAirportCode());
        flight.setDestinationAirportCode(dto.getDestinationAirportCode());
        flight.setDepartureTime(dto.getDepartureTime());
        flight.setArrivalTime(dto.getArrivalTime());
        flight.setDurationMinutes(dto.getDurationMinutes());
        flight.setAircraftType(dto.getAircraftType());
        flight.setTotalSeats(dto.getTotalSeats());
        flight.setAvailableSeats(dto.getTotalSeats());
        flight.setBasePrice(dto.getBasePrice());
        flight.setStatus(Flight.FlightStatus.ON_TIME);

        flight = flightRepository.save(flight);
        return mapToDTO(flight);
    }

    @Override
    @Cacheable(value = "flight", key = "#id")
    public FlightDTO getFlightById(String id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new CustomExceptions.FlightNotFoundException(
                        "Flight not found with ID: " + id
                ));
        return mapToDTO(flight);
    }

    @Override
    public FlightDTO getFlightByNumber(String flightNumber) {
        Flight flight = flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new CustomExceptions.FlightNotFoundException(
                        "Flight not found with number: " + flightNumber
                ));
        return mapToDTO(flight);
    }

    @Override
    @Cacheable(value = "flights", key = "#request.origin + '-' + #request.destination + '-' + #request.departureDate")
    public List<FlightDTO> searchFlights(FlightSearchRequest request) {
        if (request.getOrigin() == null || request.getDestination() == null || request.getDepartureDate() == null) {
            throw new CustomExceptions.FlightSearchException(
                    "Origin, destination, and departure date are required"
            );
        }

        LocalDateTime searchDate = request.getDepartureDate().atStartOfDay();

        return flightRepository.searchFlights(
                request.getOrigin(),
                request.getDestination(),
                searchDate,
                request.getPassengers()
        ).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<FlightDTO> getFlightsByAirline(String airlineId) {
        return flightRepository.findByAirlineId(airlineId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "flights", allEntries = true),
            @CacheEvict(value = "flight", key = "#id")
    })
    public FlightDTO updateFlight(String id, FlightDTO dto) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new CustomExceptions.FlightNotFoundException(
                        "Flight not found with ID: " + id
                ));

        flight.setDepartureTime(dto.getDepartureTime());
        flight.setArrivalTime(dto.getArrivalTime());
        flight.setDurationMinutes(dto.getDurationMinutes());
        flight.setBasePrice(dto.getBasePrice());

        flight = flightRepository.save(flight);
        return mapToDTO(flight);
    }

    @Override
    @CacheEvict(value = "flights", allEntries = true)
    public FlightDTO updateFlightStatus(String id, Flight.FlightStatus status) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new CustomExceptions.FlightNotFoundException(
                        "Flight not found with ID: " + id
                ));

        flight.setStatus(status);
        flight = flightRepository.save(flight);
        return mapToDTO(flight);
    }

    @Override
    @Transactional
    public void decrementSeats(String flightId, Integer count) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new CustomExceptions.FlightNotFoundException(
                        "Flight not found with ID: " + flightId
                ));

        if (flight.getAvailableSeats() < count) {
            throw new CustomExceptions.InsufficientSeatsException(
                    "Not enough available seats. Required: " + count + ", Available: " + flight.getAvailableSeats()
            );
        }

        flight.setAvailableSeats(flight.getAvailableSeats() - count);
        flightRepository.save(flight);
    }

    @Override
    @Transactional
    public void incrementSeats(String flightId, Integer count) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new CustomExceptions.FlightNotFoundException(
                        "Flight not found with ID: " + flightId
                ));

        flight.setAvailableSeats(flight.getAvailableSeats() + count);
        flightRepository.save(flight);
    }

    @Override
    @CacheEvict(value = "flights", allEntries = true)
    public void deleteFlight(String id) {
        if (!flightRepository.existsById(id)) {
            throw new CustomExceptions.FlightNotFoundException(
                    "Flight not found with ID: " + id
            );
        }
        flightRepository.deleteById(id);
    }

    private FlightDTO mapToDTO(Flight flight) {
        FlightDTO dto = new FlightDTO();
        dto.setFlightId(flight.getFlightId());
        dto.setFlightNumber(flight.getFlightNumber());
        dto.setAirlineId(flight.getAirlineId());
        dto.setOriginAirportCode(flight.getOriginAirportCode());
        dto.setDestinationAirportCode(flight.getDestinationAirportCode());
        dto.setDepartureTime(flight.getDepartureTime());
        dto.setArrivalTime(flight.getArrivalTime());
        dto.setDurationMinutes(flight.getDurationMinutes());
        dto.setStatus(flight.getStatus().name());
        dto.setAircraftType(flight.getAircraftType());
        dto.setTotalSeats(flight.getTotalSeats());
        dto.setAvailableSeats(flight.getAvailableSeats());
        dto.setBasePrice(flight.getBasePrice());
        return dto;
    }
}