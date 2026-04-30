//package com.airnexus.booking_service.service;
//
//import com.airnexus.booking_service.client.*;
//import com.airnexus.booking_service.dto.BookingDTO;
//import com.airnexus.booking_service.dto.BookingRequest;
//import com.airnexus.booking_service.dto.FareSummary;
//import com.airnexus.booking_service.entity.Booking;
//import com.airnexus.booking_service.exception.CustomExceptions;
//import com.airnexus.booking_service.repository.BookingRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Random;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class BookingServiceImpl implements BookingService {
//
//    private final BookingRepository bookingRepository;
//    private final FlightClient flightClient;
//    private final SeatClient seatClient;
//    private final PassengerClient passengerClient;
//    private final NotificationClient notificationClient;
//
//    @Override
//    @Transactional
//    public BookingDTO createBooking(BookingRequest request) {
//        // Get flight details - Feign will throw FeignException which is handled by GlobalExceptionHandler
//        FlightDTO flight = flightClient.getFlightById(request.getFlightId());
//
//        // Calculate fare
//        FareSummary fare = calculateFare(
//                request.getFlightId(),
//                request.getPassengers().size(),
//                request.getSeatIds(),
//                request.getLuggageKg()
//        );
//
//        // Create booking
//        Booking booking = new Booking();
//        booking.setUserId(request.getUserId());
//        booking.setFlightId(request.getFlightId());
//        booking.setPnrCode(generatePnr());
//        booking.setTripType(Booking.TripType.valueOf(request.getTripType()));
//        booking.setStatus(Booking.BookingStatus.PENDING);
//        booking.setBaseFare(fare.getBaseFare());
//        booking.setTaxes(fare.getTaxes());
//        booking.setAncillaryCharges(fare.getLuggageCharges() + fare.getMealCharges());
//        booking.setTotalFare(fare.getTotalFare());
//        booking.setLuggageKg(request.getLuggageKg());
//        booking.setContactEmail(request.getContactEmail());
//        booking.setContactPhone(request.getContactPhone());
//        booking.setNumberOfPassengers(request.getPassengers().size());
//
//        booking = bookingRepository.save(booking);
//
//        // Add passengers
//        final String bookingId = booking.getBookingId();
//        List<PassengerDTO> passengerDTOs = request.getPassengers().stream()
//                .map(p -> {
//                    PassengerDTO dto = new PassengerDTO();
//                    dto.setBookingId(bookingId);
//                    dto.setTitle(p.getTitle());
//                    dto.setFirstName(p.getFirstName());
//                    dto.setLastName(p.getLastName());
//                    dto.setDateOfBirth(p.getDateOfBirth());
//                    dto.setGender(p.getGender());
//                    dto.setPassportNumber(p.getPassportNumber());
//                    dto.setNationality(p.getNationality());
//                    dto.setPassportExpiry(p.getPassportExpiry());
//                    dto.setMealPreference(p.getMealPreference());
//                    return dto;
//                })
//                .collect(Collectors.toList());
//
//        List<PassengerDTO> savedPassengers = passengerClient.addPassengers(passengerDTOs);
//
//        // Assign seats to passengers
//        for (int i = 0; i < savedPassengers.size() && i < request.getSeatIds().size(); i++) {
//            String seatId = request.getSeatIds().get(i);
//            SeatDTO seat = seatClient.getSeatById(seatId);
//            passengerClient.assignSeat(
//                    savedPassengers.get(i).getPassengerId(),
//                    seatId,
//                    seat.getSeatNumber()
//            );
//        }
//
//        return mapToDTO(booking);
//    }
//
//    @Override
//    public BookingDTO getBookingById(String id) {
//        Booking booking = bookingRepository.findById(id)
//                .orElseThrow(() -> new CustomExceptions.BookingNotFoundException(
//                        "Booking not found with ID: " + id
//                ));
//        return mapToDTO(booking);
//    }
//
//    @Override
//    public BookingDTO getBookingByPnr(String pnrCode) {
//        Booking booking = bookingRepository.findByPnrCode(pnrCode)
//                .orElseThrow(() -> new CustomExceptions.InvalidPnrException(
//                        "No booking found with PNR: " + pnrCode
//                ));
//        return mapToDTO(booking);
//    }
//
//    @Override
//    public List<BookingDTO> getBookingsByUser(String userId) {
//        return bookingRepository.findByUserId(userId).stream()
//                .map(this::mapToDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<BookingDTO> getUpcomingBookings(String userId) {
//        LocalDateTime now = LocalDateTime.now();
//        return bookingRepository.findByUserIdAndBookedAtAfter(userId, now.minusDays(1)).stream()
//                .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED)
//                .map(this::mapToDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional
//    public BookingDTO cancelBooking(String id) {
//        Booking booking = bookingRepository.findById(id)
//                .orElseThrow(() -> new CustomExceptions.BookingNotFoundException(
//                        "Booking not found with ID: " + id
//                ));
//
//        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
//            throw new CustomExceptions.BookingAlreadyCancelledException(
//                    "Booking is already cancelled. PNR: " + booking.getPnrCode()
//            );
//        }
//
//        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED &&
//                booking.getStatus() != Booking.BookingStatus.PENDING) {
//            throw new CustomExceptions.InvalidBookingStatusException(
//                    "Cannot cancel booking with status: " + booking.getStatus()
//            );
//        }
//
//        booking.setStatus(Booking.BookingStatus.CANCELLED);
//        booking = bookingRepository.save(booking);
//
//        // Release seats & increment flight available seats
//        flightClient.incrementSeats(booking.getFlightId(), booking.getNumberOfPassengers());
//
//        // Delete passengers
//        passengerClient.deletePassengersByBooking(booking.getBookingId());
//
//        // Send cancellation notification
//        CancellationNotificationRequest notificationRequest = new CancellationNotificationRequest(
//                booking.getUserId(),
//                booking.getContactEmail(),
//                booking.getBookingId(),
//                booking.getPnrCode()
//        );
//        notificationClient.sendCancellationNotification(notificationRequest);
//
//        return mapToDTO(booking);
//    }
//
//    @Override
//    @Transactional
//    public BookingDTO updateStatus(String id, Booking.BookingStatus status) {
//        Booking booking = bookingRepository.findById(id)
//                .orElseThrow(() -> new CustomExceptions.BookingNotFoundException(
//                        "Booking not found with ID: " + id
//                ));
//
//        booking.setStatus(status);
//        booking = bookingRepository.save(booking);
//        return mapToDTO(booking);
//    }
//
//    @Override
//    @Transactional
//    public BookingDTO confirmBooking(String id, String paymentId) {
//        Booking booking = bookingRepository.findById(id)
//                .orElseThrow(() -> new CustomExceptions.BookingNotFoundException(
//                        "Booking not found with ID: " + id
//                ));
//
//        if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
//            throw new CustomExceptions.BookingAlreadyConfirmedException(
//                    "Booking is already confirmed. PNR: " + booking.getPnrCode()
//            );
//        }
//
//        booking.setStatus(Booking.BookingStatus.CONFIRMED);
//        booking.setPaymentId(paymentId);
//        booking = bookingRepository.save(booking);
//
//        // Decrement flight available seats
//        flightClient.decrementSeats(booking.getFlightId(), booking.getNumberOfPassengers());
//
//        // Get flight details for notification
//        FlightDTO flight = flightClient.getFlightById(booking.getFlightId());
//        String flightDetails = String.format(
//                "Flight %s departing on %s",
//                flight.getFlightId(),
//                booking.getBookedAt().toLocalDate()
//        );
//
//        // Send booking confirmation notification
//        BookingNotificationRequest notificationRequest = new BookingNotificationRequest(
//                booking.getUserId(),
//                booking.getContactEmail(),
//                booking.getContactPhone(),
//                booking.getBookingId(),
//                booking.getPnrCode(),
//                flightDetails
//        );
//        notificationClient.sendBookingConfirmation(notificationRequest);
//
//        return mapToDTO(booking);
//    }
//
//    @Override
//    public FareSummary calculateFare(String flightId, Integer passengers, List<String> seatIds, Integer luggageKg) {
//        // Get flight base price
//        FlightDTO flight = flightClient.getFlightById(flightId);
//        Double baseFare = flight.getBasePrice() * passengers;
//
//        // Calculate seat charges
//        Double seatCharges = 0.0;
//        if (seatIds != null && !seatIds.isEmpty()) {
//            for (String seatId : seatIds) {
//                SeatDTO seat = seatClient.getSeatById(seatId);
//                seatCharges += (flight.getBasePrice() * (seat.getPriceMultiplier() - 1.0));
//            }
//        }
//
//        // Calculate taxes (18% GST + 5% fuel surcharge)
//        Double taxes = baseFare * 0.23;
//
//        // Meal charges (₹300 per passenger)
//        Double mealCharges = passengers * 300.0;
//
//        // Luggage charges (₹500 per 5kg)
//        Double luggageCharges = (luggageKg != null && luggageKg > 0) ? (luggageKg / 5.0) * 500.0 : 0.0;
//
//        // Total fare
//        Double totalFare = baseFare + taxes + seatCharges + mealCharges + luggageCharges;
//
//        return new FareSummary(baseFare, taxes, seatCharges, mealCharges, luggageCharges, totalFare);
//    }
//
//    @Override
//    public String generatePnr() {
//        String pnr;
//        Random random = new Random();
//        do {
//            pnr = String.format("%06d", random.nextInt(1000000));
//        } while (bookingRepository.existsByPnrCode(pnr));
//        return pnr;
//    }
//
//    private BookingDTO mapToDTO(Booking booking) {
//        BookingDTO dto = new BookingDTO();
//        dto.setBookingId(booking.getBookingId());
//        dto.setUserId(booking.getUserId());
//        dto.setFlightId(booking.getFlightId());
//        dto.setPnrCode(booking.getPnrCode());
//        dto.setTripType(booking.getTripType().name());
//        dto.setStatus(booking.getStatus().name());
//        dto.setTotalFare(booking.getTotalFare());
//        dto.setBaseFare(booking.getBaseFare());
//        dto.setTaxes(booking.getTaxes());
//        dto.setAncillaryCharges(booking.getAncillaryCharges());
//        dto.setMealPreference(booking.getMealPreference());
//        dto.setLuggageKg(booking.getLuggageKg());
//        dto.setContactEmail(booking.getContactEmail());
//        dto.setContactPhone(booking.getContactPhone());
//        dto.setBookedAt(booking.getBookedAt());
//        dto.setPaymentId(booking.getPaymentId());
//        dto.setNumberOfPassengers(booking.getNumberOfPassengers());
//        return dto;
//    }
//}

