package org.example.authappbackened.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @PrePersist
    private void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }
}
