package com.ict.project.policy.cal;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ict.project.policy.dto.EligibilityResultDto;
import com.ict.project.policy.dto.PolicyBaseDto;
import com.ict.project.policy.dto.PolicyImpactResultDto;
import com.ict.project.policy.dto.PolicyResultDto;
import com.ict.project.policy.dto.ResultDeltaDto;
import com.ict.project.policy.entity.PolicyEffectEntity;
import com.ict.project.policy.entity.PolicyEntity;
import com.ict.project.policy.repository.PolicyEffectRepository;
import com.ict.project.policy.repository.PolicyRepository;
import com.ict.project.simulator.dto.PolicyImpactDto;
import com.ict.project.simulator.dto.ProfileSnapshotDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PolicyImpactCalculator {

    private static final Logger log = LoggerFactory.getLogger(PolicyImpactCalculator.class);
    private static final boolean DBG = true;

    private static final String STATUS_APPLICABLE = "Applicable";
    private static final String STATUS_NOT_APPLICABLE = "NotApplicable";

    private final PolicyRepository policyRepository;
    private final PolicyEffectRepository effectRepository;
    private final PolicyEligibilityEvaluator eligibilityEvaluator;
    private final PolicyInputModifierApplier inputModifierApplier;
    private final PolicyResultImpactCalculator resultImpactCalculator;

    public PolicyResultDto evaluateEligibility(ProfileSnapshotDto profile, List<Long> selectedPolicyIds) {
        List<Long> selected = (selectedPolicyIds == null) ? List.of() : selectedPolicyIds;

        List<PolicyEntity> policies;
        try {
            policies = policyRepository.findAll();
        } catch (Exception e) {
            err("policyRepository.findAll() failed", e);
            policies = List.of();
        }
        if (policies == null) {
            policies = List.of();
        }

        List<PolicyImpactDto> policyList = new ArrayList<>();

        for (PolicyEntity policy : policies) {
            if (policy == null) {
                continue;
            }

            Long policyId = policy.getPolicyId();
            boolean isSelected = policyId != null && selected.contains(policyId);

            EligibilityResultDto eligibility;
            try {
                eligibility = eligibilityEvaluator.evaluate(profile, policy);
            } catch (Exception e) {
                err("eligibilityEvaluator.evaluate() failed for policyId=" + policyId, e);
                eligibility = EligibilityResultDto.builder()
                        .applicable(false)
                        .reasons(List.of("Eligibility evaluation failed"))
                        .conditions(List.of())
                        .build();
            }

            String reasonSummary = eligibility.isApplicable() ? STATUS_APPLICABLE : STATUS_NOT_APPLICABLE;
            List<String> reasons = new ArrayList<>();
            reasons.add(isSelected ? "Selected" : "Not selected");
            reasons.addAll(defaultList(eligibility.getReasons()));
            reasons.add(reasonSummary);

            policyList.add(PolicyImpactDto.builder()
                    .policyId(policyId)
                    .name(policy.getPolicyName())
                    .impactAmount(0L)
                    .impactPercent(null)
                    .monthlyImpact(0L)
                    .reasons(reasons)
                    .reasonSummary(reasonSummary)
                    .conditions(defaultList(eligibility.getConditions()))
                    .caution(null)
                    .build());
        }

        return PolicyResultDto.builder()
                .policyList(policyList)
                .selectedPolicyIds(selected)
                .build();
    }

    public PolicyImpactResultDto applyImpact(PolicyResultDto policyResult, PolicyBaseDto base) {
        if (policyResult == null) {
            return PolicyImpactResultDto.builder()
                    .previewTotalLoanDelta(0L)
                    .previewTotalMonthlyDelta(0L)
                    .previewTotalTaxDelta(0L)
                    .totalLoanDelta(0L)
                    .totalMonthlyDelta(0L)
                    .totalTaxDelta(0L)
                    .appliedPolicyIds(List.of())
                    .policyList(List.of())
                    .build();
        }

        List<PolicyImpactDto> list = defaultList(policyResult.getPolicyList());
        List<Long> selected = defaultList(policyResult.getSelectedPolicyIds());

        PolicyBaseDto calcBase = PolicyBaseDto.builder()
                .loanBaseAmount(base == null ? 0L : base.getLoanBaseAmount())
                .taxBaseAmount(base == null ? 0L : base.getTaxBaseAmount())
                .monthlyBaseAmount(base == null ? 0L : base.getMonthlyBaseAmount())
                .build();

        long previewLoan = 0L;
        long previewMonthly = 0L;
        long previewTax = 0L;

        long appliedLoan = 0L;
        long appliedMonthly = 0L;
        long appliedTax = 0L;

        List<Long> appliedPolicyIds = new ArrayList<>();
        List<PolicyImpactDto> out = new ArrayList<>();

        for (PolicyImpactDto dto : list) {
            if (dto == null) {
                continue;
            }

            Long policyId = dto.getPolicyId();
            boolean isSelected = policyId != null && selected.contains(policyId);
            boolean applicable = STATUS_APPLICABLE.equals(dto.getReasonSummary());

            List<String> reasons = new ArrayList<>(defaultList(dto.getReasons()));

            ResultDeltaDto delta;
            try {
                delta = resultImpactCalculator.computeResultDeltaFromDb(policyId, calcBase, reasons);
            } catch (Exception e) {
                err("computeResultDeltaFromDb() failed for policyId=" + policyId, e);
                delta = ResultDeltaDto.builder()
                        .loanDelta(0L)
                        .monthlyDelta(0L)
                        .taxDelta(0L)
                        .build();
                reasons.add("Failed to calculate impact; defaulted to 0");
            }

            if (!applicable) {
                reasons.add("Policy not applicable; impact shown as preview only");
            }

            if (applicable) {
                previewLoan += delta.getLoanDelta();
                previewMonthly += delta.getMonthlyDelta();
                previewTax += delta.getTaxDelta();
            }

            if (applicable && isSelected) {
                appliedLoan += delta.getLoanDelta();
                appliedMonthly += delta.getMonthlyDelta();
                appliedTax += delta.getTaxDelta();
                appliedPolicyIds.add(policyId);
            }

            out.add(PolicyImpactDto.builder()
                    .policyId(policyId)
                    .name(dto.getName())
                    .impactAmount(delta.getLoanDelta())
                    .impactPercent(null)
                    .monthlyImpact(delta.getMonthlyDelta())
                    .reasons(reasons)
                    .reasonSummary(dto.getReasonSummary())
                    .conditions(dto.getConditions())
                    .caution(dto.getCaution())
                    .build());
        }

        return PolicyImpactResultDto.builder()
                .previewTotalLoanDelta(previewLoan)
                .previewTotalMonthlyDelta(previewMonthly)
                .previewTotalTaxDelta(previewTax)
                .totalLoanDelta(appliedLoan)
                .totalMonthlyDelta(appliedMonthly)
                .totalTaxDelta(appliedTax)
                .appliedPolicyIds(appliedPolicyIds)
                .policyList(out)
                .build();
    }

    public PolicyImpactResultDto applyImpactAndAddInputDelta(
            PolicyResultDto policyResult,
            PolicyBaseDto base,
            long beforeLoanLimit,
            long afterLoanLimit,
            long beforeMonthlyPayment,
            long afterMonthlyPayment,
            long beforeTaxAmount,
            long afterTaxAmount
    ) {
        PolicyImpactResultDto result = applyImpact(policyResult, base);

        long inputLoanDelta = afterLoanLimit - beforeLoanLimit;
        long inputMonthlyDelta = afterMonthlyPayment - beforeMonthlyPayment;
        long inputTaxDelta = afterTaxAmount - beforeTaxAmount;

        if (result == null) {
            return PolicyImpactResultDto.builder()
                    .previewTotalLoanDelta(0L)
                    .previewTotalMonthlyDelta(0L)
                    .previewTotalTaxDelta(0L)
                    .totalLoanDelta(inputLoanDelta)
                    .totalMonthlyDelta(inputMonthlyDelta)
                    .totalTaxDelta(inputTaxDelta)
                    .appliedPolicyIds(List.of())
                    .policyList(List.of())
                    .build();
        }

        return PolicyImpactResultDto.builder()
                .previewTotalLoanDelta(result.getPreviewTotalLoanDelta())
                .previewTotalMonthlyDelta(result.getPreviewTotalMonthlyDelta())
                .previewTotalTaxDelta(result.getPreviewTotalTaxDelta())
                .totalLoanDelta(result.getTotalLoanDelta() + inputLoanDelta)
                .totalMonthlyDelta(result.getTotalMonthlyDelta() + inputMonthlyDelta)
                .totalTaxDelta(result.getTotalTaxDelta() + inputTaxDelta)
                .appliedPolicyIds(result.getAppliedPolicyIds())
                .policyList(result.getPolicyList())
                .build();
    }

    public <T> T applyInputModifiers(PolicyResultDto policyResult, T financeInput) {
        return applyInputModifiers(policyResult, financeInput, null);
    }

    public <T> T applyInputModifiers(PolicyResultDto policyResult, T financeInput, Long baselineLoanLimit) {
        if (policyResult == null || financeInput == null) {
            return financeInput;
        }

        List<PolicyImpactDto> list = defaultList(policyResult.getPolicyList());
        List<Long> selected = defaultList(policyResult.getSelectedPolicyIds());

        for (PolicyImpactDto dto : list) {
            if (dto == null) {
                continue;
            }

            Long policyId = dto.getPolicyId();
            boolean isSelected = policyId != null && selected.contains(policyId);
            boolean applicable = STATUS_APPLICABLE.equals(dto.getReasonSummary());

            if (!isSelected || !applicable) {
                continue;
            }

            try {
                financeInput = inputModifierApplier.applyInputEffectsForPolicy(policyId, financeInput, baselineLoanLimit);
            } catch (Exception e) {
                err("applyInputEffectsForPolicy() failed for policyId=" + policyId, e);
            }
        }

        return financeInput;
    }

    public boolean hasInputTargetField(Long policyId, String targetField) {
        if (policyId == null) {
            return false;
        }

        String key = safeUpper(targetField);
        if (key == null) {
            return false;
        }

        List<PolicyEffectEntity> effects;
        try {
            effects = effectRepository.findByPolicy_PolicyId(policyId);
        } catch (Exception e) {
            err("findByPolicy_PolicyId() failed for policyId=" + policyId, e);
            return false;
        }

        for (PolicyEffectEntity effect : defaultList(effects)) {
            if (effect == null) {
                continue;
            }
            if (!"INPUT".equalsIgnoreCase(safeTrim(effect.getEffectStage()))) {
                continue;
            }

            String target = safeUpper(effect.getTargetField());
            String effectKey = safeUpper(effect.getEffectKey());
            if (key.equals(target) || key.equals(effectKey)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasAnyInputEffects(Long policyId) {
        if (policyId == null) {
            return false;
        }

        List<PolicyEffectEntity> effects;
        try {
            effects = effectRepository.findByPolicy_PolicyId(policyId);
        } catch (Exception e) {
            err("findByPolicy_PolicyId() failed for policyId=" + policyId, e);
            return false;
        }

        for (PolicyEffectEntity effect : defaultList(effects)) {
            if (effect != null && "INPUT".equalsIgnoreCase(safeTrim(effect.getEffectStage()))) {
                return true;
            }
        }

        return false;
    }

    public <T> T applyInputEffectsForPolicy(Long policyId, T financeInput) {
        try {
            return inputModifierApplier.applyInputEffectsForPolicy(policyId, financeInput);
        } catch (Exception e) {
            err("applyInputEffectsForPolicy() failed policyId=" + policyId, e);
            return financeInput;
        }
    }

    public <T> T applyInputEffectsForPolicy(Long policyId, T financeInput, Long baselineLoanLimit) {
        try {
            return inputModifierApplier.applyInputEffectsForPolicy(policyId, financeInput, baselineLoanLimit);
        } catch (Exception e) {
            err("applyInputEffectsForPolicy(baseline) failed policyId=" + policyId, e);
            return financeInput;
        }
    }

    private <T> List<T> defaultList(List<T> list) {
        return (list == null) ? List.of() : list;
    }

    private String safeUpper(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t.toUpperCase();
    }

    private String safeTrim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private void err(String msg, Exception e) {
        if (DBG) {
            log.error("[PolicyImpactCalculator] {}", msg, e);
        }
    }
}
