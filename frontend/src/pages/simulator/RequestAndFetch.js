import axios from "axios";
import { URL } from "../../config/constants";

const DEFAULT_POLICY_IDS = [];

const toLoanPreference = (v) => {
  if (v <= 2) return "CONSERVATIVE";
  if (v === 3) return "BALANCED";
  return "AGGRESSIVE";
};

export const buildSimulationRequestBody = (fields, selectedPolicyIds = []) => {
  const body = {
    userId: 2, // TODO: 나중에 session에서 가져오기
    selectedPolicyIds,
  };

  // 사용자가 값 세팅한 것만 포함 (null/undefined면 미포함)
  if (fields?.cash != null) body.cashAvailable = Number(fields.cash);
  if (fields?.monthlyLimit != null) body.monthlyHousingBudget = Number(fields.monthlyLimit);
  if (fields?.emergencyFund != null) body.emergencyFund = Number(fields.emergencyFund);

  if (fields?.loanPreference != null) body.loanPreference = toLoanPreference(fields.loanPreference);
  if (fields?.targetMonths != null) body.targetMonths = Number(fields.targetMonths);
  if (fields?.targetPrice != null) body.targetPropertyPrice = Number(fields.targetPrice);

  return body;
};

const requestSimulation = (fields, selectedPolicyIds = DEFAULT_POLICY_IDS) => {
  const body = buildSimulationRequestBody(fields, selectedPolicyIds);
  console.log("simulation request body", body);
  return axios.post(URL.SIMULATOR, body, { timeout: 3000 });
};


export const requestInitialSimulation = (fields) => requestSimulation(fields);

export const requestRecalculation = (fields, selectedPolicyIds = DEFAULT_POLICY_IDS) =>
  requestSimulation(fields, selectedPolicyIds);

export const getRequestErrorMessage = (
  err,
  fallback = "데이터를 불러오지 못했습니다."
) => err?.response?.data?.message || err?.message || fallback;
