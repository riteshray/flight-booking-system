package com.example.flightbookingsystem.service;

import com.example.flightbookingsystem.dto.FlightResponse;
import com.example.flightbookingsystem.dto.FlightSearchRequest;

import java.util.List;

public interface FlightSearchService {
    List<FlightResponse> searchFlights(FlightSearchRequest searchRequest);
    List<FlightResponse> getAllFlights();
    FlightResponse getFlightById(Long flightId);
}
