export function TaxYearPanel({
  taxYears,
  taxYearInput,
  selectedTaxYear,
  loading,
  selectedHospitalId,
  onInputChange,
  onSelect,
  onSubmit,
}) {
  return (
    <form className="panel tax-year-panel" onSubmit={onSubmit}>
      <div className="panel-header">
        <h2>과세연도 작업공간</h2>
        <span>연도별 자료 분리</span>
      </div>

      <div className="tax-year-form">
        <input
          type="number"
          min="2000"
          max="2100"
          value={taxYearInput}
          onChange={(event) => onInputChange(event.target.value)}
        />
        <button className="secondary-button" type="submit" disabled={loading || !selectedHospitalId}>
          생성
        </button>
      </div>

      <div className="tax-year-list">
        {taxYears.length === 0 && <p className="empty">생성된 과세연도가 없습니다.</p>}
        {taxYears.map((workspace) => (
          <button
            className={`tax-year-item ${workspace.taxYear === selectedTaxYear ? 'selected' : ''}`}
            key={workspace.id}
            type="button"
            onClick={() => onSelect(workspace.taxYear)}
          >
            <strong>{workspace.taxYear}</strong>
            <span>{workspace.storagePath}</span>
          </button>
        ))}
      </div>
    </form>
  )
}
