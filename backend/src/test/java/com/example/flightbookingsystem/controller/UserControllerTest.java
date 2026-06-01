package com.example.flightbookingsystem.controller;

import com.example.flightbookingsystem.exception.ResourceNotFoundException;
import com.example.flightbookingsystem.model.User;
import com.example.flightbookingsystem.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void getAllUsers_ReturnsListOfUsers() throws Exception {
        // Given
        User user1 = createUser(1L, "John Doe", "john.doe@example.com");
        User user2 = createUser(2L, "Jane Smith", "jane.smith@example.com");
        User user3 = createUser(3L, "Bob Johnson", "bob.johnson@example.com");
        List<User> users = Arrays.asList(user1, user2, user3);

        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"))
                .andExpect(jsonPath("$[2].id").value(3))
                .andExpect(jsonPath("$[2].name").value("Bob Johnson"));
    }

    @Test
    void getAllUsers_WhenNoUsersExist_ReturnsEmptyList() throws Exception {
        // Given
        when(userService.getAllUsers()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getUserById_WithValidId_ReturnsUser() throws Exception {
        // Given
        Long userId = 1L;
        User user = createUser(userId, "John Doe", "john.doe@example.com");
        when(userService.getUserById(userId)).thenReturn(user);

        // When & Then
        mockMvc.perform(get("/api/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void getUserById_WithNonExistentId_ReturnsNotFound() throws Exception {
        // Given
        Long invalidUserId = 999L;
        when(userService.getUserById(invalidUserId)).thenThrow(new ResourceNotFoundException(String.format("User not found with id: %s", invalidUserId)));

        // When & Then
        mockMvc.perform(get("/api/users/{userId}", invalidUserId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserById_WithInvalidId_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/{userId}", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WithValidUser_ReturnsCreatedUser() throws Exception {
        // Given
        User inputUser = new User("John Doe", "john.doe@example.com", "password123");
        User savedUser = createUser(1L, "John Doe", "john.doe@example.com");
        
        when(userService.createUser(inputUser)).thenReturn(savedUser);

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void createUser_WithDuplicateEmail_ReturnsInternalServerError() throws Exception {
        // Given
        User user = new User("John Doe", "john.doe@example.com", "password123");
        
        when(userService.createUser(user)).thenThrow(new IllegalArgumentException("User with email john.doe@example.com already exists"));

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void createUser_WithMissingName_ReturnsBadRequest() throws Exception {
        // Given
        User user = new User(null, "john.doe@example.com", "password123");

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WithMissingEmail_ReturnsBadRequest() throws Exception {
        // Given
        User user = new User("John Doe", null, "password123");

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WithInvalidEmailFormat_ReturnsBadRequest() throws Exception {
        // Given
        User user = new User("John Doe", "invalid-email", "password123");

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WithEmptyRequestBody_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private User createUser(Long id, String name, String email) {
        User user = new User(name, email, "password123");
        user.setId(id);
        return user;
    }
}
