package com.example.flightbookingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    
    private Long id;
    private String bookingReference;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long flightId;
    private String flightNumber;
    private String originCode;
    private String destinationCode;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Double price;
    private Long passengerId;
    private String passengerFirstName;
    private String passengerLastName;
    private String passengerEmail;
    private String passengerPhoneNumber;
    private String status;
    private LocalDateTime bookingDate;
}
