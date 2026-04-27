package com.airnexus.booking_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class BookingRequest {
    private String userId;
    private String flightId;
    private String tripType;
    private String contactEmail;
    private String contactPhone;
    private Integer luggageKg;
    private List<PassengerDetails> passengers;
    private List<String> seatIds;

    @Data
    public static class PassengerDetails {
        private String title;
        private String firstName;
        private String lastName;
        private String dateOfBirth;
        private String gender;
        private String passportNumber;
        private String nationality;
        private String passportExpiry;
        private String mealPreference;
    }
}
