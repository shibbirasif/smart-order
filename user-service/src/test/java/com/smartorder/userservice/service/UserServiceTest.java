package com.smartorder.userservice.service;

import com.smartorder.userservice.dto.CreateUserRequest;
import com.smartorder.userservice.dto.UserResponse;
import com.smartorder.userservice.exception.EmailAlreadyUsedException;
import com.smartorder.userservice.model.User;
import com.smartorder.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private CreateUserRequest createUserRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createUserRequest = new CreateUserRequest();
        createUserRequest.setName("Jane Doe");
        createUserRequest.setEmail("jane.doe@example.com");
    }

    @Test
    @DisplayName("Should return all users when getAllUsers is called")
    void shouldReturnAllUsers() {
        // Given
        List<User> expectedUsers = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // When
        List<User> actualUsers = userService.getAllUsers();

        // Then
        assertThat(actualUsers).isEqualTo(expectedUsers);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should create user when email is not already used")
    void shouldCreateUserWhenEmailIsNotUsed() {
        // Given
        User savedUser = User.builder()
                .id(2L)
                .name(createUserRequest.getName())
                .email(createUserRequest.getEmail())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserResponse response = userService.createUser(createUserRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(savedUser.getId());
        assertThat(response.getName()).isEqualTo(savedUser.getName());
        assertThat(response.getEmail()).isEqualTo(savedUser.getEmail());
        assertThat(response.getCreatedAt()).isEqualTo(savedUser.getCreatedAt());
        assertThat(response.getUpdatedAt()).isEqualTo(savedUser.getUpdatedAt());

        verify(userRepository, times(1)).existsByEmail(createUserRequest.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw EmailAlreadyUsedException when email is already used")
    void shouldThrowExceptionWhenEmailAlreadyUsed() {
        // Given
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(createUserRequest))
                .isInstanceOf(EmailAlreadyUsedException.class)
                .hasMessage("Email already in use");

        verify(userRepository, times(1)).existsByEmail(createUserRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should return empty list when no users exist")
    void shouldReturnEmptyListWhenNoUsersExist() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<User> users = userService.getAllUsers();

        // Then
        assertThat(users).isEmpty();
        verify(userRepository, times(1)).findAll();
    }
}
