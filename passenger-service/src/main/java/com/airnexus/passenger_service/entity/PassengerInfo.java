package com.airnexus.passenger_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "passengers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassengerInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String passengerId;

    @Column(nullable = false)
    private String bookingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Title title;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    private String passportNumber;

    private String nationality;

    private LocalDate passportExpiry;

    private String seatId;

    private String seatNumber;

    @Column(unique = true, nullable = false)
    private String ticketNumber; // Auto-generated

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PassengerType passengerType;

    @Enumerated(EnumType.STRING)
    private MealPreference mealPreference;

    private Boolean isCheckedIn = false;

    public enum Title {
        MR, MRS, MS, MISS, DR
    }

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public enum PassengerType {
        ADULT, CHILD, INFANT
    }

    public enum MealPreference {
        VEG, NON_VEG, JAIN, VEGAN, NONE
    }
}