package com.airnexus.booking_service.service;

import com.airnexus.booking_service.client.*;
import com.airnexus.booking_service.dto.BookingDTO;
import com.airnexus.booking_service.dto.BookingRequest;
import com.airnexus.booking_service.dto.FareSummary;
import com.airnexus.booking_service.entity.Booking;
import com.airnexus.booking_service.exception.CustomExceptions;
import com.airnexus.booking_service.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final FlightClient flightClient;
    private final SeatClient seatClient;
    private final PassengerClient passengerClient;
    private final NotificationClient notificationClient;

    @Override
    @Transactional
    public BookingDTO createBooking(BookingRequest request) {
        // Get flight details - Feign will throw FeignException which is handled by GlobalExceptionHandler
        FlightDTO flight = flightClient.getFlightById(request.getFlightId());

        // Calculate fare
        FareSummary fare = calculateFare(
                request.getFlightId(),
                request.getPassengers().size(),
                request.getSeatIds(),
                request.getLuggageKg()
        );

        // Create booking
        Booking booking = new Booking();
        booking.setUserId(request.getUserId());
        booking.setFlightId(request.getFlightId());
        booking.setPnrCode(generatePnr());
        booking.setTripType(Booking.TripType.valueOf(request.getTripType()));
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setBaseFare(fare.getBaseFare());
        booking.setTaxes(fare.getTaxes());
        booking.setAncillaryCharges(fare.getLuggageCharges() + fare.getMealCharges());
        booking.setTotalFare(fare.getTotalFare());
        booking.setLuggageKg(request.getLuggageKg());
        booking.setContactEmail(request.getContactEmail());
        booking.setContactPhone(request.getContactPhone());
        booking.setNumberOfPassengers(request.getPassengers().size());

        booking = bookingRepository.save(booking);

        // Add passengers
        final String bookingId = booking.getBookingId();
        List<PassengerDTO> passengerDTOs = request.getPassengers().stream()
                .map(p -> {
                    PassengerDTO dto = new PassengerDTO();
                    dto.setBookingId(bookingId);

                    // Derive title from gender with safe default
                    String rawGender = p.getGender() != null ? p.getGender().toUpperCase().trim() : "MALE";
                    String normalizedGender = rawGender.equals("FEMALE") ? "FEMALE"
                            : rawGender.equals("OTHER")  ? "OTHER"
                              : "MALE";

                    String title = p.getTitle() != null ? p.getTitle().toUpperCase().trim() : null;
                    if (title == null || title.isEmpty()) {
                        title = normalizedGender.equals("FEMALE") ? "MRS" : "MR";
                    }
                    dto.setTitle(title);
                    dto.setGender(normalizedGender);

                    // Name — ensure both fields are non-null
                    String firstName = p.getFirstName() != null ? p.getFirstName().trim() : "";
                    String lastName  = p.getLastName()  != null ? p.getLastName().trim()  : "";
                    if (firstName.isEmpty() && !lastName.isEmpty()) firstName = lastName;
                    if (lastName.isEmpty()  && !firstName.isEmpty()) lastName  = firstName;
                    dto.setFirstName(firstName.isEmpty() ? "Unknown" : firstName);
                    dto.setLastName(lastName.isEmpty()   ? "Unknown" : lastName);

                    // Date of birth — default to a reasonable adult DOB if missing
                    dto.setDateOfBirth(p.getDateOfBirth() != null ? p.getDateOfBirth() : "2000-01-01");

                    dto.setPassportNumber(p.getPassportNumber());
                    dto.setNationality(p.getNationality());
                    dto.setPassportExpiry(p.getPassportExpiry());

                    // Normalize meal preference to valid enum value
                    String meal = p.getMealPreference() != null ? p.getMealPreference().toUpperCase().trim() : "NONE";
                    try {
                        // Validate it's a real enum value
                        java.util.Arrays.asList("VEG","NON_VEG","JAIN","VEGAN","NONE").contains(meal);
                    } catch (Exception e) {
                        meal = "NONE";
                    }
                    dto.setMealPreference(meal);

                    return dto;
                })
                .collect(Collectors.toList());

        List<PassengerDTO> savedPassengers = passengerClient.addPassengers(passengerDTOs);

        // Assign seats to passengers
        for (int i = 0; i < savedPassengers.size() && i < request.getSeatIds().size(); i++) {
            String seatId = request.getSeatIds().get(i);
            SeatDTO seat = seatClient.getSeatById(seatId);
            passengerClient.assignSeat(
                    savedPassengers.get(i).getPassengerId(),
                    seatId,
                    seat.getSeatNumber()
            );
        }

        return mapToDTO(booking);
    }

    @Override
    public BookingDTO getBookingById(String id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new CustomExceptions.BookingNotFoundException(
                        "Booking not found with ID: " + id
                ));
        return mapToDTO(booking);
    }

    @Override
    public BookingDTO getBookingByPnr(String pnrCode) {
        Booking booking = bookingRepository.findByPnrCode(pnrCode)
                .orElseThrow(() -> new CustomExceptions.InvalidPnrException(
                        "No booking found with PNR: " + pnrCode
                ));
        return mapToDTO(booking);
    }

    @Override
    public List<BookingDTO> getBookingsByUser(String userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getUpcomingBookings(String userId) {
        LocalDateTime now = LocalDateTime.now();
        return bookingRepository.findByUserIdAndBookedAtAfter(userId, now.minusDays(1)).stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingDTO cancelBooking(String id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new CustomExceptions.BookingNotFoundException(
                        "Booking not found with ID: " + id
                ));

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new CustomExceptions.BookingAlreadyCancelledException(
                    "Booking is already cancelled. PNR: " + booking.getPnrCode()
            );
        }

        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED &&
                booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new CustomExceptions.InvalidBookingStatusException(
                    "Cannot cancel booking with status: " + booking.getStatus()
            );
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);

        // Release seats & increment flight available seats
        flightClient.incrementSeats(booking.getFlightId(), booking.getNumberOfPassengers());

        // Delete passengers
        passengerClient.deletePassengersByBooking(booking.getBookingId());

        // Send cancellation notification
        CancellationNotificationRequest notificationRequest = new CancellationNotificationRequest(
                booking.getUserId(),
                booking.getContactEmail(),
                booking.getBookingId(),
                booking.getPnrCode()
        );
        notificationClient.sendCancellationNotification(notificationRequest);

        return mapToDTO(booking);
    }

    @Override
    @Transactional
    public BookingDTO updateStatus(String id, Booking.BookingStatus status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new CustomExceptions.BookingNotFoundException(
                        "Booking not found with ID: " + id
                ));

        booking.setStatus(status);
        booking = bookingRepository.save(booking);
        return mapToDTO(booking);
    }

    @Override
    @Transactional
    public BookingDTO confirmBooking(String id, String paymentId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new CustomExceptions.BookingNotFoundException(
                        "Booking not found with ID: " + id
                ));

        if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
            throw new CustomExceptions.BookingAlreadyConfirmedException(
                    "Booking is already confirmed. PNR: " + booking.getPnrCode()
            );
        }

        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentId(paymentId);
        booking = bookingRepository.save(booking);

        // Decrement flight available seats
        flightClient.decrementSeats(booking.getFlightId(), booking.getNumberOfPassengers());

        // Get flight details for notification
        FlightDTO flight = flightClient.getFlightById(booking.getFlightId());
        String flightDetails = String.format(
                "Flight %s departing on %s",
                flight.getFlightId(),
                booking.getBookedAt().toLocalDate()
        );

        // Send booking confirmation notification
        BookingNotificationRequest notificationRequest = new BookingNotificationRequest(
                booking.getUserId(),
                booking.getContactEmail(),
                booking.getContactPhone(),
                booking.getBookingId(),
                booking.getPnrCode(),
                flightDetails
        );
        notificationClient.sendBookingConfirmation(notificationRequest);

        return mapToDTO(booking);
    }

    @Override
    public FareSummary calculateFare(String flightId, Integer passengers, List<String> seatIds, Integer luggageKg) {
        // Get flight base price
        FlightDTO flight = flightClient.getFlightById(flightId);
        Double baseFare = flight.getBasePrice() * passengers;

        // Calculate seat charges
        Double seatCharges = 0.0;
        if (seatIds != null && !seatIds.isEmpty()) {
            for (String seatId : seatIds) {
                SeatDTO seat = seatClient.getSeatById(seatId);
                seatCharges += (flight.getBasePrice() * (seat.getPriceMultiplier() - 1.0));
            }
        }

        // Calculate taxes (18% GST + 5% fuel surcharge)
        Double taxes = baseFare * 0.23;

        // Meal charges (₹300 per passenger)
        Double mealCharges = passengers * 300.0;

        // Luggage charges (₹500 per 5kg)
        Double luggageCharges = (luggageKg != null && luggageKg > 0) ? (luggageKg / 5.0) * 500.0 : 0.0;

        // Total fare
        Double totalFare = baseFare + taxes + seatCharges + mealCharges + luggageCharges;

        return new FareSummary(baseFare, taxes, seatCharges, mealCharges, luggageCharges, totalFare);
    }

    @Override
    public String generatePnr() {
        String pnr;
        Random random = new Random();
        do {
            pnr = String.format("%06d", random.nextInt(1000000));
        } while (bookingRepository.existsByPnrCode(pnr));
        return pnr;
    }

    private BookingDTO mapToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setBookingId(booking.getBookingId());
        dto.setUserId(booking.getUserId());
        dto.setFlightId(booking.getFlightId());
        dto.setPnrCode(booking.getPnrCode());
        dto.setTripType(booking.getTripType().name());
        dto.setStatus(booking.getStatus().name());
        dto.setTotalFare(booking.getTotalFare());
        dto.setBaseFare(booking.getBaseFare());
        dto.setTaxes(booking.getTaxes());
        dto.setAncillaryCharges(booking.getAncillaryCharges());
        dto.setMealPreference(booking.getMealPreference());
        dto.setLuggageKg(booking.getLuggageKg());
        dto.setContactEmail(booking.getContactEmail());
        dto.setContactPhone(booking.getContactPhone());
        dto.setBookedAt(booking.getBookedAt());
        dto.setPaymentId(booking.getPaymentId());
        dto.setNumberOfPassengers(booking.getNumberOfPassengers());
        return dto;
    }
}