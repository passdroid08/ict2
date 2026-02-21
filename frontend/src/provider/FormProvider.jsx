import React, { useReducer } from "react";
import formReducer from "../reducer/formReducer";

export const FormContext = React.createContext(null);

const initialState = {
  fields: {
    cash: null,
    monthlyLimit: null,
    emergencyFund: null,
    loanPreference: null,
    targetMonths: null,
    targetPrice: null,
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
