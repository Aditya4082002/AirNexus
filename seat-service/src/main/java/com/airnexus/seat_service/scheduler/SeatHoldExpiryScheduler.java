package com.airnexus.seat_service.scheduler;

import com.airnexus.seat_service.entity.Seat;
import com.airnexus.seat_service.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatHoldExpiryScheduler {

    private final SeatRepository seatRepository;

    @Scheduled(fixedRate = 120000) // Every 2 minutes
    @Transactional
    public void releaseExpiredSeatHolds() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(15);

        List<Seat> expiredSeats = seatRepository.findExpiredHolds(expiryTime);

        if (!expiredSeats.isEmpty()) {
            log.info("Releasing {} expired seat holds", expiredSeats.size());

            expiredSeats.forEach(seat -> {
                seat.setStatus(Seat.SeatStatus.AVAILABLE);
                seat.setHoldTime(null);
                seat.setHeldByUserId(null);
            });

            seatRepository.saveAll(expiredSeats);
            log.info("Released {} seats back to AVAILABLE", expiredSeats.size());
        }
    }
}