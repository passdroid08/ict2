import styles from "../styles";

export default function ListRowResult({ left }) {
  return (
    <div style={{...styles.listRow,display:"flex"}}>
      <span style={styles.listLeft}>{left}</span>
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
      <i className="fa-solid fa-download" style={{marginLeft: "auto",cursor: "pointer",marginTop:4}}></i>
    </div>
  );
};