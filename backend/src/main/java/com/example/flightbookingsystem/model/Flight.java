package com.example.flightbookingsystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Flight {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String flightNumber;

	@ManyToOne
	@JoinColumn(name = "origin_id", nullable = false)
	private Airport origin;

	@ManyToOne
	@JoinColumn(name = "destination_id", nullable = false)
	private Airport destination;

	@Column(nullable = false)
	private LocalDateTime departureTime;

	@Column(nullable = false)
	private LocalDateTime arrivalTime;

	@Column(nullable = false)
	private Double price;

	@Column(nullable = false)
	private Integer availableSeats;

	public Flight() {
	}

	public Flight(String flightNumber, Airport origin, Airport destination, 
	              LocalDateTime departureTime, LocalDateTime arrivalTime, 
	              Double price, Integer availableSeats) {
		this.flightNumber = flightNumber;
		this.origin = origin;
		this.destination = destination;
		this.departureTime = departureTime;
		this.arrivalTime = arrivalTime;
		this.price = price;
		this.availableSeats = availableSeats;
	}
}
