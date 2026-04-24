package com.airnexus.passenger_service.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PassengerDTO {
    private String passengerId;
    private String bookingId;
    private String title;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String passportNumber;
    private String nationality;
    private LocalDate passportExpiry;
    private String seatId;
    private String seatNumber;
    private String ticketNumber;
    private String passengerType;
    private String mealPreference;
    private Boolean isCheckedIn;
}
