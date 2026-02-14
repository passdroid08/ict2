import KpiBox from "../Component/kpiBox";
import styles from "../styles";

export default function Summary(){

    return <>
        <div style={styles.kpiGrid}>
                <div style={styles.kpiMain}>
                  <KpiBox label="구매 가능 범위" />
                </div>

                <div style={styles.kpiSubGrid}>
                  <KpiBox label="자금 안전도" />
                  <KpiBox label="목표 달성 가능성" />
                  <KpiBox label="월 상환 부담률" />
                </div>
        </div>
    </>
}