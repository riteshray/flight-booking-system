package com.example.flightbookingsystem.service;

import com.example.flightbookingsystem.dto.FlightResponse;
import com.example.flightbookingsystem.dto.FlightSearchRequest;
import com.example.flightbookingsystem.exception.ResourceNotFoundException;
import com.example.flightbookingsystem.model.Airport;
import com.example.flightbookingsystem.model.Flight;
import com.example.flightbookingsystem.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightSearchServiceImplTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private FlightSearchServiceImpl flightSearchService;

    private Airport laxAirport;
    private Flight flight1;
    private Flight flight2;

    @BeforeEach
    void setUp() {
        Airport jfkAirport = createAirport("JFK", "John F. Kennedy International Airport", "New York", "USA");
        laxAirport = createAirport("LAX", "Los Angeles International Airport", "Los Angeles", "USA");

        flight1 = createFlight(1L, "AA100", jfkAirport, laxAirport,
                LocalDateTime.of(2026, 6, 15, 10, 0),
                LocalDateTime.of(2026, 6, 15, 14, 0),
                299.99, 50);

        flight2 = createFlight(2L, "UA200", jfkAirport, laxAirport,
                LocalDateTime.of(2026, 6, 15, 16, 0),
                LocalDateTime.of(2026, 6, 15, 20, 0),
                349.99, 30);
    }

    @Test
    void searchFlights_WithValidCriteria_ReturnsMatchingFlights() {
        // Given
        FlightSearchRequest searchRequest = new FlightSearchRequest();
        searchRequest.setOriginCode("JFK");
        searchRequest.setDestinationCode("LAX");
        searchRequest.setDepartureDate(LocalDate.of(2026, 6, 15));

        LocalDateTime startOfDay = LocalDateTime.of(2026, 6, 15, 0, 0);
        LocalDateTime nextDay = startOfDay.plusDays(1);

        when(flightRepository.searchFlights("JFK", "LAX", startOfDay, nextDay)).thenReturn(Arrays.asList(flight1, flight2));

        // When
        List<FlightResponse> result = flightSearchService.searchFlights(searchRequest);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("AA100", result.get(0).getFlightNumber());
        assertEquals("UA200", result.get(1).getFlightNumber());
        assertEquals("JFK", result.get(0).getOriginCode());
        assertEquals("LAX", result.get(0).getDestinationCode());
        assertEquals(299.99, result.get(0).getPrice());
        assertEquals(50, result.get(0).getAvailableSeats());

        verify(flightRepository).searchFlights("JFK", "LAX", startOfDay, nextDay);
    }

    @Test
    void searchFlights_WithNoMatches_ReturnsEmptyList() {
        // Given
        FlightSearchRequest searchRequest = new FlightSearchRequest();
        searchRequest.setOriginCode("ABC");
        searchRequest.setDestinationCode("XYZ");
        searchRequest.setDepartureDate(LocalDate.of(2026, 12, 31));

        LocalDateTime startOfDay = LocalDateTime.of(2026, 12, 31, 0, 0);
        LocalDateTime nextDay = startOfDay.plusDays(1);

        when(flightRepository.searchFlights(anyString(), anyString(), any(), any())).thenReturn(List.of());

        // When
        List<FlightResponse> result = flightSearchService.searchFlights(searchRequest);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(flightRepository).searchFlights("ABC", "XYZ", startOfDay, nextDay);
    }

    @Test
    void getAllFlights_ReturnsAllFlights() {
        // Given
        Airport ordAirport = createAirport("ORD", "O'Hare International Airport", "Chicago", "USA");
        Flight flight3 = createFlight(3L, "DL300", ordAirport, laxAirport,
                LocalDateTime.of(2026, 6, 20, 8, 0),
                LocalDateTime.of(2026, 6, 20, 11, 0),
                199.99, 75);

        when(flightRepository.findAll()).thenReturn(Arrays.asList(flight1, flight2, flight3));

        // When
        List<FlightResponse> result = flightSearchService.getAllFlights();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("AA100", result.get(0).getFlightNumber());
        assertEquals("UA200", result.get(1).getFlightNumber());
        assertEquals("DL300", result.get(2).getFlightNumber());
        verify(flightRepository).findAll();
    }

    @Test
    void getAllFlights_WhenNoFlightsExist_ReturnsEmptyList() {
        // Given
        when(flightRepository.findAll()).thenReturn(List.of());

        // When
        List<FlightResponse> result = flightSearchService.getAllFlights();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(flightRepository).findAll();
    }

    @Test
    void getFlightByFlightNumber_WithValidFlightNumber_ReturnsFlight() {
        // Given
        String flightNumber = "AA100";
        when(flightRepository.findByFlightNumber(flightNumber)).thenReturn(Optional.of(flight1));

        // When
        FlightResponse result = flightSearchService.getFlightByFlightNumber(flightNumber);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("AA100", result.getFlightNumber());
        assertEquals("JFK", result.getOriginCode());
        assertEquals("John F. Kennedy International Airport", result.getOriginName());
        assertEquals("New York", result.getOriginCity());
        assertEquals("LAX", result.getDestinationCode());
        assertEquals("Los Angeles International Airport", result.getDestinationName());
        assertEquals("Los Angeles", result.getDestinationCity());
        assertEquals(299.99, result.getPrice());
        assertEquals(50, result.getAvailableSeats());

        verify(flightRepository).findByFlightNumber(flightNumber);
    }

    @Test
    void getFlightByFlightNumber_WithInvalidFlightNumber_ThrowsResourceNotFoundException() {
        // Given
        String invalidFlightNumber = "INVALID";
        when(flightRepository.findByFlightNumber(invalidFlightNumber)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> flightSearchService.getFlightByFlightNumber(invalidFlightNumber));
        assertEquals("Flight not found with number: INVALID", exception.getMessage());
        verify(flightRepository).findByFlightNumber(invalidFlightNumber);
    }

    @Test
    void mapToFlightResponse_MapsAllFieldsCorrectly() {
        // Given
        String flightNumber = "AA100";
        when(flightRepository.findByFlightNumber(flightNumber)).thenReturn(Optional.of(flight1));

        // When
        FlightResponse result = flightSearchService.getFlightByFlightNumber(flightNumber);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("AA100", result.getFlightNumber());
        assertEquals("JFK", result.getOriginCode());
        assertEquals("John F. Kennedy International Airport", result.getOriginName());
        assertEquals("New York", result.getOriginCity());
        assertEquals("LAX", result.getDestinationCode());
        assertEquals("Los Angeles International Airport", result.getDestinationName());
        assertEquals("Los Angeles", result.getDestinationCity());
        assertEquals(LocalDateTime.of(2026, 6, 15, 10, 0), result.getDepartureTime());
        assertEquals(LocalDateTime.of(2026, 6, 15, 14, 0), result.getArrivalTime());
        assertEquals(299.99, result.getPrice());
        assertEquals(50, result.getAvailableSeats());
    }

    private Airport createAirport(String code, String name, String city, String country) {
        Airport airport = new Airport();
        airport.setCode(code);
        airport.setName(name);
        airport.setCity(city);
        airport.setCountry(country);
        return airport;
    }

    private Flight createFlight(Long id, String flightNumber, Airport origin, Airport destination,
                                LocalDateTime departureTime, LocalDateTime arrivalTime,
                                Double price, Integer availableSeats) {
        Flight flight = new Flight();
        flight.setId(id);
        flight.setFlightNumber(flightNumber);
        flight.setOrigin(origin);
        flight.setDestination(destination);
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(arrivalTime);
        flight.setPrice(price);
        flight.setAvailableSeats(availableSeats);
        return flight;
    }
}
