package com.ict.project.policy.model;

import com.ict.project.model.Location;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "POLICY")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PolicyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_POLICY_GEN")
    @SequenceGenerator(name = "SEQ_POLICY_GEN", sequenceName = "SEQ_POLICY", allocationSize = 1)
    @Column(name = "POLICY_ID", nullable = false)
    private Long policyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REGION_CODE")
    private Location location;

    @Column(name = "POLICY_NAME", length = 100)
    private String policyName;

    @Column(name = "TARGET_TYPE", length = 30)
    private String targetType;

    @Column(name = "POLICY_CATEGORY", length = 30)
    private String policyCategory;
}
