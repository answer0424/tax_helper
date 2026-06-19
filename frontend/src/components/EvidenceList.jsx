import { duplicateStatusLabels, ocrStatusLabels, uploadStatusLabels } from '../constants/labels'
import { formatBytes } from '../utils/formatters'

export function EvidenceList({
  evidences,
  selectedEvidenceId,
  loading,
  onSelect,
  onDetectDuplicate,
  onReviewDuplicate,
}) {
  return (
    <div className="panel evidence-panel">
      <div className="panel-header">
        <h2>증빙 목록</h2>
        <span>{evidences.length}건</span>
      </div>

      <div className="evidence-list">
        {evidences.length === 0 && <p className="empty">업로드된 증빙이 없습니다.</p>}
        {evidences.map((evidence) => (
          <div className={`evidence-item ${evidence.id === selectedEvidenceId ? 'selected' : ''}`} key={evidence.id}>
            <button className="evidence-main-button" type="button" onClick={() => onSelect(evidence.id)}>
              <strong>{evidence.originalFileName}</strong>
              <span>
                {formatBytes(evidence.fileSize)} · {uploadStatusLabels[evidence.uploadStatus]}
              </span>
              <span>
                {ocrStatusLabels[evidence.ocrStatus] ?? 'OCR 대기'}
                {evidence.ocrReviewRequired ? ' · 검토필요' : ''}
              </span>
              <DuplicateSummary evidence={evidence} />
              <small>{evidence.filePath}</small>
            </button>

            <div className="duplicate-actions">
              <button type="button" disabled={loading} onClick={() => onDetectDuplicate(evidence.id)}>
                재탐지
              </button>
              <button
                type="button"
                disabled={loading}
                onClick={() => onReviewDuplicate(evidence.id, 'CONFIRMED_DUPLICATE')}
              >
                중복확정
              </button>
              <button
                type="button"
                disabled={loading}
                onClick={() => onReviewDuplicate(evidence.id, 'SEPARATE_TRANSACTION')}
              >
                별도거래
              </button>
              <button type="button" disabled={loading} onClick={() => onReviewDuplicate(evidence.id, 'NOT_DUPLICATE')}>
                중복아님
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

function DuplicateSummary({ evidence }) {
  const status = evidence.duplicateStatus ?? (evidence.duplicateSuspected ? 'SUSPECTED' : 'NOT_DUPLICATE')
  return (
    <div className={`duplicate-summary ${status === 'SUSPECTED' || status === 'CONFIRMED_DUPLICATE' ? 'warning' : ''}`}>
      <span>{duplicateStatusLabels[status] ?? status}</span>
      {evidence.duplicateCandidateEvidenceId && <span>후보 #{evidence.duplicateCandidateEvidenceId}</span>}
      {evidence.duplicateScore !== null && evidence.duplicateScore !== undefined && <span>점수 {evidence.duplicateScore}</span>}
      {evidence.duplicateManuallyReviewed && <span>수동확정</span>}
      {evidence.duplicateReason && <small>{evidence.duplicateReason}</small>}
    </div>
  )
}
