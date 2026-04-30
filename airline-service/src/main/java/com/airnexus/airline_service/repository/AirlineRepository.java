package com.airnexus.airline_service.repository;

import com.airnexus.airline_service.entity.Airline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirlineRepository extends JpaRepository<Airline, String> {
    Optional<Airline> findByIataCode(String iataCode);
    List<Airline> findByIsActive(Boolean isActive);
    List<Airline> findByCountry(String country);
    Boolean existsByIataCode(String iataCode);
}
