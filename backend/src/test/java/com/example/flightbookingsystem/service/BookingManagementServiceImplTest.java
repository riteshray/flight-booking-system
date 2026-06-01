package com.example.flightbookingsystem.service;

import com.example.flightbookingsystem.dto.BookingRequest;
import com.example.flightbookingsystem.dto.BookingResponse;
import com.example.flightbookingsystem.exception.ResourceNotFoundException;
import com.example.flightbookingsystem.model.Flight;
import com.example.flightbookingsystem.model.FlightBooking;
import com.example.flightbookingsystem.model.Passenger;
import com.example.flightbookingsystem.model.User;
import com.example.flightbookingsystem.model.Airport;
import com.example.flightbookingsystem.repository.FlightBookingRepository;
import com.example.flightbookingsystem.repository.FlightRepository;
import com.example.flightbookingsystem.repository.PassengerRepository;
import com.example.flightbookingsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingManagementServiceImplTest {

    @Mock
    private FlightBookingRepository bookingRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PassengerRepository passengerRepository;

    @InjectMocks
    private BookingManagementServiceImpl bookingService;

    private User testUser;
    private Flight testFlight;
    private Passenger testPassenger;

    @BeforeEach
    void setUp() {
        Airport originAirport = new Airport();
        originAirport.setCode("JFK");
        originAirport.setName("John F. Kennedy International Airport");
        originAirport.setCity("New York");
        originAirport.setCountry("USA");

        Airport destinationAirport = new Airport();
        destinationAirport.setCode("LAX");
        destinationAirport.setName("Los Angeles International Airport");
        destinationAirport.setCity("Los Angeles");
        destinationAirport.setCountry("USA");

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");

        testFlight = new Flight();
        testFlight.setId(1L);
        testFlight.setFlightNumber("AA101");
        testFlight.setOrigin(originAirport);
        testFlight.setDestination(destinationAirport);
        testFlight.setDepartureTime(LocalDateTime.of(2026, 6, 15, 10, 0));
        testFlight.setArrivalTime(LocalDateTime.of(2026, 6, 15, 14, 0));
        testFlight.setPrice(299.99);
        testFlight.setAvailableSeats(50);

        testPassenger = new Passenger();
        testPassenger.setId(1L);
        testPassenger.setFirstName("Jane");
        testPassenger.setLastName("Smith");
        testPassenger.setEmail("jane.smith@example.com");
        testPassenger.setPhoneNumber("123-456-7890");
    }

    @Test
    void createBooking_WithValidRequest_CreatesBookingSuccessfully() {
        // Given
        BookingRequest request = BookingRequest.builder()
                .flightId(1L)
                .passengerFirstName("Jane")
                .passengerLastName("Smith")
                .passengerEmail("jane.smith@example.com")
                .passengerPhoneNumber("123-456-7890")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(flightRepository.findById(1L)).thenReturn(Optional.of(testFlight));
        when(passengerRepository.findByEmail("jane.smith@example.com")).thenReturn(Optional.of(testPassenger));

        FlightBooking savedBooking = createFlightBooking(1L, "BK123456", "CONFIRMED");
        when(bookingRepository.save(any(FlightBooking.class))).thenReturn(savedBooking);

        // When
        BookingResponse response = bookingService.createBooking(1L, request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("BK123456", response.getBookingReference());
        assertEquals("CONFIRMED", response.getStatus());
        assertEquals(1L, response.getUserId());
        assertEquals("John Doe", response.getUserName());
        assertEquals(1L, response.getFlightId());
        assertEquals("AA101", response.getFlightNumber());
        assertEquals(1L, response.getPassengerId());

        verify(userRepository).findById(1L);
        verify(flightRepository).findById(1L);
        verify(passengerRepository).findByEmail("jane.smith@example.com");
        verify(bookingRepository).save(any(FlightBooking.class));
    }

    @Test
    void createBooking_WithNewPassenger_CreatesPassengerAndBooking() {
        // Given
        BookingRequest request = BookingRequest.builder()
                .flightId(1L)
                .passengerFirstName("New")
                .passengerLastName("Passenger")
                .passengerEmail("new.passenger@example.com")
                .passengerPhoneNumber("987-654-3210")
                .build();

        Passenger newPassenger = new Passenger();
        newPassenger.setId(2L);
        newPassenger.setFirstName("New");
        newPassenger.setLastName("Passenger");
        newPassenger.setEmail("new.passenger@example.com");
        newPassenger.setPhoneNumber("987-654-3210");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(flightRepository.findById(1L)).thenReturn(Optional.of(testFlight));
        when(passengerRepository.findByEmail("new.passenger@example.com")).thenReturn(Optional.empty());
        when(passengerRepository.save(any(Passenger.class))).thenReturn(newPassenger);

        FlightBooking savedBooking = createFlightBooking(1L, "BK123456", "CONFIRMED");
        when(bookingRepository.save(any(FlightBooking.class))).thenReturn(savedBooking);

        // When
        BookingResponse response = bookingService.createBooking(1L, request);

        // Then
        assertNotNull(response);
        assertEquals("CONFIRMED", response.getStatus());

        ArgumentCaptor<Passenger> passengerCaptor = ArgumentCaptor.forClass(Passenger.class);
        verify(passengerRepository).save(passengerCaptor.capture());
        
        Passenger capturedPassenger = passengerCaptor.getValue();
        assertEquals("New", capturedPassenger.getFirstName());
        assertEquals("Passenger", capturedPassenger.getLastName());
        assertEquals("new.passenger@example.com", capturedPassenger.getEmail());

        verify(bookingRepository).save(any(FlightBooking.class));
    }

    @Test
    void createBooking_WithNonExistentUser_ThrowsResourceNotFoundException() {
        // Given
        BookingRequest request = BookingRequest.builder()
                .flightId(1L)
                .passengerFirstName("Jane")
                .passengerLastName("Smith")
                .passengerEmail("jane.smith@example.com")
                .build();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> bookingService.createBooking(999L, request));
        assertTrue(exception.getMessage().contains("User not found with id: 999"));
        verify(userRepository).findById(999L);
        verify(flightRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_WithNonExistentFlight_ThrowsResourceNotFoundException() {
        // Given
        BookingRequest request = BookingRequest.builder()
                .flightId(999L)
                .passengerFirstName("Jane")
                .passengerLastName("Smith")
                .passengerEmail("jane.smith@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(flightRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> bookingService.createBooking(1L, request));
        assertTrue(exception.getMessage().contains("Flight not found with id: 999"));
        verify(userRepository).findById(1L);
        verify(flightRepository).findById(999L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_WithNoAvailableSeats_ThrowsIllegalStateException() {
        // Given
        testFlight.setAvailableSeats(0);

        BookingRequest request = BookingRequest.builder()
                .flightId(1L)
                .passengerFirstName("Jane")
                .passengerLastName("Smith")
                .passengerEmail("jane.smith@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(flightRepository.findById(1L)).thenReturn(Optional.of(testFlight));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> bookingService.createBooking(1L, request));
        assertEquals("No available seats on this flight", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void getBookingByReference_WithValidReference_ReturnsBooking() {
        // Given
        String bookingReference = "BK123456";
        FlightBooking booking = createFlightBooking(1L, bookingReference, "CONFIRMED");
        when(bookingRepository.findByBookingReference(bookingReference)).thenReturn(Optional.of(booking));

        // When
        BookingResponse response = bookingService.getBookingByReference(bookingReference);

        // Then
        assertNotNull(response);
        assertEquals(bookingReference, response.getBookingReference());
        assertEquals("CONFIRMED", response.getStatus());
        assertEquals("AA101", response.getFlightNumber());
        verify(bookingRepository).findByBookingReference(bookingReference);
    }

    @Test
    void getBookingByReference_WithNonExistentReference_ThrowsResourceNotFoundException() {
        // Given
        String invalidReference = "BK999999";
        when(bookingRepository.findByBookingReference(invalidReference)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> bookingService.getBookingByReference(invalidReference));
        assertTrue(exception.getMessage().contains(String.format("Booking not found with reference: %s", invalidReference)));
        verify(bookingRepository).findByBookingReference(invalidReference);
    }

    @Test
    void getUserBookings_WithValidUserId_ReturnsListOfBookings() {
        // Given
        Long userId = 1L;
        FlightBooking booking1 = createFlightBooking(1L, "BK123456", "CONFIRMED");
        FlightBooking booking2 = createFlightBooking(2L, "BK789012", "CONFIRMED");
        FlightBooking booking3 = createFlightBooking(3L, "BK345678", "CANCELLED");

        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookingRepository.findByUserId(userId)).thenReturn(Arrays.asList(booking1, booking2, booking3));

        // When
        List<BookingResponse> responses = bookingService.getUserBookings(userId);

        // Then
        assertNotNull(responses);
        assertEquals(3, responses.size());
        assertEquals("BK123456", responses.get(0).getBookingReference());
        assertEquals("CONFIRMED", responses.get(0).getStatus());
        assertEquals("BK789012", responses.get(1).getBookingReference());
        assertEquals("BK345678", responses.get(2).getBookingReference());
        assertEquals("CANCELLED", responses.get(2).getStatus());

        verify(userRepository).existsById(userId);
        verify(bookingRepository).findByUserId(userId);
    }

    @Test
    void getUserBookings_WithNoBookings_ReturnsEmptyList() {
        // Given
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookingRepository.findByUserId(userId)).thenReturn(List.of());

        // When
        List<BookingResponse> responses = bookingService.getUserBookings(userId);

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(userRepository).existsById(userId);
        verify(bookingRepository).findByUserId(userId);
    }

    @Test
    void getUserBookings_WithNonExistentUser_ThrowsResourceNotFoundException() {
        // Given
        Long invalidUserId = 999L;
        when(userRepository.existsById(invalidUserId)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> bookingService.getUserBookings(invalidUserId));
        assertTrue(exception.getMessage().contains(String.format("User not found with id: %s", invalidUserId)));
        verify(userRepository).existsById(invalidUserId);
        verify(bookingRepository, never()).findByUserId(anyLong());
    }

    @Test
    void cancelBooking_WithValidBookingId_CancelsBookingSuccessfully() {
        // Given
        String bookingReference = "BK123456";
        FlightBooking booking = createFlightBooking(1L, bookingReference, "CONFIRMED");
        FlightBooking cancelledBooking = createFlightBooking(1L, bookingReference, "CANCELLED");

        when(bookingRepository.findByBookingReference(bookingReference)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(FlightBooking.class))).thenReturn(cancelledBooking);

        // When
        BookingResponse response = bookingService.cancelBooking(bookingReference);

        // Then
        assertNotNull(response);
        assertEquals("CANCELLED", response.getStatus());
        assertEquals("BK123456", response.getBookingReference());

        ArgumentCaptor<FlightBooking> bookingCaptor = ArgumentCaptor.forClass(FlightBooking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        assertEquals("CANCELLED", bookingCaptor.getValue().getStatus());
    }

    @Test
    void cancelBooking_WithNonExistentBooking_ThrowsResourceNotFoundException() {
        // Given
        String invalidBookingReference = "INVALID";
        when(bookingRepository.findByBookingReference(invalidBookingReference)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> bookingService.cancelBooking(invalidBookingReference));
        assertTrue(exception.getMessage().contains(String.format("Booking not found with reference: %s", invalidBookingReference)));
        verify(bookingRepository).findByBookingReference(invalidBookingReference);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void cancelBooking_WhenAlreadyCancelled_ThrowsIllegalStateException() {
        // Given
        String bookingReference = "BK123456";
        FlightBooking booking = createFlightBooking(1L, bookingReference, "CANCELLED");

        when(bookingRepository.findByBookingReference(bookingReference)).thenReturn(Optional.of(booking));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> bookingService.cancelBooking(bookingReference));
        assertEquals("Booking is already cancelled", exception.getMessage());
        verify(bookingRepository).findByBookingReference(bookingReference);
        verify(bookingRepository, never()).save(any());
    }

    private FlightBooking createFlightBooking(Long id, String bookingReference, String status) {
        FlightBooking booking = new FlightBooking();
        booking.setId(id);
        booking.setBookingReference(bookingReference);
        booking.setUser(testUser);
        booking.setFlight(testFlight);
        booking.setPassenger(testPassenger);
        booking.setStatus(status);
        booking.setBookingDate(LocalDateTime.now());
        return booking;
    }
}
