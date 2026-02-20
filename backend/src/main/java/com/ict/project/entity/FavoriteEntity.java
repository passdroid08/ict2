package com.ict.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "FAVORITE",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_FAVORITE_USER_PROPERTY",
            columnNames = {"USER_ID", "PROPERTY_ID"}
        )
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FavoriteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FAVORITE_GEN")
    @SequenceGenerator(name = "SEQ_FAVORITE_GEN", sequenceName = "SEQ_FAVORITE", allocationSize = 1)
    @Column(name = "FAVORITE_ID", nullable = false)
    private Long favoriteId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private UsersEntity users;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROPERTY_ID", nullable = false)
    private PropertyEntity property;

    @Column(name = "FAVORITED_AT")
    private LocalDateTime favoritedAt;
    
}
