package org.example.authappbackened.entities.enums;

public record LoginRequest(
        String email,
        String password
) {
}
