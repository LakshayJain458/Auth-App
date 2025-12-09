package org.example.authappbackened.services.Impls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.authappbackened.dtos.UserDto;
import org.example.authappbackened.services.AuthService;
import org.example.authappbackened.services.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto registerUser(final UserDto userDto) {
        log.info("Registering new user with email: {}", userDto.getEmail());
        if (userDto == null) {
            log.error("User registration failed: UserDto is null");
            throw new IllegalArgumentException("User data cannot be null");
        }
        if (userDto.getPassword() == null || userDto.getPassword().isBlank()) {
            log.error("User registration failed: Password is missing for email: {}", userDto.getEmail());
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        final String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        userDto.setPassword(encodedPassword);
        final UserDto registeredUser = userService.createUser(userDto);
        log.info("User registered successfully with email: {}", registeredUser.getEmail());
        return registeredUser;
    }
}
