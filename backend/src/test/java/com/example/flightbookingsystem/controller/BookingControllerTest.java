package com.example.flightbookingsystem.controller;

import com.example.flightbookingsystem.dto.BookingRequest;
import com.example.flightbookingsystem.dto.BookingResponse;
import com.example.flightbookingsystem.exception.ResourceNotFoundException;
import com.example.flightbookingsystem.security.JwtUtil;
import com.example.flightbookingsystem.service.BookingManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingManagementService bookingService;

    @Autowired
    private JwtUtil jwtUtil;

    private String accessToken;
    private final Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        accessToken = jwtUtil.generateAccessToken(testUserId);
    }

    @Test
    void createBooking_WithValidRequest_ReturnsCreatedBooking() throws Exception {
        // Given
        BookingRequest request = createValidBookingRequest();
        BookingResponse response = createBookingResponse(1L, "BK1234567890", "CONFIRMED");
        when(bookingService.createBooking(anyLong(), any(BookingRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", "Bearer " + accessToken)
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
    void createBooking_WithoutToken_ReturnsUnauthorized() throws Exception {
        // Given
        BookingRequest request = createValidBookingRequest();

        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createBooking_WithInvalidToken_ReturnsUnauthorized() throws Exception {
        // Given
        BookingRequest request = createValidBookingRequest();

        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", "Bearer invalid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createBooking_WithMissingFlightId_ReturnsBadRequest() throws Exception {
        // Given
        BookingRequest request = createValidBookingRequest();
        request.setFlightId(null);

        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", "Bearer " + accessToken)
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
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_WithNonExistentFlight_ReturnsNotFound() throws Exception {
        // Given
        BookingRequest request = createValidBookingRequest();
        when(bookingService.createBooking(anyLong(), any(BookingRequest.class))).thenThrow(new ResourceNotFoundException("Flight not found with id: 999"));

        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBooking_WithNoAvailableSeats_ReturnsConflict() throws Exception {
        // Given
        BookingRequest request = createValidBookingRequest();
        when(bookingService.createBooking(anyLong(), any(BookingRequest.class))).thenThrow(new IllegalStateException("No available seats on this flight"));

        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getBookingByReference_WithValidReference_ReturnsBooking() throws Exception {
        // Given
        String bookingReference = "BK1234567890";
        BookingResponse response = createBookingResponse(1L, bookingReference, "CONFIRMED");
        when(bookingService.getBookingByReference(bookingReference)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/reference/{bookingReference}", bookingReference)
                        .header("Authorization", "Bearer " + accessToken))
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
        when(bookingService.getBookingByReference(invalidReference)).thenThrow(new ResourceNotFoundException("Booking not found with reference: " + invalidReference));

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/reference/{bookingReference}", invalidReference)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMyBookings_WithValidToken_ReturnsListOfBookings() throws Exception {
        // Given
        BookingResponse booking1 = createBookingResponse(1L, "BK1234567890", "CONFIRMED");
        BookingResponse booking2 = createBookingResponse(2L, "BK0987654321", "CONFIRMED");
        BookingResponse booking3 = createBookingResponse(3L, "BK1111111111", "CANCELLED");
        List<BookingResponse> bookings = Arrays.asList(booking1, booking2, booking3);

        when(bookingService.getUserBookings(testUserId)).thenReturn(bookings);

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/my-bookings")
                        .header("Authorization", "Bearer " + accessToken))
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
    void getMyBookings_WithNoBookings_ReturnsEmptyList() throws Exception {
        // Given
        when(bookingService.getUserBookings(testUserId)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/my-bookings")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getMyBookings_WithoutToken_ReturnsUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/bookings/my-bookings"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelBooking_WithValidBookingReference_ReturnsCancelledBooking() throws Exception {
        // Given
        String bookingReference = "BK1234567890";
        BookingResponse response = createBookingResponse(1L, bookingReference, "CANCELLED");
        when(bookingService.cancelBooking(bookingReference)).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/bookings/{bookingReference}/cancel", bookingReference)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.bookingReference").value(bookingReference))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelBooking_WithNonExistentBooking_ReturnsNotFound() throws Exception {
        // Given
        String invalidBookingReference = "BK1";
        when(bookingService.cancelBooking(invalidBookingReference)).thenThrow(new ResourceNotFoundException("Booking not found with reference: " + invalidBookingReference));

        // When & Then
        mockMvc.perform(put("/api/v1/bookings/{bookingReference}/cancel", invalidBookingReference)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelBooking_WhenAlreadyCancelled_ReturnsConflict() throws Exception {
        // Given
        String bookingReference = "BK1234567890";
        when(bookingService.cancelBooking(bookingReference))
                .thenThrow(new IllegalStateException("Booking is already cancelled"));

        // When & Then
        mockMvc.perform(put("/api/v1/bookings/{bookingReference}/cancel", bookingReference)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isConflict());
    }

    private BookingRequest createValidBookingRequest() {
        return BookingRequest.builder()
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
