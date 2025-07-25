package com.smartorder.userservice.service;

import com.smartorder.userservice.dto.CreateUserRequest;
import com.smartorder.userservice.dto.UserResponse;
import com.smartorder.userservice.exception.EmailAlreadyUsedException;
import com.smartorder.userservice.model.User;
import com.smartorder.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public UserResponse createUser(CreateUserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new EmailAlreadyUsedException("Email already in use");
        }

        User user = userRepository.save(User.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .build());
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

}
