package com.airnexus.seat_service.repository;

import com.airnexus.seat_service.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, String> {
    List<Seat> findByFlightId(String flightId);

    List<Seat> findByFlightIdAndSeatClass(String flightId, Seat.SeatClass seatClass);

    Optional<Seat> findByFlightIdAndSeatNumber(String flightId, String seatNumber);

    @Query("SELECT s FROM Seat s WHERE s.flightId = :flightId AND s.status = 'AVAILABLE'")
    List<Seat> findAvailableByFlightId(@Param("flightId") String flightId);

    @Query("SELECT s FROM Seat s WHERE s.flightId = :flightId AND s.seatClass = :seatClass AND s.status = 'AVAILABLE'")
    List<Seat> findAvailableByFlightIdAndClass(
            @Param("flightId") String flightId,
            @Param("seatClass") Seat.SeatClass seatClass
    );

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.flightId = :flightId AND s.seatClass = :seatClass AND s.status = 'AVAILABLE'")
    Integer countAvailableByFlightIdAndClass(
            @Param("flightId") String flightId,
            @Param("seatClass") Seat.SeatClass seatClass
    );

    @Query("SELECT s FROM Seat s WHERE s.status = 'HELD' AND s.holdTime < :expiryTime")
    List<Seat> findExpiredHolds(@Param("expiryTime") LocalDateTime expiryTime);

    void deleteByFlightId(String flightId);
}
