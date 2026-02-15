import styles from "../styles";

export default function ListRowPolicy({ left, impact }) {
  return (
    <div
      style={{
        ...styles.listRow,
        display: "grid",
        gridTemplateColumns: "400px 300px", // 마지막 24px은 아이콘
        alignItems: "center",
        columnGap: 40, // 정책명-영향 사이 기본 간격
      }}
    >
      {/* 정책명 */}
      <span
        style={{
          ...styles.listLeft,
          overflow: "hidden",
          textOverflow: "ellipsis",
          whiteSpace: "nowrap",
        }}
        title={left}
      >
        {left}
      </span>

      {/* 영향 */}
      <span style={{ ...styles.listLeft, textAlign: "right", whiteSpace: "nowrap" }}>
        {impact}
      </span>
    </div>
  );
}
