import styles from "../styles";

export default function ListRowPolicy({ left }) {
  return (
    <div style={{...styles.listRow,display:"flex"}}>
      <span style={styles.listLeft}>{left}</span>
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
      <i class="fa-solid fa-magnifying-glass" style={{marginLeft: "auto",cursor: "pointer",marginTop:4}}></i>
    </div>
  );
};