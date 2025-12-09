package org.example.authappbackened.repositories;

import org.example.authappbackened.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<User, UUID> {
    User findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findUserByEmail(String email);

    User findByUsername(String username);
}
