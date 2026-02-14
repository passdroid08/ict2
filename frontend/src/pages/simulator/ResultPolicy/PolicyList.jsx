import ListRowPolicy from "../Component/ListRowPolicy";
import styles from "../styles";

export default function PloicyList({ onSelectPolicy }) {
  const dummyPolicies = [
  {
    id: 1,
    name: "청년 우대 주택담보대출",
    eligible: true,
    reason: "만 34세 이하 + 무주택 세대주 조건 충족",
    impactAmount: 18000000,
    impactPercent: 65,
    impactType: "구매가능증가",
    monthlyImpact: -85000,
    conditions: [
      "만 19~34세",
      "무주택 세대주",
      "연소득 5천만원 이하",
      "전용면적 85㎡ 이하"
    ],
    caution: "지역 및 금융기관에 따라 금리 차이가 있을 수 있습니다.",
    source: "국토교통부 고시 2024-01",
    baseDate: "2026-01-01"
  },

  {
    id: 2,
    name: "생애최초 LTV 완화",
    eligible: true,
    reason: "생애 최초 주택 구입 + 소득 기준 충족",
    impactAmount: 32000000,
    impactPercent: 85,
    impactType: "대출한도증가",
    monthlyImpact: 120000,
    conditions: [
      "생애 최초 주택 구입",
      "무주택 기간 3년 이상",
      "소득 7천만원 이하"
    ],
    caution: "규제지역 여부에 따라 LTV 한도가 달라집니다.",
    source: "금융위원회 발표자료",
    baseDate: "2026-01-01"
  },

  {
    id: 3,
    name: "신혼부부 특별 대출",
    eligible: false,
    reason: "혼인 기간 조건 미충족",
    impactAmount: 25000000,
    impactPercent: 70,
    impactType: "금리우대",
    monthlyImpact: -60000,
    conditions: [
      "혼인 7년 이내",
      "부부합산 소득 8천만원 이하",
      "무주택 세대"
    ],
    caution: "자녀 수에 따라 한도 차등 적용됩니다.",
    source: "주택도시기금",
    baseDate: "2026-01-01"
  },

  {
    id: 4,
    name: "지방 중소도시 취득세 감면",
    eligible: true,
    reason: "비규제지역 + 3억 이하 매물 조건 충족",
    impactAmount: 5000000,
    impactPercent: 30,
    impactType: "세금감면",
    monthlyImpact: 0,
    conditions: [
      "비규제지역",
      "3억원 이하 주택",
      "1주택자 제한"
    ],
    caution: "지역 조례에 따라 감면율이 다를 수 있습니다.",
    source: "지방세 특례제한법",
    baseDate: "2026-01-01"
  }
];

  return (
    <div style={styles.list}>
      {dummyPolicies.map((policy) => (
        <div
          key={policy.id}
          onClick={() => onSelectPolicy?.(policy)}
          style={{ cursor: "pointer" }}
        >
          <ListRowPolicy
            left={policy.name}
            right={policy.eligible ? `+${policy.impactAmount.toLocaleString()}원` : "조건 미충족"}
          />
        </div>
      ))}
    </div>
  );
}
