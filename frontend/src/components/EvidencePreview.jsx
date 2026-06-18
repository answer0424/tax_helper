import { API_BASE_URL } from '../api/client'

export function EvidencePreview({ evidence }) {
  return (
    <div className="panel preview-panel">
      <div className="panel-header">
        <h2>원본 미리보기</h2>
        <span>{evidence ? evidence.contentType : '미선택'}</span>
      </div>

      {evidence ? (
        evidence.contentType?.startsWith('image/') ? (
          <img className="preview-image" src={`${API_BASE_URL}${evidence.previewUrl}`} alt={evidence.originalFileName} />
        ) : (
          <iframe
            className="preview-frame"
            src={`${API_BASE_URL}${evidence.previewUrl}`}
            title={evidence.originalFileName}
          />
        )
      ) : (
        <p className="empty">증빙을 선택하면 원본이 표시됩니다.</p>
      )}
    </div>
  )
}
