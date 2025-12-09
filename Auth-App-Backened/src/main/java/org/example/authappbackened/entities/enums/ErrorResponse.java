package org.example.authappbackened.entities.enums;

import org.springframework.http.HttpStatus;

public record ErrorResponse(
        String message,
        HttpStatus status,
        int code
) {
}
