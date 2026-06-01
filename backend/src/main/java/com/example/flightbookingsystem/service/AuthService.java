package com.example.flightbookingsystem.service;

import com.example.flightbookingsystem.dto.AuthResponse;
import com.example.flightbookingsystem.dto.LoginRequest;
import com.example.flightbookingsystem.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
}
