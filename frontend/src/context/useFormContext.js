// src/hooks/useFormContext.js
import { useContext } from "react";
import { FormContext } from "../provider/FormProvider";

export default function useFormContext() {
  const ctx = useContext(FormContext);
  if (!ctx) {
    throw new Error("useFormContext는 FormProvider 내부에서만 사용할 수 있습니다.");
  }
  return ctx; // { formState, dispatch }
}
