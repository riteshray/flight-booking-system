package com.example.flightbookingsystem.service;

import com.example.flightbookingsystem.dto.BookingRequest;
import com.example.flightbookingsystem.dto.BookingResponse;

import java.util.List;

public interface BookingManagementService {
    BookingResponse createBooking(Long userId, BookingRequest bookingRequest);
    BookingResponse getBookingByReference(String bookingReference);
    List<BookingResponse> getUserBookings(Long userId);
    BookingResponse cancelBooking(String bookingReference);
}
