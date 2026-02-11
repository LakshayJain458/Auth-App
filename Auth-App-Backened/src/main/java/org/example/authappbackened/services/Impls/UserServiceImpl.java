package org.example.authappbackened.services.Impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.authappbackened.dtos.UserDto;
import org.example.authappbackened.entities.User;
import org.example.authappbackened.entities.enums.Provider;
import org.example.authappbackened.exceptions.ResourceNotFoundException;
import org.example.authappbackened.repositories.RefreshTokenRepo;
import org.example.authappbackened.repositories.UserRepo;
import org.example.authappbackened.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final RefreshTokenRepo refreshTokenRepo;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public UserDto createUser(final UserDto userDto) {
        log.info("Creating new user with email: {}", userDto.getEmail());

        validateUserDto(userDto);
        validateEmailUniqueness(userDto.getEmail());

        final User user = mapToUser(userDto);
        setDefaultProviderIfNeeded(user, userDto.getProvider());

        final User savedUser = userRepo.save(user);
        log.info("User created successfully with id: {} and email: {}", savedUser.getId(), savedUser.getEmail());

        return mapToUserDto(savedUser);
    }

    @Override
    public Iterable<UserDto> getAllUsers() {
        log.debug("Fetching all users");
        final List<User> users = userRepo.findAll();
        log.debug("Found {} users", users.size());
        return users.stream()
                .map(this::mapToUserDto)
                .toList();
    }

    @Override
    public UserDto getUserById(final String id) {
        log.debug("Fetching user by id: {}", id);
        final UUID userId = parseUserId(id);
        final User user = findUserByIdOrThrow(userId);
        return mapToUserDto(user);
    }

    @Override
    public UserDto getUserByEmail(final String email) {
        log.debug("Fetching user by email: {}", email);
        validateEmailNotBlank(email);
        final User user = findUserByEmailOrThrow(email);
        return mapToUserDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUser(final String id, final UserDto userDto) {
        log.info("Updating user with id: {}", id);

        if (userDto == null) {
            log.error("Update failed: UserDto is null for id: {}", id);
            throw new IllegalArgumentException("User data cannot be null");
        }

        final UUID userId = parseUserId(id);
        final User existingUser = findUserByIdOrThrow(userId);

        updateUserFields(existingUser, userDto);

        final User updatedUser = userRepo.save(existingUser);
        log.info("User updated successfully with id: {}", updatedUser.getId());

        return mapToUserDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(final String id) {
        log.info("Deleting user with id: {}", id);
        final UUID userId = parseUserId(id);
        final User user = findUserByIdOrThrow(userId);
        refreshTokenRepo.deleteByUserId(userId);
        userRepo.delete(user);
        log.info("User deleted successfully with id: {}", id);
    }

    // ========== Validation Methods ==========

    private void validateUserDto(final UserDto userDto) {
        if (userDto == null) {
            log.error("User creation failed: UserDto is null");
            throw new IllegalArgumentException("User data cannot be null");
        }
        validateEmailNotBlank(userDto.getEmail());
    }

    private void validateEmailNotBlank(final String email) {
        if (email == null || email.isBlank()) {
            log.error("Validation failed: Email is null or empty");
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
    }

    private void validateEmailUniqueness(final String email) {
        if (userRepo.existsByEmail(email)) {
            log.error("User creation failed: Email already exists: {}", email);
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }
    }

    // ========== Lookup Methods ==========

    private User findUserByIdOrThrow(final UUID userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", userId);
                    return new ResourceNotFoundException("User with id " + userId + " not found");
                });
    }

    private User findUserByEmailOrThrow(final String email) {
        return userRepo.findUserByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new ResourceNotFoundException("User with email " + email + " not found");
                });
    }

    // ========== Mapping Methods ==========

    private User mapToUser(final UserDto userDto) {
        return modelMapper.map(userDto, User.class);
    }

    private UserDto mapToUserDto(final User user) {
        return modelMapper.map(user, UserDto.class);
    }

    // ========== Helper Methods ==========

    private UUID parseUserId(final String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format: {}", id);
            throw new IllegalArgumentException("Invalid user ID format: " + id, e);
        }
    }

    private void setDefaultProviderIfNeeded(final User user, final Provider provider) {
        if (provider == null) {
            user.setProvider(Provider.LOCAL);
        }
    }

    private void updateUserFields(final User existingUser, final UserDto userDto) {
        if (userDto.getUsername() != null && !userDto.getUsername().isBlank()) {
            existingUser.setUsername(userDto.getUsername());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            existingUser.setEmail(userDto.getEmail());
        }
        if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
            existingUser.setPassword(userDto.getPassword());
        }
        if (userDto.getImage() != null) {
            existingUser.setImage(userDto.getImage());
        }
        if (userDto.getProvider() != null) {
            existingUser.setProvider(userDto.getProvider());
        }
    }
}

