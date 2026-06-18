import { formatWon } from '../utils/formatters'
import { Metric } from './Metric'

export function WorkspaceSummary({ hospital, selectedTaxYear, workspaceCounts }) {
  return (
    <div className="panel summary-panel">
      <div className="panel-header">
        <h2>선택 병원 요약</h2>
        <span>{hospital ? hospital.ownerName : '미선택'}</span>
      </div>

      {hospital ? (
        <div className="summary-grid">
          <Metric label="전년도 수입금액" value={formatWon(hospital.previousYearRevenue)} />
          <Metric label="성실신고 사전 경고" value={hospital.diligentFilingWarning ? '해당' : '미해당'} />
          <Metric label="성실신고 후보" value={hospital.diligentFilingCandidate ? '확인 필요' : '일반'} />
          <Metric label="선택 과세연도" value={selectedTaxYear ?? '미선택'} />
          <Metric label="거래 DB 건수" value={`${workspaceCounts.transactions}건`} />
          <Metric label="증빙 DB 건수" value={`${workspaceCounts.evidences}건`} />
        </div>
      ) : (
        <p className="empty">병원을 선택하면 기본 판단값이 표시됩니다.</p>
      )}
    </div>
  )
}
