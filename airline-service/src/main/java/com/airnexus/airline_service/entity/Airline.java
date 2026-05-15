package com.airnexus.airline_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "airlines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Airline {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String airlineId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false, length = 3)
    private String iataCode;

    @Column(length = 4)
    private String icaoCode;

    private String logoUrl;

    private String country;

    private String contactEmail;

    private String contactPhone;

    @Column(nullable = false)
    private Boolean isActive = true;
}
