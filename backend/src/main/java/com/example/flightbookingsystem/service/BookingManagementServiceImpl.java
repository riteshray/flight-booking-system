package com.example.flightbookingsystem.service;

import com.example.flightbookingsystem.dto.BookingRequest;
import com.example.flightbookingsystem.dto.BookingResponse;
import com.example.flightbookingsystem.exception.ResourceNotFoundException;
import com.example.flightbookingsystem.model.Flight;
import com.example.flightbookingsystem.model.FlightBooking;
import com.example.flightbookingsystem.model.Passenger;
import com.example.flightbookingsystem.model.User;
import com.example.flightbookingsystem.repository.FlightBookingRepository;
import com.example.flightbookingsystem.repository.FlightRepository;
import com.example.flightbookingsystem.repository.PassengerRepository;
import com.example.flightbookingsystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BookingManagementServiceImpl implements BookingManagementService {

    private final FlightBookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final UserRepository userRepository;
    private final PassengerRepository passengerRepository;

    public BookingManagementServiceImpl(
            FlightBookingRepository bookingRepository,
            FlightRepository flightRepository,
            UserRepository userRepository,
            PassengerRepository passengerRepository) {
        this.bookingRepository = bookingRepository;
        this.flightRepository = flightRepository;
        this.userRepository = userRepository;
        this.passengerRepository = passengerRepository;
    }

    @Override
    public BookingResponse createBooking(Long userId, BookingRequest bookingRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User not found with id: %s", userId)));

        Flight flight = flightRepository.findById(bookingRequest.getFlightId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Flight not found with id: %s", bookingRequest.getFlightId())));

        if (flight.getAvailableSeats() <= 0) {
            throw new IllegalStateException("No available seats on this flight");
        }

        flight.setAvailableSeats(Math.subtractExact(flight.getAvailableSeats(), 1));
        Passenger passenger = passengerRepository.findByEmail(bookingRequest.getPassengerEmail())
                .orElseGet(() -> {
                    Passenger newPassenger = new Passenger();
                    newPassenger.setFirstName(bookingRequest.getPassengerFirstName());
                    newPassenger.setLastName(bookingRequest.getPassengerLastName());
                    newPassenger.setEmail(bookingRequest.getPassengerEmail());
                    newPassenger.setPhoneNumber(bookingRequest.getPassengerPhoneNumber());
                    return passengerRepository.save(newPassenger);
                });

        FlightBooking booking = new FlightBooking();
        booking.setUser(user);
        booking.setFlight(flight);
        booking.setPassenger(passenger);
        booking.setStatus("CONFIRMED");

        FlightBooking savedBooking = bookingRepository.save(booking);
        return mapToBookingResponse(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String bookingReference) {
        FlightBooking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Booking not found with reference: %s", bookingReference)));
        return mapToBookingResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(String.format("User not found with id: %s", userId));
        }
        
        List<FlightBooking> bookings = bookingRepository.findByUserId(userId);
        return bookings.stream()
                .map(this::mapToBookingResponse)
                .toList();
    }

    @Override
    public BookingResponse cancelBooking(String bookingReference) {
        FlightBooking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Booking not found with reference: %s", bookingReference)));

        if ("CANCELLED".equals(booking.getStatus())) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        booking.setStatus("CANCELLED");
        FlightBooking cancelledBooking = bookingRepository.save(booking);

        return mapToBookingResponse(cancelledBooking);
    }

    private BookingResponse mapToBookingResponse(FlightBooking booking) {
        Flight flight = booking.getFlight();
        User user = booking.getUser();
        Passenger passenger = booking.getPassenger();

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(user.getId())
                .userName(user.getName())
                .userEmail(user.getEmail())
                .flightId(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .originCode(flight.getOrigin().getCode())
                .destinationCode(flight.getDestination().getCode())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .price(flight.getPrice())
                .passengerId(passenger.getId())
                .passengerFirstName(passenger.getFirstName())
                .passengerLastName(passenger.getLastName())
                .passengerEmail(passenger.getEmail())
                .passengerPhoneNumber(passenger.getPhoneNumber())
                .status(booking.getStatus())
                .bookingDate(booking.getBookingDate())
                .build();
    }
}
