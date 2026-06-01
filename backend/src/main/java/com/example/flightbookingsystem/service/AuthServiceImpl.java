package com.example.flightbookingsystem.service;

import com.example.flightbookingsystem.dto.AuthResponse;
import com.example.flightbookingsystem.dto.LoginRequest;
import com.example.flightbookingsystem.dto.RegisterRequest;
import com.example.flightbookingsystem.model.User;
import com.example.flightbookingsystem.repository.UserRepository;
import com.example.flightbookingsystem.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(String.format("User with email %s already exists", request.getEmail()));
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        User user = new User(request.getName(), request.getEmail(), encodedPassword);
        user = userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getName(),
                user.getEmail(),
                "Bearer"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getName(),
                user.getEmail(),
                "Bearer"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        Long userId = jwtUtil.extractUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String newAccessToken = jwtUtil.generateAccessToken(userId);

        return new AuthResponse(
                newAccessToken,
                refreshToken,
                user.getId(),
                user.getName(),
                user.getEmail(),
                "Bearer"
        );
    }
}
