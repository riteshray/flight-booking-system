package com.example.flightbookingsystem.service;

import com.example.flightbookingsystem.exception.ResourceNotFoundException;
import com.example.flightbookingsystem.model.User;
import com.example.flightbookingsystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User not found with id: %s", userId)));
    }
    
    @Override
    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(String.format("User with email %s already exists", user.getEmail()));
        }
        return userRepository.save(user);
    }
}
