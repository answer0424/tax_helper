import { uploadStatusLabels } from '../constants/labels'
import { formatBytes } from '../utils/formatters'

export function EvidenceList({ evidences, selectedEvidenceId, onSelect }) {
  return (
    <div className="panel evidence-panel">
      <div className="panel-header">
        <h2>증빙 목록</h2>
        <span>{evidences.length}건</span>
      </div>

      <div className="evidence-list">
        {evidences.length === 0 && <p className="empty">업로드된 증빙이 없습니다.</p>}
        {evidences.map((evidence) => (
          <button
            key={evidence.id}
            className={`evidence-item ${evidence.id === selectedEvidenceId ? 'selected' : ''}`}
            type="button"
            onClick={() => onSelect(evidence.id)}
          >
            <strong>{evidence.originalFileName}</strong>
            <span>
              {formatBytes(evidence.fileSize)} · {uploadStatusLabels[evidence.uploadStatus]}
            </span>
            <small>{evidence.filePath}</small>
          </button>
        ))}
      </div>
    </div>
  )
}
