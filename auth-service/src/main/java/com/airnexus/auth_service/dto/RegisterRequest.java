package com.airnexus.auth_service.dto;

import com.airnexus.auth_service.entity.User;
import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private User.Role role = User.Role.PASSENGER;
    private String airlineId; // Required if role = AIRLINE_STAFF
}