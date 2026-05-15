package com.airnexus.booking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FareSummary {
    private Double baseFare;
    private Double taxes; // GST + Fuel surcharge
    private Double seatCharges;
    private Double mealCharges;
    private Double luggageCharges;
    private Double totalFare;
}