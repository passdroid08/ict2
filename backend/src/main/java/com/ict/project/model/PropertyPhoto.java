package com.ict.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "PROPERTY_PHOTO")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PropertyPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PROPERTY_PHOTO_GEN")
    @SequenceGenerator(name = "SEQ_PROPERTY_PHOTO_GEN", sequenceName = "SEQ_PROPERTY_PHOTO", allocationSize = 1)
    @Column(name = "PHOTO_ID", nullable = false)
    private Long photoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROPERTY_ID", nullable = false)
    private Property property;

    @Column(name = "PHOTO_URL", length = 500)
    private String photoUrl;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder;

    @Column(name = "UPLOADED_AT")
    private LocalDateTime uploadedAt;
}
