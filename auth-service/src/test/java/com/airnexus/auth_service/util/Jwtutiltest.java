package com.airnexus.auth_service.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtUtil Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    // Must be at least 32 chars for HMAC-SHA256
    private static final String SECRET = "airnexus-super-secret-key-123456789";
    private static final Long EXPIRATION = 3_600_000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "EXPIRATION", EXPIRATION);
    }

    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateToken()")
    class GenerateToken {

        @Test
        @DisplayName("should generate a non-null, non-blank JWT string")
        void generateToken_returnsNonBlank() {
            String token = jwtUtil.generateToken(1, "user@test.com", "PASSENGER", null);
            assertThat(token).isNotBlank();
        }

        @Test
        @DisplayName("should embed userId as the JWT subject")
        void generateToken_subjectIsUserId() {
            String token = jwtUtil.generateToken(42, "user@test.com", "PASSENGER", null);
            Claims claims = jwtUtil.extractAllClaims(token);
            assertThat(claims.getSubject()).isEqualTo("42");
        }

        @Test
        @DisplayName("should embed email claim in the token")
        void generateToken_emailClaim() {
            String token = jwtUtil.generateToken(1, "user@test.com", "PASSENGER", null);
            Claims claims = jwtUtil.extractAllClaims(token);
            assertThat(claims.get("email", String.class)).isEqualTo("user@test.com");
        }

        @Test
        @DisplayName("should embed role claim in the token")
        void generateToken_roleClaim() {
            String token = jwtUtil.generateToken(1, "staff@test.com", "AIRLINE_STAFF", "airline-1");
            Claims claims = jwtUtil.extractAllClaims(token);
            assertThat(claims.get("role", String.class)).isEqualTo("AIRLINE_STAFF");
        }

        @Test
        @DisplayName("should embed airlineId claim when provided")
        void generateToken_airlineIdClaim() {
            String token = jwtUtil.generateToken(1, "staff@test.com", "AIRLINE_STAFF", "airline-uuid-1");
            Claims claims = jwtUtil.extractAllClaims(token);
            assertThat(claims.get("airlineId", String.class)).isEqualTo("airline-uuid-1");
        }

        @Test
        @DisplayName("should NOT include airlineId claim when null")
        void generateToken_noAirlineIdWhenNull() {
            String token = jwtUtil.generateToken(1, "user@test.com", "PASSENGER", null);
            Claims claims = jwtUtil.extractAllClaims(token);
            assertThat(claims.get("airlineId")).isNull();
        }

        @Test
        @DisplayName("should set a future expiration date")
        void generateToken_futureExpiration() {
            String token = jwtUtil.generateToken(1, "user@test.com", "PASSENGER", null);
            Claims claims = jwtUtil.extractAllClaims(token);
            assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
        }
    }

    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("extractUserId()")
    class ExtractUserId {

        @Test
        @DisplayName("should extract userId string correctly")
        void extractUserId_correct() {
            String token = jwtUtil.generateToken(99, "a@b.com", "PASSENGER", null);
            assertThat(jwtUtil.extractUserId(token)).isEqualTo("99");
        }
    }

    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("validateToken()")
    class ValidateToken {

        @Test
        @DisplayName("should return true for a valid token")
        void validateToken_valid() {
            String token = jwtUtil.generateToken(1, "user@test.com", "PASSENGER", null);
            assertThat(jwtUtil.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("should return false for a tampered token")
        void validateToken_tampered() {
            String token = jwtUtil.generateToken(1, "user@test.com", "PASSENGER", null);
            String tampered = token.substring(0, token.length() - 5) + "XXXXX";
            assertThat(jwtUtil.validateToken(tampered)).isFalse();
        }

        @Test
        @DisplayName("should return false for a completely invalid string")
        void validateToken_garbage() {
            assertThat(jwtUtil.validateToken("this.is.not.a.jwt")).isFalse();
        }

        @Test
        @DisplayName("should return false for an empty string")
        void validateToken_empty() {
            assertThat(jwtUtil.validateToken("")).isFalse();
        }

        @Test
        @DisplayName("should return false for a token signed with wrong secret")
        void validateToken_wrongSecret() {
            // Generate with a different secret
            JwtUtil other = new JwtUtil();
            ReflectionTestUtils.setField(other, "SECRET_KEY", "completely-different-secret-key-99");
            ReflectionTestUtils.setField(other, "EXPIRATION", EXPIRATION);

            String foreignToken = other.generateToken(1, "x@y.com", "PASSENGER", null);
            assertThat(jwtUtil.validateToken(foreignToken)).isFalse();
        }
    }

    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("extractAllClaims()")
    class ExtractAllClaims {

        @Test
        @DisplayName("should return Claims object with all expected entries")
        void extractAllClaims_containsAll() {
            String token = jwtUtil.generateToken(7, "test@test.com", "ADMIN", "airline-x");
            Claims claims = jwtUtil.extractAllClaims(token);

            assertThat(claims.getSubject()).isEqualTo("7");
            assertThat(claims.get("email")).isEqualTo("test@test.com");
            assertThat(claims.get("role")).isEqualTo("ADMIN");
            assertThat(claims.get("airlineId")).isEqualTo("airline-x");
        }
    }
}