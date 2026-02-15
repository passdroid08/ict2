import { useEffect, useState } from "react";
import axios from "axios";

import InputSection from "./InputSection/InputSection";
import styles from "./styles";
import Title from "./SimulTitle/Title";
import PloicyList from "./ResultPolicy/PolicyList";
import { useNavigate } from "react-router-dom";
import Summary from "./Summary/Summary";
import ResultList from "./ResultSave/ResultList";
import PolicyDetailModal from "./ResultPolicy/PolicyDetailModal";
import Loading from "../../components/Loading";
import Failure from "../../components/Failure";

export default function Result() {
  const navigate = useNavigate();

  const [open, setOpen] = useState(false);
  const [policyOpen, setPolicyOpen] = useState(false);
  const [selectedPolicy, setSelectedPolicy] = useState(null);

  // ✅ 정책 목록 상태
  const [policies, setPolicies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  //시뮬 계산 결과
  const [simulRes, setSimulRes] = useState(null);
  const [simulLoading, setSimulLoading] = useState(false);

  const toggle = () => setOpen((prev) => !prev);
  
  useEffect(() => {
    let alive = true;

    const fetchResultData  = async () => {
      try {
        setLoading(true);
        setErrorMessage("");

      
       

        // 시뮬 POST
        const simulRes = await axios.post(
          "http://localhost:8080/api/simulator/calculate",
          {
            cashAvailable: 30000000,
            monthlyHousingBudget: 1200000,
            emergencyFund: 5000000,
            loanPreference: "CONSERVATIVE",
            targetMonths: 6,
            targetPropertyPrice: 450000000,
            selectedPolicyIds: [1, 2, 3],
            requestedAt: new Date().toISOString(),
          },
          { timeout: 3000 }
        );

        if (!alive) return;

        setSimulRes(simulRes.data);
        console.log("simulate response:", simulRes.data);


      } catch (err) {
        if (!alive) return;

        const isNetworkDown =
          err?.code === "ERR_NETWORK" ||
          err?.code === "ECONNABORTED" ||
          !err?.response;

        if (isNetworkDown) return;

        setErrorMessage(
          err?.response?.data?.message ||
          err?.message ||
          "데이터를 불러오지 못했습니다."
        );
      } finally {
        if (!alive) return;
        setLoading(false);
      }
    };

    fetchResultData();

    return () => {
      alive = false;
    };
  }, []);

  // ✅ 로딩/에러 처리 (더미 필요 없음)
  if (loading || simulLoading) return <Loading />;
  if (errorMessage) return <Failure message={errorMessage} />;


  return (
    <>
      <div style={styles.page}>
        <div style={styles.container}>
          <Title />

          <main style={styles.mainFlex}>
            <div style={{ ...styles.card }}>
              <div style={styles.resultBody}>
                <div style={styles.subMaintitle}>Summary</div>
                    <Summary summary={simulRes?.summary} />
                <div style={styles.explain}>
                  설명 영역: 결과 근거 및 전략 제안 부분
                </div>       

                <div style={styles.sectionDivider} />

                <div>
                  <div style={styles.subMaintitle}>PolicyList</div>

                  <PloicyList
                    policies={simulRes?.policyList || []}
                    onSelectPolicy={(p) => {
                      setSelectedPolicy(p);
                      setPolicyOpen(true);
                    }}
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
                toggle();
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
        open={policyOpen}
        policy={selectedPolicy}
        onClose={() => setPolicyOpen(false)}
      />
    </>
  );
}
