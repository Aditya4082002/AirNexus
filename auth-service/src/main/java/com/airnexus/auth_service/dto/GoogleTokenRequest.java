package com.airnexus.auth_service.dto;
import lombok.Data;

@Data
public class GoogleTokenRequest {
    private String token; // Google ID token from frontend
}