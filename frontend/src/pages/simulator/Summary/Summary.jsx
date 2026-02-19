import KpiBox from "../Component/KpiBox";
import styles from "../styles";

export default function Summary({ summary }) {

    return <>
        <div style={styles.kpiGrid}>
                <div style={styles.kpiMain}>
                  <KpiBox
                      label="구매 가능 범위"
                      value={summary?.purchaseRange}
                    />
                </div>          

                <div style={styles.kpiSubGrid}>
                  <KpiBox
                    label="자금 안전도"
                    value={summary?.assetSafety}
                  />
                  <KpiBox
                    label="목표 달성 가능성"
                    value={summary?.goalFeasibility}
                  />
                  <KpiBox
                    label="월 상환 부담률"
                    value={summary?.monthlyBurdenRatio}
                  />
                </div>
        </div>
    </>
}