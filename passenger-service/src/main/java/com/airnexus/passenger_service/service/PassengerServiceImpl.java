package com.airnexus.passenger_service.service;


import com.airnexus.passenger_service.client.BookingClient;
import com.airnexus.passenger_service.client.SeatClient;
import com.airnexus.passenger_service.dto.PassengerDTO;
import com.airnexus.passenger_service.entity.PassengerInfo;
import com.airnexus.passenger_service.exception.CustomExceptions;
import com.airnexus.passenger_service.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassengerServiceImpl implements PassengerService {

    private final PassengerRepository passengerRepository;
    private final SeatClient seatClient;
    private final BookingClient bookingClient;

    @Override
    @Transactional
    public PassengerDTO addPassenger(PassengerDTO dto) {
        validatePassengerData(dto);

        PassengerInfo passenger = new PassengerInfo();
        passenger.setBookingId(dto.getBookingId());
        passenger.setTitle(PassengerInfo.Title.valueOf(dto.getTitle()));
        passenger.setFirstName(dto.getFirstName());
        passenger.setLastName(dto.getLastName());
        passenger.setDateOfBirth(dto.getDateOfBirth());
        passenger.setGender(PassengerInfo.Gender.valueOf(dto.getGender()));
        passenger.setPassportNumber(dto.getPassportNumber());
        passenger.setNationality(dto.getNationality());
        passenger.setPassportExpiry(dto.getPassportExpiry());
        passenger.setPassengerType(determinePassengerType(dto.getDateOfBirth()));
        passenger.setMealPreference(dto.getMealPreference() != null ?
                PassengerInfo.MealPreference.valueOf(dto.getMealPreference()) :
                PassengerInfo.MealPreference.NONE);
        passenger.setTicketNumber(generateTicketNumber());
        passenger.setIsCheckedIn(false);

        passenger = passengerRepository.save(passenger);
        return mapToDTO(passenger);
    }

    @Override
    @Transactional
    public List<PassengerDTO> addPassengers(List<PassengerDTO> passengers) {
        return passengers.stream()
                .map(this::addPassenger)
                .collect(Collectors.toList());
    }

    @Override
    public PassengerDTO getPassengerById(String id) {
        PassengerInfo passenger = passengerRepository.findById(id)
                .orElseThrow(() -> new CustomExceptions.PassengerNotFoundException(
                        "Passenger not found with ID: " + id
                ));
        return mapToDTO(passenger);
    }

    @Override
    public PassengerDTO getByTicketNumber(String ticketNumber) {
        PassengerInfo passenger = passengerRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new CustomExceptions.PassengerNotFoundException(
                        "Passenger not found with ticket number: " + ticketNumber
                ));
        return mapToDTO(passenger);
    }

    @Override
    public List<PassengerDTO> getPassengersByBooking(String bookingId) {
        return passengerRepository.findByBookingId(bookingId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PassengerDTO> getPassengersByPnr(String pnrCode) {
        com.airnexus.passenger_service.client.dto.BookingDTO booking = bookingClient.getBookingByPnr(pnrCode);
        if (booking == null || booking.getBookingId() == null) {
            throw new CustomExceptions.PassengerNotFoundException(
                    "No booking found for PNR: " + pnrCode
            );
        }
        return passengerRepository.findByBookingId(booking.getBookingId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PassengerDTO updatePassenger(String id, PassengerDTO dto) {
        PassengerInfo passenger = passengerRepository.findById(id)
                .orElseThrow(() -> new CustomExceptions.PassengerNotFoundException(
                        "Passenger not found with ID: " + id
                ));

        passenger.setFirstName(dto.getFirstName());
        passenger.setLastName(dto.getLastName());
        passenger.setPassportNumber(dto.getPassportNumber());
        passenger.setNationality(dto.getNationality());
        passenger.setPassportExpiry(dto.getPassportExpiry());

        if (dto.getMealPreference() != null) {
            passenger.setMealPreference(PassengerInfo.MealPreference.valueOf(dto.getMealPreference()));
        }

        passenger = passengerRepository.save(passenger);
        return mapToDTO(passenger);
    }

    @Override
    @Transactional
    public PassengerDTO assignSeat(String passengerId, String seatId, String seatNumber) {
        PassengerInfo passenger = passengerRepository.findById(passengerId)
                .orElseThrow(() -> new CustomExceptions.PassengerNotFoundException(
                        "Passenger not found with ID: " + passengerId
                ));

        // If passenger already has a different seat, release it back to AVAILABLE
        String previousSeatId = passenger.getSeatId();
        if (previousSeatId != null && !previousSeatId.equals(seatId)) {
            try {
                seatClient.releaseSeat(previousSeatId);
                log.info("Released previous seat {} for passenger {}", previousSeatId, passengerId);
            } catch (Exception e) {
                log.warn("Could not release previous seat {}: {}", previousSeatId, e.getMessage());
            }
        }

        // Confirm the new seat directly.
        // SeatServiceImpl.confirmSeat() now accepts both AVAILABLE and HELD,
        // so no gateway-dependent holdSeat call is needed here.
        seatClient.confirmSeat(seatId);
        log.info("Confirmed seat {} for passenger {}", seatId, passengerId);

        passenger.setSeatId(seatId);
        passenger.setSeatNumber(seatNumber);
        passenger = passengerRepository.save(passenger);

        return mapToDTO(passenger);
    }

    @Override
    @Transactional
    public PassengerDTO checkIn(String passengerId) {
        PassengerInfo passenger = passengerRepository.findById(passengerId)
                .orElseThrow(() -> new CustomExceptions.PassengerNotFoundException(
                        "Passenger not found with ID: " + passengerId
                ));

        if (passenger.getSeatId() == null) {
            throw new CustomExceptions.CheckInNotAllowedException(
                    "Seat must be assigned before check-in"
            );
        }

        passenger.setIsCheckedIn(true);
        passenger = passengerRepository.save(passenger);
        return mapToDTO(passenger);
    }

    @Override
    @Transactional
    public void deletePassenger(String id) {
        if (!passengerRepository.existsById(id)) {
            throw new CustomExceptions.PassengerNotFoundException(
                    "Passenger not found with ID: " + id
            );
        }
        passengerRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deletePassengersByBooking(String bookingId) {
        passengerRepository.deleteByBookingId(bookingId);
    }

    @Override
    public Integer countByBooking(String bookingId) {
        return passengerRepository.countByBookingId(bookingId);
    }

    @Override
    public String generateTicketNumber() {
        String ticketNumber;
        do {
            ticketNumber = "TKT" + System.currentTimeMillis() + new Random().nextInt(1000);
        } while (passengerRepository.existsByTicketNumber(ticketNumber));
        return ticketNumber;
    }

    // ============ HELPER METHODS ============

    private void validatePassengerData(PassengerDTO dto) {
        if (dto.getPassportExpiry() != null) {
            LocalDate sixMonthsFromNow = LocalDate.now().plusMonths(6);
            if (dto.getPassportExpiry().isBefore(sixMonthsFromNow)) {
                throw new CustomExceptions.PassportExpiredException(
                        "Passport must be valid for at least 6 months from today. Expiry: " + dto.getPassportExpiry()
                );
            }
        }

        int age = Period.between(dto.getDateOfBirth(), LocalDate.now()).getYears();
        if (age < 2 && dto.getPassportNumber() == null) {
            throw new CustomExceptions.InvalidPassportException(
                    "Passport required for infants traveling internationally"
            );
        }
    }

    private PassengerInfo.PassengerType determinePassengerType(LocalDate dateOfBirth) {
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (age < 2) {
            return PassengerInfo.PassengerType.INFANT;
        } else if (age < 12) {
            return PassengerInfo.PassengerType.CHILD;
        } else {
            return PassengerInfo.PassengerType.ADULT;
        }
    }

    private PassengerDTO mapToDTO(PassengerInfo passenger) {
        PassengerDTO dto = new PassengerDTO();
        dto.setPassengerId(passenger.getPassengerId());
        dto.setBookingId(passenger.getBookingId());
        dto.setTitle(passenger.getTitle().name());
        dto.setFirstName(passenger.getFirstName());
        dto.setLastName(passenger.getLastName());
        dto.setDateOfBirth(passenger.getDateOfBirth());
        dto.setGender(passenger.getGender().name());
        dto.setPassportNumber(passenger.getPassportNumber());
        dto.setNationality(passenger.getNationality());
        dto.setPassportExpiry(passenger.getPassportExpiry());
        dto.setSeatId(passenger.getSeatId());
        dto.setSeatNumber(passenger.getSeatNumber());
        dto.setTicketNumber(passenger.getTicketNumber());
        dto.setPassengerType(passenger.getPassengerType().name());
        dto.setMealPreference(passenger.getMealPreference().name());
        dto.setIsCheckedIn(passenger.getIsCheckedIn());
        return dto;
    }
}