package com.airnexus.seat_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "seats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String seatId;

    @Column(nullable = false)
    private String flightId;

    @Column(nullable = false)
    private String seatNumber; // e.g., 12A, 5B

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatClass seatClass;

    @Column(nullable = false)
    private Integer seat_row;

    @Column(nullable = false)
    private String seat_column; // A, B, C, D, E, F

    private Boolean isWindow = false;

    private Boolean isAisle = false;

    private Boolean hasExtraLegroom = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Column(nullable = false)
    private Double priceMultiplier = 1.0; // 1.2 for extra legroom, 1.5 for premium

    private LocalDateTime holdTime; // When seat was held

    private String heldByUserId; // User who held the seat

    @Version
    private Long version; // For optimistic locking

    public enum SeatClass {
        ECONOMY, BUSINESS, FIRST
    }

    public enum SeatStatus {
        AVAILABLE, HELD, CONFIRMED, BLOCKED
    }
}
