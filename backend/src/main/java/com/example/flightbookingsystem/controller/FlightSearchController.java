package com.example.flightbookingsystem.controller;

import com.example.flightbookingsystem.dto.FlightResponse;
import com.example.flightbookingsystem.dto.FlightSearchRequest;
import com.example.flightbookingsystem.service.FlightSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for flight search operations
 */
@RestController
@RequestMapping("/api/v1/flights")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Flight Search", description = "APIs for searching and viewing flights")
public class FlightSearchController {
    
    private final FlightSearchService flightSearchService;
    
    public FlightSearchController(FlightSearchService flightSearchService) {
        this.flightSearchService = flightSearchService;
    }
    
    @Operation(summary = "Search for flights", description = "Search for available flights based on origin, destination and date")
    @PostMapping("/search")
    public ResponseEntity<List<FlightResponse>> searchFlights(@Valid @RequestBody FlightSearchRequest searchRequest) {
        List<FlightResponse> flights = flightSearchService.searchFlights(searchRequest);
        return ResponseEntity.ok(flights);
    }
    
    @Operation(summary = "Get all flights", description = "Retrieve all available flights")
    @GetMapping
    public ResponseEntity<List<FlightResponse>> getAllFlights() {
        List<FlightResponse> flights = flightSearchService.getAllFlights();
        return ResponseEntity.ok(flights);
    }
    
    @Operation(summary = "Get flight by flight number", description = "Retrieve flight details by flight number")
    @GetMapping("/{flightNumber}")
    public ResponseEntity<FlightResponse> getFlightById(@PathVariable String flightNumber) {
        FlightResponse flight = flightSearchService.getFlightByFlightNumber(flightNumber);
        return ResponseEntity.ok(flight);
    }
}
