// styles.js
// 구성
// 1) Layout(배치) 관련 스타일: 페이지/컨테이너/2컬럼/그리드/정렬/간격
// 2) Component(아이템) 스타일: 카드/필드/KPI/리스트/버튼/패널/FAB 등

const styles = {
  /* =====================================================
   * 1) Layout (배치/구조) 전용
   * ===================================================== */

  // 페이지 전체 배경/기본 타이포
  page: {
    padding: "24px 0",
    color: "#111827",
    background: "#F6F7FB",
    fontFamily:
      'ui-sans-serif, system-ui, -apple-system, "Segoe UI", Roboto, "Noto Sans KR", Arial',
  },

  // 중앙 컨테이너 (max 1280 / 가운데 정렬)
  container: {
    maxWidth: 1280,
    margin: "0 auto",
    padding: "0 12px",
    width: "100%",
  },

  // 상단 헤더 영역
  header: {
    marginBottom: 16,
  },

  // 메인 2컬럼 (좌: 결과 / 우: 입력 패널)
  mainFlex: {
    display: "flex",
    alignItems: "flex-start",
    marginTop: 16,
  },

  // 우측 컬럼(패널 자리)
  rightCol: {
    width: 0,
    minWidth: 0,
  },
  
  rightColOpen: {
    width: 420,
    minWidth: 420,
  },

  // 버튼 묶음(필터 버튼 등) 그리드
  buttonGrid: {
    display: "grid",
    gridTemplateColumns: "repeat(4, 1fr)",
    gap: 10,
  },

  // KPI 영역(필요 시)
  kpiGrid: {
    display: "grid",
    gridTemplateColumns: "repeat(4, 1fr)",
    gap: 10,
  },

  // 1행: main이 3칸 전부 차지
  kpiMain: {
    gridColumn: "1 / -1",
    height:150
  },

  // 2행: 3열 균등(서브 KPI들을 여기 안에 3개 넣으면 됨)
  kpiSubGrid: {
    display: "grid",
    gridColumn: "1 / -1",          // 2행 전체
    gridTemplateColumns: "repeat(3, 1fr)",
    gap: 10,
    marginTop: 10,
  },


  // 액션 버튼 줄(여러 버튼 가로 배치)
  actions: {
    display: "flex",
    gap: 10,
    marginTop: 14,
  },

  // 패널 내부 스크롤 영역(필요한 컴포넌트에서 사용)
  panelBody: {
    padding: 20,
    overflowY: "auto",
  },

  /* =====================================================
   * 2) Typography (텍스트/타이틀)
   * ===================================================== */

  title: {
    marginLeft:10,
    fontSize: 45,
    fontWeight: 800,
    letterSpacing: "-0.2px",
  },

  subMaintitle: {
    marginBottom: 10,
    fontWeight: 800,
  },

  subtitle: {
    marginLeft:20,
    marginTop: 6,
    fontSize: 13,
    color: "#6B7280",
  },

  /* =====================================================
   * 3) Card (공통 카드)
   * ===================================================== */

  // 좌측 결과 카드(유연하게 늘어남)
  card: {
    background: "#fff",
    border: "1px solid #E5E7EB",
    borderRadius: 16,
    boxShadow: "0 10px 26px rgba(0,0,0,.05)",
    overflow: "hidden",
    marginBottom: 16,
    marginLeft:10,
    marginRight:10,
    flex: 1,
    minWidth: 0,
  },

  // (필요 시) 고정 높이 스크롤 카드
  filterCard: {
    background: "#fff",
    border: "1px solid #E5E7EB",
    borderRadius: 16,
    boxShadow: "0 10px 26px rgba(0,0,0,.08)",
    overflow: "hidden",
    maxHeight: "72vh",
    marginBottom: 16,
  },

  cardHeader: {
    padding: 16,
    borderBottom: "1px solid #E5E7EB",
  },

  cardTitle: {
    fontSize: 16,
    fontWeight: 800,
  },

  cardTitleSmall: {
    fontSize: 14,
    fontWeight: 800,
    marginTop: 12,
    marginBottom: 8,
  },

  cardDesc: {
    marginTop: 6,
    fontSize: 13,
    color: "#6B7280",
    lineHeight: 1.4,
  },

  /* =====================================================
   * 4) Field (입력 블록)
   * ===================================================== */

  field: {
    border: "1px solid #E5E7EB",
    borderRadius: 12,
    padding: 12,
    marginBottom: 10,
    background: "#FFFFFF",
  },

  // 라벨(좌) / 값(우) 가로 정렬
  fieldTop: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: 12,
    marginBottom: 10,
  },

  fieldLabel: {
    fontSize: 15,
    fontWeight: 600,
    color: "#6B7280",
  },

  fieldValue: {
    fontSize: 14,
    fontWeight: 800,
  },

  fakeControl: {
    height: 12,
    borderRadius: 999,
    background: "#E5E7EB",
  },

  // 최소/최대 힌트 좌우 정렬
  fieldHint: {
    display: "flex",
    justifyContent: "space-between",
    marginTop: 8,
    fontSize: 12,
    color: "#9CA3AF",
  },

  miniNote: {
    fontSize: 12,
    color: "#6B7280",
    padding: "8px 2px 0 2px",
  },

  /* =====================================================
   * 5) Result (결과 영역)
   * ===================================================== */

  resultBody: {
    padding: 16,
  },

  kpiBox: {
    border: "1px solid #E5E7EB",
    borderRadius: 12,
    padding: 12,
    background: "#FFFFFF",
    height:"100%"
  },

  kpiLabel: {
    fontSize: 12,
    color: "#6B7280",
    marginBottom: 6,
  },

  kpiValue: {
    fontSize: 18,
    fontWeight: 900,
    letterSpacing: "-0.2px",
  },

  /* =====================================================
   * 6) List (추천/결과 리스트)
   * ===================================================== */

  list: {
    display:'flex', 
    marginTop: 12,
    flexDirection:'column',
  },

  listRow: {
    border: "1px solid #E5E7EB",
    borderRadius: 12,
    padding: "10px 12px",
    background: "#FFFFFF",
    fontSize: 13,
    marginBottom: 8,
  },

  listLeft: {
    color: "#6B7280",
  },

  /* =====================================================
   * 7) Buttons (일반/Primary/필터/이동)
   * ===================================================== */

  // actions 안에서 쓰면 자동 가로배치(= flex:1)
  button: {
    padding: "11px 10px",
    borderRadius: 12,
    border: "1px solid #E5E7EB",
    background: "#FFFFFF",
    cursor: "pointer",
    fontSize: 13,
    fontWeight: 800,
    flex: 1,
  },

  primaryBtn: {
    background: "#2563EB",
    borderColor: "transparent",
    color: "#FFFFFF",
  },

  filterBtn: {
    width: "100%",        
    borderRadius: 10,
    border: "1px solid #1fb857",
    background: "#22C55E",
    color: "#FFFFFF",
    fontWeight: 700,
    fontSize: 20,
    cursor: "pointer",
  },


  filterBtnActive: {
    background: "#15803D",
    border: "1px solid #166534",
    opacity: 0.85,
  },

  goRecommendBtn: {
    width: "100%",
    padding: 8,
    borderRadius: 12,
    border: "none",
    background: "#2355c0",
    color: "#FFFFFF",
    fontWeight: 600,
    cursor: "pointer",
  },

  /* =====================================================
   * 8) Explain / Divider (설명/구분선)
   * ===================================================== */

  explain: {
    marginTop: 12,
    padding: 12,
    border: "1px dashed #E5E7EB",
    borderRadius: 12,
    color: "#6B7280",
    fontSize: 13,
    lineHeight: 1.45,
    background: "#FFFFFF",
  },

  sectionDivider: {
    height: 1,
    background: "#E5E7EB",
    margin: "12px 0",
  },

  /* =====================================================
   * 9) Scenario (시나리오 카드)
   * ===================================================== */

  scenarioRow: {
    marginTop: 10,
  },

  scenarioCard: {
    border: "1px solid #E5E7EB",
    borderRadius: 12,
    padding: 12,
    background: "#FFFFFF",
    marginBottom: 10,
  },

  scenarioTitle: {
    fontSize: 13,
    fontWeight: 900,
    marginBottom: 6,
  },

  scenarioMeta: {
    fontSize: 12,
    color: "#6B7280",
  },

  /* =====================================================
   * 10) Panel (우측 조건 패널)
   * ===================================================== */

  panelBase: {
    overflow: "hidden",
    transformOrigin: "100% 0%",
    transition: "transform .2s, opacity .2s, visibility 0s",
  },

  panelOpen: {
    opacity: 1,
    visibility: "visible",
    pointerEvents: "auto",
    transform: "scale(1)",
  },

  panelClosed: {
    opacity: 0,
    visibility: "hidden",
    pointerEvents: "none",
    transform: "scale(0)",
  },

  closeBtn: {
    width: 36,
    height: 36,
    borderRadius: "50%",
    border: "none",
    background: "#ffffff",
    fontSize: 25,
    cursor: "pointer",

    position: "absolute",
    top: 10,
    right: 20,
  },


  /* =====================================================
   * 11) FAB (+ 버튼)
   * - 컨테이너(1280) 오른쪽 바깥에 고정
   * ===================================================== */

  fab: {
    width: 60,
    height: 60,
    borderRadius: "50%",
    background: "#000000",
    color: "#FFFFFF",
    fontSize: 32,
    cursor: "pointer",
    boxShadow: "0 0 5px rgba(0,0,0,.15)",
    marginLeft:10,
  },

  fabHidden: {
    pointerEvents: "none",
    opacity: 0,
    display:"none"
  },
  
  /* =========================
   * Modal (Center)
   * ========================= */
  modalOverlay: {
    position: "fixed",
    inset: 0,
    background: "rgba(0,0,0,0.35)",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    zIndex: 9999,
    padding: 16,
  },

  modal: {
    width: "min(720px, 100%)",
    maxHeight: "85vh",
    overflowY: "auto",
    background: "#fff",
    borderRadius: 16,
    boxShadow: "0 12px 40px rgba(0,0,0,0.25)",
    padding: 20,
  },

  modalHeader: {
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    gap: 12,
    marginBottom: 16,
  },

  modalTitle: {
    fontSize: 20,
    fontWeight: 700,
  },

  modalCloseBtn: {
    width: 36,
    height: 36,
    borderRadius: "50%",
    border: "none",
    background: "#ffffff",
    fontSize: 22,
    cursor: "pointer",
    lineHeight: "36px",
  },

  modalSection: {
    padding: "14px 0",
    borderTop: "1px solid #eee",
  },

  modalSectionTitle: {
    fontSize: 14,
    fontWeight: 700,
    marginBottom: 10,
  },

  modalText: {
    fontSize: 14,
    lineHeight: 1.6,
    color: "#333",
    marginTop: 8,
  },

  modalList: {
    margin: 0,
    paddingLeft: 18,
    lineHeight: 1.7,
    fontSize: 14,
  },

  modalFooter: {
    paddingTop: 16,
    borderTop: "1px solid #eee",
    marginTop: 10,
  },

  /* =========================
   * Common (Modal 내부에서 사용)
   * ========================= */
  badge: {
    display: "inline-block",
    padding: "6px 12px",
    borderRadius: 20,
    fontSize: 14,
    fontWeight: 600,
    marginBottom: 10,
  },

  impactRow: {
    display: "flex",
    justifyContent: "space-between",
    marginBottom: 10,
  },

  barWrapper: {
    height: 8,
    background: "#EEE",
    borderRadius: 4,
  },

  bar: {
    height: "100%",
    background: "#4650F1",
    borderRadius: 4,
  },

  actionBtn: {
    width: "100%",
    padding: "12px 0",
    background: "#4650F1",
    color: "#fff",
    border: "none",
    borderRadius: 8,
    fontSize: 16,
    cursor: "pointer",
  },

};


export default styles;
