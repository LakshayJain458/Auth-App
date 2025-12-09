package org.example.authappbackened.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.authappbackened.dtos.UserDto;
import org.example.authappbackened.entities.RefreshToken;
import org.example.authappbackened.entities.User;
import org.example.authappbackened.entities.enums.LoginRequest;
import org.example.authappbackened.entities.enums.TokenResponse;
import org.example.authappbackened.jwt.CookieService;
import org.example.authappbackened.jwt.JwtService;
import org.example.authappbackened.repositories.RefreshTokenRepo;
import org.example.authappbackened.repositories.UserRepo;
import org.example.authappbackened.services.AuthService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepo;
    private final RefreshTokenRepo refreshTokenRepo;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final ModelMapper modelMapper;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody final UserDto userDto) {
        log.info("Received registration request for email: {}", userDto != null ? userDto.getEmail() : "null");
        final UserDto registeredUser = authService.registerUser(userDto);
        log.info("User registered successfully with email: {}", registeredUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        log.info("Received login request for email: {}", loginRequest != null ? loginRequest.email() : "null");
        if (loginRequest == null || loginRequest.email() == null || loginRequest.password() == null) {
            log.error("Login request is missing email or password");
            throw new BadCredentialsException("Email and password must be provided");
        }
        Authentication authenticatedUser = authenticateUser(loginRequest.email(), loginRequest.password());
        User user = userRepo.findUserByEmail(loginRequest.email()).orElseThrow(() -> {
            log.error("User not found after authentication for email: {}", loginRequest.email());
            return new BadCredentialsException("User not found");
        });
        if (!user.isEnabled()) {
            log.error("User account is not enabled for email: {}", loginRequest.email());
            throw new DisabledException("User account is not enabled");
        }

        String jwtId = UUID.randomUUID().toString();
        var refreshTokenDb = RefreshToken.builder()
                .jwtId(jwtId)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();
        refreshTokenRepo.save(refreshTokenDb);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, refreshTokenDb.getJwtId());
        cookieService.attachRefreshTokenCookie(response, refreshToken, (int) jwtService.getRefreshTtlSeconds());
        cookieService.addNoCacheHeaders(response);
        TokenResponse tokenResponse = TokenResponse.createTokenResponse(accessToken, refreshToken, jwtService.getAccessTtlSeconds(), modelMapper.map(user, UserDto.class));
        log.info("User logged in successfully with email: {}", loginRequest.email());
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("Received logout request");
        readRefreshTokenFromRequest(null, request).ifPresent(token -> {
            try {
                if (jwtService.isRefreshToken(token)) {
                    String jwtId = jwtService.getJwtId(token);
                    refreshTokenRepo.findByJwtId(jwtId).ifPresent(rt -> {
                        rt.setRevoked(true);
                        refreshTokenRepo.save(rt);
                        log.info("Refresh token revoked for JWT ID: {}", jwtId);
                    });
                }
            } catch (Exception ignored) {
            }
        });
        cookieService.clearRefreshTokenCookie(response);
        cookieService.addNoCacheHeaders(response);
        SecurityContextHolder.clearContext();
        log.info("User logged out successfully");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody(required = false) String refreshToken, HttpServletResponse response, HttpServletRequest request) {
        log.info("Received token refresh request");
        String refreshTokenStr = readRefreshTokenFromRequest(refreshToken, request).orElseThrow(() -> {
            log.error("Refresh token not found in request");
            return new BadCredentialsException("Refresh token is missing");
        });
        if (!jwtService.isRefreshToken(refreshTokenStr)) {
            throw new BadCredentialsException("Refresh token is invalid");
        }
        String jwtId = jwtService.getJwtId(refreshTokenStr);
        UUID userId = jwtService.getUserId(refreshTokenStr);
        RefreshToken storedRefreshToken = refreshTokenRepo.findByJwtId(jwtId).orElseThrow(() -> {
            log.error("Refresh token not found in database for JWT ID: {}", jwtId);
            return new BadCredentialsException("Refresh token is invalid");
        });
        if (storedRefreshToken.getUser().getId() != userId) {
            log.error("Refresh token user ID does not match for JWT ID: {}", jwtId);
            throw new BadCredentialsException("Refresh token is invalid");
        }
        if (storedRefreshToken.isRevoked()) {
            log.error("Refresh token has been revoked for JWT ID: {}", jwtId);
            throw new BadCredentialsException("Refresh token has been revoked");
        }
        if (storedRefreshToken.getExpiresAt().isBefore(Instant.now())) {
            log.error("Refresh token has expired for JWT ID: {}", jwtId);
            throw new BadCredentialsException("Refresh token has expired");
        }

        storedRefreshToken.setRevoked(true);
        String newJwtId = UUID.randomUUID().toString();
        storedRefreshToken.setReplacedByToken(newJwtId);
        refreshTokenRepo.save(storedRefreshToken);

        User user = storedRefreshToken.getUser();
        var newRefreshTokenDb = RefreshToken.builder()
                .jwtId(newJwtId)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();
        refreshTokenRepo.save(newRefreshTokenDb);

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user, newRefreshTokenDb.getJwtId());
        cookieService.attachRefreshTokenCookie(response, newRefreshToken, (int) jwtService.getRefreshTtlSeconds());
        cookieService.addNoCacheHeaders(response);
        TokenResponse tokenResponse = TokenResponse.createTokenResponse(newAccessToken, newRefreshToken, jwtService.getAccessTtlSeconds(), modelMapper.map(user, UserDto.class));

        log.info("Token refreshed successfully");
        return ResponseEntity.ok(tokenResponse);
    }

    private Optional<String> readRefreshTokenFromRequest(String refreshToken, HttpServletRequest request) {
        if (request.getCookies() != null) {
            Optional<String> fromCookie = Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals(cookieService.getRefreshTokenCookieName()))
                    .map(Cookie::getValue)
                    .filter(s -> !s.isBlank())
                    .findFirst();
            if (fromCookie.isPresent()) {
                return fromCookie;
            }
        }

        if (refreshToken != null && !refreshToken.isBlank()) {
            return Optional.of(refreshToken);
        }

        String refreshHeader = request.getHeader("X-Refresh-Token");
        if (refreshHeader != null && !refreshHeader.isBlank()) {
            return Optional.of(refreshHeader.trim());
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 4)) {
            String token = authHeader.substring(7).trim();
            if (!token.isEmpty()) {
                try {
                    if (jwtService.isRefreshToken(token)) {
                        return Optional.of(token);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return Optional.empty();
    }

    private Authentication authenticateUser(String email, String password) {
        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (Exception e) {
            log.error("Authentication failed for email: {}", email, e);
            throw new BadCredentialsException("Invalid credentials");
        }
    }
}
