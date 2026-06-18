export function EvidenceUploadPanel({
  uploadFiles,
  loading,
  selectedHospitalId,
  selectedTaxYear,
  onFilesChange,
  onSubmit,
}) {
  return (
    <form className="panel upload-panel" onSubmit={onSubmit}>
      <div className="panel-header">
        <h2>영수증/증빙 업로드</h2>
        <span>JPG, PNG, PDF, ZIP</span>
      </div>

      <label className="file-drop">
        <input
          type="file"
          multiple
          accept=".jpg,.jpeg,.png,.pdf,.zip"
          onChange={(event) => onFilesChange(Array.from(event.target.files ?? []))}
        />
        <strong>{uploadFiles.length === 0 ? '파일 선택' : `${uploadFiles.length}개 파일 선택됨`}</strong>
        <span>ZIP 파일은 내부의 이미지/PDF만 자동 등록됩니다.</span>
      </label>

      <button className="primary-button" type="submit" disabled={loading || !selectedHospitalId || !selectedTaxYear}>
        증빙 업로드
      </button>
    </form>
  )
}
