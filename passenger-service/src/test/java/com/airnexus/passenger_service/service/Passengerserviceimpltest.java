package com.airnexus.passenger_service.service;

import com.airnexus.passenger_service.client.BookingClient;
import com.airnexus.passenger_service.client.SeatClient;
import com.airnexus.passenger_service.client.dto.BookingDTO;
import com.airnexus.passenger_service.dto.PassengerDTO;
import com.airnexus.passenger_service.entity.PassengerInfo;
import com.airnexus.passenger_service.exception.CustomExceptions;
import com.airnexus.passenger_service.repository.PassengerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PassengerServiceImpl Tests")
class PassengerServiceImplTest {

    @Mock private PassengerRepository passengerRepository;
    @Mock private SeatClient seatClient;
    @Mock private BookingClient bookingClient;

    @InjectMocks
    private PassengerServiceImpl passengerService;

    // ─── Helpers ────────────────────────────────────────────────────────────

    /** Adult DTO with valid passport expiry (2 years from now). */
    private PassengerDTO buildAdultDTO() {
        PassengerDTO dto = new PassengerDTO();
        dto.setBookingId("bk-1");
        dto.setTitle("MR");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setDateOfBirth(LocalDate.now().minusYears(30));
        dto.setGender("MALE");
        dto.setPassportNumber("P1234567");
        dto.setNationality("Indian");
        dto.setPassportExpiry(LocalDate.now().plusYears(2));
        dto.setMealPreference("VEG");
        return dto;
    }

    /** Child DTO (age 8). */
    private PassengerDTO buildChildDTO() {
        PassengerDTO dto = buildAdultDTO();
        dto.setDateOfBirth(LocalDate.now().minusYears(8));
        return dto;
    }

    /** Infant DTO (age 1, passport required for intl). */
    private PassengerDTO buildInfantDTO() {
        PassengerDTO dto = buildAdultDTO();
        dto.setDateOfBirth(LocalDate.now().minusYears(1));
        return dto;
    }

