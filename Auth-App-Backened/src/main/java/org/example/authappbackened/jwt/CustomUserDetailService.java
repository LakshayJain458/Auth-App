package org.example.authappbackened.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.authappbackened.repositories.UserRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        log.debug("Loading user by username (email): {}", username);

        if (username == null || username.isBlank()) {
            log.error("Username cannot be null or empty");
            throw new UsernameNotFoundException("Username cannot be null or empty");
        }

        return userRepo.findUserByEmail(username)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", username);
                    return new UsernameNotFoundException("User with email " + username + " not found");
                });
    }
}
