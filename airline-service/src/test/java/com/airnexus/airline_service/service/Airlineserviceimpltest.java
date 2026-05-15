package com.airnexus.airline_service.service;

import com.airnexus.airline_service.dto.AirlineDTO;
import com.airnexus.airline_service.dto.AirportDTO;
import com.airnexus.airline_service.entity.Airline;
import com.airnexus.airline_service.entity.Airport;
import com.airnexus.airline_service.exception.CustomExceptions;
import com.airnexus.airline_service.repository.AirlineRepository;
import com.airnexus.airline_service.repository.AirportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AirlineServiceImpl Tests")
class AirlineServiceImplTest {

    @Mock
    private AirlineRepository airlineRepository;

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private AirlineServiceImpl airlineService;

    // ─── Helpers ────────────────────────────────────────────────────────────

    private Airline buildAirline(String id, String iata, boolean active) {
        Airline a = new Airline();
        a.setAirlineId(id);
        a.setName("Test Airline");
        a.setIataCode(iata);
        a.setIcaoCode("TEST");
        a.setLogoUrl("http://logo.png");
        a.setCountry("IN");
        a.setContactEmail("info@test.com");
        a.setContactPhone("+91-0000000000");
        a.setIsActive(active);
        return a;
    }

    private AirlineDTO buildAirlineDTO(String iata) {
        AirlineDTO dto = new AirlineDTO();
        dto.setName("Test Airline");
        dto.setIataCode(iata);
        dto.setIcaoCode("TEST");
        dto.setLogoUrl("http://logo.png");
        dto.setCountry("IN");
        dto.setContactEmail("info@test.com");
        dto.setContactPhone("+91-0000000000");
        return dto;
    }

    private Airport buildAirport(String id, String iata) {
        Airport ap = new Airport();
        ap.setAirportId(id);
        ap.setName("Test Airport");
        ap.setIataCode(iata);
        ap.setIcaoCode("VTES");
        ap.setCity("Delhi");
        ap.setCountry("IN");
        ap.setLatitude(28.6139);
        ap.setLongitude(77.2090);
        ap.setTimezone("Asia/Kolkata");
        return ap;
    }

