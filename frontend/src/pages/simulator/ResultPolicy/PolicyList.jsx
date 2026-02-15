import ListRowPolicy from "../Component/ListRowPolicy";
import styles from "../styles";

export default function PloicyList({ policies = [], onSelectPolicy }) {
  const formatWon = (v) => {
    const sign = v >= 0 ? "+" : "";
    return `${sign}${Number(v).toLocaleString()}원`;
  };

  if (!policies.length) {
    return <div style={styles.explain}>적용 가능한 정책이 없습니다.</div>;
  }

  return (
    <div style={styles.list}>
      {policies.map((policy) => (
        <div
          key={policy.policyId}
          onClick={() => onSelectPolicy?.(policy)}
          style={{ cursor: "pointer" }}
        >
          <ListRowPolicy
            left={policy.name}
            impact={`${formatWon(policy.impactAmount)} | 월 ${formatWon(policy.monthlyImpact)}`}
          />
        </div>
      ))}
    </div>
  );
}
