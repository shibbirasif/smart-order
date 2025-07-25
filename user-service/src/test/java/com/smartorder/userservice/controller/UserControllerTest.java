package com.smartorder.userservice.controller;

import com.smartorder.userservice.dto.CreateUserRequest;
import com.smartorder.userservice.dto.UserResponse;
import com.smartorder.userservice.exception.EmailAlreadyUsedException;
import com.smartorder.userservice.model.User;
import com.smartorder.userservice.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("Should return list of users when GET /api/users")
    void shouldReturnListOfUsers() throws Exception {
        // Given
        List<User> users = Arrays.asList(
            User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build(),
            User.builder()
                .id(2L)
                .name("Jane Doe")
                .email("jane@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        );
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("John Doe"))
            .andExpect(jsonPath("$[0].email").value("john@example.com"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].name").value("Jane Doe"))
            .andExpect(jsonPath("$[1].email").value("jane@example.com"));
    }

    @Test
    @DisplayName("Should return empty list when no users exist")
    void shouldReturnEmptyListWhenNoUsers() throws Exception {
        // Given
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should create user when POST /api/users with valid data")
    void createUser_shouldReturnCreatedUser() throws Exception {

        CreateUserRequest request = new CreateUserRequest();
        request.setName("John");
        request.setEmail("john@example.com");

        UserResponse response = UserResponse.builder()
            .id(1L)
            .name("John")
            .email("john@example.com")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "John",
                      "email": "john@example.com"
                    }
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("John"))
            .andExpect(jsonPath("$.email").value("john@example.com"))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("Should return 400 when POST /api/users with missing name")
    void shouldReturn400WithMissingName() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "john@example.com"
                    }
                """))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when POST /api/users with missing email")
    void shouldReturn400WithMissingEmail() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "John"
                    }
                """))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when POST /api/users with invalid email format")
    void shouldReturn400WithInvalidEmailFormat() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "John",
                      "email": "invalid-email"
                    }
                """))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when POST /api/users with empty name")
    void shouldReturn400WithEmptyName() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "",
                      "email": "john@example.com"
                    }
                """))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle email already exists exception")
    void shouldHandleEmailAlreadyExistsException() throws Exception {
        // Given
        when(userService.createUser(any(CreateUserRequest.class)))
            .thenThrow(new EmailAlreadyUsedException("Email already in use"));

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "John",
                      "email": "john@example.com"
                    }
                """))
            .andExpect(status().isConflict()); // Default behavior without @ControllerAdvice
    }

    @Test
    @DisplayName("Should return 400 when POST /api/users with malformed JSON")
    void shouldReturn400WithMalformedJson() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 415 when POST /api/users without content type")
    void shouldReturn415WithoutContentType() throws Exception {
        mockMvc.perform(post("/api/users")
                .content("""
                    {
                      "name": "John",
                      "email": "john@example.com"
                    }
                """))
            .andExpect(status().isUnsupportedMediaType());
    }
}