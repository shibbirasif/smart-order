package com.smartorder.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartorder.userservice.dto.CreateUserRequest;
import com.smartorder.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("UserController Integration Tests")
class UserControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @BeforeEach
        void setUp() {
                // Clean up database before each test
                userRepository.deleteAll();
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        void shouldReturnEmptyListWhenNoUsers() throws Exception {
                // When & Then
                mockMvc.perform(get("/api/users"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should create user and return 201 with user data")
        void shouldCreateUserAndReturn201() throws Exception {
                // Given
                CreateUserRequest request = new CreateUserRequest();
                request.setName("John Doe");
                request.setEmail("john.doe@example.com");

                // When & Then
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.id", notNullValue()))
                                .andExpect(jsonPath("$.name").value("John Doe"))
                                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                                .andExpect(jsonPath("$.createdAt", notNullValue()))
                                .andExpect(jsonPath("$.updatedAt", notNullValue()));
        }

        @Test
        @DisplayName("Should return existing users when getting all users")
        void shouldReturnExistingUsers() throws Exception {
                // Given - Create some test users via the service to ensure proper timestamp
                // handling
                CreateUserRequest request1 = new CreateUserRequest();
                request1.setName("John Doe");
                request1.setEmail("john@example.com");

                CreateUserRequest request2 = new CreateUserRequest();
                request2.setName("Jane Smith");
                request2.setEmail("jane@example.com");

                // Create users via API to ensure proper handling
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request1)))
                                .andExpect(status().isCreated());

                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request2)))
                                .andExpect(status().isCreated());

                // When & Then
                mockMvc.perform(get("/api/users"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("Should return 400 when creating user with missing name")
        void shouldReturn400WithMissingName() throws Exception {
                // Given
                CreateUserRequest request = new CreateUserRequest();
                request.setEmail("john@example.com");
                // name is missing

                // When & Then
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when creating user with missing email")
        void shouldReturn400WithMissingEmail() throws Exception {
                // Given
                CreateUserRequest request = new CreateUserRequest();
                request.setName("John Doe");
                // email is missing

                // When & Then
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when creating user with invalid email format")
        void shouldReturn400WithInvalidEmail() throws Exception {
                // Given
                CreateUserRequest request = new CreateUserRequest();
                request.setName("John Doe");
                request.setEmail("invalid-email-format");

                // When & Then
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when creating user with empty name")
        void shouldReturn400WithEmptyName() throws Exception {
                // Given
                CreateUserRequest request = new CreateUserRequest();
                request.setName("");
                request.setEmail("john@example.com");

                // When & Then
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle duplicate email gracefully")
        void shouldHandleDuplicateEmailGracefully() throws Exception {
                // Given - Create a user first via API
                CreateUserRequest firstRequest = new CreateUserRequest();
                firstRequest.setName("Existing User");
                firstRequest.setEmail("duplicate@example.com");

                // Create the first user
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(firstRequest)))
                                .andExpect(status().isCreated());

                // Try to create another user with the same email
                CreateUserRequest duplicateRequest = new CreateUserRequest();
                duplicateRequest.setName("New User");
                duplicateRequest.setEmail("duplicate@example.com"); // Same email

                // When & Then
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(duplicateRequest)))
                                .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 400 with malformed JSON")
        void shouldReturn400WithMalformedJson() throws Exception {
                // When & Then
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{invalid json}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 415 when content type is missing")
        void shouldReturn415WithMissingContentType() throws Exception {
                // Given
                CreateUserRequest request = new CreateUserRequest();
                request.setName("John Doe");
                request.setEmail("john@example.com");

                // When & Then
                mockMvc.perform(post("/api/users")
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("Should handle multiple user creation and retrieval")
        void shouldHandleMultipleUserCreationAndRetrieval() throws Exception {
                // Given - Create multiple users via API
                CreateUserRequest request1 = new CreateUserRequest();
                request1.setName("User One");
                request1.setEmail("user1@example.com");

                CreateUserRequest request2 = new CreateUserRequest();
                request2.setName("User Two");
                request2.setEmail("user2@example.com");

                // When - Create first user
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request1)))
                                .andExpect(status().isCreated());

                // Create second user
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request2)))
                                .andExpect(status().isCreated());

                // Then - Verify both users can be retrieved
                mockMvc.perform(get("/api/users"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(2)));
        }
}