    private AirportDTO buildAirportDTO(String iata) {
        AirportDTO dto = new AirportDTO();
        dto.setName("Test Airport");
        dto.setIataCode(iata);
        dto.setIcaoCode("VTES");
        dto.setCity("Delhi");
        dto.setCountry("IN");
        dto.setLatitude(28.6139);
        dto.setLongitude(77.2090);
        dto.setTimezone("Asia/Kolkata");
        return dto;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  AIRLINE OPERATIONS
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("createAirline()")
    class CreateAirline {

        @Test
        @DisplayName("should create airline and return DTO when IATA code is unique")
        void createAirline_success() {
            AirlineDTO dto = buildAirlineDTO("AI");
            Airline saved = buildAirline("uuid-1", "AI", true);

            when(airlineRepository.existsByIataCode("AI")).thenReturn(false);
            when(airlineRepository.save(any(Airline.class))).thenReturn(saved);

            AirlineDTO result = airlineService.createAirline(dto);

            assertThat(result).isNotNull();
            assertThat(result.getAirlineId()).isEqualTo("uuid-1");
            assertThat(result.getIataCode()).isEqualTo("AI");
            assertThat(result.getIsActive()).isTrue();
            verify(airlineRepository).save(any(Airline.class));
        }

        @Test
        @DisplayName("should throw DuplicateIataCodeException when IATA code already exists")
        void createAirline_duplicateIata() {
            AirlineDTO dto = buildAirlineDTO("AI");
            when(airlineRepository.existsByIataCode("AI")).thenReturn(true);

            assertThatThrownBy(() -> airlineService.createAirline(dto))
                    .isInstanceOf(CustomExceptions.DuplicateIataCodeException.class)
                    .hasMessageContaining("AI");

            verify(airlineRepository, never()).save(any());
        }

        @Test
        @DisplayName("should set isActive=true for every new airline")
        void createAirline_alwaysActive() {
            AirlineDTO dto = buildAirlineDTO("6E");
            Airline saved = buildAirline("uuid-2", "6E", true);

            when(airlineRepository.existsByIataCode("6E")).thenReturn(false);
            when(airlineRepository.save(any(Airline.class))).thenAnswer(inv -> {
                Airline a = inv.getArgument(0);
                assertThat(a.getIsActive()).isTrue(); // assert before save
                return saved;
            });

            airlineService.createAirline(dto);
        }
    }

    @Nested
    @DisplayName("getAirlineById()")
    class GetAirlineById {

        @Test
        @DisplayName("should return DTO when airline exists")
        void getAirlineById_found() {
            Airline airline = buildAirline("uuid-1", "AI", true);
            when(airlineRepository.findById("uuid-1")).thenReturn(Optional.of(airline));

            AirlineDTO result = airlineService.getAirlineById("uuid-1");

            assertThat(result.getAirlineId()).isEqualTo("uuid-1");
            assertThat(result.getName()).isEqualTo("Test Airline");
        }

        @Test
        @DisplayName("should throw AirlineNotFoundException when airline does not exist")
        void getAirlineById_notFound() {
            when(airlineRepository.findById("bad-id")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> airlineService.getAirlineById("bad-id"))
                    .isInstanceOf(CustomExceptions.AirlineNotFoundException.class)
                    .hasMessageContaining("bad-id");
        }
    }

    @Nested
    @DisplayName("getAirlineByIata()")
    class GetAirlineByIata {

        @Test
        @DisplayName("should return DTO when IATA code matches")
        void getAirlineByIata_found() {
            Airline airline = buildAirline("uuid-1", "AI", true);
            when(airlineRepository.findByIataCode("AI")).thenReturn(Optional.of(airline));

            AirlineDTO result = airlineService.getAirlineByIata("AI");

            assertThat(result.getIataCode()).isEqualTo("AI");
        }

        @Test
        @DisplayName("should throw AirlineNotFoundException for unknown IATA code")
        void getAirlineByIata_notFound() {
            when(airlineRepository.findByIataCode("XX")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> airlineService.getAirlineByIata("XX"))
                    .isInstanceOf(CustomExceptions.AirlineNotFoundException.class)
                    .hasMessageContaining("XX");
        }
    }

    @Nested
    @DisplayName("getAllAirlines()")
    class GetAllAirlines {

        @Test
        @DisplayName("should return mapped list of all airlines")
        void getAllAirlines_returnsList() {
            List<Airline> airlines = List.of(
                    buildAirline("id-1", "AI", true),
                    buildAirline("id-2", "6E", false)
            );
            when(airlineRepository.findAll()).thenReturn(airlines);

            List<AirlineDTO> result = airlineService.getAllAirlines();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(AirlineDTO::getIataCode)
                    .containsExactlyInAnyOrder("AI", "6E");
        }

        @Test
        @DisplayName("should return empty list when no airlines exist")
        void getAllAirlines_empty() {
            when(airlineRepository.findAll()).thenReturn(List.of());

            assertThat(airlineService.getAllAirlines()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getActiveAirlines()")
    class GetActiveAirlines {

        @Test
        @DisplayName("should return only active airlines")
        void getActiveAirlines_returnsOnlyActive() {
            List<Airline> active = List.of(buildAirline("id-1", "AI", true));
            when(airlineRepository.findByIsActive(true)).thenReturn(active);

            List<AirlineDTO> result = airlineService.getActiveAirlines();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIsActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("updateAirline()")
    class UpdateAirline {

        @Test
        @DisplayName("should update mutable fields and return updated DTO")
        void updateAirline_success() {
            Airline existing = buildAirline("uuid-1", "AI", true);
            when(airlineRepository.findById("uuid-1")).thenReturn(Optional.of(existing));

            AirlineDTO updateDTO = buildAirlineDTO("AI");
            updateDTO.setName("Air India Updated");
            updateDTO.setCountry("US");

            Airline updated = buildAirline("uuid-1", "AI", true);
            updated.setName("Air India Updated");
            updated.setCountry("US");
            when(airlineRepository.save(any(Airline.class))).thenReturn(updated);

            AirlineDTO result = airlineService.updateAirline("uuid-1", updateDTO);

            assertThat(result.getName()).isEqualTo("Air India Updated");
            assertThat(result.getCountry()).isEqualTo("US");
            verify(airlineRepository).save(existing);
        }

        @Test
        @DisplayName("should throw AirlineNotFoundException when airline not found")
        void updateAirline_notFound() {
            when(airlineRepository.findById("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> airlineService.updateAirline("bad", buildAirlineDTO("AI")))
                    .isInstanceOf(CustomExceptions.AirlineNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deactivateAirline()")
    class DeactivateAirline {

        @Test
        @DisplayName("should set isActive=false and save")
        void deactivateAirline_success() {
            Airline airline = buildAirline("uuid-1", "AI", true);
            when(airlineRepository.findById("uuid-1")).thenReturn(Optional.of(airline));
            when(airlineRepository.save(any(Airline.class))).thenReturn(airline);

            airlineService.deactivateAirline("uuid-1");

            assertThat(airline.getIsActive()).isFalse();
            verify(airlineRepository).save(airline);
        }

        @Test
        @DisplayName("should throw AirlineNotFoundException when airline not found")
        void deactivateAirline_notFound() {
            when(airlineRepository.findById("nope")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> airlineService.deactivateAirline("nope"))
                    .isInstanceOf(CustomExceptions.AirlineNotFoundException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  AIRPORT OPERATIONS
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("createAirport()")
    class CreateAirport {

        @Test
        @DisplayName("should create airport and return DTO when IATA code is unique")
        void createAirport_success() {
            AirportDTO dto = buildAirportDTO("DEL");
            Airport saved = buildAirport("ap-uuid-1", "DEL");

            when(airportRepository.existsByIataCode("DEL")).thenReturn(false);
            when(airportRepository.save(any(Airport.class))).thenReturn(saved);

            AirportDTO result = airlineService.createAirport(dto);

            assertThat(result).isNotNull();
            assertThat(result.getAirportId()).isEqualTo("ap-uuid-1");
            assertThat(result.getIataCode()).isEqualTo("DEL");
            verify(airportRepository).save(any(Airport.class));
        }

        @Test
        @DisplayName("should throw DuplicateIataCodeException when airport IATA code already exists")
        void createAirport_duplicateIata() {
            AirportDTO dto = buildAirportDTO("DEL");
            when(airportRepository.existsByIataCode("DEL")).thenReturn(true);

            assertThatThrownBy(() -> airlineService.createAirport(dto))
                    .isInstanceOf(CustomExceptions.DuplicateIataCodeException.class)
                    .hasMessageContaining("DEL");

            verify(airportRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAirportById()")
    class GetAirportById {

        @Test
        @DisplayName("should return DTO when airport exists")
        void getAirportById_found() {
            Airport airport = buildAirport("ap-1", "DEL");
            when(airportRepository.findById("ap-1")).thenReturn(Optional.of(airport));

            AirportDTO result = airlineService.getAirportById("ap-1");

            assertThat(result.getAirportId()).isEqualTo("ap-1");
            assertThat(result.getCity()).isEqualTo("Delhi");
        }

        @Test
        @DisplayName("should throw AirportNotFoundException when airport not found")
        void getAirportById_notFound() {
            when(airportRepository.findById("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> airlineService.getAirportById("bad"))
                    .isInstanceOf(CustomExceptions.AirportNotFoundException.class)
                    .hasMessageContaining("bad");
        }
    }

    @Nested
    @DisplayName("getAirportByIata()")
    class GetAirportByIata {

        @Test
        @DisplayName("should return DTO when IATA code matches")
        void getAirportByIata_found() {
            Airport airport = buildAirport("ap-1", "DEL");
            when(airportRepository.findByIataCode("DEL")).thenReturn(Optional.of(airport));

            AirportDTO result = airlineService.getAirportByIata("DEL");

            assertThat(result.getIataCode()).isEqualTo("DEL");
        }

        @Test
        @DisplayName("should throw AirportNotFoundException for unknown IATA code")
        void getAirportByIata_notFound() {
            when(airportRepository.findByIataCode("XYZ")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> airlineService.getAirportByIata("XYZ"))
                    .isInstanceOf(CustomExceptions.AirportNotFoundException.class)
                    .hasMessageContaining("XYZ");
        }
    }

    @Nested
    @DisplayName("getAllAirports()")
    class GetAllAirports {

        @Test
        @DisplayName("should return mapped list of all airports")
        void getAllAirports_returnsList() {
            List<Airport> airports = List.of(
                    buildAirport("ap-1", "DEL"),
                    buildAirport("ap-2", "BOM")
            );
            when(airportRepository.findAll()).thenReturn(airports);

            List<AirportDTO> result = airlineService.getAllAirports();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(AirportDTO::getIataCode)
                    .containsExactlyInAnyOrder("DEL", "BOM");
        }

        @Test
        @DisplayName("should return empty list when no airports exist")
        void getAllAirports_empty() {
            when(airportRepository.findAll()).thenReturn(List.of());

            assertThat(airlineService.getAllAirports()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAirportsByCity()")
    class GetAirportsByCity {

        @Test
        @DisplayName("should return airports filtered by city")
        void getAirportsByCity_found() {
            List<Airport> airports = List.of(buildAirport("ap-1", "DEL"));
            when(airportRepository.findByCity("Delhi")).thenReturn(airports);

            List<AirportDTO> result = airlineService.getAirportsByCity("Delhi");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCity()).isEqualTo("Delhi");
        }

        @Test
        @DisplayName("should return empty list when no airports in city")
        void getAirportsByCity_empty() {
            when(airportRepository.findByCity("Unknown City")).thenReturn(List.of());

            assertThat(airlineService.getAirportsByCity("Unknown City")).isEmpty();
        }
    }

    @Nested
    @DisplayName("searchAirports()")
    class SearchAirports {

        @Test
        @DisplayName("should return airports matching the search query")
        void searchAirports_found() {
            List<Airport> airports = List.of(buildAirport("ap-1", "DEL"));
            when(airportRepository.searchAirports("del")).thenReturn(airports);

            List<AirportDTO> result = airlineService.searchAirports("del");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIataCode()).isEqualTo("DEL");
        }

        @Test
        @DisplayName("should return empty list when no airports match search query")
        void searchAirports_noMatch() {
            when(airportRepository.searchAirports("zzz")).thenReturn(List.of());

            assertThat(airlineService.searchAirports("zzz")).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateAirport()")
    class UpdateAirport {

        @Test
        @DisplayName("should update airport fields and return updated DTO")
        void updateAirport_success() {
            Airport existing = buildAirport("ap-1", "DEL");
            when(airportRepository.findById("ap-1")).thenReturn(Optional.of(existing));

            AirportDTO updateDTO = buildAirportDTO("DEL");
            updateDTO.setCity("New Delhi");
            updateDTO.setTimezone("UTC+5:30");

            Airport updated = buildAirport("ap-1", "DEL");
            updated.setCity("New Delhi");
            updated.setTimezone("UTC+5:30");
            when(airportRepository.save(any(Airport.class))).thenReturn(updated);

            AirportDTO result = airlineService.updateAirport("ap-1", updateDTO);

            assertThat(result.getCity()).isEqualTo("New Delhi");
            assertThat(result.getTimezone()).isEqualTo("UTC+5:30");
            verify(airportRepository).save(existing);
        }

        @Test
        @DisplayName("should throw AirportNotFoundException when airport not found")
        void updateAirport_notFound() {
            when(airportRepository.findById("bad")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> airlineService.updateAirport("bad", buildAirportDTO("DEL")))
                    .isInstanceOf(CustomExceptions.AirportNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteAirport()")
    class DeleteAirport {

        @Test
        @DisplayName("should delete airport when it exists")
        void deleteAirport_success() {
            when(airportRepository.existsById("ap-1")).thenReturn(true);

            airlineService.deleteAirport("ap-1");

            verify(airportRepository).deleteById("ap-1");
        }

        @Test
        @DisplayName("should throw AirportNotFoundException when airport not found")
        void deleteAirport_notFound() {
            when(airportRepository.existsById("nope")).thenReturn(false);

            assertThatThrownBy(() -> airlineService.deleteAirport("nope"))
                    .isInstanceOf(CustomExceptions.AirportNotFoundException.class)
                    .hasMessageContaining("nope");

            verify(airportRepository, never()).deleteById(any());
        }
    }
}