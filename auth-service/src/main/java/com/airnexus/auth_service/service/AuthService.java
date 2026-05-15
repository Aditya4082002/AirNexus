package com.airnexus.auth_service.service;

import com.airnexus.auth_service.dto.*;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserDTO getUserById(Integer userId);
    UserDTO updateProfile(Integer userId, UserDTO userDTO);
    void changePassword(Integer userId, String oldPassword, String newPassword);
}
