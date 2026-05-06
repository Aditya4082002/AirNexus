package com.airnexus.booking_service.service;

import com.airnexus.booking_service.client.*;
import com.airnexus.booking_service.dto.BookingDTO;
import com.airnexus.booking_service.dto.BookingRequest;
import com.airnexus.booking_service.dto.FareSummary;
import com.airnexus.booking_service.entity.Booking;
import com.airnexus.booking_service.exception.CustomExceptions;
import com.airnexus.booking_service.repository.BookingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingServiceImpl Tests")
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private FlightClient flightClient;
    @Mock private SeatClient seatClient;
    @Mock private PassengerClient passengerClient;
    @Mock private NotificationClient notificationClient;

    @InjectMocks
    private BookingServiceImpl bookingService;

    // ─── Helpers ────────────────────────────────────────────────────────────

    private Booking buildBooking(String id, String pnr, Booking.BookingStatus status) {
        Booking b = new Booking();
        b.setBookingId(id);
        b.setUserId("user-1");
        b.setFlightId("flight-1");
        b.setPnrCode(pnr);
        b.setTripType(Booking.TripType.ONE_WAY);
        b.setStatus(status);
        b.setBaseFare(5000.0);
        b.setTaxes(1150.0);
        b.setAncillaryCharges(300.0);
        b.setTotalFare(6450.0);
        b.setLuggageKg(0);
        b.setContactEmail("user@test.com");
        b.setContactPhone("+91-9999999999");
        b.setBookedAt(LocalDateTime.now());
        b.setNumberOfPassengers(1);
        return b;
    }

    private FlightDTO buildFlight(String id, double basePrice) {
        FlightDTO f = new FlightDTO();
        f.setFlightId(id);
        f.setBasePrice(basePrice);
        f.setAvailableSeats(50);
        return f;
    }

    private SeatDTO buildSeat(String id, String seatNum, double multiplier) {
        SeatDTO s = new SeatDTO();
        s.setSeatId(id);
        s.setSeatNumber(seatNum);
        s.setPriceMultiplier(multiplier);
        return s;
    }

    private BookingRequest buildRequest(String flightId, int luggageKg) {
        BookingRequest req = new BookingRequest();
        req.setUserId("user-1");
        req.setFlightId(flightId);
        req.setTripType("ONE_WAY");
        req.setContactEmail("user@test.com");
        req.setContactPhone("+91-9999999999");
        req.setLuggageKg(luggageKg);
        req.setSeatIds(List.of("seat-1"));

        BookingRequest.PassengerDetails p = new BookingRequest.PassengerDetails();
        p.setFirstName("John");
        p.setLastName("Doe");
        p.setGender("MALE");
        p.setDateOfBirth("1990-01-01");
        p.setMealPreference("VEG");
        req.setPassengers(List.of(p));
        return req;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  createBooking()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("createBooking()")
    class CreateBooking {

        @Test
        @DisplayName("should create booking with PENDING status and correct fare fields")
        void createBooking_success() {
            BookingRequest req = buildRequest("flight-1", 0);
            FlightDTO flight = buildFlight("flight-1", 5000.0);
            Booking saved = buildBooking("bk-1", "123456", Booking.BookingStatus.PENDING);

            PassengerDTO savedPassenger = new PassengerDTO();
            savedPassenger.setPassengerId("pax-1");

            when(flightClient.getFlightById("flight-1")).thenReturn(flight);
            when(seatClient.getSeatById("seat-1")).thenReturn(buildSeat("seat-1", "12A", 1.0));
            when(bookingRepository.existsByPnrCode(anyString())).thenReturn(false);
            when(bookingRepository.save(any(Booking.class))).thenReturn(saved);
            when(passengerClient.addPassengers(anyList())).thenReturn(List.of(savedPassenger));
            when(passengerClient.assignSeat(any(), any(), any())).thenReturn(new PassengerDTO());

            BookingDTO result = bookingService.createBooking(req);

            assertThat(result).isNotNull();
            assertThat(result.getBookingId()).isEqualTo("bk-1");
            assertThat(result.getStatus()).isEqualTo("PENDING");
            verify(bookingRepository).save(any(Booking.class));
            verify(passengerClient).addPassengers(anyList());
        }

        @Test
        @DisplayName("should set passenger title to MR for MALE gender when title is null")
        void createBooking_defaultsMaleTitleToMr() {
            BookingRequest req = buildRequest("flight-1", 0);
            req.getPassengers().get(0).setGender("MALE");
            req.getPassengers().get(0).setTitle(null);

            FlightDTO flight = buildFlight("flight-1", 5000.0);
            Booking saved = buildBooking("bk-1", "000001", Booking.BookingStatus.PENDING);
            PassengerDTO savedPax = new PassengerDTO();
            savedPax.setPassengerId("pax-1");

            when(flightClient.getFlightById(any())).thenReturn(flight);
            when(seatClient.getSeatById(any())).thenReturn(buildSeat("seat-1", "12A", 1.0));
            when(bookingRepository.existsByPnrCode(any())).thenReturn(false);
            when(bookingRepository.save(any())).thenReturn(saved);
            when(passengerClient.addPassengers(anyList())).thenAnswer(inv -> {
                List<PassengerDTO> dtos = inv.getArgument(0);
                assertThat(dtos.get(0).getTitle()).isEqualTo("MR");
                return List.of(savedPax);
            });
            when(passengerClient.assignSeat(any(), any(), any())).thenReturn(new PassengerDTO());

            bookingService.createBooking(req);
        }

        @Test
        @DisplayName("should set passenger title to MRS for FEMALE gender when title is null")
        void createBooking_defaultsFemaleTitleToMrs() {
            BookingRequest req = buildRequest("flight-1", 0);
            req.getPassengers().get(0).setGender("FEMALE");
            req.getPassengers().get(0).setTitle(null);

            FlightDTO flight = buildFlight("flight-1", 5000.0);
            Booking saved = buildBooking("bk-1", "000001", Booking.BookingStatus.PENDING);
            PassengerDTO savedPax = new PassengerDTO();
            savedPax.setPassengerId("pax-1");

            when(flightClient.getFlightById(any())).thenReturn(flight);
            when(seatClient.getSeatById(any())).thenReturn(buildSeat("seat-1", "12A", 1.0));
            when(bookingRepository.existsByPnrCode(any())).thenReturn(false);
            when(bookingRepository.save(any())).thenReturn(saved);
            when(passengerClient.addPassengers(anyList())).thenAnswer(inv -> {
                List<PassengerDTO> dtos = inv.getArgument(0);
                assertThat(dtos.get(0).getTitle()).isEqualTo("MRS");
                return List.of(savedPax);
            });
            when(passengerClient.assignSeat(any(), any(), any())).thenReturn(new PassengerDTO());

            bookingService.createBooking(req);
        }

        @Test
        @DisplayName("should use 'Unknown' for firstName when both name fields are null")
        void createBooking_fallbackNameWhenNull() {
            BookingRequest req = buildRequest("flight-1", 0);
            req.getPassengers().get(0).setFirstName(null);
            req.getPassengers().get(0).setLastName(null);

            FlightDTO flight = buildFlight("flight-1", 5000.0);
            Booking saved = buildBooking("bk-1", "000001", Booking.BookingStatus.PENDING);
            PassengerDTO savedPax = new PassengerDTO();
            savedPax.setPassengerId("pax-1");

            when(flightClient.getFlightById(any())).thenReturn(flight);
            when(seatClient.getSeatById(any())).thenReturn(buildSeat("seat-1", "12A", 1.0));
            when(bookingRepository.existsByPnrCode(any())).thenReturn(false);
            when(bookingRepository.save(any())).thenReturn(saved);
            when(passengerClient.addPassengers(anyList())).thenAnswer(inv -> {
                List<PassengerDTO> dtos = inv.getArgument(0);
                assertThat(dtos.get(0).getFirstName()).isEqualTo("Unknown");
                assertThat(dtos.get(0).getLastName()).isEqualTo("Unknown");
                return List.of(savedPax);
            });
            when(passengerClient.assignSeat(any(), any(), any())).thenReturn(new PassengerDTO());

            bookingService.createBooking(req);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  getBookingById()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getBookingById()")
    class GetBookingById {

        @Test
        @DisplayName("should return BookingDTO when booking exists")
        void getBookingById_found() {
            Booking booking = buildBooking("bk-1", "123456", Booking.BookingStatus.CONFIRMED);
            when(bookingRepository.findById("bk-1")).thenReturn(Optional.of(booking));

            BookingDTO result = bookingService.getBookingById("bk-1");

            assertThat(result.getBookingId()).isEqualTo("bk-1");
            assertThat(result.getPnrCode()).isEqualTo("123456");
            assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        }

        @Test
        @DisplayName("should throw BookingNotFoundException when booking does not exist")
        void getBookingById_notFound() {
            when(bookingRepository.findById("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.getBookingById("bad"))
                    .isInstanceOf(CustomExceptions.BookingNotFoundException.class)
                    .hasMessageContaining("bad");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  getBookingByPnr()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getBookingByPnr()")
    class GetBookingByPnr {

        @Test
        @DisplayName("should return BookingDTO when PNR matches")
        void getBookingByPnr_found() {
            Booking booking = buildBooking("bk-1", "ABC123", Booking.BookingStatus.CONFIRMED);
            when(bookingRepository.findByPnrCode("ABC123")).thenReturn(Optional.of(booking));

            BookingDTO result = bookingService.getBookingByPnr("ABC123");

            assertThat(result.getPnrCode()).isEqualTo("ABC123");
        }

        @Test
        @DisplayName("should throw InvalidPnrException for unknown PNR")
        void getBookingByPnr_notFound() {
            when(bookingRepository.findByPnrCode("XXXXX")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.getBookingByPnr("XXXXX"))
                    .isInstanceOf(CustomExceptions.InvalidPnrException.class)
                    .hasMessageContaining("XXXXX");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  getBookingsByUser()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getBookingsByUser()")
    class GetBookingsByUser {

        @Test
        @DisplayName("should return all bookings for a user")
        void getBookingsByUser_returnsList() {
            List<Booking> bookings = List.of(
                    buildBooking("bk-1", "111111", Booking.BookingStatus.CONFIRMED),
                    buildBooking("bk-2", "222222", Booking.BookingStatus.PENDING)
            );
            when(bookingRepository.findByUserId("user-1")).thenReturn(bookings);

            List<BookingDTO> result = bookingService.getBookingsByUser("user-1");

            assertThat(result).hasSize(2);
            assertThat(result).extracting(BookingDTO::getBookingId)
                    .containsExactlyInAnyOrder("bk-1", "bk-2");
        }

        @Test
        @DisplayName("should return empty list when user has no bookings")
        void getBookingsByUser_empty() {
            when(bookingRepository.findByUserId("user-99")).thenReturn(List.of());

            assertThat(bookingService.getBookingsByUser("user-99")).isEmpty();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  getUpcomingBookings()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getUpcomingBookings()")
    class GetUpcomingBookings {

        @Test
        @DisplayName("should return only CONFIRMED bookings from the last 24h+")
        void getUpcomingBookings_returnsOnlyConfirmed() {
            Booking confirmed = buildBooking("bk-1", "111111", Booking.BookingStatus.CONFIRMED);
            Booking pending   = buildBooking("bk-2", "222222", Booking.BookingStatus.PENDING);

            when(bookingRepository.findByUserIdAndBookedAtAfter(eq("user-1"), any(LocalDateTime.class)))
                    .thenReturn(List.of(confirmed, pending));

            List<BookingDTO> result = bookingService.getUpcomingBookings("user-1");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo("CONFIRMED");
        }

        @Test
        @DisplayName("should return empty list when no upcoming bookings")
        void getUpcomingBookings_empty() {
            when(bookingRepository.findByUserIdAndBookedAtAfter(eq("user-1"), any()))
                    .thenReturn(List.of());

            assertThat(bookingService.getUpcomingBookings("user-1")).isEmpty();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  cancelBooking()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cancelBooking()")
    class CancelBooking {

        @Test
        @DisplayName("should cancel a CONFIRMED booking and notify all downstream clients")
        void cancelBooking_fromConfirmed() {
            Booking booking = buildBooking("bk-1", "123456", Booking.BookingStatus.CONFIRMED);
            when(bookingRepository.findById("bk-1")).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
            doNothing().when(flightClient).incrementSeats(any(), any());
            doNothing().when(passengerClient).deletePassengersByBooking(any());
            doNothing().when(notificationClient).sendCancellationNotification(any());

            BookingDTO result = bookingService.cancelBooking("bk-1");

            assertThat(result.getStatus()).isEqualTo("CANCELLED");
            verify(flightClient).incrementSeats("flight-1", 1);
            verify(passengerClient).deletePassengersByBooking("bk-1");
            verify(notificationClient).sendCancellationNotification(any(CancellationNotificationRequest.class));
        }

        @Test
        @DisplayName("should cancel a PENDING booking successfully")
        void cancelBooking_fromPending() {
            Booking booking = buildBooking("bk-2", "654321", Booking.BookingStatus.PENDING);
            when(bookingRepository.findById("bk-2")).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any())).thenReturn(booking);
            doNothing().when(flightClient).incrementSeats(any(), any());
            doNothing().when(passengerClient).deletePassengersByBooking(any());
            doNothing().when(notificationClient).sendCancellationNotification(any());

            BookingDTO result = bookingService.cancelBooking("bk-2");

            assertThat(result.getStatus()).isEqualTo("CANCELLED");
        }

        @Test
        @DisplayName("should throw BookingAlreadyCancelledException when already cancelled")
        void cancelBooking_alreadyCancelled() {
            Booking booking = buildBooking("bk-1", "123456", Booking.BookingStatus.CANCELLED);
            when(bookingRepository.findById("bk-1")).thenReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.cancelBooking("bk-1"))
                    .isInstanceOf(CustomExceptions.BookingAlreadyCancelledException.class);

            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw InvalidBookingStatusException for COMPLETED booking")
        void cancelBooking_completedStatus() {
            Booking booking = buildBooking("bk-1", "123456", Booking.BookingStatus.COMPLETED);
            when(bookingRepository.findById("bk-1")).thenReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.cancelBooking("bk-1"))
                    .isInstanceOf(CustomExceptions.InvalidBookingStatusException.class)
                    .hasMessageContaining("COMPLETED");
        }

        @Test
        @DisplayName("should throw BookingNotFoundException when booking does not exist")
        void cancelBooking_notFound() {
            when(bookingRepository.findById("nope")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.cancelBooking("nope"))
                    .isInstanceOf(CustomExceptions.BookingNotFoundException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  updateStatus()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatus {

        @Test
        @DisplayName("should update booking status and return updated DTO")
        void updateStatus_success() {
            Booking booking = buildBooking("bk-1", "123456", Booking.BookingStatus.PENDING);
            when(bookingRepository.findById("bk-1")).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any())).thenAnswer(inv -> {
                Booking b = inv.getArgument(0);
                assertThat(b.getStatus()).isEqualTo(Booking.BookingStatus.COMPLETED);
                return b;
            });

            BookingDTO result = bookingService.updateStatus("bk-1", Booking.BookingStatus.COMPLETED);

            assertThat(result.getStatus()).isEqualTo("COMPLETED");
        }

        @Test
        @DisplayName("should throw BookingNotFoundException when booking not found")
        void updateStatus_notFound() {
            when(bookingRepository.findById("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.updateStatus("bad", Booking.BookingStatus.CONFIRMED))
                    .isInstanceOf(CustomExceptions.BookingNotFoundException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  confirmBooking()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("confirmBooking()")
    class ConfirmBooking {

        @Test
        @DisplayName("should confirm a PENDING booking, decrement seats and send notification")
        void confirmBooking_success() {
            Booking booking = buildBooking("bk-1", "123456", Booking.BookingStatus.PENDING);
            when(bookingRepository.findById("bk-1")).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any())).thenReturn(booking);
            doNothing().when(flightClient).decrementSeats(any(), any());
            when(flightClient.getFlightById("flight-1")).thenReturn(buildFlight("flight-1", 5000.0));
            doNothing().when(notificationClient).sendBookingConfirmation(any());

            BookingDTO result = bookingService.confirmBooking("bk-1", "pay_abc123");

            assertThat(result.getStatus()).isEqualTo("CONFIRMED");
            assertThat(booking.getPaymentId()).isEqualTo("pay_abc123");
            verify(flightClient).decrementSeats("flight-1", 1);
            verify(notificationClient).sendBookingConfirmation(any(BookingNotificationRequest.class));
        }

        @Test
        @DisplayName("should throw BookingAlreadyConfirmedException when already confirmed")
        void confirmBooking_alreadyConfirmed() {
            Booking booking = buildBooking("bk-1", "123456", Booking.BookingStatus.CONFIRMED);
            when(bookingRepository.findById("bk-1")).thenReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.confirmBooking("bk-1", "pay_xyz"))
                    .isInstanceOf(CustomExceptions.BookingAlreadyConfirmedException.class);

            verify(bookingRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw BookingNotFoundException when booking not found")
        void confirmBooking_notFound() {
            when(bookingRepository.findById("nope")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.confirmBooking("nope", "pay_xyz"))
                    .isInstanceOf(CustomExceptions.BookingNotFoundException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  calculateFare()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("calculateFare()")
    class CalculateFare {

        @Test
        @DisplayName("should calculate correct base fare for 2 passengers at ₹5000 each")
        void calculateFare_baseFareForTwoPassengers() {
            FlightDTO flight = buildFlight("flight-1", 5000.0);
            when(flightClient.getFlightById("flight-1")).thenReturn(flight);
            when(seatClient.getSeatById("seat-1")).thenReturn(buildSeat("seat-1", "12A", 1.0));
            when(seatClient.getSeatById("seat-2")).thenReturn(buildSeat("seat-2", "12B", 1.0));

            FareSummary fare = bookingService.calculateFare("flight-1", 2, List.of("seat-1", "seat-2"), 0);

            assertThat(fare.getBaseFare()).isEqualTo(10000.0);       // 5000 * 2
            assertThat(fare.getTaxes()).isEqualTo(2300.0);            // 10000 * 0.23
            assertThat(fare.getMealCharges()).isEqualTo(600.0);       // 2 * 300
            assertThat(fare.getLuggageCharges()).isEqualTo(0.0);
            assertThat(fare.getTotalFare()).isEqualTo(12900.0);       // 10000+2300+0+600+0
        }

        @Test
        @DisplayName("should apply seat price multiplier to seat charges")
        void calculateFare_seatMultiplierApplied() {
            FlightDTO flight = buildFlight("flight-1", 5000.0);
            when(flightClient.getFlightById("flight-1")).thenReturn(flight);
            // Multiplier 1.5 → extra 50% of base price per seat = 2500
            when(seatClient.getSeatById("seat-biz")).thenReturn(buildSeat("seat-biz", "2A", 1.5));

            FareSummary fare = bookingService.calculateFare("flight-1", 1, List.of("seat-biz"), 0);

            assertThat(fare.getSeatCharges()).isEqualTo(2500.0);      // 5000 * (1.5 - 1.0)
        }

        @Test
        @DisplayName("should calculate luggage charges correctly (₹500 per 5 kg)")
        void calculateFare_luggageCharges() {
            FlightDTO flight = buildFlight("flight-1", 5000.0);
            when(flightClient.getFlightById("flight-1")).thenReturn(flight);

            FareSummary fare = bookingService.calculateFare("flight-1", 1, List.of(), 10);

            // 10kg / 5 * 500 = 1000
            assertThat(fare.getLuggageCharges()).isEqualTo(1000.0);
        }

        @Test
        @DisplayName("should return zero luggage charges when luggageKg is null")
        void calculateFare_noLuggage() {
            FlightDTO flight = buildFlight("flight-1", 5000.0);
            when(flightClient.getFlightById("flight-1")).thenReturn(flight);

            FareSummary fare = bookingService.calculateFare("flight-1", 1, List.of(), null);

            assertThat(fare.getLuggageCharges()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should return zero seat charges when seatIds list is empty")
        void calculateFare_emptySeatIds() {
            FlightDTO flight = buildFlight("flight-1", 5000.0);
            when(flightClient.getFlightById("flight-1")).thenReturn(flight);

            FareSummary fare = bookingService.calculateFare("flight-1", 1, List.of(), 0);

            assertThat(fare.getSeatCharges()).isEqualTo(0.0);
            verify(seatClient, never()).getSeatById(any());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  generatePnr()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("generatePnr()")
    class GeneratePnr {

        @Test
        @DisplayName("should return a 6-digit numeric PNR string")
        void generatePnr_isNumericSixDigits() {
            when(bookingRepository.existsByPnrCode(anyString())).thenReturn(false);

            String pnr = bookingService.generatePnr();

            assertThat(pnr).matches("\\d{6}");
        }

        @Test
        @DisplayName("should retry until a unique PNR is found")
        void generatePnr_retriesOnCollision() {
            // First 3 calls collide, 4th succeeds
            when(bookingRepository.existsByPnrCode(anyString()))
                    .thenReturn(true)
                    .thenReturn(true)
                    .thenReturn(true)
                    .thenReturn(false);

            String pnr = bookingService.generatePnr();

            assertThat(pnr).matches("\\d{6}");
            verify(bookingRepository, atLeast(4)).existsByPnrCode(anyString());
        }
    }
}