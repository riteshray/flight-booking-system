package com.example.flightbookingsystem.repository;

import com.example.flightbookingsystem.model.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public interface AirportRepository extends JpaRepository<Airport, String> {
}