package com.app.entity;

import com.app.DTOs.ERole;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a role in the system
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private ERole name;

    public Role(ERole name) {
        this.name = name;
    }
}