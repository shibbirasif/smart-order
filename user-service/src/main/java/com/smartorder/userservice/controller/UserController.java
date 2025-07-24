package com.smartorder.userservice.controller;

import com.smartorder.userservice.dto.CreateUserRequest;
import com.smartorder.userservice.dto.UserResponse;
import com.smartorder.userservice.model.User;
import com.smartorder.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest userRequest) {
        return ResponseEntity.ok(userService.createUser(userRequest));
    }
}
