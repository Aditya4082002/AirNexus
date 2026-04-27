package com.airnexus.booking_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String bookingId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String flightId;

    @Column(unique = true, nullable = false, length = 6)
    private String pnrCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripType tripType = TripType.ONE_WAY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(nullable = false)
    private Double totalFare;

    @Column(nullable = false)
    private Double baseFare;

    @Column(nullable = false)
    private Double taxes;

    private Double ancillaryCharges = 0.0;

    private String mealPreference;

    private Integer luggageKg = 0;

    @Column(nullable = false)
    private String contactEmail;

    @Column(nullable = false)
    private String contactPhone;

    @Column(nullable = false)
    private LocalDateTime bookedAt = LocalDateTime.now();

    private String paymentId;

    private Integer numberOfPassengers;

    public enum TripType {
        ONE_WAY, ROUND_TRIP
    }

    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW
    }
}