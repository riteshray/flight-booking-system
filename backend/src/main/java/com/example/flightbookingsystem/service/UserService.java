package com.example.flightbookingsystem.service;

import com.example.flightbookingsystem.model.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User getUserById(Long userId);
    User createUser(User user);
}
