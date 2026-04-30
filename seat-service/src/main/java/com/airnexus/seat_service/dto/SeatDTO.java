package com.airnexus.seat_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SeatDTO {
    private String seatId;
    private String flightId;
    private String seatNumber;
    private String seatClass;
    private Integer seat_row;
    private String seat_column;
    private Boolean isWindow;
    private Boolean isAisle;
    private Boolean hasExtraLegroom;
    private String status;
    private Double priceMultiplier;
    private LocalDateTime holdTime;
    private String heldByUserId;
}