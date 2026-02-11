package org.example.authappbackened.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.authappbackened.entities.User;
import org.example.authappbackened.repositories.UserRepo;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = 7;

    private final JwtService jwtService;
    private final UserRepo userRepo;

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (!hasValidAuthorizationHeader(authorizationHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = extractToken(authorizationHeader);

        try {
            if (jwtService.isAccessToken(token)) {
                authenticateUser(token, request);
            } else {
                log.debug("Token is not an access token, skipping authentication");
            }
        } catch (ExpiredJwtException e) {
            log.warn("JWT token has expired: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("JWT authentication failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    // ========== Authentication Methods ==========

    private void authenticateUser(final String token, final HttpServletRequest request) {
        final Jws<Claims> parsedToken = jwtService.parse(token);
        final Claims payload = parsedToken.getPayload();
        final String userIdString = payload.getSubject();
        final UUID userId = parseUserId(userIdString);

        final Optional<User> userOptional = userRepo.findById(userId);

        if (userOptional.isEmpty()) {
            log.warn("User not found with id: {}", userId);
            return;
        }

        final User user = userOptional.get();

        if (!user.isEnabled()) {
            log.warn("User account is disabled: {}", user.getEmail());
            return;
        }

        setAuthentication(user, request);
    }

    private void setAuthentication(final User user, final HttpServletRequest request) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.debug("Authentication already set in SecurityContext");
            return;
        }

        final List<GrantedAuthority> authorities = extractAuthorities(user);

        final UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("User authenticated successfully: {}", user.getEmail());
    }

    // ========== Helper Methods ==========

    private boolean hasValidAuthorizationHeader(final String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX);
    }

    private String extractToken(final String authorizationHeader) {
        return authorizationHeader.substring(BEARER_PREFIX_LENGTH);
    }

    private UUID parseUserId(final String userIdString) {
        try {
            return UUID.fromString(userIdString);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format in token subject: {}", userIdString);
            throw new JwtException("Invalid user ID in token", e);
        }
    }

    private List<GrantedAuthority> extractAuthorities(final User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return Collections.emptyList();
        }
        return user.getRoles().stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role.getName()))
                .toList();
    }
}
