package com.airnexus.seat_service.service;


import com.airnexus.seat_service.dto.SeatDTO;
import com.airnexus.seat_service.entity.Seat;
import com.airnexus.seat_service.exception.CustomExceptions;
import com.airnexus.seat_service.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
//import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
//    private final RedisTemplate<String, Object> redisTemplate;

    private static final int HOLD_DURATION_MINUTES = 15;

    @Override
    public SeatDTO addSeat(SeatDTO dto) {
        if (seatRepository.findByFlightIdAndSeatNumber(dto.getFlightId(), dto.getSeatNumber()).isPresent()) {
            throw new CustomExceptions.DuplicateSeatException(
                    "Seat already exists: " + dto.getSeatNumber() + " for flight " + dto.getFlightId()
            );
        }

        Seat seat = new Seat();
        seat.setFlightId(dto.getFlightId());
        seat.setSeatNumber(dto.getSeatNumber());
        seat.setSeatClass(Seat.SeatClass.valueOf(dto.getSeatClass()));
        seat.setSeat_row(dto.getSeat_row());
        seat.setSeat_column(dto.getSeat_column());
        seat.setIsWindow(dto.getIsWindow());
        seat.setIsAisle(dto.getIsAisle());
        seat.setHasExtraLegroom(dto.getHasExtraLegroom());
        seat.setPriceMultiplier(dto.getPriceMultiplier());
        seat.setStatus(Seat.SeatStatus.AVAILABLE);

        seat = seatRepository.save(seat);
        return mapToDTO(seat);
    }

    @Override
    @Transactional
    public List<SeatDTO> addSeatsForFlight(String flightId, List<SeatDTO> seats) {
        return seats.stream()
                .map(dto -> {
                    dto.setFlightId(flightId);
                    return addSeat(dto);
                })
                .collect(Collectors.toList());
    }

    @Override
    public SeatDTO getSeatById(String id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new CustomExceptions.SeatNotFoundException(
                        "Seat not found with ID: " + id
                ));
        return mapToDTO(seat);
    }

    @Override
    public List<SeatDTO> getAvailableSeats(String flightId) {
        return seatRepository.findAvailableByFlightId(flightId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SeatDTO> getAvailableByClass(String flightId, Seat.SeatClass seatClass) {
        return seatRepository.findAvailableByFlightIdAndClass(flightId, seatClass).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SeatDTO> getSeatMap(String flightId) {
        return seatRepository.findByFlightId(flightId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SeatDTO holdSeat(String seatId, String userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new CustomExceptions.SeatNotFoundException(
                        "Seat not found with ID: " + seatId
                ));

        if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
            throw new CustomExceptions.SeatNotAvailableException(
                    "Seat is not available. Current status: " + seat.getStatus()
            );
        }

        seat.setStatus(Seat.SeatStatus.HELD);
        seat.setHoldTime(LocalDateTime.now());
        seat.setHeldByUserId(userId);

        seat = seatRepository.save(seat);

        // Store in Redis with 15-minute TTL
        String redisKey = "seat:hold:" + seatId;
//        redisTemplate.opsForValue().set(redisKey, seatId, HOLD_DURATION_MINUTES, TimeUnit.MINUTES);

        return mapToDTO(seat);
    }

    @Override
    @Transactional
    public SeatDTO releaseSeat(String seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new CustomExceptions.SeatNotFoundException(
                        "Seat not found with ID: " + seatId
                ));

        seat.setStatus(Seat.SeatStatus.AVAILABLE);
        seat.setHoldTime(null);
        seat.setHeldByUserId(null);

        seat = seatRepository.save(seat);

        // Remove from Redis
        String redisKey = "seat:hold:" + seatId;
//        redisTemplate.delete(redisKey);

        return mapToDTO(seat);
    }

    @Override
    @Transactional
    public SeatDTO confirmSeat(String seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new CustomExceptions.SeatNotFoundException(
                        "Seat not found with ID: " + seatId
                ));

        if (seat.getStatus() != Seat.SeatStatus.HELD) {
            throw new CustomExceptions.InvalidSeatStatusException(
                    "Seat must be in HELD status to confirm. Current status: " + seat.getStatus()
            );
        }

        seat.setStatus(Seat.SeatStatus.CONFIRMED);
        seat.setHoldTime(null);

        seat = seatRepository.save(seat);

        // Remove from Redis
        String redisKey = "seat:hold:" + seatId;
//        redisTemplate.delete(redisKey);

        return mapToDTO(seat);
    }

    @Override
    @Transactional
    public SeatDTO updateSeat(String id, SeatDTO dto) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new CustomExceptions.SeatNotFoundException(
                        "Seat not found with ID: " + id
                ));

        seat.setPriceMultiplier(dto.getPriceMultiplier());
        seat.setIsWindow(dto.getIsWindow());
        seat.setIsAisle(dto.getIsAisle());
        seat.setHasExtraLegroom(dto.getHasExtraLegroom());

        seat = seatRepository.save(seat);
        return mapToDTO(seat);
    }

    @Override
    public Integer countAvailableByClass(String flightId, Seat.SeatClass seatClass) {
        return seatRepository.countAvailableByFlightIdAndClass(flightId, seatClass);
    }

    @Override
    @Transactional
    public void deleteSeatsForFlight(String flightId) {
        seatRepository.deleteByFlightId(flightId);
    }

    private SeatDTO mapToDTO(Seat seat) {
        SeatDTO dto = new SeatDTO();
        dto.setSeatId(seat.getSeatId());
        dto.setFlightId(seat.getFlightId());
        dto.setSeatNumber(seat.getSeatNumber());
        dto.setSeatClass(seat.getSeatClass().name());
        dto.setSeat_row(seat.getSeat_row());
        dto.setSeat_column(seat.getSeat_column());
        dto.setIsWindow(seat.getIsWindow());
        dto.setIsAisle(seat.getIsAisle());
        dto.setHasExtraLegroom(seat.getHasExtraLegroom());
        dto.setStatus(seat.getStatus().name());
        dto.setPriceMultiplier(seat.getPriceMultiplier());
        dto.setHoldTime(seat.getHoldTime());
        dto.setHeldByUserId(seat.getHeldByUserId());
        return dto;
    }
}