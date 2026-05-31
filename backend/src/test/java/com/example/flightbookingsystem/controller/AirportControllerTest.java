package com.example.flightbookingsystem.controller;

import com.example.flightbookingsystem.model.Airport;
import com.example.flightbookingsystem.service.AirportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(AirportController.class)
class AirportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AirportService airportService;

    @Test
    void getAllAirports_ReturnsAllAirports() throws Exception {
        // Given
        Airport airport1 = createAirport("JFK", "John F. Kennedy International Airport", "New York", "USA");
        Airport airport2 = createAirport("LAX", "Los Angeles International Airport", "Los Angeles", "USA");
        Airport airport3 = createAirport("ORD", "O'Hare International Airport", "Chicago", "USA");

        List<Airport> airports = Arrays.asList(airport1, airport2, airport3);

        when(airportService.getAllAirports()).thenReturn(airports);

        // When & Then
        mockMvc.perform(get("/api/airports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].code").value("JFK"))
                .andExpect(jsonPath("$[0].name").value("John F. Kennedy International Airport"))
                .andExpect(jsonPath("$[0].city").value("New York"))
                .andExpect(jsonPath("$[1].code").value("LAX"))
                .andExpect(jsonPath("$[2].code").value("ORD"));
    }

    @Test
    void getAllAirports_WhenNoAirportsExist_ReturnsEmptyList() throws Exception {
        // Given
        when(airportService.getAllAirports()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/airports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
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
