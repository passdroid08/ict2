export const URL={USERS:'http://localhost:3002/users',
                    BBS:'http://localhost:3002/bbs',
                    PHOTOS:'https://jsonplaceholder.typicode.com/photos',
                    SIMULATOR:'http://localhost:8080/api/simulator/calculate'};
                    
export const AUTH_KEY ={USERNAME:'username',PASSWORD:'password'};
export const BBS_PAGING={PAGESIZE:2,BLOCKPAGE:3};

//리듀서 사용시 action의 type정의
//예 : ALL-모든 사용자 목록 요청,LOGIN:로그인 요청
//     WRITE-게시글 등록 요청,TOTALSIZE-총 글수 수정 요청,NOWPAGE-현재 페이지 수정 요청
export const USERS={ALL:'all',LOGIN:'login',LOGOUT:'logout',LIKES:'likes'};
export const BBS ={ALL:'all',WRITE:'write',DELETE:'delete',TOTALSIZE:'totalsize',NOWPAGE:'nowpage'}

// formReducer action types
export const FORM_ACTION = {
  SET_FIELD: "FORM/SET_FIELD",        // 단일 값 변경 (슬라이더 onChange)
  SET_FIELDS: "FORM/SET_FIELDS",      // 여러 값 일괄 변경 (프리셋/서버 로드)
  RESET: "FORM/RESET",                // 전체 초기화

  COERCE_FIELD: "FORM/COERCE_FIELD",  // (선택) 특정 필드 값 정규화/형변환
  CLAMP_FIELD: "FORM/CLAMP_FIELD",    // (선택) 특정 필드 min/max 보정
  CLAMP_ALL: "FORM/CLAMP_ALL",        // (선택) 전체 필드 min/max 보정

  TOUCH_START: "FORM/TOUCH_START",    // (선택) 슬라이더 조작 시작
  TOUCH_END: "FORM/TOUCH_END",        // (선택) 슬라이더 조작 종료(여기서 재계산 트리거)
};

//시뮬레이션 결과 상수
export const RESULT_REQUEST_ACTION = {
  FETCH_START: "RESULT/FETCH_START",
  FETCH_SUCCESS: "RESULT/FETCH_SUCCESS",
  FETCH_ERROR: "RESULT/FETCH_ERROR",
  RECALC_START: "RESULT/RECALC_START",
  RECALC_SUCCESS: "RESULT/RECALC_SUCCESS",
  RECALC_ERROR: "RESULT/RECALC_ERROR",
};


//입력 패널 버튼 상수
export const SIMULATOR_GROUPS = [
  { key: "ALL", label: "전체" },
  { key: "CASH", label: "현금" },
  { key: "MONTHLY", label: "월한도" },
  { key: "EMG", label: "비상금" },
  { key: "LOAN", label: "대출 성향" },
  { key: "TIME", label: "목표 시점" },
  { key: "PRICE", label: "목표 매매" },
];

//입력 패널 버튼 클릭시 생성 블락 상수
export const SIMULATOR_BLOCKS = [
  { group: "CASH", title: "가용 현금", right: "원", fieldKey: "cash", min: 0, max: 10000000, step: 10000 },
  { group: "MONTHLY", title: "월 한도", right: "원", fieldKey: "monthlyLimit", min: 0, max: 5000000, step: 10000 },
  { group: "EMG", title: "비상금", right: "원", fieldKey: "emergencyFund", min: 0, max: 10000000, step: 10000 },
  { group: "LOAN", title: "대출 성향", right: "", fieldKey: "loanPreference", min: 1, max: 5, step: 1 },
  { group: "TIME", title: "목표 시점", right: "개월", fieldKey: "targetMonths", min: 1, max: 120, step: 1 },
  { group: "PRICE", title: "목표 매물", right: "원", fieldKey: "targetPrice", min: 0, max: 3000000000, step: 10000000 },
];
//슬라이더 초기 Valule 상수
export const FORM_DEFAULT = {
  CASH: 0,             // 가용 현금
  MONTHLY_LIMIT: 1250000,    // 월 한도
  EMERGENCY_FUND: 0,   // 비상금
  LOAN_PREFERENCE: 3, // 대출 성향
  TARGET_MONTHS: 12,   // 목표 시점(개월)
  TARGET_PRICE: 0,     // 목표 매물(금액)
};

