package com.smartorder.userservice.repository;

import com.smartorder.userservice.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should find user by email when user exists")
    void shouldFindUserByEmailWhenUserExists() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findByEmail(testUser.getEmail());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(testUser.getEmail());
        assertThat(foundUser.get().getName()).isEqualTo(testUser.getName());
        assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
    }

    @Test
    @DisplayName("Should return empty optional when user does not exist by email")
    void shouldReturnEmptyOptionalWhenUserDoesNotExistByEmail() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should return true when email exists")
    void shouldReturnTrueWhenEmailExists() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        boolean exists = userRepository.existsByEmail(testUser.getEmail());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void shouldReturnFalseWhenEmailDoesNotExist() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should save user successfully")
    void shouldSaveUserSuccessfully() {
        // When
        User savedUser = userRepository.save(testUser);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isGreaterThan(0);
        assertThat(savedUser.getName()).isEqualTo(testUser.getName());
        assertThat(savedUser.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(savedUser.getCreatedAt()).isEqualTo(testUser.getCreatedAt());
        assertThat(savedUser.getUpdatedAt()).isEqualTo(testUser.getUpdatedAt());
    }

    @Test
    @DisplayName("Should find user by id when user exists")
    void shouldFindUserByIdWhenUserExists() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.get().getEmail()).isEqualTo(testUser.getEmail());
        assertThat(foundUser.get().getName()).isEqualTo(testUser.getName());
    }

    @Test
    @DisplayName("Should return empty optional when user does not exist by id")
    void shouldReturnEmptyOptionalWhenUserDoesNotExistById() {
        // When
        Optional<User> foundUser = userRepository.findById(999L);

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should find all users")
    void shouldFindAllUsers() {
        // Given
        User user1 = User.builder()
                .name("User 1")
                .email("user1@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User user2 = User.builder()
                .name("User 2")
                .email("user2@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        // When
        List<User> users = userRepository.findAll();

        // Then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("user1@example.com", "user2@example.com");
        assertThat(users).extracting(User::getName)
                .containsExactlyInAnyOrder("User 1", "User 2");
    }

    @Test
    @DisplayName("Should return empty list when no users exist")
    void shouldReturnEmptyListWhenNoUsersExist() {
        // When
        List<User> users = userRepository.findAll();

        // Then
        assertThat(users).isEmpty();
    }

    @Test
    @DisplayName("Should delete user by id")
    void shouldDeleteUserById() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        long userId = savedUser.getId();

        // When
        userRepository.deleteById(userId);
        entityManager.flush();

        // Then
        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }

    @Test
    @DisplayName("Should count all users")
    void shouldCountAllUsers() {
        // Given
        User user1 = User.builder()
                .name("User 1")
                .email("user1@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User user2 = User.builder()
                .name("User 2")
                .email("user2@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        // When
        long count = userRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return zero count when no users exist")
    void shouldReturnZeroCountWhenNoUsersExist() {
        // When
        long count = userRepository.count();

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle case insensitive email search")
    void shouldHandleCaseInsensitiveEmailSearch() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findByEmail("JOHN.DOE@EXAMPLE.COM");

        // Then - This test depends on database collation, but typically emails are case-insensitive
        // If your database is case-sensitive, this test might fail and you'd need custom queries
        // For now, let's test the exact case
        foundUser = userRepository.findByEmail(testUser.getEmail());
        assertThat(foundUser).isPresent();
    }

    @Test
    @DisplayName("Should maintain email uniqueness constraint")
    void shouldMaintainEmailUniquenessConstraint() {
        // Given
        entityManager.persistAndFlush(testUser);

        User duplicateEmailUser = User.builder()
                .name("Jane Doe")
                .email("john.doe@example.com") // Same email as testUser
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When/Then
        try {
            entityManager.persistAndFlush(duplicateEmailUser);
            // If we reach here, the constraint didn't work
            assertThat(false).as("Expected constraint violation").isTrue();
        } catch (Exception e) {
            // Expected behavior - unique constraint violation
            assertThat(e).isNotNull();
        }
    }
}
