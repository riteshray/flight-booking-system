package com.example.flightbookingsystem.service;

import com.example.flightbookingsystem.exception.ResourceNotFoundException;
import com.example.flightbookingsystem.model.Airport;
import com.example.flightbookingsystem.repository.AirportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AirportServiceImpl implements AirportService {

	private final AirportRepository airportRepo;

	public AirportServiceImpl(AirportRepository airportRepo) {
		this.airportRepo = airportRepo;
	}

	@Override
	public Airport getAirportById(String airportId) {
		return airportRepo.findById(airportId).orElseThrow(() -> new ResourceNotFoundException(airportId));
	}

	@Override
	public List<Airport> getAllAirports() {
		return airportRepo.findAll();
	}

}
