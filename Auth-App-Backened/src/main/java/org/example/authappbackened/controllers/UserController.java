package org.example.authappbackened.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.authappbackened.dtos.UserDto;
import org.example.authappbackened.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/add")
    public ResponseEntity<UserDto> createUser(@RequestBody final UserDto userDto) {
        log.info("Received request to create user with email: {}", userDto != null ? userDto.getEmail() : "null");

        final UserDto createdUser = userService.createUser(userDto);

        log.info("User created successfully with id: {}", createdUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/getAll")
    public ResponseEntity<Iterable<UserDto>> getAllUsers() {
        log.info("Received request to fetch all users");

        final Iterable<UserDto> users = userService.getAllUsers();

        log.info("Returning all users");
        return ResponseEntity.ok(users);
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable final String id) {
        log.info("Received request to fetch user by id: {}", id);

        final UserDto user = userService.getUserById(id);

        log.info("User found with id: {}", id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/getByEmail/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable final String email) {
        log.info("Received request to fetch user by email: {}", email);

        final UserDto user = userService.getUserByEmail(email);

        log.info("User found with email: {}", email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable final String id,
            @RequestBody final UserDto userDto) {
        log.info("Received request to update user with id: {}", id);

        final UserDto updatedUser = userService.updateUser(id, userDto);

        log.info("User updated successfully with id: {}", id);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable final String id) {
        log.info("Received request to delete user with id: {}", id);

        userService.deleteUser(id);

        log.info("User deleted successfully with id: {}", id);
        return ResponseEntity.noContent().build();
    }
}
