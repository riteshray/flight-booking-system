package com.example.flightbookingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchRequest {
    
    @NotBlank(message = "Origin airport code is required")
    private String originCode;
    
    @NotBlank(message = "Destination airport code is required")
    private String destinationCode;
    
    @NotNull(message = "Departure date is required")
    private LocalDate departureDate;
}
