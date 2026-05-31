package com.example.flightbookingsystem.controller;

import com.example.flightbookingsystem.dto.BookingRequest;
import com.example.flightbookingsystem.dto.BookingResponse;
import com.example.flightbookingsystem.service.BookingManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Booking Management", description = "APIs for managing flight bookings")
public class BookingController {
    
    private final BookingManagementService bookingService;
    
    public BookingController(BookingManagementService bookingService) {
        this.bookingService = bookingService;
    }
    
    @Operation(summary = "Create a new booking", description = "Book a flight for a passenger")
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest bookingRequest) {
        BookingResponse booking = bookingService.createBooking(bookingRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }
    
    @Operation(summary = "Get booking by reference", description = "Retrieve booking details using booking reference number")
    @GetMapping("/reference/{bookingReference}")
    public ResponseEntity<BookingResponse> getBookingByReference(@PathVariable String bookingReference) {
        BookingResponse booking = bookingService.getBookingByReference(bookingReference);
        return ResponseEntity.ok(booking);
    }
    
    @Operation(summary = "Get user bookings", description = "Retrieve all bookings for a specific user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getUserBookings(@PathVariable Long userId) {
        List<BookingResponse> bookings = bookingService.getUserBookings(userId);
        return ResponseEntity.ok(bookings);
    }
    
    @Operation(summary = "Cancel a booking", description = "Cancel an existing booking")
    @PutMapping("/{bookingReference}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable String bookingReference) {
        BookingResponse booking = bookingService.cancelBooking(bookingReference);
        return ResponseEntity.ok(booking);
    }
}
