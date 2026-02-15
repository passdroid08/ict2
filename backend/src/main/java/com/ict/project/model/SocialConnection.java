package com.ict.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "SOCIAL_CONNECTION")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SocialConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SOCIAL_CONNECTION_GEN")
    @SequenceGenerator(name = "SEQ_SOCIAL_CONNECTION_GEN", sequenceName = "SEQ_SOCIAL_CONNECTION", allocationSize = 1)
    @Column(name = "SOCIAL_ID", nullable = false)
    private Long socialId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private Users users;

    @Column(name = "PROVIDER", length = 20, nullable = false)
    private String provider;

    @Column(name = "PROVIDER_UID", length = 100, nullable = false)
    private String providerUid;

    @Column(name = "CONNECTED_AT")
    private LocalDateTime connectedAt;

    @Column(name = "STATUS", length = 20)
    private String status;
}
