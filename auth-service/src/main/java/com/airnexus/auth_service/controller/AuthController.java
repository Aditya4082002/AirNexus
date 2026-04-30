package com.airnexus.auth_service.controller;

import com.airnexus.auth_service.dto.*;
import com.airnexus.auth_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user registration, login and profile management")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user",
            description = "Creates a new user account. Role can be PASSENGER or AIRLINE_STAFF.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully, returns JWT token"),
            @ApiResponse(responseCode = "400", description = "Email already in use or invalid data")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Login with email and password",
            description = "Authenticates the user and returns a JWT token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful, returns JWT token"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Get current user profile",
            description = "Returns profile details of the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile returned"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "Bearer Auth")
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile(
            @Parameter(description = "User ID injected by API Gateway", hidden = true)
            @RequestHeader("X-User-Id") Integer userId) {
        return ResponseEntity.ok(authService.getUserById(userId));
    }

    @Operation(summary = "Update current user profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "Bearer Auth")
    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(
            @Parameter(description = "User ID injected by API Gateway", hidden = true)
            @RequestHeader("X-User-Id") Integer userId,
            @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(authService.updateProfile(userId, userDTO));
    }

    @Operation(summary = "Change user password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Old password is incorrect"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "Bearer Auth")
    @PutMapping("/password")
    public ResponseEntity<String> changePassword(
            @Parameter(description = "User ID injected by API Gateway", hidden = true)
            @RequestHeader("X-User-Id") Integer userId,
            @Parameter(description = "Current password") @RequestParam String oldPassword,
            @Parameter(description = "New password") @RequestParam String newPassword) {
        authService.changePassword(userId, oldPassword, newPassword);
        return ResponseEntity.ok("Password changed successfully");
    }
}
