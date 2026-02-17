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
import { RESULT_REQUEST_ACTION } from "../../config/constants";
import resultRequestReducer, {
  initialResultRequestState,
} from "../../reducer/resultRequestReducer";
import useFormContext from "../../context/useFormContext";
import {
  requestInitialSimulation,
  requestRecalculation,
  getRequestErrorMessage,
} from "./RequestAndFetch";

export default function Result() {
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [selectedPolicy, setSelectedPolicy] = useState(null);

  const [requestState, requestDispatch] = useReducer(
    resultRequestReducer,
    initialResultRequestState
  );

  const { formState } = useFormContext();
  const f = formState.fields;
  const debounceRef = useRef(null);


  const [selectedPolicyIds, setSelectedPolicyIds] = useState([]);

  const handleApplyPolicy = (policyId) => {
    const next = selectedPolicyIds.includes(policyId)
      ? selectedPolicyIds.filter((id) => id !== policyId) // ✅ 해제
      : [...selectedPolicyIds, policyId];                  // ✅ 적용

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
      setSelectedPolicyIds(res.data.appliedPolicyIds || []);
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
        setSelectedPolicyIds(res.data.appliedPolicyIds || []);
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
  }, []);

  useEffect(() => {
    if (!requestState.data) return; // 초기 fetch 전에는 스킵
    clearTimeout(debounceRef.current);

      debounceRef.current = setTimeout(() => {
          recalculate(selectedPolicyIds);
        }, 300);
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
                <Summary summary={requestState.data?.summary} />
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
              style={{ ...styles.rightCol, ...(open ? styles.rightColOpen : null) }}
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
