package com.airnexus.auth_service.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Integer userId;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String passportNumber;
    private String nationality;
    private String airlineId;
    private Boolean isActive;
}
