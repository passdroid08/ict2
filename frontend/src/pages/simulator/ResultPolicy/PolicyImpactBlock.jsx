import styles from "../styles";

export default function PolicyImpactBlock({ impactAmount = 0, impactPercent = 0, monthlyImpact }) {
  const safePercent = Math.min(100, Math.max(0, impactPercent ?? 0));

  return (
    <>
      <section style={styles.modalSection}>
        <div style={styles.modalSectionTitle}>예상 영향</div>

        <div style={styles.impactRow}>
          <span>구매 가능 금액 변화</span>
          <strong>
            {impactAmount >= 0 ? "+" : ""}
            {Number(impactAmount).toLocaleString()}원
          </strong>
        </div>

        <div style={styles.barWrapper}>
          <div
            style={{
              ...styles.bar,
              width: `${safePercent}%`,
            }}
          />
        </div>

        {typeof monthlyImpact === "number" && (
          <div style={{ marginTop: 10, ...styles.modalText }}>
            월 부담 변화: {monthlyImpact >= 0 ? "+" : ""}
            {monthlyImpact.toLocaleString()}원
          </div>
        )}
      </section>
    </>
  );
}
