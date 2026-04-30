package com.airnexus.airline_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "airports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String airportId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false, length = 3)
    private String iataCode;

    @Column(length = 4)
    private String icaoCode;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    private Double latitude;

    private Double longitude;

    private String timezone;
}
