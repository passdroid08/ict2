package com.ict.project.policy.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "POLICY_EFFECT")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PolicyEffectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_POLICY_EFFECT_GEN")
    @SequenceGenerator(name = "SEQ_POLICY_EFFECT_GEN", sequenceName = "SEQ_POLICY_EFFECT", allocationSize = 1)
    @Column(name = "EFFECT_ID", nullable = false)
    private Long effectId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "POLICY_ID", nullable = false)
    private PolicyEntity policy;

    /**
     * 레거시 호환용 키(있으면 유지)
     * 예: MAX_LOAN_AMOUNT, LTV_BONUS, ACQUISITION_TAX_REDUCTION ...
     */
    @Column(name = "EFFECT_KEY", length = 50)
    private String effectKey;

    /**
     * 연산자 (기존 컬럼 유지)
     * 예: ADD, MULTIPLY, REPLACE
     */
    @Column(name = "OPERATOR", length = 10)
    private String operator;

    /**
     * 사람이 읽는 값(선택)
     */
    @Column(name = "EFFECT_VALUE", length = 100)
    private String effectValue;

    /**
     * 단위 (예: WON, PERCENT, YEAR, MONTH ...)
     */
    @Column(name = "UNIT", length = 10)
    private String unit;

    /**
     * 적용 우선순위 (낮을수록 먼저 적용)
     */
    @Column(name = "PRIORITY")
    private Integer priority;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    /**
     * 값 타입 (기존 컬럼 유지)
     * 예: NUM, STR, DATE, BOOL ...
     */
    @Column(name = "VALUE_TYPE", length = 10, nullable = false)
    private String valueType;

    @Column(name = "EFFECT_VALUE_NUM", precision = 20, scale = 6)
    private BigDecimal effectValueNum;

    @Column(name = "EFFECT_VALUE_DATE")
    private LocalDateTime effectValueDate;

    /**
     * 'Y'/'N' 같이 저장한다면 CHAR(1) 유지
     */
    @Column(name="EFFECT_VALUE_BOOL", columnDefinition="CHAR(1)")
    private String effectValueBool;

    // =========================================================
    // ✅ 신규 컬럼 (새 구조 핵심)
    // =========================================================

    /**
     * 언제 적용하나?
     * - INPUT  : 재무 계산 전에 FinanceInput을 수정
     * - RESULT : 재무 계산 후 FinanceResult를 수정(정액 추가, 세금 감면 등)
     */
    @Column(name = "EFFECT_STAGE", length = 10, nullable = false)
    private String effectStage; // "INPUT" or "RESULT"

    /**
     * 무엇을 바꾸나? (계산 엔진에서 인식할 대상 필드)
     * 예: LTV_LIMIT, INTEREST_RATE, TERM_MONTHS, LOAN_LIMIT, TAX_AMOUNT, MONTHLY_PAYMENT ...
     */
    @Column(name = "TARGET_FIELD", length = 30, nullable = false)
    private String targetField;
}