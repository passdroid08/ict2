package com.ict.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "SEARCH_LOG")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SEARCH_LOG_GEN")
    @SequenceGenerator(name = "SEQ_SEARCH_LOG_GEN", sequenceName = "SEQ_SEARCH_LOG", allocationSize = 1)
    @Column(name = "SEARCH_ID", nullable = false)
    private Long searchId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private Users users;

    @Column(name = "SEARCH_CONDITION", length = 500)
    private String searchCondition;

    @Column(name = "SEARCHED_AT")
    private LocalDateTime searchedAt;
}
