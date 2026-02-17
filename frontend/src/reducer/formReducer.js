import { FORM_ACTION } from "../config/constants";

const formReducer = (state, action) => {
  switch (action.type) {
    case FORM_ACTION.SET_FIELD: {
            const { key, value } = action.payload;
                return {
                    ...state,
                    fields: {
                    ...state.fields,
                    [key]: value,
                    },
                    touchedKey: key,
                    dirty: true,
                };
            }
    case FORM_ACTION.SET_FIELDS:
      return state;

    case FORM_ACTION.RESET:
      return state;

    case FORM_ACTION.COERCE_FIELD:
      return state;

    case FORM_ACTION.CLAMP_FIELD:
      return state;

    case FORM_ACTION.CLAMP_ALL:
      return state;

    case FORM_ACTION.TOUCH_START:
      return state;

    case FORM_ACTION.TOUCH_END:
      return state;

    default:
      throw new Error(`존재하지 않는 액션 요청:${action.type}`);
  }
};

export default formReducer;
