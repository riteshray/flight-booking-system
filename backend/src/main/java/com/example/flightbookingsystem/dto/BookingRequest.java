package com.example.flightbookingsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Flight ID is required")
    private Long flightId;
    
    @NotBlank(message = "Passenger first name is required")
    private String passengerFirstName;
    
    @NotBlank(message = "Passenger last name is required")
    private String passengerLastName;
    
    @NotBlank(message = "Passenger email is required")
    @Email(message = "Invalid email format")
    private String passengerEmail;
    
    private String passengerPhoneNumber;
}
