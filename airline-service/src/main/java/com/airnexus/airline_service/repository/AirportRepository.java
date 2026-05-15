package com.airnexus.airline_service.repository;

import com.airnexus.airline_service.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirportRepository extends JpaRepository<Airport, String> {
    Optional<Airport> findByIataCode(String iataCode);
    List<Airport> findByCity(String city);
    List<Airport> findByCountry(String country);
    Boolean existsByIataCode(String iataCode);

    @Query("SELECT a FROM Airport a WHERE " +
            "LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.city) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.iataCode) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Airport> searchAirports(@Param("query") String query);
}