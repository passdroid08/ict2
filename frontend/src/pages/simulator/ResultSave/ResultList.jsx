import ListRowResult from "../Component/ListRowResult";
import styles from "../styles";

export default function ResultList(){

    return <>
        <div style={styles.actions}>                
                <button style={styles.button} type="button">
                  저장
                </button>
        </div>
                <div style={styles.sectionDivider} />
        <div style={styles.list}>
                <ListRowResult left="시나리오 결과 1"/>
                <ListRowResult left="시나리오 결과 2"/>
                <ListRowResult left="시나리오 결과 3"/>
                <ListRowResult left="시나리오 결과 4"/>
              </div>
              
    </>
}