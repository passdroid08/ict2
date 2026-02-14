import styles from "./styles";

export default function ScenarioCard({ title }) {
  return (
    <div style={styles.scenarioCard}>
      <div style={styles.scenarioTitle}>{title}</div>
      <div style={styles.scenarioMeta}>가능/불가 · 부족분 · 월상환</div>
    </div>
  );
};