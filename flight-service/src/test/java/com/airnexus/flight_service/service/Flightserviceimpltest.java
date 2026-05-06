package com.airnexus.flight_service.service;

import com.airnexus.flight_service.dto.FlightDTO;
import com.airnexus.flight_service.dto.FlightSearchRequest;
import com.airnexus.flight_service.entity.Flight;
import com.airnexus.flight_service.exception.CustomExceptions;
import com.airnexus.flight_service.repository.FlightRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FlightServiceImpl Tests")
class FlightServiceImplTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private FlightServiceImpl flightService;

    // ─── Helpers ────────────────────────────────────────────────────────────

    private Flight buildFlight(String id, String number, int totalSeats, int availableSeats) {
        Flight f = new Flight();
        f.setFlightId(id);
        f.setFlightNumber(number);
        f.setAirlineId("airline-1");
        f.setOriginAirportCode("DEL");
        f.setDestinationAirportCode("BOM");
        f.setDepartureTime(LocalDateTime.of(2026, 6, 1, 10, 0));
        f.setArrivalTime(LocalDateTime.of(2026, 6, 1, 12, 0));
        f.setDurationMinutes(120);
        f.setStatus(Flight.FlightStatus.ON_TIME);
        f.setAircraftType("Boeing 737");
        f.setTotalSeats(totalSeats);
        f.setAvailableSeats(availableSeats);
        f.setBasePrice(5000.0);
        return f;
    }

    private FlightDTO buildFlightDTO(String number) {
        FlightDTO dto = new FlightDTO();
        dto.setFlightNumber(number);
        dto.setAirlineId("airline-1");
        dto.setOriginAirportCode("DEL");
        dto.setDestinationAirportCode("BOM");
        dto.setDepartureTime(LocalDateTime.of(2026, 6, 1, 10, 0));
        dto.setArrivalTime(LocalDateTime.of(2026, 6, 1, 12, 0));
        dto.setDurationMinutes(120);
        dto.setAircraftType("Boeing 737");
        dto.setTotalSeats(150);
        dto.setBasePrice(5000.0);
        return dto;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  addFlight()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("addFlight()")
    class AddFlight {

        @Test
        @DisplayName("should add flight and return DTO when flight number is unique")
        void addFlight_success() {
            FlightDTO dto = buildFlightDTO("AI-202");
            Flight saved = buildFlight("f-1", "AI-202", 150, 150);

            when(flightRepository.findByFlightNumber("AI-202")).thenReturn(Optional.empty());
            when(flightRepository.save(any(Flight.class))).thenReturn(saved);

            FlightDTO result = flightService.addFlight(dto);

            assertThat(result.getFlightId()).isEqualTo("f-1");
            assertThat(result.getFlightNumber()).isEqualTo("AI-202");
            assertThat(result.getAvailableSeats()).isEqualTo(150); // equals totalSeats on creation
            assertThat(result.getStatus()).isEqualTo("ON_TIME");
            verify(flightRepository).save(any(Flight.class));
        }

        @Test
        @DisplayName("should set availableSeats = totalSeats on creation")
        void addFlight_setsAvailableSeatsToTotal() {
            FlightDTO dto = buildFlightDTO("6E-100");
            dto.setTotalSeats(200);

            when(flightRepository.findByFlightNumber("6E-100")).thenReturn(Optional.empty());
            when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> {
                Flight f = inv.getArgument(0);
                assertThat(f.getAvailableSeats()).isEqualTo(200);
                return buildFlight("f-2", "6E-100", 200, 200);
            });

            flightService.addFlight(dto);
        }

        @Test
        @DisplayName("should throw DuplicateFlightNumberException when flight number exists")
        void addFlight_duplicateNumber() {
            FlightDTO dto = buildFlightDTO("AI-202");
            when(flightRepository.findByFlightNumber("AI-202"))
                    .thenReturn(Optional.of(buildFlight("f-1", "AI-202", 150, 150)));

            assertThatThrownBy(() -> flightService.addFlight(dto))
                    .isInstanceOf(CustomExceptions.DuplicateFlightNumberException.class)
                    .hasMessageContaining("AI-202");

            verify(flightRepository, never()).save(any());
        }

        @Test
        @DisplayName("should set status to ON_TIME for every new flight")
        void addFlight_statusIsOnTime() {
            FlightDTO dto = buildFlightDTO("SG-300");
            when(flightRepository.findByFlightNumber("SG-300")).thenReturn(Optional.empty());
            when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> {
                Flight f = inv.getArgument(0);
                assertThat(f.getStatus()).isEqualTo(Flight.FlightStatus.ON_TIME);
                return buildFlight("f-3", "SG-300", 150, 150);
            });

            flightService.addFlight(dto);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  getFlightById()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getFlightById()")
    class GetFlightById {

        @Test
        @DisplayName("should return FlightDTO when flight exists")
        void getFlightById_found() {
            Flight flight = buildFlight("f-1", "AI-202", 150, 100);
            when(flightRepository.findById("f-1")).thenReturn(Optional.of(flight));

            FlightDTO result = flightService.getFlightById("f-1");

            assertThat(result.getFlightId()).isEqualTo("f-1");
            assertThat(result.getFlightNumber()).isEqualTo("AI-202");
            assertThat(result.getAvailableSeats()).isEqualTo(100);
        }

        @Test
        @DisplayName("should throw FlightNotFoundException when flight not found")
        void getFlightById_notFound() {
            when(flightRepository.findById("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> flightService.getFlightById("bad"))
                    .isInstanceOf(CustomExceptions.FlightNotFoundException.class)
                    .hasMessageContaining("bad");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  getFlightByNumber()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getFlightByNumber()")
    class GetFlightByNumber {

        @Test
        @DisplayName("should return FlightDTO when flight number matches")
        void getFlightByNumber_found() {
            when(flightRepository.findByFlightNumber("AI-202"))
                    .thenReturn(Optional.of(buildFlight("f-1", "AI-202", 150, 100)));

            FlightDTO result = flightService.getFlightByNumber("AI-202");

            assertThat(result.getFlightNumber()).isEqualTo("AI-202");
        }

        @Test
        @DisplayName("should throw FlightNotFoundException for unknown flight number")
        void getFlightByNumber_notFound() {
            when(flightRepository.findByFlightNumber("XX-999")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> flightService.getFlightByNumber("XX-999"))
                    .isInstanceOf(CustomExceptions.FlightNotFoundException.class)
                    .hasMessageContaining("XX-999");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  searchFlights()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("searchFlights()")
    class SearchFlights {

        @Test
        @DisplayName("should return matching flights when all fields provided")
        void searchFlights_success() {
            FlightSearchRequest req = new FlightSearchRequest();
            req.setOrigin("DEL");
            req.setDestination("BOM");
            req.setDepartureDate(LocalDate.of(2026, 6, 1));
            req.setPassengers(2);

            List<Flight> flights = List.of(buildFlight("f-1", "AI-202", 150, 100));
            when(flightRepository.searchFlights(eq("DEL"), eq("BOM"), any(LocalDateTime.class), eq(2)))
                    .thenReturn(flights);

            List<FlightDTO> result = flightService.searchFlights(req);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getOriginAirportCode()).isEqualTo("DEL");
            assertThat(result.get(0).getDestinationAirportCode()).isEqualTo("BOM");
        }

        @Test
        @DisplayName("should return empty list when no flights match")
        void searchFlights_noResults() {
            FlightSearchRequest req = new FlightSearchRequest();
            req.setOrigin("DEL");
            req.setDestination("BOM");
            req.setDepartureDate(LocalDate.of(2026, 6, 1));
            req.setPassengers(1);

            when(flightRepository.searchFlights(any(), any(), any(), any())).thenReturn(List.of());

            assertThat(flightService.searchFlights(req)).isEmpty();
        }

        @Test
        @DisplayName("should throw FlightSearchException when origin is null")
        void searchFlights_missingOrigin() {
            FlightSearchRequest req = new FlightSearchRequest();
            req.setDestination("BOM");
            req.setDepartureDate(LocalDate.of(2026, 6, 1));

            assertThatThrownBy(() -> flightService.searchFlights(req))
                    .isInstanceOf(CustomExceptions.FlightSearchException.class)
                    .hasMessageContaining("required");
        }

        @Test
        @DisplayName("should throw FlightSearchException when departure date is null")
        void searchFlights_missingDate() {
            FlightSearchRequest req = new FlightSearchRequest();
            req.setOrigin("DEL");
            req.setDestination("BOM");

            assertThatThrownBy(() -> flightService.searchFlights(req))
                    .isInstanceOf(CustomExceptions.FlightSearchException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  getFlightsByAirline()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getFlightsByAirline()")
    class GetFlightsByAirline {

        @Test
        @DisplayName("should return all flights for given airline")
        void getFlightsByAirline_returnsList() {
            List<Flight> flights = List.of(
                    buildFlight("f-1", "AI-101", 150, 100),
                    buildFlight("f-2", "AI-102", 180, 50)
            );
            when(flightRepository.findByAirlineId("airline-1")).thenReturn(flights);

            List<FlightDTO> result = flightService.getFlightsByAirline("airline-1");

            assertThat(result).hasSize(2);
            assertThat(result).extracting(FlightDTO::getAirlineId)
                    .containsOnly("airline-1");
        }

        @Test
        @DisplayName("should return empty list when airline has no flights")
        void getFlightsByAirline_empty() {
            when(flightRepository.findByAirlineId("airline-99")).thenReturn(List.of());

            assertThat(flightService.getFlightsByAirline("airline-99")).isEmpty();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  updateFlight()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updateFlight()")
    class UpdateFlight {

        @Test
        @DisplayName("should update mutable fields and return updated DTO")
        void updateFlight_success() {
            Flight existing = buildFlight("f-1", "AI-202", 150, 100);
            when(flightRepository.findById("f-1")).thenReturn(Optional.of(existing));

            FlightDTO update = buildFlightDTO("AI-202");
            update.setDepartureTime(LocalDateTime.of(2026, 6, 1, 11, 0));
            update.setArrivalTime(LocalDateTime.of(2026, 6, 1, 13, 30));
            update.setDurationMinutes(150);
            update.setBasePrice(6000.0);

            Flight saved = buildFlight("f-1", "AI-202", 150, 100);
            saved.setDepartureTime(update.getDepartureTime());
            saved.setArrivalTime(update.getArrivalTime());
            saved.setDurationMinutes(150);
            saved.setBasePrice(6000.0);
            when(flightRepository.save(existing)).thenReturn(saved);

            FlightDTO result = flightService.updateFlight("f-1", update);

            assertThat(result.getDurationMinutes()).isEqualTo(150);
            assertThat(result.getBasePrice()).isEqualTo(6000.0);
            verify(flightRepository).save(existing);
        }

        @Test
        @DisplayName("should throw FlightNotFoundException when flight not found")
        void updateFlight_notFound() {
            when(flightRepository.findById("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> flightService.updateFlight("bad", buildFlightDTO("AI-202")))
                    .isInstanceOf(CustomExceptions.FlightNotFoundException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  updateFlightStatus()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updateFlightStatus()")
    class UpdateFlightStatus {

        @Test
        @DisplayName("should update status and return updated DTO")
        void updateFlightStatus_success() {
            Flight flight = buildFlight("f-1", "AI-202", 150, 100);
            when(flightRepository.findById("f-1")).thenReturn(Optional.of(flight));

            Flight saved = buildFlight("f-1", "AI-202", 150, 100);
            saved.setStatus(Flight.FlightStatus.DELAYED);
            when(flightRepository.save(flight)).thenReturn(saved);

            FlightDTO result = flightService.updateFlightStatus("f-1", Flight.FlightStatus.DELAYED);

            assertThat(result.getStatus()).isEqualTo("DELAYED");
            verify(flightRepository).save(flight);
        }

        @Test
        @DisplayName("should throw FlightNotFoundException when flight not found")
        void updateFlightStatus_notFound() {
            when(flightRepository.findById("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> flightService.updateFlightStatus("bad", Flight.FlightStatus.CANCELLED))
                    .isInstanceOf(CustomExceptions.FlightNotFoundException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  decrementSeats()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("decrementSeats()")
    class DecrementSeats {

        @Test
        @DisplayName("should reduce available seats by given count")
        void decrementSeats_success() {
            Flight flight = buildFlight("f-1", "AI-202", 150, 50);
            when(flightRepository.findById("f-1")).thenReturn(Optional.of(flight));
            when(flightRepository.save(flight)).thenReturn(flight);

            flightService.decrementSeats("f-1", 10);

            assertThat(flight.getAvailableSeats()).isEqualTo(40);
            verify(flightRepository).save(flight);
        }

        @Test
        @DisplayName("should throw InsufficientSeatsException when not enough seats")
        void decrementSeats_insufficient() {
            Flight flight = buildFlight("f-1", "AI-202", 150, 5);
            when(flightRepository.findById("f-1")).thenReturn(Optional.of(flight));

            assertThatThrownBy(() -> flightService.decrementSeats("f-1", 10))
                    .isInstanceOf(CustomExceptions.InsufficientSeatsException.class)
                    .hasMessageContaining("5");

            verify(flightRepository, never()).save(any());
        }

        @Test
        @DisplayName("should succeed when decrementing exactly all available seats")
        void decrementSeats_exactMatch() {
            Flight flight = buildFlight("f-1", "AI-202", 150, 10);
            when(flightRepository.findById("f-1")).thenReturn(Optional.of(flight));
            when(flightRepository.save(flight)).thenReturn(flight);

            flightService.decrementSeats("f-1", 10);

            assertThat(flight.getAvailableSeats()).isZero();
        }

        @Test
        @DisplayName("should throw FlightNotFoundException when flight not found")
        void decrementSeats_flightNotFound() {
            when(flightRepository.findById("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> flightService.decrementSeats("bad", 2))
                    .isInstanceOf(CustomExceptions.FlightNotFoundException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  incrementSeats()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("incrementSeats()")
    class IncrementSeats {

        @Test
        @DisplayName("should increase available seats by given count")
        void incrementSeats_success() {
            Flight flight = buildFlight("f-1", "AI-202", 150, 40);
            when(flightRepository.findById("f-1")).thenReturn(Optional.of(flight));
            when(flightRepository.save(flight)).thenReturn(flight);

            flightService.incrementSeats("f-1", 5);

            assertThat(flight.getAvailableSeats()).isEqualTo(45);
            verify(flightRepository).save(flight);
        }

        @Test
        @DisplayName("should throw FlightNotFoundException when flight not found")
        void incrementSeats_flightNotFound() {
            when(flightRepository.findById("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> flightService.incrementSeats("bad", 3))
                    .isInstanceOf(CustomExceptions.FlightNotFoundException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  deleteFlight()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteFlight()")
    class DeleteFlight {

        @Test
        @DisplayName("should delete flight when it exists")
        void deleteFlight_success() {
            when(flightRepository.existsById("f-1")).thenReturn(true);

            flightService.deleteFlight("f-1");

            verify(flightRepository).deleteById("f-1");
        }

        @Test
        @DisplayName("should throw FlightNotFoundException when flight does not exist")
        void deleteFlight_notFound() {
            when(flightRepository.existsById("bad")).thenReturn(false);

            assertThatThrownBy(() -> flightService.deleteFlight("bad"))
                    .isInstanceOf(CustomExceptions.FlightNotFoundException.class)
                    .hasMessageContaining("bad");

            verify(flightRepository, never()).deleteById(any());
        }
    }
}