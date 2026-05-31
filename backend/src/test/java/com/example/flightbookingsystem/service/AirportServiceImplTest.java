package com.example.flightbookingsystem.service;

import com.example.flightbookingsystem.model.Airport;
import com.example.flightbookingsystem.repository.AirportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AirportServiceImplTest {

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private AirportServiceImpl airportService;

    private Airport jfkAirport;
    private Airport laxAirport;
    private Airport ordAirport;

    @BeforeEach
    void setUp() {
        jfkAirport = createAirport("JFK", "John F. Kennedy International Airport", "New York", "USA");
        laxAirport = createAirport("LAX", "Los Angeles International Airport", "Los Angeles", "USA");
        ordAirport = createAirport("ORD", "O'Hare International Airport", "Chicago", "USA");
    }

    @Test
    void getAllAirports_ReturnsAllAirports() {
        // Given
        List<Airport> airports = Arrays.asList(jfkAirport, laxAirport, ordAirport);
        when(airportRepository.findAll()).thenReturn(airports);

        // When
        List<Airport> result = airportService.getAllAirports();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("JFK", result.get(0).getCode());
        assertEquals("John F. Kennedy International Airport", result.get(0).getName());
        assertEquals("New York", result.get(0).getCity());
        assertEquals("LAX", result.get(1).getCode());
        assertEquals("ORD", result.get(2).getCode());

        verify(airportRepository).findAll();
    }

    @Test
    void getAllAirports_WhenNoAirportsExist_ReturnsEmptyList() {
        // Given
        when(airportRepository.findAll()).thenReturn(List.of());

        // When
        List<Airport> result = airportService.getAllAirports();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(airportRepository).findAll();
    }

    private Airport createAirport(String code, String name, String city, String country) {
        Airport airport = new Airport();
        airport.setCode(code);
        airport.setName(name);
        airport.setCity(city);
        airport.setCountry(country);
        return airport;
    }
}
