import { useMemo, useState } from "react";
import SliderBlock from "./SliderBlock";
import styles from "../styles";
import { SIMULATOR_GROUPS } from "../../../config/constants";

export default function InputSection({ onClose }) {
  // (1) 현재 선택된 그룹들(토글되는 배열) - "ALL"은 여기 넣지 않습니다.
  const [selectedGroups, setSelectedGroups] = useState([]); // 처음엔 아무것도 없음

  // blocks는 그대로
  
  // ✅ ALL 제외한 실제 그룹 키들
  const ALL_KEYS = useMemo(
    () => SIMULATOR_GROUPS.map((g) => g.key).filter((k) => k !== "ALL"),
    [SIMULATOR_GROUPS]
  );

  // ✅ "전체 상태"인지 판단
  const isAllSelected =
    ALL_KEYS.length > 0 && ALL_KEYS.every((k) => selectedGroups.includes(k));

  // ✅ UI에서 버튼 활성 여부 판단 (ALL 포함)
  const isActive = (key) => {
    if (key === "ALL") return isAllSelected;
    return selectedGroups.includes(key);
  };

  function toggleGroup(key) {
    setSelectedGroups((prev) => {
      // ✅ 전체(ALL): 전체면 비우고, 아니면 전부 채우기
      if (key === "ALL") {
        const prevIsAll =
          ALL_KEYS.length > 0 && ALL_KEYS.every((k) => prev.includes(k));
        return prevIsAll ? [] : [...ALL_KEYS];
      }

      // ✅ 나머지: 추가/삭제 토글
      return prev.includes(key) ? prev.filter((x) => x !== key) : [...prev, key];
    });
  }

  



  return (
    <section style={styles.card}>
      <button
        onClick={onClose}
        style={styles.closeBtn}
      >
        <i className="fa-solid fa-x"></i>
      </button>
      <div style={styles.cardHeader}>
        <div style={styles.cardTitle}>시뮬레이션 설정 패널</div>
        <div style={styles.cardDesc}>
          버튼을 누르면 오른쪽에 해당 조건만 표시됩니다.<br/>
          레버를 조절하면 결과가 즉시 반영됩니다.
        </div>
      </div>

      <div style={styles.panelBody}>
        {/* button 위치 영역 */}
        <div style={styles.buttonGrid}>
          {SIMULATOR_GROUPS.map((g) => {
              const active = isActive(g.key);

              return (
                <button
                  className="btn btn-success"
                  key={g.key}
                  type="button"
                  onClick={() => toggleGroup(g.key)}
                  style={{
                    ...styles.filterBtn,
                    ...(active ? styles.filterBtnActive : {}),
                  }}
                >
                  {g.label}
                </button>
              );
            })}
        </div>

        <div style={styles.sectionDivider} />

        <SliderBlock selectedGroups={selectedGroups}/>
      </div>
    </section>
  );
}
