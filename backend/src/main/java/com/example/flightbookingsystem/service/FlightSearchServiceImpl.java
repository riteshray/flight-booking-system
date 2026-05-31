package com.example.flightbookingsystem.service;

import com.example.flightbookingsystem.dto.FlightResponse;
import com.example.flightbookingsystem.dto.FlightSearchRequest;
import com.example.flightbookingsystem.exception.ResourceNotFoundException;
import com.example.flightbookingsystem.model.Flight;
import com.example.flightbookingsystem.repository.FlightRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class FlightSearchServiceImpl implements FlightSearchService {
    
    private final FlightRepository flightRepository;
    
    public FlightSearchServiceImpl(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }
    
    @Override
    public List<FlightResponse> searchFlights(FlightSearchRequest searchRequest) {
        LocalDateTime startOfDay = searchRequest.getDepartureDate().atStartOfDay();
        LocalDateTime nextDay = startOfDay.plusDays(1);

        List<Flight> flights = flightRepository.searchFlights(searchRequest.getOriginCode(), searchRequest.getDestinationCode(), startOfDay, nextDay);
        
        return flights.stream()
            .map(this::mapToFlightResponse)
            .toList();
    }
    
    @Override
    public List<FlightResponse> getAllFlights() {
        return flightRepository.findAll().stream()
            .map(this::mapToFlightResponse)
            .toList();
    }
    
    @Override
    public FlightResponse getFlightByFlightNumber(String flightNumber) {
        Flight flight = flightRepository.findByFlightNumber(flightNumber)
            .orElseThrow(() -> new ResourceNotFoundException(String.format("Flight not found with number: %s", flightNumber)));
        return mapToFlightResponse(flight);
    }

    private FlightResponse mapToFlightResponse(Flight flight) {
        return FlightResponse.builder()
            .id(flight.getId())
            .flightNumber(flight.getFlightNumber())
            .originCode(flight.getOrigin().getCode())
            .originName(flight.getOrigin().getName())
            .originCity(flight.getOrigin().getCity())
            .destinationCode(flight.getDestination().getCode())
            .destinationName(flight.getDestination().getName())
            .destinationCity(flight.getDestination().getCity())
            .departureTime(flight.getDepartureTime())
            .arrivalTime(flight.getArrivalTime())
            .price(flight.getPrice())
            .availableSeats(flight.getAvailableSeats())
            .build();
    }
}
