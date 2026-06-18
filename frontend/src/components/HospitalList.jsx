import { taxationLabels } from '../constants/labels'
import { formatBusinessNumber } from '../utils/formatters'

export function HospitalList({ hospitals, selectedHospitalId, onSelect }) {
  return (
    <aside className="panel list-panel">
      <div className="panel-header">
        <h2>등록 병원</h2>
        <span>{hospitals.length}곳</span>
      </div>

      <div className="hospital-list">
        {hospitals.length === 0 && <p className="empty">등록된 병원이 없습니다.</p>}
        {hospitals.map((hospital) => (
          <button
            key={hospital.id}
            className={`hospital-item ${hospital.id === selectedHospitalId ? 'selected' : ''}`}
            type="button"
            onClick={() => onSelect(hospital.id)}
          >
            <strong>{hospital.name}</strong>
            <span>
              {hospital.medicalDepartment} · {taxationLabels[hospital.taxationType]}
            </span>
            <small>{formatBusinessNumber(hospital.businessRegistrationNumber)}</small>
          </button>
        ))}
      </div>
    </aside>
  )
}
