import styles from "../styles";
import PolicyImpactBlock from "./PolicyImpactBlock";
import PolicyReasonBlock from "./PolicyReasonBlock";

export default function PolicyDetailModal({ open, policy, onClose }) {
  if (!open || !policy) return null;

  return (
    <div style={styles.modalOverlay} onClick={onClose}>
      <div style={styles.modal} onClick={(e) => e.stopPropagation()}>
        {/* Header */}
        <div style={styles.modalHeader}>
          <div style={styles.modalTitle}>{policy.name}</div>
          <button onClick={onClose} style={styles.modalCloseBtn}><i className="fa-solid fa-x"></i></button>
        </div>

        {/* 어떤 조건 때문에 해당 정책이 선정되었는지에 대한 내용 */}
        <PolicyReasonBlock
          reasons={policy?.reasons}
          summary={policy?.reasonSummary}
        />

        {/* 영향 수치 */}
        <PolicyImpactBlock
          impactAmount={policy.impactAmount}
          impactPercent={policy.impactPercent}
          monthlyImpact={policy.monthlyImpact}
        />

        {/* 주요 조건 */}
        <section style={styles.modalSection}>
          <div style={styles.modalSectionTitle}>주요 조건</div>
          <ul style={styles.modalList}>
            {(policy.conditions ?? []).map((c, idx) => (
              <li key={idx}>{c}</li>
            ))}
          </ul>
        </section>

        {/* 유의 사항 */}
        <section style={styles.modalSection}>
          <div style={styles.modalSectionTitle}>유의 사항</div>
          <div style={styles.modalText}>{policy.caution}</div>
        </section>

        {/* Footer */}
        <div style={styles.modalFooter}>
          <button style={styles.actionBtn}>해당 정책 적용 하기</button>
        </div>
      </div>
    </div>
  );
}
