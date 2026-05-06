package com.airnexus.seat_service.service;

import com.airnexus.seat_service.dto.SeatDTO;
import com.airnexus.seat_service.entity.Seat;
import com.airnexus.seat_service.exception.CustomExceptions;
import com.airnexus.seat_service.repository.SeatRepository;
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
@DisplayName("SeatServiceImpl Tests")
class SeatServiceImplTest {

    @Mock private SeatRepository seatRepository;

    @InjectMocks
    private SeatServiceImpl seatService;

    // ─── Helpers ────────────────────────────────────────────────────────────

    private Seat buildSeat(String id, String flightId, String seatNumber, Seat.SeatStatus status) {
        Seat s = new Seat();
        s.setSeatId(id);
        s.setFlightId(flightId);
        s.setSeatNumber(seatNumber);
        s.setSeatClass(Seat.SeatClass.ECONOMY);
        s.setSeat_row(12);
        s.setSeat_column("A");
        s.setIsWindow(true);
        s.setIsAisle(false);
        s.setHasExtraLegroom(false);
        s.setStatus(status);
        s.setPriceMultiplier(1.0);
        return s;
    }

    private SeatDTO buildSeatDTO(String flightId, String seatNumber) {
        SeatDTO dto = new SeatDTO();
        dto.setFlightId(flightId);
        dto.setSeatNumber(seatNumber);
        dto.setSeatClass("ECONOMY");
        dto.setSeat_row(12);
        dto.setSeat_column("A");
        dto.setIsWindow(true);
        dto.setIsAisle(false);
        dto.setHasExtraLegroom(false);
        dto.setPriceMultiplier(1.0);
        return dto;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  addSeat()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("addSeat()")
    class AddSeat {

        @Test
        @DisplayName("should add seat with AVAILABLE status when seat number is unique for flight")
        void addSeat_success() {
            SeatDTO dto = buildSeatDTO("f-1", "12A");
            Seat saved = buildSeat("s-1", "f-1", "12A", Seat.SeatStatus.AVAILABLE);

            when(seatRepository.findByFlightIdAndSeatNumber("f-1", "12A")).thenReturn(Optional.empty());
            when(seatRepository.save(any(Seat.class))).thenReturn(saved);

            SeatDTO result = seatService.addSeat(dto);

            assertThat(result.getSeatId()).isEqualTo("s-1");
            assertThat(result.getStatus()).isEqualTo("AVAILABLE");
            verify(seatRepository).save(any(Seat.class));
        }

        @Test
        @DisplayName("should throw DuplicateSeatException when seat number already exists for flight")
        void addSeat_duplicate() {
            SeatDTO dto = buildSeatDTO("f-1", "12A");
            when(seatRepository.findByFlightIdAndSeatNumber("f-1", "12A"))
                    .thenReturn(Optional.of(buildSeat("s-1", "f-1", "12A", Seat.SeatStatus.AVAILABLE)));

            assertThatThrownBy(() -> seatService.addSeat(dto))
                    .isInstanceOf(CustomExceptions.DuplicateSeatException.class)
                    .hasMessageContaining("12A");

            verify(seatRepository, never()).save(any());
        }

        @Test
        @DisplayName("should set status to AVAILABLE for every new seat")
        void addSeat_statusIsAvailable() {
            SeatDTO dto = buildSeatDTO("f-1", "14B");
            when(seatRepository.findByFlightIdAndSeatNumber(any(), any())).thenReturn(Optional.empty());
            when(seatRepository.save(any(Seat.class))).thenAnswer(inv -> {
                Seat s = inv.getArgument(0);
                assertThat(s.getStatus()).isEqualTo(Seat.SeatStatus.AVAILABLE);
                return buildSeat("s-2", "f-1", "14B", Seat.SeatStatus.AVAILABLE);
            });

            seatService.addSeat(dto);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  addSeatsForFlight()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("addSeatsForFlight()")
    class AddSeatsForFlight {

        @Test
        @DisplayName("should add all seats and inject flightId into each DTO")
        void addSeatsForFlight_success() {
            List<SeatDTO> dtos = List.of(buildSeatDTO(null, "1A"), buildSeatDTO(null, "1B"));

            when(seatRepository.findByFlightIdAndSeatNumber(eq("f-1"), any())).thenReturn(Optional.empty());
            when(seatRepository.save(any())).thenAnswer(inv -> {
                Seat s = inv.getArgument(0);
                Seat saved = buildSeat("s-" + s.getSeatNumber(), "f-1", s.getSeatNumber(), Seat.SeatStatus.AVAILABLE);
                return saved;
            });

            List<SeatDTO> result = seatService.addSeatsForFlight("f-1", dtos);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(SeatDTO::getFlightId).containsOnly("f-1");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  getSeatById()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getSeatById()")
    class GetSeatById {

        @Test
        @DisplayName("should return SeatDTO when seat exists")
        void getSeatById_found() {
            Seat seat = buildSeat("s-1", "f-1", "12A", Seat.SeatStatus.AVAILABLE);
            when(seatRepository.findById("s-1")).thenReturn(Optional.of(seat));

            SeatDTO result = seatService.getSeatById("s-1");

            assertThat(result.getSeatId()).isEqualTo("s-1");
            assertThat(result.getSeatNumber()).isEqualTo("12A");
        }

        @Test
        @DisplayName("should throw SeatNotFoundException when seat not found")
        void getSeatById_notFound() {
            when(seatRepository.findById("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> seatService.getSeatById("bad"))
                    .isInstanceOf(CustomExceptions.SeatNotFoundException.class)
                    .hasMessageContaining("bad");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  getAvailableSeats()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getAvailableSeats()")
    class GetAvailableSeats {

        @Test
        @DisplayName("should return all AVAILABLE seats for flight")
        void getAvailableSeats_returnsList() {
            List<Seat> available = List.of(
                    buildSeat("s-1", "f-1", "12A", Seat.SeatStatus.AVAILABLE),
                    buildSeat("s-2", "f-1", "12B", Seat.SeatStatus.AVAILABLE)
            );
            when(seatRepository.findAvailableByFlightId("f-1")).thenReturn(available);

            List<SeatDTO> result = seatService.getAvailableSeats("f-1");

            assertThat(result).hasSize(2);
            assertThat(result).extracting(SeatDTO::getStatus).containsOnly("AVAILABLE");
        }

        @Test
        @DisplayName("should return empty list when no available seats")
        void getAvailableSeats_empty() {
            when(seatRepository.findAvailableByFlightId("f-1")).thenReturn(List.of());

            assertThat(seatService.getAvailableSeats("f-1")).isEmpty();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  getSeatMap()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getSeatMap()")
    class GetSeatMap {

        @Test
        @DisplayName("should return all seats for the flight regardless of status")
        void getSeatMap_returnsAll() {
            List<Seat> all = List.of(
                    buildSeat("s-1", "f-1", "1A", Seat.SeatStatus.CONFIRMED),
                    buildSeat("s-2", "f-1", "1B", Seat.SeatStatus.HELD),
                    buildSeat("s-3", "f-1", "1C", Seat.SeatStatus.AVAILABLE)
            );
            when(seatRepository.findByFlightId("f-1")).thenReturn(all);

            List<SeatDTO> result = seatService.getSeatMap("f-1");

            assertThat(result).hasSize(3);
            assertThat(result).extracting(SeatDTO::getStatus)
                    .containsExactlyInAnyOrder("CONFIRMED", "HELD", "AVAILABLE");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  holdSeat()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("holdSeat()")
    class HoldSeat {

        @Test
        @DisplayName("should set status to HELD and record hold time and user")
        void holdSeat_success() {
            Seat seat = buildSeat("s-1", "f-1", "12A", Seat.SeatStatus.AVAILABLE);
            when(seatRepository.findById("s-1")).thenReturn(Optional.of(seat));
            when(seatRepository.save(seat)).thenReturn(seat);

            SeatDTO result = seatService.holdSeat("s-1", "user-1");

            assertThat(seat.getStatus()).isEqualTo(Seat.SeatStatus.HELD);
            assertThat(seat.getHeldByUserId()).isEqualTo("user-1");
            assertThat(seat.getHoldTime()).isNotNull();
            assertThat(result.getStatus()).isEqualTo("HELD");
        }

        @Test
        @DisplayName("should throw SeatNotAvailableException when seat is already HELD")
        void holdSeat_alreadyHeld() {
            Seat seat = buildSeat("s-1", "f-1", "12A", Seat.SeatStatus.HELD);
            when(seatRepository.findById("s-1")).thenReturn(Optional.of(seat));

            assertThatThrownBy(() -> seatService.holdSeat("s-1", "user-2"))
                    .isInstanceOf(CustomExceptions.SeatNotAvailableException.class)
                    .hasMessageContaining("HELD");

            verify(seatRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw SeatNotAvailableException when seat is CONFIRMED")
        void holdSeat_alreadyConfirmed() {
            Seat seat = buildSeat("s-1", "f-1", "12A", Seat.SeatStatus.CONFIRMED);
            when(seatRepository.findById("s-1")).thenReturn(Optional.of(seat));

            assertThatThrownBy(() -> seatService.holdSeat("s-1", "user-1"))
                    .isInstanceOf(CustomExceptions.SeatNotAvailableException.class)
                    .hasMessageContaining("CONFIRMED");
        }

        @Test
        @DisplayName("should throw SeatNotFoundException when seat not found")
        void holdSeat_notFound() {
            when(seatRepository.findById("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> seatService.holdSeat("bad", "user-1"))
                    .isInstanceOf(CustomExceptions.SeatNotFoundException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  releaseSeat()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("releaseSeat()")
    class ReleaseSeat {

        @Test
        @DisplayName("should set status to AVAILABLE and clear hold metadata")
        void releaseSeat_success() {
            Seat seat = buildSeat("s-1", "f-1", "12A", Seat.SeatStatus.HELD);
            seat.setHeldByUserId("user-1");
            seat.setHoldTime(LocalDateTime.now().minusMinutes(5));

            when(seatRepository.findById("s-1")).thenReturn(Optional.of(seat));
            when(seatRepository.save(seat)).thenReturn(seat);

            SeatDTO result = seatService.releaseSeat("s-1");

            assertThat(seat.getStatus()).isEqualTo(Seat.SeatStatus.AVAILABLE);
            assertThat(seat.getHeldByUserId()).isNull();
            assertThat(seat.getHoldTime()).isNull();
            assertThat(result.getStatus()).isEqualTo("AVAILABLE");
        }

        @Test
        @DisplayName("should throw SeatNotFoundException when seat not found")
        void releaseSeat_notFound() {
            when(seatRepository.findById("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> seatService.releaseSeat("bad"))
                    .isInstanceOf(CustomExceptions.SeatNotFoundException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  confirmSeat()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("confirmSeat()")
    class ConfirmSeat {

        @Test
        @DisplayName("should confirm a HELD seat")
        void confirmSeat_fromHeld() {
            Seat seat = buildSeat("s-1", "f-1", "12A", Seat.SeatStatus.HELD);
            when(seatRepository.findById("s-1")).thenReturn(Optional.of(seat));
            when(seatRepository.save(seat)).thenReturn(seat);

            SeatDTO result = seatService.confirmSeat("s-1");

            assertThat(seat.getStatus()).isEqualTo(Seat.SeatStatus.CONFIRMED);
            assertThat(seat.getHoldTime()).isNull();
            assertThat(seat.getHeldByUserId()).isNull();
            assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        }

        @Test
        @DisplayName("should confirm an AVAILABLE seat directly (Feign internal call)")
        void confirmSeat_fromAvailable() {
            Seat seat = buildSeat("s-1", "f-1", "12A", Seat.SeatStatus.AVAILABLE);
            when(seatRepository.findById("s-1")).thenReturn(Optional.of(seat));
            when(seatRepository.save(seat)).thenReturn(seat);

            SeatDTO result = seatService.confirmSeat("s-1");

            assertThat(seat.getStatus()).isEqualTo(Seat.SeatStatus.CONFIRMED);
            assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        }

        @Test
        @DisplayName("should throw InvalidSeatStatusException when seat is already CONFIRMED")
        void confirmSeat_alreadyConfirmed() {
            Seat seat = buildSeat("s-1", "f-1", "12A", Seat.SeatStatus.CONFIRMED);
            when(seatRepository.findById("s-1")).thenReturn(Optional.of(seat));

            assertThatThrownBy(() -> seatService.confirmSeat("s-1"))
                    .isInstanceOf(CustomExceptions.InvalidSeatStatusException.class)
                    .hasMessageContaining("already confirmed");

            verify(seatRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw InvalidSeatStatusException when seat is BLOCKED")
        void confirmSeat_blocked() {
            Seat seat = buildSeat("s-1", "f-1", "12A", Seat.SeatStatus.BLOCKED);
            when(seatRepository.findById("s-1")).thenReturn(Optional.of(seat));

            assertThatThrownBy(() -> seatService.confirmSeat("s-1"))
                    .isInstanceOf(CustomExceptions.InvalidSeatStatusException.class)
                    .hasMessageContaining("blocked");
        }

        @Test
        @DisplayName("should throw SeatNotFoundException when seat not found")
        void confirmSeat_notFound() {
            when(seatRepository.findById("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> seatService.confirmSeat("bad"))
                    .isInstanceOf(CustomExceptions.SeatNotFoundException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  updateSeat()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updateSeat()")
    class UpdateSeat {

        @Test
        @DisplayName("should update mutable fields and return updated DTO")
        void updateSeat_success() {
            Seat existing = buildSeat("s-1", "f-1", "12A", Seat.SeatStatus.AVAILABLE);
            when(seatRepository.findById("s-1")).thenReturn(Optional.of(existing));

            SeatDTO update = buildSeatDTO("f-1", "12A");
            update.setPriceMultiplier(1.5);
            update.setIsWindow(false);
            update.setIsAisle(true);
            update.setHasExtraLegroom(true);

            Seat saved = buildSeat("s-1", "f-1", "12A", Seat.SeatStatus.AVAILABLE);
            saved.setPriceMultiplier(1.5);
            saved.setIsWindow(false);
            saved.setIsAisle(true);
            saved.setHasExtraLegroom(true);
            when(seatRepository.save(existing)).thenReturn(saved);

            SeatDTO result = seatService.updateSeat("s-1", update);

            assertThat(result.getPriceMultiplier()).isEqualTo(1.5);
            assertThat(result.getIsWindow()).isFalse();
            assertThat(result.getIsAisle()).isTrue();
            assertThat(result.getHasExtraLegroom()).isTrue();
            verify(seatRepository).save(existing);
        }

        @Test
        @DisplayName("should throw SeatNotFoundException when seat not found")
        void updateSeat_notFound() {
            when(seatRepository.findById("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> seatService.updateSeat("bad", buildSeatDTO("f-1", "12A")))
                    .isInstanceOf(CustomExceptions.SeatNotFoundException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  countAvailableByClass()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("countAvailableByClass()")
    class CountAvailableByClass {

        @Test
        @DisplayName("should return count from repository")
        void countAvailableByClass_success() {
            when(seatRepository.countAvailableByFlightIdAndClass("f-1", Seat.SeatClass.ECONOMY)).thenReturn(45);

            assertThat(seatService.countAvailableByClass("f-1", Seat.SeatClass.ECONOMY)).isEqualTo(45);
        }

        @Test
        @DisplayName("should return zero when no available seats in class")
        void countAvailableByClass_zero() {
            when(seatRepository.countAvailableByFlightIdAndClass("f-1", Seat.SeatClass.FIRST)).thenReturn(0);

            assertThat(seatService.countAvailableByClass("f-1", Seat.SeatClass.FIRST)).isZero();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  deleteSeatsForFlight()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteSeatsForFlight()")
    class DeleteSeatsForFlight {

        @Test
        @DisplayName("should delegate to repository deleteByFlightId")
        void deleteSeatsForFlight_success() {
            doNothing().when(seatRepository).deleteByFlightId("f-1");

            seatService.deleteSeatsForFlight("f-1");

            verify(seatRepository).deleteByFlightId("f-1");
        }
    }
}