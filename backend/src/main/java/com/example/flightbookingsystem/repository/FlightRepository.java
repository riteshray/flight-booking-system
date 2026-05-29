package com.example.flightbookingsystem.repository;

import com.example.flightbookingsystem.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    
    @Query("SELECT f FROM Flight f WHERE f.origin.code = :originCode " +
           "AND f.destination.code = :destinationCode " +
           "AND f.departureTime >= :startOfDay " +
            "AND f.departureTime < :nextDay " +
           "AND f.availableSeats > 0")
    List<Flight> searchFlights(@Param("originCode") String originCode,
                                @Param("destinationCode") String destinationCode,
                               @Param("startOfDay") LocalDateTime startOfDay,
                               @Param("nextDay") LocalDateTime nextDay);
}