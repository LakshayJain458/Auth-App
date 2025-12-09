package org.example.authappbackened.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.example.authappbackened.entities.enums.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            final ResourceNotFoundException ex,
            final WebRequest request) {

        log.error("Resource not found: {} | Request: {}", ex.getMessage(), request.getDescription(false));

        final ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                HttpStatus.NOT_FOUND.value());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            final IllegalArgumentException ex,
            final WebRequest request) {

        log.error("Invalid argument: {} | Request: {}", ex.getMessage(), request.getDescription(false));

        final ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler({BadCredentialsException.class, DisabledException.class, AuthenticationException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            final Exception ex,
            final WebRequest request) {

        log.warn("Authentication failed: {} | Request: {}", ex.getMessage(), request.getDescription(false));

        final ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED,
                HttpStatus.UNAUTHORIZED.value());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            final Exception ex,
            final WebRequest request) {

        log.error("Unexpected error occurred: {} | Request: {}", ex.getMessage(), request.getDescription(false), ex);

        final ErrorResponse errorResponse = new ErrorResponse(
                "An unexpected error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
