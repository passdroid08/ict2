import styles from "../styles";

export default function EmptyState() {
  return (
    <div style={styles.empty}>
      <div style={styles.emptyTitle}>표시할 조건이 없습니다</div>
      <div style={styles.emptyDesc}>왼쪽에서 조건을 선택해 주세요.</div>
    </div>
  );
}