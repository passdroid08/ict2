package com.ict.project.simulator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.ict.project.entity.PropertyEntity;
import com.ict.project.entity.UsersEntity;

@Entity
@Table(name = "AI_FEATURE_SNAPSHOT")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AiFeatureSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AI_FEATURE_SNAPSHOT_GEN")
    @SequenceGenerator(name = "SEQ_AI_FEATURE_SNAPSHOT_GEN", sequenceName = "SEQ_AI_FEATURE_SNAPSHOT", allocationSize = 1)
    @Column(name = "FEATURE_ID", nullable = false)
    private Long featureId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private UsersEntity users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROPERTY_ID")
    private PropertyEntity property;

    @Column(name = "FEATURE_SNAPSHOT_AT")
    private LocalDateTime featureSnapshotAt;

    @Lob
    @Column(name = "FEATURES_JSON")
    private String featuresJson;

    @Column(name = "SOURCE_NOTE", length = 200)
    private String sourceNote;
}
