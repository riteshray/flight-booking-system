package com.example.flightbookingsystem.controller;

import com.example.flightbookingsystem.model.Airport;
import com.example.flightbookingsystem.service.AirportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/airports")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Airport Management", description = "APIs for managing airports")
public class AirportController {
	
	private final AirportService airportService;

	public AirportController(AirportService airportService) {
		this.airportService = airportService;
	}

	@Operation(summary = "Get all airports", description = "Retrieve a list of all airports")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved list of airports")
	@GetMapping
	public List<Airport> getAllAirports() {
		return airportService.getAllAirports();
	}
}