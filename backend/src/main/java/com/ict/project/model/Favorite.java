package com.ict.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "FAVORITE")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FAVORITE_GEN")
    @SequenceGenerator(name = "SEQ_FAVORITE_GEN", sequenceName = "SEQ_FAVORITE", allocationSize = 1)
    @Column(name = "FAVORITE_ID", nullable = false)
    private Long favoriteId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private Users users;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROPERTY_ID", nullable = false)
    private Property property;

    @Column(name = "FAVORITED_AT")
    private LocalDateTime favoritedAt;
    
}
