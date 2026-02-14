import styles from "../styles";

export default function Title(){

    return <>
    <header style={styles.header}>
          <div>
            <div style={styles.title}>내집마련 시뮬레이션</div>
            <div style={styles.subtitle}>
              *사용자님*의 시뮬레이션 계산 화면입니다.
            </div>
          </div>         
        </header>
    </>
}