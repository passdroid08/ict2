import { useEffect, useReducer, useRef, useState } from "react";

import InputSection from "./InputSection/InputSection";
import styles from "./styles";
import Title from "./SimulTitle/Title";
import { useNavigate } from "react-router-dom";
import Summary from "./Summary/Summary";
import ResultList from "./ResultSave/ResultList";
import PolicyDetailModal from "./ResultPolicy/PolicyDetailModal";
import Loading from "../../components/Loading";
import Failure from "../../components/Failure";
import PolicyList from "./ResultPolicy/PolicyList";
import { FORM_ACTION, RESULT_REQUEST_ACTION } from "../../config/constants";
import resultRequestReducer, {
  initialResultRequestState,
} from "../../reducer/resultRequestReducer";
import useFormContext from "../../context/useFormContext";
import {
  requestInitialSimulation,
  requestRecalculation,
  getRequestErrorMessage,
} from "./RequestAndFetch";

const toNum = (v) => {
  if (v == null) return null;
  const n = Number(v);
  return Number.isFinite(n) ? n : null;
};

const loanCodeToSlider = (v) => {
  if (v == null) return null;
  const m = String(v).toUpperCase().match(/^L([1-5])$/);
  return m ? Number(m[1]) : null;
};

export default function Result() {
  const navigate = useNavigate();

  const [open, setOpen] = useState(false);
  const [selectedPolicy, setSelectedPolicy] = useState(null);
  const [selectedPolicyIds, setSelectedPolicyIds] = useState([]);

  const [requestState, requestDispatch] = useReducer(
    resultRequestReducer,
    initialResultRequestState
  );

  const { formState, dispatch } = useFormContext();
  const f = formState.fields;

  const debounceRef = useRef(null);

  const skipNextRecalcRef = useRef(false);

  const handleApplyPolicy = (policyId) => {
    const next = selectedPolicyIds.includes(policyId)
      ? selectedPolicyIds.filter((id) => id !== policyId)
      : [...selectedPolicyIds, policyId];

    setSelectedPolicyIds(next);
    clearTimeout(debounceRef.current);
    recalculate(next);
    setSelectedPolicy(null);
  };

  const recalculate = async (nextPolicyIds = []) => {
    requestDispatch({ type: RESULT_REQUEST_ACTION.RECALC_START });
    try {
      const res = await requestRecalculation(f, nextPolicyIds);
      requestDispatch({
        type: RESULT_REQUEST_ACTION.RECALC_SUCCESS,
        payload: res.data,
      });
      console.log("simulation response body(ReCal)", res.data);
    } catch (err) {
      requestDispatch({
        type: RESULT_REQUEST_ACTION.RECALC_ERROR,
        payload: getRequestErrorMessage(err, "재계산에 실패했습니다."),
      });
    }
  };

  useEffect(() => {
    let alive = true;

    const init = async () => {
      requestDispatch({ type: RESULT_REQUEST_ACTION.FETCH_START });

      try {
        const res = await requestInitialSimulation(f);
        if (!alive) return;

        requestDispatch({
          type: RESULT_REQUEST_ACTION.FETCH_SUCCESS,
          payload: res.data,
        });

        console.log("simulation response body(FirstCal)", res.data);

        
        if (!formState.dirty) {
          const snap = res.data?.financeSnapshot;
          if (snap) {
            // 이 dispatch로 인해 f 값 변경 -> 자동 재계산 useEffect가 바로 돌 수 있어서 1회 스킵
            skipNextRecalcRef.current = true;

            dispatch({
              type: FORM_ACTION.SET_FIELD,
              payload: { key: "cash", value: toNum(snap.cashAvailable) },
            });
            dispatch({
              type: FORM_ACTION.SET_FIELD,
              payload: {
                key: "monthlyLimit",
                value: toNum(snap.monthlyHousingBudget),
              },
            });
            dispatch({
              type: FORM_ACTION.SET_FIELD,
              payload: { key: "emergencyFund", value: toNum(snap.emergencyFund) },
            });
            dispatch({
              type: FORM_ACTION.SET_FIELD,
              payload: {
                key: "loanPreference",
                value: loanCodeToSlider(snap.loanPreference),
              },
            });

            // 목표값은 사용자 선택이 맞으니(현재 결론) 초기에는 건드리지 않음
            // dispatch({ type: FORM_ACTION.SET_FIELD, payload: { key: "targetMonths", value: toNum(snap.targetMonths) } });
            // dispatch({ type: FORM_ACTION.SET_FIELD, payload: { key: "targetPrice", value: toNum(snap.targetPropertyPrice) } });
          }
        }
      } catch (err) {
        if (!alive) return;
        requestDispatch({
          type: RESULT_REQUEST_ACTION.FETCH_ERROR,
          payload: getRequestErrorMessage(err),
        });
      }
    };

    init();
    return () => {
      alive = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // 초기 1회

  useEffect(() => {
    if (!requestState.data) return; // 초기 fetch 전에는 스킵

    if (skipNextRecalcRef.current) {
      skipNextRecalcRef.current = false;
      return;
    }

    clearTimeout(debounceRef.current);

    debounceRef.current = setTimeout(() => {
      recalculate(selectedPolicyIds);
    }, 300);

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    f.cash,
    f.monthlyLimit,
    f.emergencyFund,
    f.loanPreference,
    f.targetMonths,
    f.targetPrice,
  ]);

  if (requestState.loading) return <Loading />;
  if (requestState.error && !requestState.data) {
    return <Failure message={requestState.error} />;
  }

  return (
    <>
      <div style={styles.page}>
        <div style={styles.container}>
          <Title />

          <main style={styles.mainFlex}>
            <div style={{ ...styles.card }}>
              <div style={styles.resultBody}>
                <div style={styles.subMaintitle}>Summary</div>
                <Summary summary={requestState.data?.summaryDto} />
                <div style={styles.explain}>
                  설명 영역: 결과 근거 및 정책 제안 부분
                </div>

                <div style={styles.sectionDivider} />

                <div>
                  <div style={styles.subMaintitle}>PolicyList</div>
                  <PolicyList
                    policies={requestState.data?.policyList || []}
                    selectedPolicyIds={selectedPolicyIds}
                    onSelectPolicy={(p) => setSelectedPolicy(p)}
                  />
                </div>

                <div style={styles.sectionDivider} />

                <button
                  onClick={() => navigate("/recommend")}
                  style={styles.goRecommendBtn}
                >
                  추천 매물 보러가기
                </button>

                <div style={styles.sectionDivider} />

                <div style={styles.subMaintitle}>ResultList</div>
                <ResultList />
              </div>
            </div>

            <button
              onClick={(e) => {
                e.stopPropagation();
                setOpen((prev) => !prev);
              }}
              style={{
                ...styles.fab,
                ...(open ? styles.fabHidden : null),
              }}
            >
              +
            </button>

            <div
              style={{
                ...styles.rightCol,
                ...(open ? styles.rightColOpen : null),
              }}
              onClick={() => setOpen(false)}
            >
              <div
                onClick={(e) => e.stopPropagation()}
                style={{
                  ...styles.panelBase,
                  ...(open ? styles.panelOpen : styles.panelClosed),
                }}
              >
                {open && <InputSection onClose={() => setOpen(false)} />}
              </div>
            </div>
          </main>
        </div>
      </div>

      <PolicyDetailModal
        open={Boolean(selectedPolicy)}
        policy={selectedPolicy}
        onClose={() => setSelectedPolicy(null)}
        applied={
          selectedPolicy
            ? selectedPolicyIds.includes(selectedPolicy.policyId)
            : false
        }
        onApply={(policyId) => handleApplyPolicy(policyId)}
      />
    </>
  );
}