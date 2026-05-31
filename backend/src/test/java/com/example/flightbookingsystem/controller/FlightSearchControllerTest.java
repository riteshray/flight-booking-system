package com.example.flightbookingsystem.controller;

import com.example.flightbookingsystem.dto.FlightResponse;
import com.example.flightbookingsystem.dto.FlightSearchRequest;
import com.example.flightbookingsystem.exception.ResourceNotFoundException;
import com.example.flightbookingsystem.service.FlightSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(FlightSearchController.class)
class FlightSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FlightSearchService flightSearchService;

    @Test
    void searchFlights_WithValidRequest_ReturnsFlights() throws Exception {
        // Given
        FlightSearchRequest searchRequest = new FlightSearchRequest();
        searchRequest.setOriginCode("JFK");
        searchRequest.setDestinationCode("LAX");
        searchRequest.setDepartureDate(LocalDate.of(2026, 6, 15));

        FlightResponse flight1 = createFlightResponse(1L, "AA100", "JFK", "New York JFK", "New York", 
                "LAX", "Los Angeles", "Los Angeles", 299.99, 50);
        FlightResponse flight2 = createFlightResponse(2L, "UA200", "JFK", "New York JFK", "New York", 
                "LAX", "Los Angeles", "Los Angeles", 349.99, 30);

        List<FlightResponse> flights = Arrays.asList(flight1, flight2);
        when(flightSearchService.searchFlights(any(FlightSearchRequest.class))).thenReturn(flights);

        // When & Then
        mockMvc.perform(post("/api/v1/flights/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].flightNumber").value("AA100"))
                .andExpect(jsonPath("$[0].originCode").value("JFK"))
                .andExpect(jsonPath("$[0].destinationCode").value("LAX"))
                .andExpect(jsonPath("$[0].price").value(299.99))
                .andExpect(jsonPath("$[0].availableSeats").value(50))
                .andExpect(jsonPath("$[1].flightNumber").value("UA200"))
                .andExpect(jsonPath("$[1].price").value(349.99));
    }

    @Test
    void searchFlights_WithNoResults_ReturnsEmptyList() throws Exception {
        // Given
        FlightSearchRequest searchRequest = new FlightSearchRequest();
        searchRequest.setOriginCode("ABC");
        searchRequest.setDestinationCode("XYZ");
        searchRequest.setDepartureDate(LocalDate.of(2026, 12, 31));

        when(flightSearchService.searchFlights(any(FlightSearchRequest.class))).thenReturn(List.of());

        // When & Then
        mockMvc.perform(post("/api/v1/flights/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void searchFlights_WithInvalidRequest_ReturnsBadRequest() throws Exception {
        // Given
        FlightSearchRequest invalidRequest = new FlightSearchRequest();

        // When & Then
        mockMvc.perform(post("/api/v1/flights/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllFlights_ReturnsAllFlights() throws Exception {
        // Given
        FlightResponse flight1 = createFlightResponse(1L, "AA100", "JFK", "New York JFK", "New York", 
                "LAX", "Los Angeles", "Los Angeles", 299.99, 50);
        FlightResponse flight2 = createFlightResponse(2L, "UA200", "ORD", "Chicago O'Hare", "Chicago", 
                "MIA", "Miami International", "Miami", 199.99, 75);
        FlightResponse flight3 = createFlightResponse(3L, "DL300", "ATL", "Atlanta Airport", "Atlanta", 
                "SEA", "Seattle Airport", "Seattle", 399.99, 20);

        List<FlightResponse> allFlights = Arrays.asList(flight1, flight2, flight3);
        when(flightSearchService.getAllFlights()).thenReturn(allFlights);

        // When & Then
        mockMvc.perform(get("/api/v1/flights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].flightNumber").value("AA100"))
                .andExpect(jsonPath("$[1].flightNumber").value("UA200"))
                .andExpect(jsonPath("$[2].flightNumber").value("DL300"));
    }

    @Test
    void getAllFlights_WhenNoFlightsExist_ReturnsEmptyList() throws Exception {
        // Given
        when(flightSearchService.getAllFlights()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/v1/flights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getFlightByFlightNumber_WithValidFlightNumber_ReturnsFlight() throws Exception {
        // Given
        String flightNumber = "AA101";
        FlightResponse flight = createFlightResponse(1L, flightNumber, "JFK", "New York JFK", "New York",
                "LAX", "Los Angeles", "Los Angeles", 299.99, 50);

        when(flightSearchService.getFlightByFlightNumber(flightNumber)).thenReturn(flight);

        // When & Then
        mockMvc.perform(get("/api/v1/flights/{flightNumber}", flightNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.flightNumber").value("AA101"))
                .andExpect(jsonPath("$.originCode").value("JFK"))
                .andExpect(jsonPath("$.destinationCode").value("LAX"))
                .andExpect(jsonPath("$.price").value(299.99))
                .andExpect(jsonPath("$.availableSeats").value(50));
    }

    @Test
    void getFlightByFlightNumber_WithInvalidFlightNumber_ReturnsNotFound() throws Exception {
        // Given
        String invalidFlightNumber = "INVALID";
        when(flightSearchService.getFlightByFlightNumber(invalidFlightNumber)).thenThrow(new ResourceNotFoundException(String.format("Flight not found with number: %s", invalidFlightNumber)));

        // When & Then
        mockMvc.perform(get("/api/v1/flights/{flightNumber}", invalidFlightNumber))
                .andExpect(status().isNotFound());
    }

    private FlightResponse createFlightResponse(Long id, String flightNumber, 
            String originCode, String originName, String originCity,
            String destCode, String destName, String destCity,
            Double price, Integer availableSeats) {
        return FlightResponse.builder()
                .id(id)
                .flightNumber(flightNumber)
                .originCode(originCode)
                .originName(originName)
                .originCity(originCity)
                .destinationCode(destCode)
                .destinationName(destName)
                .destinationCity(destCity)
                .departureTime(LocalDateTime.of(2026, 6, 15, 10, 0))
                .arrivalTime(LocalDateTime.of(2026, 6, 15, 14, 0))
                .price(price)
                .availableSeats(availableSeats)
                .build();
    }
}
