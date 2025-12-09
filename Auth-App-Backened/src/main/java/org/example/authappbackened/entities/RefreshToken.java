package org.example.authappbackened.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token_jwt_id", columnList = "jwt_id", unique = true),
        @Index(name = "idx_refresh_token_user_id", columnList = "user_id")
})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "jwt_id", nullable = false, updatable = false)
    private String jwtId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    private boolean revoked = false;

    private String replacedByToken;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
