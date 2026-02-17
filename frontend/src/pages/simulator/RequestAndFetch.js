import axios from "axios";
import { URL } from "../../config/constants";

const DEFAULT_POLICY_IDS = [];

const toLoanPreference = (v) => {
  if (v <= 2) return "CONSERVATIVE";
  if (v === 3) return "BALANCED";
  return "AGGRESSIVE";
};

const buildSimulationRequestBody = (fields, selectedPolicyIds = DEFAULT_POLICY_IDS) => ({
  cashAvailable: fields.cash,
  monthlyHousingBudget: fields.monthlyLimit,
  emergencyFund: fields.emergencyFund,
  loanPreference: toLoanPreference(fields.loanPreference),
  targetMonths: fields.targetMonths,
  targetPropertyPrice: fields.targetPrice,
  selectedPolicyIds,
  requestedAt: new Date().toISOString(),
});

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
