export function HospitalRegistrationForm({ form, loading, onChange, onSubmit }) {
  return (
    <form className="panel register-panel" onSubmit={onSubmit}>
      <div className="panel-header">
        <h2>병원 기본정보</h2>
        <span>병원별 자료 구분</span>
      </div>

      <div className="form-grid">
        <label>
          병원명
          <input required value={form.name} onChange={(event) => onChange('name', event.target.value)} />
        </label>
        <label>
          대표자명
          <input required value={form.ownerName} onChange={(event) => onChange('ownerName', event.target.value)} />
        </label>
        <label>
          사업자등록번호
          <input
            required
            inputMode="numeric"
            maxLength={12}
            value={form.businessRegistrationNumber}
            onChange={(event) => onChange('businessRegistrationNumber', event.target.value)}
            placeholder="숫자 10자리"
          />
        </label>
        <label>
          진료과
          <input
            required
            value={form.medicalDepartment}
            onChange={(event) => onChange('medicalDepartment', event.target.value)}
          />
        </label>
        <label>
          개업일
          <input type="date" value={form.openedOn} onChange={(event) => onChange('openedOn', event.target.value)} />
        </label>
        <label>
          과세 유형
          <select value={form.taxationType} onChange={(event) => onChange('taxationType', event.target.value)}>
            <option value="VAT_EXEMPT">면세 중심</option>
            <option value="TAXABLE">과세</option>
            <option value="MIXED">과세/면세 겸영</option>
          </select>
        </label>
        <label>
          전년도 수입금액
          <input
            type="number"
            min="0"
            step="10000"
            value={form.previousYearRevenue}
            onChange={(event) => onChange('previousYearRevenue', event.target.value)}
          />
        </label>
      </div>

      <div className="check-row">
        <label>
          <input
            type="checkbox"
            checked={form.jointBusiness}
            onChange={(event) => onChange('jointBusiness', event.target.checked)}
          />
          공동사업자
        </label>
        <label>
          <input
            type="checkbox"
            checked={form.diligentFilingManuallyChecked}
            onChange={(event) => onChange('diligentFilingManuallyChecked', event.target.checked)}
          />
          성실신고확인대상 수동 표시
        </label>
      </div>

      <button className="primary-button" type="submit" disabled={loading}>
        병원 등록
      </button>
    </form>
  )
}
