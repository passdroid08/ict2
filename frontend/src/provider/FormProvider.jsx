import React, { useReducer } from "react";
import formReducer from "../reducer/formReducer";
import { FORM_DEFAULT } from "../config/constants";

export const FormContext = React.createContext(null);

const initialState = {
  fields: {
    cash: FORM_DEFAULT.CASH,
    monthlyLimit: FORM_DEFAULT.MONTHLY_LIMIT,
    emergencyFund: FORM_DEFAULT.EMERGENCY_FUND,
    loanPreference: FORM_DEFAULT.LOAN_PREFERENCE,
    targetMonths: FORM_DEFAULT.TARGET_MONTHS,
    targetPrice: FORM_DEFAULT.TARGET_PRICE,
  },
  touchedKey: null,
  dirty: false,
};

export default function FormProvider({ children }) {
  const [formState, dispatch] = useReducer(formReducer, initialState);

  return (
    <FormContext.Provider value={{ formState, dispatch }}>
      {children}
    </FormContext.Provider>
  );
}