    private PassengerInfo buildEntity(String id, String bookingId, boolean checkedIn) {
        PassengerInfo p = new PassengerInfo();
        p.setPassengerId(id);
        p.setBookingId(bookingId);
        p.setTitle(PassengerInfo.Title.MR);
        p.setFirstName("John");
        p.setLastName("Doe");
        p.setDateOfBirth(LocalDate.now().minusYears(30));
        p.setGender(PassengerInfo.Gender.MALE);
        p.setPassportNumber("P1234567");
        p.setNationality("Indian");
        p.setPassportExpiry(LocalDate.now().plusYears(2));
        p.setPassengerType(PassengerInfo.PassengerType.ADULT);
        p.setMealPreference(PassengerInfo.MealPreference.VEG);
        p.setTicketNumber("TKT001");
        p.setIsCheckedIn(checkedIn);
        return p;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  addPassenger()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("addPassenger()")
    class AddPassenger {

        @Test
        @DisplayName("should add ADULT passenger and return DTO with generated ticket number")
        void addPassenger_adult_success() {
            PassengerDTO dto = buildAdultDTO();
            PassengerInfo saved = buildEntity("pax-1", "bk-1", false);

            when(passengerRepository.existsByTicketNumber(anyString())).thenReturn(false);
            when(passengerRepository.save(any(PassengerInfo.class))).thenReturn(saved);

            PassengerDTO result = passengerService.addPassenger(dto);

            assertThat(result.getPassengerId()).isEqualTo("pax-1");
            assertThat(result.getPassengerType()).isEqualTo("ADULT");
            assertThat(result.getIsCheckedIn()).isFalse();
            verify(passengerRepository).save(any(PassengerInfo.class));
        }

        @Test
        @DisplayName("should classify age 8 as CHILD")
        void addPassenger_child_classifiedAsChild() {
            PassengerDTO dto = buildChildDTO();

            when(passengerRepository.existsByTicketNumber(anyString())).thenReturn(false);
            when(passengerRepository.save(any(PassengerInfo.class))).thenAnswer(inv -> {
                PassengerInfo p = inv.getArgument(0);
                assertThat(p.getPassengerType()).isEqualTo(PassengerInfo.PassengerType.CHILD);
                PassengerInfo saved = buildEntity("pax-2", "bk-1", false);
                saved.setPassengerType(PassengerInfo.PassengerType.CHILD);
                return saved;
            });

            PassengerDTO result = passengerService.addPassenger(dto);
            assertThat(result.getPassengerType()).isEqualTo("CHILD");
        }

        @Test
        @DisplayName("should classify age < 2 as INFANT")
        void addPassenger_infant_classifiedAsInfant() {
            PassengerDTO dto = buildInfantDTO(); // age 1, has passport

            when(passengerRepository.existsByTicketNumber(anyString())).thenReturn(false);
            when(passengerRepository.save(any(PassengerInfo.class))).thenAnswer(inv -> {
                PassengerInfo p = inv.getArgument(0);
                assertThat(p.getPassengerType()).isEqualTo(PassengerInfo.PassengerType.INFANT);
                PassengerInfo saved = buildEntity("pax-3", "bk-1", false);
                saved.setPassengerType(PassengerInfo.PassengerType.INFANT);
                return saved;
            });

            passengerService.addPassenger(dto);
        }

        @Test
        @DisplayName("should default meal preference to NONE when null")
        void addPassenger_defaultMealPreference() {
            PassengerDTO dto = buildAdultDTO();
            dto.setMealPreference(null);

            when(passengerRepository.existsByTicketNumber(anyString())).thenReturn(false);
            when(passengerRepository.save(any(PassengerInfo.class))).thenAnswer(inv -> {
                PassengerInfo p = inv.getArgument(0);
                assertThat(p.getMealPreference()).isEqualTo(PassengerInfo.MealPreference.NONE);
                return buildEntity("pax-1", "bk-1", false);
            });

            passengerService.addPassenger(dto);
        }

        @Test
        @DisplayName("should throw PassportExpiredException when passport expires within 6 months")
        void addPassenger_passportExpiringSoon_throws() {
            PassengerDTO dto = buildAdultDTO();
            dto.setPassportExpiry(LocalDate.now().plusMonths(3)); // < 6 months

            assertThatThrownBy(() -> passengerService.addPassenger(dto))
                    .isInstanceOf(CustomExceptions.PassportExpiredException.class)
                    .hasMessageContaining("6 months");

            verify(passengerRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw InvalidPassportException for infant without passport number")
        void addPassenger_infantWithoutPassport_throws() {
            PassengerDTO dto = buildInfantDTO();
            dto.setPassportNumber(null); // no passport for infant

            assertThatThrownBy(() -> passengerService.addPassenger(dto))
                    .isInstanceOf(CustomExceptions.InvalidPassportException.class)
                    .hasMessageContaining("Passport required");

            verify(passengerRepository, never()).save(any());
        }

        @Test
        @DisplayName("should set isCheckedIn=false for every new passenger")
        void addPassenger_checkedInFalse() {
            PassengerDTO dto = buildAdultDTO();

            when(passengerRepository.existsByTicketNumber(anyString())).thenReturn(false);
            when(passengerRepository.save(any(PassengerInfo.class))).thenAnswer(inv -> {
                PassengerInfo p = inv.getArgument(0);
                assertThat(p.getIsCheckedIn()).isFalse();
                return buildEntity("pax-1", "bk-1", false);
            });

            passengerService.addPassenger(dto);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  addPassengers() (bulk)
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("addPassengers()")
    class AddPassengers {

        @Test
        @DisplayName("should add all passengers and return list of DTOs")
        void addPassengers_success() {
            PassengerDTO dto1 = buildAdultDTO();
            PassengerDTO dto2 = buildAdultDTO();
            dto2.setFirstName("Jane");

            PassengerInfo saved1 = buildEntity("pax-1", "bk-1", false);
            PassengerInfo saved2 = buildEntity("pax-2", "bk-1", false);

            when(passengerRepository.existsByTicketNumber(anyString())).thenReturn(false);
            when(passengerRepository.save(any(PassengerInfo.class)))
                    .thenReturn(saved1)
                    .thenReturn(saved2);

            List<PassengerDTO> result = passengerService.addPassengers(List.of(dto1, dto2));

            assertThat(result).hasSize(2);
            verify(passengerRepository, times(2)).save(any(PassengerInfo.class));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  getPassengerById()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getPassengerById()")
    class GetPassengerById {

        @Test
        @DisplayName("should return PassengerDTO when passenger exists")
        void getPassengerById_found() {
            PassengerInfo entity = buildEntity("pax-1", "bk-1", false);
            when(passengerRepository.findById("pax-1")).thenReturn(Optional.of(entity));

            PassengerDTO result = passengerService.getPassengerById("pax-1");

            assertThat(result.getPassengerId()).isEqualTo("pax-1");
            assertThat(result.getFirstName()).isEqualTo("John");
        }

        @Test
        @DisplayName("should throw PassengerNotFoundException when passenger not found")
        void getPassengerById_notFound() {
            when(passengerRepository.findById("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> passengerService.getPassengerById("bad"))
                    .isInstanceOf(CustomExceptions.PassengerNotFoundException.class)
                    .hasMessageContaining("bad");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  getByTicketNumber()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getByTicketNumber()")
    class GetByTicketNumber {

        @Test
        @DisplayName("should return PassengerDTO when ticket matches")
        void getByTicketNumber_found() {
            PassengerInfo entity = buildEntity("pax-1", "bk-1", false);
            when(passengerRepository.findByTicketNumber("TKT001")).thenReturn(Optional.of(entity));

            PassengerDTO result = passengerService.getByTicketNumber("TKT001");

            assertThat(result.getTicketNumber()).isEqualTo("TKT001");
        }

        @Test
        @DisplayName("should throw PassengerNotFoundException for unknown ticket number")
        void getByTicketNumber_notFound() {
            when(passengerRepository.findByTicketNumber("BADTKT")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> passengerService.getByTicketNumber("BADTKT"))
                    .isInstanceOf(CustomExceptions.PassengerNotFoundException.class)
                    .hasMessageContaining("BADTKT");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  getPassengersByBooking()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getPassengersByBooking()")
    class GetPassengersByBooking {

        @Test
        @DisplayName("should return all passengers for a booking")
        void getPassengersByBooking_returnsList() {
            List<PassengerInfo> entities = List.of(
                    buildEntity("pax-1", "bk-1", false),
                    buildEntity("pax-2", "bk-1", false)
            );
            when(passengerRepository.findByBookingId("bk-1")).thenReturn(entities);

            List<PassengerDTO> result = passengerService.getPassengersByBooking("bk-1");

            assertThat(result).hasSize(2);
            assertThat(result).extracting(PassengerDTO::getBookingId).containsOnly("bk-1");
        }

        @Test
        @DisplayName("should return empty list when no passengers for booking")
        void getPassengersByBooking_empty() {
            when(passengerRepository.findByBookingId("bk-99")).thenReturn(List.of());

            assertThat(passengerService.getPassengersByBooking("bk-99")).isEmpty();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  getPassengersByPnr()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getPassengersByPnr()")
    class GetPassengersByPnr {

        @Test
        @DisplayName("should return passengers when PNR resolves to a booking")
        void getPassengersByPnr_found() {
            BookingDTO booking = new BookingDTO();
            booking.setBookingId("bk-1");
            booking.setPnrCode("123456");

            when(bookingClient.getBookingByPnr("123456")).thenReturn(booking);
            when(passengerRepository.findByBookingId("bk-1"))
                    .thenReturn(List.of(buildEntity("pax-1", "bk-1", false)));

            List<PassengerDTO> result = passengerService.getPassengersByPnr("123456");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should throw PassengerNotFoundException when booking is null")
        void getPassengersByPnr_nullBooking() {
            when(bookingClient.getBookingByPnr("XXXXX")).thenReturn(null);

            assertThatThrownBy(() -> passengerService.getPassengersByPnr("XXXXX"))
                    .isInstanceOf(CustomExceptions.PassengerNotFoundException.class)
                    .hasMessageContaining("XXXXX");
        }

        @Test
        @DisplayName("should throw PassengerNotFoundException when booking has null bookingId")
        void getPassengersByPnr_nullBookingId() {
            BookingDTO booking = new BookingDTO();
            booking.setBookingId(null);
            when(bookingClient.getBookingByPnr("YYYYY")).thenReturn(booking);

            assertThatThrownBy(() -> passengerService.getPassengersByPnr("YYYYY"))
                    .isInstanceOf(CustomExceptions.PassengerNotFoundException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  updatePassenger()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updatePassenger()")
    class UpdatePassenger {

        @Test
        @DisplayName("should update mutable fields and return updated DTO")
        void updatePassenger_success() {
            PassengerInfo existing = buildEntity("pax-1", "bk-1", false);
            when(passengerRepository.findById("pax-1")).thenReturn(Optional.of(existing));

            PassengerDTO update = buildAdultDTO();
            update.setFirstName("Jane");
            update.setNationality("British");
            update.setMealPreference("VEGAN");

            PassengerInfo saved = buildEntity("pax-1", "bk-1", false);
            saved.setFirstName("Jane");
            saved.setNationality("British");
            saved.setMealPreference(PassengerInfo.MealPreference.VEGAN);
            when(passengerRepository.save(existing)).thenReturn(saved);

            PassengerDTO result = passengerService.updatePassenger("pax-1", update);

            assertThat(result.getFirstName()).isEqualTo("Jane");
            assertThat(result.getNationality()).isEqualTo("British");
            assertThat(result.getMealPreference()).isEqualTo("VEGAN");
            verify(passengerRepository).save(existing);
        }

        @Test
        @DisplayName("should throw PassengerNotFoundException when passenger not found")
        void updatePassenger_notFound() {
            when(passengerRepository.findById("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> passengerService.updatePassenger("bad", buildAdultDTO()))
                    .isInstanceOf(CustomExceptions.PassengerNotFoundException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  assignSeat()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("assignSeat()")
    class AssignSeat {

        @Test
        @DisplayName("should confirm new seat and save passenger with seat details")
        void assignSeat_success_noExistingSeat() {
            PassengerInfo entity = buildEntity("pax-1", "bk-1", false);
            entity.setSeatId(null); // no existing seat

            when(passengerRepository.findById("pax-1")).thenReturn(Optional.of(entity));
            doNothing().when(seatClient).confirmSeat("seat-1");
            when(passengerRepository.save(entity)).thenReturn(entity);

            PassengerDTO result = passengerService.assignSeat("pax-1", "seat-1", "12A");

            verify(seatClient).confirmSeat("seat-1");
            verify(seatClient, never()).releaseSeat(any());
            assertThat(entity.getSeatId()).isEqualTo("seat-1");
            assertThat(entity.getSeatNumber()).isEqualTo("12A");
        }

        @Test
        @DisplayName("should release old seat before assigning new seat")
        void assignSeat_releasesPreviousSeat() {
            PassengerInfo entity = buildEntity("pax-1", "bk-1", false);
            entity.setSeatId("old-seat"); // previously assigned

            when(passengerRepository.findById("pax-1")).thenReturn(Optional.of(entity));
            doNothing().when(seatClient).releaseSeat("old-seat");
            doNothing().when(seatClient).confirmSeat("new-seat");
            when(passengerRepository.save(entity)).thenReturn(entity);

            passengerService.assignSeat("pax-1", "new-seat", "14B");

            verify(seatClient).releaseSeat("old-seat");
            verify(seatClient).confirmSeat("new-seat");
        }

        @Test
        @DisplayName("should not release seat when same seat is re-assigned")
        void assignSeat_sameSeat_doesNotRelease() {
            PassengerInfo entity = buildEntity("pax-1", "bk-1", false);
            entity.setSeatId("seat-1");

            when(passengerRepository.findById("pax-1")).thenReturn(Optional.of(entity));
            doNothing().when(seatClient).confirmSeat("seat-1");
            when(passengerRepository.save(entity)).thenReturn(entity);

            passengerService.assignSeat("pax-1", "seat-1", "12A");

            verify(seatClient, never()).releaseSeat(any());
            verify(seatClient).confirmSeat("seat-1");
        }

        @Test
        @DisplayName("should continue gracefully when release of previous seat fails")
        void assignSeat_releaseFailsContinues() {
            PassengerInfo entity = buildEntity("pax-1", "bk-1", false);
            entity.setSeatId("old-seat");

            when(passengerRepository.findById("pax-1")).thenReturn(Optional.of(entity));
            doThrow(new RuntimeException("Seat service unavailable")).when(seatClient).releaseSeat("old-seat");
            doNothing().when(seatClient).confirmSeat("new-seat");
            when(passengerRepository.save(entity)).thenReturn(entity);

            // Should not throw — release failure is a warn only
            assertThatCode(() -> passengerService.assignSeat("pax-1", "new-seat", "14B"))
                    .doesNotThrowAnyException();

            verify(seatClient).confirmSeat("new-seat");
        }

        @Test
        @DisplayName("should throw PassengerNotFoundException when passenger not found")
        void assignSeat_notFound() {
            when(passengerRepository.findById("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> passengerService.assignSeat("bad", "seat-1", "12A"))
                    .isInstanceOf(CustomExceptions.PassengerNotFoundException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  checkIn()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("checkIn()")
    class CheckIn {

        @Test
        @DisplayName("should set isCheckedIn=true when seat is assigned")
        void checkIn_success() {
            PassengerInfo entity = buildEntity("pax-1", "bk-1", false);
            entity.setSeatId("seat-1"); // seat assigned

            when(passengerRepository.findById("pax-1")).thenReturn(Optional.of(entity));
            when(passengerRepository.save(entity)).thenReturn(entity);

            PassengerDTO result = passengerService.checkIn("pax-1");

            assertThat(entity.getIsCheckedIn()).isTrue();
            assertThat(result.getIsCheckedIn()).isTrue();
            verify(passengerRepository).save(entity);
        }

        @Test
        @DisplayName("should throw CheckInNotAllowedException when no seat assigned")
        void checkIn_noSeat_throws() {
            PassengerInfo entity = buildEntity("pax-1", "bk-1", false);
            entity.setSeatId(null); // no seat

            when(passengerRepository.findById("pax-1")).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> passengerService.checkIn("pax-1"))
                    .isInstanceOf(CustomExceptions.CheckInNotAllowedException.class)
                    .hasMessageContaining("Seat must be assigned");

            verify(passengerRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw PassengerNotFoundException when passenger not found")
        void checkIn_notFound() {
            when(passengerRepository.findById("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> passengerService.checkIn("bad"))
                    .isInstanceOf(CustomExceptions.PassengerNotFoundException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  deletePassenger()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deletePassenger()")
    class DeletePassenger {

        @Test
        @DisplayName("should delete passenger when exists")
        void deletePassenger_success() {
            when(passengerRepository.existsById("pax-1")).thenReturn(true);

            passengerService.deletePassenger("pax-1");

            verify(passengerRepository).deleteById("pax-1");
        }

        @Test
        @DisplayName("should throw PassengerNotFoundException when not found")
        void deletePassenger_notFound() {
            when(passengerRepository.existsById("bad")).thenReturn(false);

            assertThatThrownBy(() -> passengerService.deletePassenger("bad"))
                    .isInstanceOf(CustomExceptions.PassengerNotFoundException.class);

            verify(passengerRepository, never()).deleteById(any());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  deletePassengersByBooking()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deletePassengersByBooking()")
    class DeletePassengersByBooking {

        @Test
        @DisplayName("should delegate to repository deleteByBookingId")
        void deletePassengersByBooking_success() {
            doNothing().when(passengerRepository).deleteByBookingId("bk-1");

            passengerService.deletePassengersByBooking("bk-1");

            verify(passengerRepository).deleteByBookingId("bk-1");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  countByBooking()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("countByBooking()")
    class CountByBooking {

        @Test
        @DisplayName("should return passenger count from repository")
        void countByBooking_returnsCount() {
            when(passengerRepository.countByBookingId("bk-1")).thenReturn(3);

            assertThat(passengerService.countByBooking("bk-1")).isEqualTo(3);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  generateTicketNumber()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("generateTicketNumber()")
    class GenerateTicketNumber {

        @Test
        @DisplayName("should return a unique TKT-prefixed ticket number")
        void generateTicketNumber_success() {
            when(passengerRepository.existsByTicketNumber(anyString())).thenReturn(false);

            String ticket = passengerService.generateTicketNumber();

            assertThat(ticket).startsWith("TKT");
        }

        @Test
        @DisplayName("should retry until unique ticket number is found")
        void generateTicketNumber_retriesOnCollision() {
            when(passengerRepository.existsByTicketNumber(anyString()))
                    .thenReturn(true)
                    .thenReturn(true)
                    .thenReturn(false);

            String ticket = passengerService.generateTicketNumber();

            assertThat(ticket).startsWith("TKT");
            verify(passengerRepository, atLeast(3)).existsByTicketNumber(anyString());
        }
    }
}