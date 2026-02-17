import { FORM_ACTION } from "../../../config/constants";
import useFormContext from "../../../context/useFormContext";
import styles from "../styles";

export default function FieldBlock({ title, right, fieldKey, min, max, step }) {
  const { formState, dispatch } = useFormContext();
  const value = formState.fields?.[fieldKey] ?? 0;

  // 구간 값 계산 (개월이면 10단위 스냅, 그 외 step 스냅)
  const isMonths = right === "개월" || fieldKey === "targetMonths";

  const baseG1 = min + ((max - min) * 1) / 4;
  const baseG2 = min + ((max - min) * 2) / 4;
  const baseG3 = min + ((max - min) * 3) / 4;

  const snapToStep = (n) => Math.round(n / step) * step;
  const snap10 = (n) => Math.round(n / 10) * 10;

  const g1 = isMonths ? snap10(baseG1) : snapToStep(baseG1);
  const g2 = isMonths ? snap10(baseG2) : snapToStep(baseG2);
  const g3 = isMonths ? snap10(baseG3) : snapToStep(baseG3);

  // 힌트 표시 포맷 (단위는 바깥에서 {right}로 붙임)
  const format = (n) => {
    if (right !== "원") return String(n);

    const num = Number(n) || 0;
    if (num === 0) return "0만";

    if (num >= 100_000_000) {
      const eok = num / 100_000_000;
      const s = (Math.round(eok * 10) / 10).toFixed(1).replace(/\.0$/, "");
      return `${s}억`;
    }

    const man = Math.round(num / 10_000);
    return `${man.toLocaleString("ko-KR")}만`;
  };

  const isActive = (v) => Math.abs(value - v) <= step / 2;

  // 부모가 공통 스타일 제공
  const hintWrapStyle = {
    ...styles.fieldHint,
    display: "flex",
    justifyContent: "space-between",
    gap: 8,
  };  

  const renderHint = (v) => (
    <span
      key={v}
      onClick={() =>
        dispatch({
          type: FORM_ACTION.SET_FIELD,
          payload: { key: fieldKey, value: v },
        })
      }
      style={{
          cursor: "pointer",
          whiteSpace: "nowrap",
          fontWeight: isActive(v) ? 700 : 500,
          opacity: isActive(v) ? 1 : 0.9,
          color: isActive(v) ? "#111" : "#a8a0a0",
        }}
      title={`${v}${right}`}
    >
      {format(v)}{right}
    </span>
  );

  const hints = [min, g1, g2, g3, max];

  return (
    <div style={styles.field}>
      <div style={styles.fieldTop}>
        <div style={styles.fieldLabel}>{title}</div>
        <div style={styles.fieldValue}>
          {right === "원"
            ? `${value.toLocaleString("ko-KR")}${right}`
            : `${value}${right}`}
        </div>
      </div>

      <input
        type="range"
        value={value}
        min={min}
        max={max}
        step={step}
        onChange={(e) =>
          dispatch({
            type: FORM_ACTION.SET_FIELD,
            payload: { key: fieldKey, value: Number(e.target.value) },
          })
        }
        style={{ width: "100%" }}
      />

      <div style={hintWrapStyle}>
        {hints.map(renderHint)}
      </div>
    </div>
  );
}
