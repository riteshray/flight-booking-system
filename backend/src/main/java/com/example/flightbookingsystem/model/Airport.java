package com.example.flightbookingsystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Airport {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String code;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String city;

	@Column(nullable = false)
	private String country;

	public Airport() {
	}

	public Airport(String code, String name, String city, String country) {
		this.code = code;
		this.name = name;
		this.city = city;
		this.country = country;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
}
