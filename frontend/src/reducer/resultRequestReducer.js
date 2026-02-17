import { RESULT_REQUEST_ACTION } from "../config/constants";

export const initialResultRequestState = {
  loading: true,
  recalcLoading: false,
  error: null,
  data: null,
};

export default function resultRequestReducer(state, action) {
  switch (action.type) {
    case RESULT_REQUEST_ACTION.FETCH_START:
      return { ...state, loading: true, error: null };
    case RESULT_REQUEST_ACTION.FETCH_SUCCESS:
      return { ...state, loading: false, error: null, data: action.payload };
    case RESULT_REQUEST_ACTION.FETCH_ERROR:
      return { ...state, loading: false, error: action.payload };
    case RESULT_REQUEST_ACTION.RECALC_START:
  return { ...state, recalcLoading: true, error: null };
    case RESULT_REQUEST_ACTION.RECALC_SUCCESS:
        return { ...state, recalcLoading: false, error: null, data: action.payload };
    case RESULT_REQUEST_ACTION.RECALC_ERROR:
        return { ...state, recalcLoading: false, error: action.payload };
    default:
      throw new Error(`존재하지 않는 액션 요청:${action.type}`);
  }
}
