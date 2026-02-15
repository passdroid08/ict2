package com.ict.project.simulator.model;

import com.ict.project.model.Users;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "AI_INFERENCE_LOG")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AiInferenceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AI_INFERENCE_LOG_GEN")
    @SequenceGenerator(name = "SEQ_AI_INFERENCE_LOG_GEN", sequenceName = "SEQ_AI_INFERENCE_LOG", allocationSize = 1)
    @Column(name = "INFER_ID", nullable = false)
    private Long inferId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private Users users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FEATURE_ID")
    private AiFeatureSnapshot featureSnapshot;

    @Column(name = "REQUEST_TYPE", length = 50)
    private String requestType;

    @Column(name = "REQUESTED_AT")
    private LocalDateTime requestedAt;

    @Column(name = "RESULT_SUMMARY", length = 2000)
    private String resultSummary;

    @Column(name = "STATUS", length = 20)
    private String status;

    @Column(name = "ERROR_MSG", length = 2000)
    private String errorMsg;
}
