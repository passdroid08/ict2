package com.ict.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "USER_INTEREST_REGION",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_UIR_USER_REGION",
            columnNames = {"USER_ID", "REGION_CODE"}
        )
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserInterestRegionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_USER_INTEREST_REGION_GEN")
    @SequenceGenerator(name = "SEQ_USER_INTEREST_REGION_GEN", sequenceName = "SEQ_USER_INTEREST_REGION", allocationSize = 1)
    @Column(name = "INTEREST_ID", nullable = false)
    private Long interestId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private UsersEntity users;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "REGION_CODE", nullable = false)
    private LocationEntity location;

    @Column(name = "INTERESTED_AT")
    private LocalDateTime interestedAt;
}
