package org.example.authappbackened.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.authappbackened.entities.Role;
import org.example.authappbackened.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

import static io.jsonwebtoken.Jwts.SIG.HS512;

@Slf4j
@Data
@Service
public class JwtService {

    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";
    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLES = "roles";
    private static final int MIN_SECRET_KEY_LENGTH = 64;

    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;
    private final SecretKey key;
    private final String issuer;

    public JwtService(
            @Value("${security.jwt.access-ttl-seconds}") final long accessTtlSeconds,
            @Value("${security.jwt.refresh-ttl-seconds}") final long refreshTtlSeconds,
            @Value("${security.jwt.secret}") final String secretKey,
            @Value("${security.jwt.issuer}") final String issuer) {

        validateSecretKey(secretKey);
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        log.info("JwtService initialized with issuer: {}, access TTL: {}s, refresh TTL: {}s",
                issuer, accessTtlSeconds, refreshTtlSeconds);
    }

    public String generateAccessToken(final User user) {
        if (user == null) {
            log.error("Cannot generate access token: User is null");
            throw new IllegalArgumentException("User cannot be null");
        }

        log.debug("Generating access token for user: {}", user.getEmail());

        final Instant now = Instant.now();
        final List<String> roles = extractRoleNames(user);

        final String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlSeconds)))
                .claims(Map.of(
                        CLAIM_EMAIL, user.getEmail(),
                        CLAIM_ROLES, roles,
                        CLAIM_TYPE, TOKEN_TYPE_ACCESS
                ))
                .signWith(key, HS512)
                .compact();

        log.debug("Access token generated successfully for user: {}", user.getEmail());
        return token;
    }

    public String generateRefreshToken(final User user, final String jwtId) {
        if (user == null) {
            log.error("Cannot generate refresh token: User is null");
            throw new IllegalArgumentException("User cannot be null");
        }

        if (jwtId == null || jwtId.isBlank()) {
            log.error("Cannot generate refresh token: JWT ID is null or empty");
            throw new IllegalArgumentException("JWT ID cannot be null or empty");
        }

        log.debug("Generating refresh token for user: {}", user.getEmail());

        final Instant now = Instant.now();

        final String token = Jwts.builder()
                .id(jwtId)
                .issuer(issuer)
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTtlSeconds)))
                .claim(CLAIM_TYPE, TOKEN_TYPE_REFRESH)
                .signWith(key, HS512)
                .compact();

        log.debug("Refresh token generated successfully for user: {}", user.getEmail());
        return token;
    }

    public Jws<Claims> parse(final String token) {
        if (token == null || token.isBlank()) {
            log.error("Cannot parse token: Token is null or empty");
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
        } catch (JwtException e) {
            log.error("Failed to parse JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public boolean isAccessToken(final String token) {
        final Claims claims = parse(token).getPayload();
        final String tokenType = (String) claims.get(CLAIM_TYPE);
        return TOKEN_TYPE_ACCESS.equals(tokenType);
    }

    public boolean isRefreshToken(final String token) {
        final Claims claims = parse(token).getPayload();
        final String tokenType = (String) claims.get(CLAIM_TYPE);
        return TOKEN_TYPE_REFRESH.equals(tokenType);
    }

    // ========== Helper Methods ==========

    private void validateSecretKey(final String secretKey) {
        if (secretKey == null || secretKey.length() < MIN_SECRET_KEY_LENGTH) {
            log.error("Invalid secret key for JWT signing. It must be at least {} characters long", MIN_SECRET_KEY_LENGTH);
            throw new IllegalArgumentException(
                    "Invalid secret key for JWT signing. It must be at least " + MIN_SECRET_KEY_LENGTH + " characters long");
        }
    }

    private List<String> extractRoleNames(final User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return Collections.emptyList();
        }
        return user.getRoles().stream()
                .map(Role::getName)
                .toList();
    }

    public UUID getUserId(final String token) {
        final Claims claims = parse(token).getPayload();
        return UUID.fromString(claims.getSubject());
    }

    public String getJwtId(final String token) {
        return parse(token).getPayload().getId();
    }
}
