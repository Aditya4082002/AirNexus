package com.airnexus.auth_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.PASSENGER;

    @Enumerated(EnumType.STRING)
    private Provider provider = Provider.LOCAL;

    @Column(nullable = false)
    private Boolean isActive = true;

    private String passportNumber;

    private String nationality;

    private String airlineId; // For AIRLINE_STAFF only

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role {
        GUEST, PASSENGER, AIRLINE_STAFF, ADMIN
    }

    public enum Provider {
        LOCAL, GOOGLE
    }
}