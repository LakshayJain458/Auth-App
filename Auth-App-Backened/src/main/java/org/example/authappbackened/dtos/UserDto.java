package org.example.authappbackened.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.authappbackened.entities.enums.Provider;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private UUID id;
    private String email;
    private String username;
    private String password;
    private String image;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
    private Provider provider;
    private Set<RoleDto> roles = new HashSet<>();
}
