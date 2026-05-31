package com.example.flightbookingsystem.service;

import com.example.flightbookingsystem.exception.ResourceNotFoundException;
import com.example.flightbookingsystem.model.User;
import com.example.flightbookingsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser1;
    private User testUser2;
    private User testUser3;

    @BeforeEach
    void setUp() {
        testUser1 = createUser(1L, "John Doe", "john.doe@example.com");
        testUser2 = createUser(2L, "Jane Smith", "jane.smith@example.com");
        testUser3 = createUser(3L, "Bob Johnson", "bob.johnson@example.com");
    }

    @Test
    void getAllUsers_ReturnsListOfUsers() {
        // Given
        List<User> users = Arrays.asList(testUser1, testUser2, testUser3);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("john.doe@example.com", result.get(0).getEmail());
        assertEquals("Jane Smith", result.get(1).getName());
        assertEquals("Bob Johnson", result.get(2).getName());

        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_WhenNoUsersExist_ReturnsEmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(List.of());

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_WithValidId_ReturnsUser() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser1));

        // When
        User result = userService.getUserById(userId);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john.doe@example.com", result.getEmail());

        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_WithNonExistentId_ThrowsResourceNotFoundException() {
        // Given
        Long invalidUserId = 999L;
        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(invalidUserId));
        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository).findById(invalidUserId);
    }

    @Test
    void getUserById_CalledMultipleTimes_ReturnsCorrectUsers() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser2));
        when(userRepository.findById(3L)).thenReturn(Optional.of(testUser3));

        // When
        User result1 = userService.getUserById(1L);
        User result2 = userService.getUserById(2L);
        User result3 = userService.getUserById(3L);

        // Then
        assertEquals("John Doe", result1.getName());
        assertEquals("Jane Smith", result2.getName());
        assertEquals("Bob Johnson", result3.getName());

        verify(userRepository, times(3)).findById(anyLong());
    }

    @Test
    void createUser_WithValidUser_CreatesAndReturnsUser() {
        // Given
        User newUser = new User("New User", "new.user@example.com");
        User savedUser = createUser(4L, "New User", "new.user@example.com");

        when(userRepository.existsByEmail("new.user@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = userService.createUser(newUser);

        // Then
        assertNotNull(result);
        assertEquals(4L, result.getId());
        assertEquals("New User", result.getName());
        assertEquals("new.user@example.com", result.getEmail());

        verify(userRepository).existsByEmail("new.user@example.com");
        verify(userRepository).save(newUser);
    }

    @Test
    void createUser_WithDuplicateEmail_ThrowsIllegalArgumentException() {
        // Given
        User newUser = new User("Duplicate User", "john.doe@example.com");

        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.createUser(newUser));
        assertEquals("User with email john.doe@example.com already exists", exception.getMessage());
        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_VerifiesUserDataIsSavedCorrectly() {
        // Given
        User newUser = new User("Test User", "test.user@example.com");
        User savedUser = createUser(5L, "Test User", "test.user@example.com");

        when(userRepository.existsByEmail("test.user@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        userService.createUser(newUser);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("Test User", capturedUser.getName());
        assertEquals("test.user@example.com", capturedUser.getEmail());
    }

    @Test
    void createUser_WithExistingEmail_NeverCallsSave() {
        // Given
        User newUser = new User("Another User", "jane.smith@example.com");

        when(userRepository.existsByEmail("jane.smith@example.com")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(newUser));
        verify(userRepository).existsByEmail("jane.smith@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_WithDifferentEmails_CreatesMultipleUsers() {
        // Given
        User user1 = new User("User One", "user1@example.com");
        User user2 = new User("User Two", "user2@example.com");
        
        User savedUser1 = createUser(10L, "User One", "user1@example.com");
        User savedUser2 = createUser(11L, "User Two", "user2@example.com");

        when(userRepository.existsByEmail("user1@example.com")).thenReturn(false);
        when(userRepository.existsByEmail("user2@example.com")).thenReturn(false);
        when(userRepository.save(user1)).thenReturn(savedUser1);
        when(userRepository.save(user2)).thenReturn(savedUser2);

        // When
        User result1 = userService.createUser(user1);
        User result2 = userService.createUser(user2);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals("user1@example.com", result1.getEmail());
        assertEquals("user2@example.com", result2.getEmail());

        verify(userRepository, times(2)).existsByEmail(anyString());
        verify(userRepository, times(2)).save(any(User.class));
    }

    private User createUser(Long id, String name, String email) {
        User user = new User(name, email);
        user.setId(id);
        return user;
    }
}
