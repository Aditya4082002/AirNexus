package com.airnexus.auth_service.controller;

import com.airnexus.auth_service.dto.AuthResponse;
import com.airnexus.auth_service.dto.GoogleTokenRequest;
import com.airnexus.auth_service.service.OAuth2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/google")
@RequiredArgsConstructor
@Tag(name = "Google OAuth2", description = "APIs for Google OAuth2 login")
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;

    @Operation(summary = "Login with Google ID Token",
            description = "Authenticates user using Google OAuth2 ID token. Returns a JWT token on success.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Google login successful, returns JWT token"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired Google token")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleTokenRequest request) {
        return ResponseEntity.ok(oAuth2Service.authenticateWithGoogle(request.getToken()));
    }
}
