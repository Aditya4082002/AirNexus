package com.airnexus.auth_service.service;

import com.airnexus.auth_service.dto.*;
import com.airnexus.auth_service.entity.User;
import com.airnexus.auth_service.repository.UserRepository;
import com.airnexus.auth_service.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    // ─── Helpers ────────────────────────────────────────────────────────────

    private User buildUser(Integer id, String email, boolean active) {
        User user = new User();
        user.setUserId(id);
        user.setFullName("John Doe");
        user.setEmail(email);
        user.setPasswordHash("hashed_secret");
        user.setPhone("+91-9999999999");
        user.setRole(User.Role.PASSENGER);
        user.setProvider(User.Provider.LOCAL);
        user.setIsActive(active);
        user.setPassportNumber("P1234567");
        user.setNationality("Indian");
        return user;
    }

    private RegisterRequest buildRegisterRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("John Doe");
        req.setEmail("john@example.com");
        req.setPassword("secret123");
        req.setPhone("+91-9999999999");
        req.setRole(User.Role.PASSENGER);
        return req;
    }

    private LoginRequest buildLoginRequest(String email, String password) {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  register()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("should register user and return AuthResponse with JWT token")
        void register_success() {
            RegisterRequest req = buildRegisterRequest();
            User saved = buildUser(1, "john@example.com", true);

            when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(passwordEncoder.encode("secret123")).thenReturn("hashed_secret");
            when(userRepository.save(any(User.class))).thenReturn(saved);
            when(jwtUtil.generateToken(1, "john@example.com", "PASSENGER", null))
                    .thenReturn("jwt.token.value");

            AuthResponse response = authService.register(req);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt.token.value");
            assertThat(response.getEmail()).isEqualTo("john@example.com");
            assertThat(response.getRole()).isEqualTo("PASSENGER");
            assertThat(response.getUserId()).isEqualTo(1);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("should throw RuntimeException when email already exists")
        void register_duplicateEmail() {
            when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(buildRegisterRequest()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Email already exists");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should encode password before saving")
        void register_encodesPassword() {
            RegisterRequest req = buildRegisterRequest();
            User saved = buildUser(1, "john@example.com", true);

            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(passwordEncoder.encode("secret123")).thenReturn("$2a$hashed");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                assertThat(u.getPasswordHash()).isEqualTo("$2a$hashed");
                return saved;
            });
            when(jwtUtil.generateToken(anyInt(), any(), any(), any())).thenReturn("tok");

            authService.register(req);

            verify(passwordEncoder).encode("secret123");
        }

        @Test
        @DisplayName("should register AIRLINE_STAFF with airlineId")
        void register_airlineStaff() {
            RegisterRequest req = buildRegisterRequest();
            req.setRole(User.Role.AIRLINE_STAFF);
            req.setAirlineId("airline-uuid-1");

            User saved = buildUser(2, "staff@example.com", true);
            saved.setRole(User.Role.AIRLINE_STAFF);
            saved.setAirlineId("airline-uuid-1");
            saved.setEmail("staff@example.com");

            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("hashed");
            when(userRepository.save(any(User.class))).thenReturn(saved);
            when(jwtUtil.generateToken(2, "staff@example.com", "AIRLINE_STAFF", "airline-uuid-1"))
                    .thenReturn("staff-token");

            AuthResponse response = authService.register(req);

            assertThat(response.getToken()).isEqualTo("staff-token");
            assertThat(response.getRole()).isEqualTo("AIRLINE_STAFF");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  login()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("should return AuthResponse when credentials are valid")
        void login_success() {
            User user = buildUser(1, "john@example.com", true);
            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("secret123", "hashed_secret")).thenReturn(true);
            when(jwtUtil.generateToken(1, "john@example.com", "PASSENGER", null))
                    .thenReturn("jwt.token.value");

            AuthResponse response = authService.login(buildLoginRequest("john@example.com", "secret123"));

            assertThat(response.getToken()).isEqualTo("jwt.token.value");
            assertThat(response.getEmail()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("should throw RuntimeException when email not found")
        void login_emailNotFound() {
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(buildLoginRequest("unknown@example.com", "pass")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid credentials");
        }

        @Test
        @DisplayName("should throw RuntimeException when password is wrong")
        void login_wrongPassword() {
            User user = buildUser(1, "john@example.com", true);
            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongpass", "hashed_secret")).thenReturn(false);

            assertThatThrownBy(() -> authService.login(buildLoginRequest("john@example.com", "wrongpass")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid credentials");
        }

        @Test
        @DisplayName("should throw RuntimeException when account is deactivated")
        void login_accountDeactivated() {
            User user = buildUser(1, "john@example.com", false); // inactive
            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("secret123", "hashed_secret")).thenReturn(true);

            assertThatThrownBy(() -> authService.login(buildLoginRequest("john@example.com", "secret123")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("deactivated");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  getUserById()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getUserById()")
    class GetUserById {

        @Test
        @DisplayName("should return UserDTO when user exists")
        void getUserById_found() {
            User user = buildUser(1, "john@example.com", true);
            when(userRepository.findById(1)).thenReturn(Optional.of(user));

            UserDTO result = authService.getUserById(1);

            assertThat(result.getUserId()).isEqualTo(1);
            assertThat(result.getEmail()).isEqualTo("john@example.com");
            assertThat(result.getFullName()).isEqualTo("John Doe");
            assertThat(result.getRole()).isEqualTo("PASSENGER");
            assertThat(result.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("should throw RuntimeException when user not found")
        void getUserById_notFound() {
            when(userRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.getUserById(99))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("should map all UserDTO fields correctly")
        void getUserById_mapsAllFields() {
            User user = buildUser(5, "test@example.com", true);
            user.setPassportNumber("P9876543");
            user.setNationality("Indian");
            user.setAirlineId("airline-1");
            when(userRepository.findById(5)).thenReturn(Optional.of(user));

            UserDTO result = authService.getUserById(5);

            assertThat(result.getPassportNumber()).isEqualTo("P9876543");
            assertThat(result.getNationality()).isEqualTo("Indian");
            assertThat(result.getAirlineId()).isEqualTo("airline-1");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  updateProfile()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updateProfile()")
    class UpdateProfile {

        @Test
        @DisplayName("should update profile fields and return updated UserDTO")
        void updateProfile_success() {
            User existing = buildUser(1, "john@example.com", true);
            when(userRepository.findById(1)).thenReturn(Optional.of(existing));

            UserDTO updateDTO = new UserDTO();
            updateDTO.setFullName("Jane Doe");
            updateDTO.setPhone("+91-8888888888");
            updateDTO.setPassportNumber("P9999999");
            updateDTO.setNationality("British");

            User updated = buildUser(1, "john@example.com", true);
            updated.setFullName("Jane Doe");
            updated.setPhone("+91-8888888888");
            updated.setPassportNumber("P9999999");
            updated.setNationality("British");
            when(userRepository.save(any(User.class))).thenReturn(updated);

            UserDTO result = authService.updateProfile(1, updateDTO);

            assertThat(result.getFullName()).isEqualTo("Jane Doe");
            assertThat(result.getPhone()).isEqualTo("+91-8888888888");
            assertThat(result.getPassportNumber()).isEqualTo("P9999999");
            assertThat(result.getNationality()).isEqualTo("British");
            verify(userRepository).save(existing);
        }

        @Test
        @DisplayName("should throw RuntimeException when user not found")
        void updateProfile_notFound() {
            when(userRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.updateProfile(999, new UserDTO()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  changePassword()
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("changePassword()")
    class ChangePassword {

        @Test
        @DisplayName("should change password when old password is correct")
        void changePassword_success() {
            User user = buildUser(1, "john@example.com", true);
            when(userRepository.findById(1)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("oldpass", "hashed_secret")).thenReturn(true);
            when(passwordEncoder.encode("newpass")).thenReturn("new_hashed");

            authService.changePassword(1, "oldpass", "newpass");

            assertThat(user.getPasswordHash()).isEqualTo("new_hashed");
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("should throw RuntimeException when old password is incorrect")
        void changePassword_wrongOldPassword() {
            User user = buildUser(1, "john@example.com", true);
            when(userRepository.findById(1)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongold", "hashed_secret")).thenReturn(false);

            assertThatThrownBy(() -> authService.changePassword(1, "wrongold", "newpass"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Old password is incorrect");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw RuntimeException when user not found")
        void changePassword_userNotFound() {
            when(userRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.changePassword(999, "old", "new"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");
        }
    }
}