package com.example.flightbookingsystem.service;

import com.example.flightbookingsystem.dto.FlightResponse;
import com.example.flightbookingsystem.dto.FlightSearchRequest;

import java.util.List;

public interface FlightSearchService {
    
    /**
     * Search for flights based on search criteria
     */
    List<FlightResponse> searchFlights(FlightSearchRequest searchRequest);
    
    /**
     * Get all flights
     */
    List<FlightResponse> getAllFlights();
    
    /**
     * Get flight by ID
     */
    FlightResponse getFlightById(Long flightId);
}
