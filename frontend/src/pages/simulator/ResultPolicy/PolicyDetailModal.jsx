import styles from "../styles";

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

        {/* 적용 여부 */}
        <section style={styles.modalSection}>
          <div style={styles.modalSectionTitle}>적용 가능 여부</div>
          <div
            style={{
              ...styles.badge,
              background: policy.eligible ? "#E8F5E9" : "#FFEBEE",
              color: policy.eligible ? "#2E7D32" : "#C62828",
            }}
          >
            {policy.eligible ? "적용 가능" : "적용 불가"}
          </div>
          <div style={styles.modalText}>{policy.reason}</div>
        </section>

        {/* 영향 수치 */}
        <section style={styles.modalSection}>
          <div style={styles.modalSectionTitle}>예상 영향</div>

          <div style={styles.impactRow}>
            <span>구매 가능 금액 변화</span>
            <strong>
              {policy.impactAmount >= 0 ? "+" : ""}
              {policy.impactAmount?.toLocaleString()}원
            </strong>
          </div>

          <div style={styles.barWrapper}>
            <div
              style={{
                ...styles.bar,
                width: `${Math.min(100, Math.max(0, policy.impactPercent ?? 0))}%`,
              }}
            />
          </div>

          {typeof policy.monthlyImpact === "number" && (
            <div style={{ marginTop: 10, ...styles.modalText }}>
              월 부담 변화: {policy.monthlyImpact >= 0 ? "+" : ""}
              {policy.monthlyImpact.toLocaleString()}원
            </div>
          )}
        </section>

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
