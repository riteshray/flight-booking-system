package com.example.flightbookingsystem.repository;

import com.example.flightbookingsystem.model.FlightBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlightBookingRepository extends JpaRepository<FlightBooking, Long> {
    Optional<FlightBooking> findByBookingReference(String bookingReference);
    List<FlightBooking> findByUserId(Long userId);
}
