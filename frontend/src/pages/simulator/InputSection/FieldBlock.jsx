import { useState } from "react";
import styles from "../styles";

export default function FieldBlock({title,right}) {
  const [value, setValue] = useState(0);

  return (
    <div style={styles.field}>
      <div style={styles.fieldTop}>
        <div style={styles.fieldLabel}>{title}</div>
        <div style={styles.fieldValue}>{value}{right}</div>
      </div>

      <input
        type="range"
        value={value}
        min={1}
        max={100}
        step={1}
        onChange={(e) => setValue(Number(e.target.value))}
        style={{ width: "100%" }}
      />

      <div style={styles.fieldHint}>
        <span>최소</span>
        <span>최대</span>
      </div>
    </div>
  );
}
