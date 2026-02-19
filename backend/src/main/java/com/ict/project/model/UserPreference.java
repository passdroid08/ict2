package com.ict.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
    name = "USER_PREFERENCE",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_UP_USER_PREFKEY",
            columnNames = {"USER_ID", "PREF_KEY"}
        )
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_USER_PREFERENCE_GEN")
    @SequenceGenerator(name = "SEQ_USER_PREFERENCE_GEN", sequenceName = "SEQ_USER_PREFERENCE", allocationSize = 1)
    @Column(name = "PREF_ID", nullable = false)
    private Long prefId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private Users users;

    @Column(name = "PREF_KEY", length = 50, nullable = false)
    private String prefKey;

    @Column(name = "PREF_WEIGHT", precision = 5, scale = 2)
    private BigDecimal prefWeight;

    @Column(name = "NOTE", length = 200)
    private String note;
}
