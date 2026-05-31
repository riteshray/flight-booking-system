package com.example.flightbookingsystem.controller;

import com.example.flightbookingsystem.dto.BookingRequest;
import com.example.flightbookingsystem.dto.BookingResponse;
import com.example.flightbookingsystem.exception.ResourceNotFoundException;
import com.example.flightbookingsystem.service.BookingManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingManagementService bookingService;

    @Test
    void createBooking_WithValidRequest_ReturnsCreatedBooking() throws Exception {
        // Given
        BookingRequest request = createValidBookingRequest();
        BookingResponse response = createBookingResponse(1L, "BK1234567890", "CONFIRMED");
        when(bookingService.createBooking(request)).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.bookingReference").value("BK1234567890"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.userName").value("John Doe"))
                .andExpect(jsonPath("$.flightId").value(1))
                .andExpect(jsonPath("$.flightNumber").value("AA101"))
                .andExpect(jsonPath("$.originCode").value("JFK"))
                .andExpect(jsonPath("$.destinationCode").value("LAX"))
                .andExpect(jsonPath("$.price").value(299.99))
                .andExpect(jsonPath("$.passengerFirstName").value("John"))
                .andExpect(jsonPath("$.passengerLastName").value("Doe"))
                .andExpect(jsonPath("$.passengerEmail").value("john.doe@example.com"));
    }

    @Test
    void createBooking_WithMissingUserId_ReturnsBadRequest() throws Exception {
        // Given
        BookingRequest request = createValidBookingRequest();
        request.setUserId(null);

        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_WithMissingFlightId_ReturnsBadRequest() throws Exception {
        // Given
        BookingRequest request = createValidBookingRequest();
        request.setFlightId(null);

        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_WithInvalidEmail_ReturnsBadRequest() throws Exception {
        // Given
        BookingRequest request = createValidBookingRequest();
        request.setPassengerEmail("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_WithNonExistentUser_ReturnsNotFound() throws Exception {
        // Given
        BookingRequest request = createValidBookingRequest();
        when(bookingService.createBooking(request)).thenThrow(new ResourceNotFoundException("User not found with id: 999"));

        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBooking_WithNonExistentFlight_ReturnsNotFound() throws Exception {
        // Given
        BookingRequest request = createValidBookingRequest();
        when(bookingService.createBooking(request)).thenThrow(new ResourceNotFoundException("Flight not found with id: 999"));

        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBooking_WithNoAvailableSeats_ReturnsInternalServerError() throws Exception {
        // Given
        BookingRequest request = createValidBookingRequest();
        when(bookingService.createBooking(request)).thenThrow(new IllegalStateException("No available seats on this flight"));

        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getBookingByReference_WithValidReference_ReturnsBooking() throws Exception {
        // Given
        String bookingReference = "BK1234567890";
        BookingResponse response = createBookingResponse(1L, bookingReference, "CONFIRMED");
        when(bookingService.getBookingByReference(bookingReference)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/reference/{bookingReference}", bookingReference))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.bookingReference").value(bookingReference))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.flightNumber").value("AA101"))
                .andExpect(jsonPath("$.passengerEmail").value("john.doe@example.com"));
    }

    @Test
    void getBookingByReference_WithNonExistentReference_ReturnsNotFound() throws Exception {
        // Given
        String invalidReference = "BK9999999999";
        when(bookingService.getBookingByReference(invalidReference)).thenThrow(new ResourceNotFoundException(String.format("Booking not found with reference: %s", invalidReference)));

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/reference/{bookingReference}", invalidReference))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookingByReference_WithEmptyReference_ReturnsNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/bookings/reference/"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserBookings_WithValidUserId_ReturnsListOfBookings() throws Exception {
        // Given
        Long userId = 1L;
        BookingResponse booking1 = createBookingResponse(1L, "BK1234567890", "CONFIRMED");
        BookingResponse booking2 = createBookingResponse(2L, "BK0987654321", "CONFIRMED");
        BookingResponse booking3 = createBookingResponse(3L, "BK1111111111", "CANCELLED");
        List<BookingResponse> bookings = Arrays.asList(booking1, booking2, booking3);
        
        when(bookingService.getUserBookings(userId)).thenReturn(bookings);

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].bookingReference").value("BK1234567890"))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$[1].bookingReference").value("BK0987654321"))
                .andExpect(jsonPath("$[1].status").value("CONFIRMED"))
                .andExpect(jsonPath("$[2].bookingReference").value("BK1111111111"))
                .andExpect(jsonPath("$[2].status").value("CANCELLED"));
    }

    @Test
    void getUserBookings_WithNoBookings_ReturnsEmptyList() throws Exception {
        // Given
        Long userId = 1L;
        when(bookingService.getUserBookings(userId)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getUserBookings_WithNonExistentUser_ReturnsNotFound() throws Exception {
        // Given
        Long invalidUserId = 999L;
        when(bookingService.getUserBookings(invalidUserId)).thenThrow(new ResourceNotFoundException(String.format("User not found with id: %s", invalidUserId)));

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/user/{userId}", invalidUserId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserBookings_WithInvalidUserId_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/bookings/user/{userId}", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelBooking_WithValidBookingReference_ReturnsCancelledBooking() throws Exception {
        // Given
        String bookingReference = "BK1234567890";
        BookingResponse response = createBookingResponse(1L, bookingReference, "CANCELLED");
        when(bookingService.cancelBooking(bookingReference)).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/bookings/{bookingReference}/cancel", bookingReference))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.bookingReference").value(bookingReference))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelBooking_WithNonExistentBooking_ReturnsNotFound() throws Exception {
        // Given
        String invalidBookingReference = "BK1";
        when(bookingService.cancelBooking(invalidBookingReference)).thenThrow(new ResourceNotFoundException(String.format("Booking not found with reference: %s", invalidBookingReference)));

        // When & Then
        mockMvc.perform(put("/api/v1/bookings/{bookingReference}/cancel", invalidBookingReference))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelBooking_WhenAlreadyCancelled_ReturnsInternalServerError() throws Exception {
        // Given
        String bookingReference = "BK1234567890";
        when(bookingService.cancelBooking(bookingReference)).thenThrow(new IllegalStateException("Booking is already cancelled"));

        // When & Then
        mockMvc.perform(put("/api/v1/bookings/{bookingReference}/cancel", bookingReference))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void cancelBooking_WithInvalidBookingReference_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/v1/bookings/{bookingReference}/cancel", "invalid"))
                .andExpect(status().isBadRequest());
    }

    private BookingRequest createValidBookingRequest() {
        return BookingRequest.builder()
                .userId(1L)
                .flightId(1L)
                .passengerFirstName("John")
                .passengerLastName("Doe")
                .passengerEmail("john.doe@example.com")
                .passengerPhoneNumber("123-456-7890")
                .build();
    }

    private BookingResponse createBookingResponse(Long id, String bookingReference, String status) {
        return BookingResponse.builder()
                .id(id)
                .bookingReference(bookingReference)
                .userId(1L)
                .userName("John Doe")
                .userEmail("john.doe@example.com")
                .flightId(1L)
                .flightNumber("AA101")
                .originCode("JFK")
                .destinationCode("LAX")
                .departureTime(LocalDateTime.of(2026, 6, 15, 10, 0))
                .arrivalTime(LocalDateTime.of(2026, 6, 15, 14, 0))
                .price(299.99)
                .passengerId(1L)
                .passengerFirstName("John")
                .passengerLastName("Doe")
                .passengerEmail("john.doe@example.com")
                .passengerPhoneNumber("123-456-7890")
                .status(status)
                .bookingDate(LocalDateTime.now())
                .build();
    }
}
