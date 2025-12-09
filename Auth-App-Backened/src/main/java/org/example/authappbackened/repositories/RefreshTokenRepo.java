package org.example.authappbackened.repositories;

import org.example.authappbackened.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByJwtId(String jwtId);

    void deleteByUserId(UUID userId);
}
