package com.airnexus.api_gateway.config;

import com.airnexus.api_gateway.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class GatewayConfig {

    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // ========== AUTH SERVICE ==========
                // Public routes
                .route("auth-public", r -> r
                        .path("/api/auth/register", "/api/auth/login", "/api/auth/google/**")
                        .uri("lb://auth-service"))

                .route("auth-oauth", r -> r
                        .path("/oauth2/**", "/login/oauth2/**")
                        .uri("lb://auth-service"))

                // Protected routes (Any authenticated user)
                .route("auth-profile", r -> r
                        .path("/api/auth/profile", "/api/auth/password")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://auth-service"))

                // ========== AIRLINE SERVICE ==========
                // Public routes - View airlines/airports
                .route("airline-public", r -> r
                        .path("/api/airlines", "/api/airlines/active", "/api/airlines/iata/**")
                        .and().method("GET")
                        .uri("lb://airline-service"))

                .route("airport-public", r -> r
                        .path("/api/airports", "/api/airports/search", "/api/airports/iata/**", "/api/airports/city/**")
                        .and().method("GET")
                        .uri("lb://airline-service"))

                // Protected - Manage airlines (ADMIN only)
                .route("airline-manage", r -> r
                        .path("/api/airlines/**")
                        .and().method("POST", "PUT", "DELETE")
                        .filters(f -> f.filter(jwtFilter.apply(configWithRoles("ADMIN"))))
                        .uri("lb://airline-service"))

                // Protected - Manage airports (ADMIN only)
                .route("airport-manage", r -> r
                        .path("/api/airports/**")
                        .and().method("POST", "PUT", "DELETE")
                        .filters(f -> f.filter(jwtFilter.apply(configWithRoles("ADMIN"))))
                        .uri("lb://airline-service"))

                // ========== FLIGHT SERVICE ==========
                // Public routes - Search/View flights
                .route("flight-search", r -> r
                        .path("/api/flights/search", "/api/flights/{id}", "/api/flights/number/**")
                        .and().method("GET", "POST")
                        .uri("lb://flight-service"))

                // Protected - Manage flights (AIRLINE_STAFF, ADMIN)
                .route("flight-manage", r -> r
                        .path("/api/flights/**")
                        .and().method("POST", "PUT", "DELETE")
                        .filters(f -> f.filter(jwtFilter.apply(configWithRoles("AIRLINE_STAFF", "ADMIN"))))
                        .uri("lb://flight-service"))

                // ========== SEAT SERVICE ==========
                // View seat map (Any authenticated user)
                .route("seat-view", r -> r
                        .path("/api/seats/flight/{flightId}/map", "/api/seats/flight/{flightId}/available")
                        .and().method("GET")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://seat-service"))

                // Hold/Release seat (PASSENGER only)
                .route("seat-passenger", r -> r
                        .path("/api/seats/{seatId}/hold", "/api/seats/{seatId}/release")
                        .filters(f -> f.filter(jwtFilter.apply(configWithRoles("PASSENGER"))))
                        .uri("lb://seat-service"))

                // Confirm seat (Internal - called by booking service)
                .route("seat-confirm", r -> r
                        .path("/api/seats/{seatId}/confirm")
                        .filters(f -> f.filter(jwtFilter.apply(configWithRoles("PASSENGER", "ADMIN"))))
                        .uri("lb://seat-service"))

                // Manage seats (AIRLINE_STAFF, ADMIN)
                .route("seat-manage", r -> r
                        .path("/api/seats/**")
                        .and().method("POST", "PUT", "DELETE")
                        .filters(f -> f.filter(jwtFilter.apply(configWithRoles("AIRLINE_STAFF", "ADMIN"))))
                        .uri("lb://seat-service"))

                // ========== PASSENGER SERVICE ==========
                // Protected - PASSENGER can manage their own, ADMIN can view all
                .route("passenger-service", r -> r
                        .path("/api/passengers/**")
                        .filters(f -> f.filter(jwtFilter.apply(configWithRoles("PASSENGER", "ADMIN"))))
                        .uri("lb://passenger-service"))

                // ========== BOOKING SERVICE ==========
                // Public - Retrieve by PNR
                .route("booking-pnr", r -> r
                        .path("/api/bookings/pnr/**")
                        .and().method("GET")
                        .uri("lb://booking-service"))

                // Protected - Create/Manage bookings (PASSENGER)
                .route("booking-passenger", r -> r
                        .path("/api/bookings", "/api/bookings/{id}/cancel")
                        .filters(f -> f.filter(jwtFilter.apply(configWithRoles("PASSENGER"))))
                        .uri("lb://booking-service"))

                // Protected - View all bookings (ADMIN)
                .route("booking-admin", r -> r
                        .path("/api/bookings/all")
                        .filters(f -> f.filter(jwtFilter.apply(configWithRoles("ADMIN"))))
                        .uri("lb://booking-service"))

                // Protected - View user's bookings (Any authenticated)
                .route("booking-user", r -> r
                        .path("/api/bookings/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://booking-service"))

                // ========== PAYMENT SERVICE ==========
                // Protected - PASSENGER can make payments, ADMIN can view all
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f.filter(jwtFilter.apply(configWithRoles("PASSENGER", "ADMIN"))))
                        .uri("lb://payment-service"))

                // ========== NOTIFICATION SERVICE ==========
                // Protected - Any authenticated user can view their notifications
                .route("notification-user", r -> r
                        .path("/api/notifications/user/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://notification-service"))

                // Protected - ADMIN can send broadcast notifications
                .route("notification-admin", r -> r
                        .path("/api/notifications/broadcast")
                        .filters(f -> f.filter(jwtFilter.apply(configWithRoles("ADMIN"))))
                        .uri("lb://notification-service"))

                // Protected - Individual notification actions (mark read / delete)
                .route("notification-individual", r -> r
                        .path("/api/notifications/{id}/read", "/api/notifications/{id}")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://notification-service"))

                .build();
    }

    // Helper method to create config with roles
    private JwtAuthenticationFilter.Config configWithRoles(String... roles) {
        JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();
        config.setAllowedRoles(Arrays.asList(roles));
        return config;
    }
}