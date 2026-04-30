package com.airnexus.auth_service.service;


import com.airnexus.auth_service.dto.AuthResponse;
import com.airnexus.auth_service.entity.User;
import com.airnexus.auth_service.repository.UserRepository;
import com.airnexus.auth_service.util.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${google.client.id}")
    private String googleClientId;

    public AuthResponse authenticateWithGoogle(String idTokenString) {
        try {
            // Verify Google ID Token
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new RuntimeException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            // Check if user exists
            Optional<User> existingUser = userRepository.findByEmail(email);

            User user;
            if (existingUser.isPresent()) {
                user = existingUser.get();
                // Update provider if it was LOCAL before
                if (user.getProvider() == User.Provider.LOCAL) {
                    user.setProvider(User.Provider.GOOGLE);
                    userRepository.save(user);
                }
            } else {
                // Create new user
                user = new User();
                user.setEmail(email);
                user.setFullName(name);
                user.setProvider(User.Provider.GOOGLE);
                user.setRole(User.Role.PASSENGER);
                user.setPasswordHash("GOOGLE_AUTH"); // No password for OAuth users
                user.setIsActive(true);
                user = userRepository.save(user);
            }

            // Generate JWT
            String token = jwtUtil.generateToken(
                    user.getUserId(),
                    user.getEmail(),
                    user.getRole().name(),
                    user.getAirlineId()
            );

            return new AuthResponse(
                    token,
                    user.getUserId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getRole().name()
            );

        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed: " + e.getMessage());
        }
    }
}