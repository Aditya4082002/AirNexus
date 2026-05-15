package com.airnexus.flight_service.repository;

import com.airnexus.flight_service.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, String> {
    Optional<Flight> findByFlightNumber(String flightNumber);
    List<Flight> findByAirlineId(String airlineId);
    List<Flight> findByStatus(Flight.FlightStatus status);

    @Query("SELECT f FROM Flight f WHERE " +
            "f.originAirportCode = :origin AND " +
            "f.destinationAirportCode = :destination AND " +
            "DATE(f.departureTime) = DATE(:date) AND " +
            "f.availableSeats >= :passengers AND " +
            "f.status = 'ON_TIME'")
    List<Flight> searchFlights(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("date") LocalDateTime date,
            @Param("passengers") Integer passengers
    );

    @Query("SELECT f FROM Flight f WHERE " +
            "f.airlineId = :airlineId AND " +
            "f.departureTime >= :startDate AND " +
            "f.departureTime < :endDate")
    List<Flight> findByAirlineAndDateRange(
            @Param("airlineId") String airlineId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
