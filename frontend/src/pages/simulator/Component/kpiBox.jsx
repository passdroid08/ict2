import styles from "../styles";

export default function KpiBox({ label }) {
  return (
    <div style={styles.kpiBox}>
      <div style={styles.kpiLabel}>{label}</div>
      <div style={styles.kpiValue}>-</div>
    </div>
  );
}