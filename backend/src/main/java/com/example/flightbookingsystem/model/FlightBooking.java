package com.example.flightbookingsystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class FlightBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String bookingReference;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @ManyToOne
    @JoinColumn(name = "passenger_id", nullable = false)
    private Passenger passenger;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime bookingDate;

    @PrePersist
    protected void onCreate() {
        bookingDate = LocalDateTime.now();
        if (bookingReference == null) {
            bookingReference = generateBookingReference();
        }
    }

    private String generateBookingReference() {
        return String.format("BK%s", System.currentTimeMillis());
    }

    public FlightBooking() {
    }
}
