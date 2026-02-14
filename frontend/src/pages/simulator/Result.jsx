import { useState } from 'react'
import InputSection from './InputSection/InputSection'
import styles from './styles'
import Title from './SimulTitle/Title'
import PloicyList from './ResultPolicy/PolicyList'
import { useNavigate } from 'react-router-dom'
import Summary from './Summary/Summary'
import ResultList from './ResultSave/ResultList'
import PolicyDetailModal from "./ResultPolicy/PolicyDetailModal"; 

export default function Result(){
    const [open, setOpen] = useState(false);
    const [policyOpen, setPolicyOpen] = useState(false);
    const [selectedPolicy, setSelectedPolicy] = useState(null);

    const toggle = () => {
      setOpen(prev => !prev);
    };

    return <>
    <div style={styles.page}>
      <div style={styles.container}>
        <Title/>
        
        <main style={styles.mainFlex}>

          <div style={{ ...styles.card}}>
            
            <div style={styles.resultBody}>
              <div style={styles.subMaintitle}>
                Summary
              </div>
              <Summary/>
              <div style={styles.explain}>
                설명 영역: 결과 근거 및 전략 제안 부분
              </div>
              <div style={styles.sectionDivider} />
              <div>
                <div style={styles.subMaintitle}>
                PolicyList
                </div>
                <PloicyList
                    onSelectPolicy={(p) => {
                      setSelectedPolicy(p);
                      setPolicyOpen(true);
                    }}
                  />

              </div>
              
              <div style={styles.sectionDivider} />              
              
              <button
                  onClick={() => useNavigate("/recommend")} // ✅ 수정
                  style={styles.goRecommendBtn}
                >
                추천 매물 보러가기
              </button>            
              <div style={styles.sectionDivider} />
              
              <div style={styles.subMaintitle}>
                ResultList
              </div>
              <ResultList/>
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
}