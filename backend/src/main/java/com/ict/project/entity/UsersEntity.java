package com.ict.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "USERS",
    uniqueConstraints = {
        @UniqueConstraint(name = "UK_USERS_LOGIN_ID", columnNames = "LOGIN_ID"),
        @UniqueConstraint(name = "UK_USERS_EMAIL", columnNames = "EMAIL")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UsersEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_USERS_GEN")
    @SequenceGenerator(name = "SEQ_USERS_GEN", sequenceName = "SEQ_USERS", allocationSize = 1)
    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "LOGIN_ID", length = 50, nullable = false, unique = true)
    private String loginId;

    @Column(name = "EMAIL", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "REGISTERED_AT")
    private LocalDateTime registeredAt;

    @Column(name = "PASSWORD_HASH", length = 255)
    private String passwordHash;
    
}
