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
public class FlightResponse {
    private Long id;
    private String flightNumber;
    private String originCode;
    private String originName;
    private String originCity;
    private String destinationCode;
    private String destinationName;
    private String destinationCity;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Double price;
    private Integer availableSeats;
}
