import { useEffect, useMemo, useState } from 'react'
import './App.css'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:18083'

const initialForm = {
  name: '',
  ownerName: '',
  businessRegistrationNumber: '',
  medicalDepartment: '',
  openedOn: '',
  taxationType: 'VAT_EXEMPT',
  jointBusiness: false,
  previousYearRevenue: '',
  diligentFilingManuallyChecked: false,
}

const taxationLabels = {
  VAT_EXEMPT: '면세 중심',
  TAXABLE: '과세',
  MIXED: '과세/면세 겸영',
}

function App() {
  const [form, setForm] = useState(initialForm)
  const [hospitals, setHospitals] = useState([])
  const [selectedHospitalId, setSelectedHospitalId] = useState(null)
  const [taxYears, setTaxYears] = useState([])
  const [selectedTaxYear, setSelectedTaxYear] = useState(null)
  const [taxYearInput, setTaxYearInput] = useState(new Date().getFullYear() - 1)
  const [workspaceCounts, setWorkspaceCounts] = useState({ transactions: 0, evidences: 0 })
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const selectedHospital = useMemo(
    () => hospitals.find((hospital) => hospital.id === selectedHospitalId),
    [hospitals, selectedHospitalId],
  )

  useEffect(() => {
    loadHospitals()
  }, [])

  useEffect(() => {
    if (selectedHospitalId) {
      loadTaxYears(selectedHospitalId)
    } else {
      setTaxYears([])
      setSelectedTaxYear(null)
    }
  }, [selectedHospitalId])

  useEffect(() => {
    if (selectedHospitalId && selectedTaxYear) {
      loadWorkspaceCounts(selectedHospitalId, selectedTaxYear)
    } else {
      setWorkspaceCounts({ transactions: 0, evidences: 0 })
    }
  }, [selectedHospitalId, selectedTaxYear])

  async function request(path, options = {}) {
    try {
      const response = await fetch(`${API_BASE_URL}${path}`, {
        headers: {
          'Content-Type': 'application/json',
          ...(options.headers ?? {}),
        },
        ...options,
      })

      if (!response.ok) {
        const body = await response.json().catch(() => ({}))
        throw new Error(getSafeErrorMessage(response.status, body.code))
      }

      if (response.status === 204) {
        return null
      }

      return response.json()
    } catch (event) {
      if (event instanceof TypeError) {
        throw new Error('백엔드 서버에 연결할 수 없습니다. 서버 실행 상태와 포트를 확인해 주세요.')
      }
      throw event
    }
  }

  async function loadHospitals() {
    setLoading(true)
    setError('')
    try {
      const data = await request('/api/hospitals')
      setHospitals(data)
      setSelectedHospitalId((currentId) => currentId ?? data[0]?.id ?? null)
    } catch (event) {
      setError(event.message)
    } finally {
      setLoading(false)
    }
  }

  async function loadTaxYears(hospitalId) {
    setError('')
    try {
      const data = await request(`/api/hospitals/${hospitalId}/tax-years`)
      setTaxYears(data)
      setSelectedTaxYear(data[0]?.taxYear ?? null)
    } catch (event) {
      setError(event.message)
    }
  }

  async function loadWorkspaceCounts(hospitalId, taxYear) {
    setError('')
    try {
      const [transactions, evidences] = await Promise.all([
        request(`/api/hospitals/${hospitalId}/tax-years/${taxYear}/transactions`),
        request(`/api/hospitals/${hospitalId}/tax-years/${taxYear}/evidences`),
      ])
      setWorkspaceCounts({
        transactions: transactions.length,
        evidences: evidences.length,
      })
    } catch (event) {
      setError(event.message)
    }
  }

  function updateForm(field, value) {
    setForm((current) => ({ ...current, [field]: value }))
  }

  async function submitHospital(event) {
    event.preventDefault()
    setLoading(true)
    setError('')
    setMessage('')

    const payload = {
      ...form,
      businessRegistrationNumber: form.businessRegistrationNumber.replaceAll('-', ''),
      openedOn: form.openedOn || null,
      previousYearRevenue: form.previousYearRevenue === '' ? null : Number(form.previousYearRevenue),
    }

    try {
      const created = await request('/api/hospitals', {
        method: 'POST',
        body: JSON.stringify(payload),
      })
      setForm(initialForm)
      setMessage('병원 기본정보를 등록했습니다.')
      await loadHospitals()
      setSelectedHospitalId(created.id)
    } catch (event) {
      setError(event.message)
    } finally {
      setLoading(false)
    }
  }

  async function createTaxYear(event) {
    event.preventDefault()
    if (!selectedHospitalId) {
      setError('먼저 병원을 선택해 주세요.')
      return
    }

    setLoading(true)
    setError('')
    setMessage('')

    try {
      const workspace = await request(`/api/hospitals/${selectedHospitalId}/tax-years`, {
        method: 'POST',
        body: JSON.stringify({ taxYear: Number(taxYearInput) }),
      })
      setMessage(`${taxYearInput}년 과세연도 작업공간을 준비했습니다.`)
      setSelectedTaxYear(workspace.taxYear)
      await loadTaxYears(selectedHospitalId)
    } catch (event) {
      setError(event.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">Hospital Tax Helper</p>
          <h1>병원 종소세 신고 보조</h1>
        </div>
        <div className="status-pill">{loading ? '처리 중' : '1단계 기반 구축'}</div>
      </header>

      {(message || error) && (
        <div className={`notice ${error ? 'error' : 'success'}`}>
          {error || message}
        </div>
      )}

      <section className="workspace">
        <form className="panel register-panel" onSubmit={submitHospital}>
          <div className="panel-header">
            <h2>병원 기본정보</h2>
            <span>복식부기 기준 자료실의 시작점</span>
          </div>

          <div className="form-grid">
            <label>
              병원명
              <input
                required
                value={form.name}
                onChange={(event) => updateForm('name', event.target.value)}
                placeholder="예: 바른의원"
              />
            </label>
            <label>
              대표자명
              <input
                required
                value={form.ownerName}
                onChange={(event) => updateForm('ownerName', event.target.value)}
                placeholder="예: 홍길동"
              />
            </label>
            <label>
              사업자등록번호
              <input
                required
                inputMode="numeric"
                maxLength={12}
                value={form.businessRegistrationNumber}
                onChange={(event) => updateForm('businessRegistrationNumber', event.target.value)}
                placeholder="숫자 10자리"
              />
            </label>
            <label>
              진료과
              <input
                required
                value={form.medicalDepartment}
                onChange={(event) => updateForm('medicalDepartment', event.target.value)}
                placeholder="예: 내과, 피부과"
              />
            </label>
            <label>
              개업일
              <input
                type="date"
                value={form.openedOn}
                onChange={(event) => updateForm('openedOn', event.target.value)}
              />
            </label>
            <label>
              과세 유형
              <select
                value={form.taxationType}
                onChange={(event) => updateForm('taxationType', event.target.value)}
              >
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
                onChange={(event) => updateForm('previousYearRevenue', event.target.value)}
                placeholder="예: 520000000"
              />
            </label>
          </div>

          <div className="check-row">
            <label>
              <input
                type="checkbox"
                checked={form.jointBusiness}
                onChange={(event) => updateForm('jointBusiness', event.target.checked)}
              />
              공동사업자
            </label>
            <label>
              <input
                type="checkbox"
                checked={form.diligentFilingManuallyChecked}
                onChange={(event) => updateForm('diligentFilingManuallyChecked', event.target.checked)}
              />
              성실신고확인대상 수동 표시
            </label>
          </div>

          <button className="primary-button" type="submit" disabled={loading}>
            병원 등록
          </button>
        </form>

        <aside className="panel list-panel">
          <div className="panel-header">
            <h2>등록 병원</h2>
            <span>{hospitals.length}곳</span>
          </div>

          <div className="hospital-list">
            {hospitals.length === 0 && (
              <p className="empty">등록된 병원이 없습니다.</p>
            )}
            {hospitals.map((hospital) => (
              <button
                key={hospital.id}
                className={`hospital-item ${hospital.id === selectedHospitalId ? 'selected' : ''}`}
                type="button"
                onClick={() => setSelectedHospitalId(hospital.id)}
              >
                <strong>{hospital.name}</strong>
                <span>{hospital.medicalDepartment} · {taxationLabels[hospital.taxationType]}</span>
                <small>{formatBusinessNumber(hospital.businessRegistrationNumber)}</small>
              </button>
            ))}
          </div>
        </aside>
      </section>

      <section className="detail-layout">
        <div className="panel summary-panel">
          <div className="panel-header">
            <h2>선택 병원 요약</h2>
            <span>{selectedHospital ? selectedHospital.ownerName : '미선택'}</span>
          </div>

          {selectedHospital ? (
            <div className="summary-grid">
              <Metric label="전년도 수입금액" value={formatWon(selectedHospital.previousYearRevenue)} />
              <Metric label="성실신고 사전 경고" value={selectedHospital.diligentFilingWarning ? '해당' : '미해당'} />
              <Metric label="성실신고 후보" value={selectedHospital.diligentFilingCandidate ? '확인 필요' : '일반'} />
              <Metric label="선택 과세연도" value={selectedTaxYear ?? '미선택'} />
              <Metric label="거래 DB 건수" value={`${workspaceCounts.transactions}건`} />
              <Metric label="증빙 DB 건수" value={`${workspaceCounts.evidences}건`} />
            </div>
          ) : (
            <p className="empty">병원을 선택하면 기본 판단값이 표시됩니다.</p>
          )}
        </div>

        <form className="panel tax-year-panel" onSubmit={createTaxYear}>
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
              onChange={(event) => setTaxYearInput(event.target.value)}
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
                onClick={() => setSelectedTaxYear(workspace.taxYear)}
              >
                <strong>{workspace.taxYear}</strong>
                <span>{workspace.storagePath}</span>
              </button>
            ))}
          </div>
        </form>
      </section>
    </main>
  )
}

function Metric({ label, value }) {
  return (
    <div className="metric">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  )
}

function getSafeErrorMessage(status, code) {
  if (code === 'VALIDATION_ERROR') {
    return '입력값을 다시 확인해 주세요.'
  }

  if (status === 400) {
    return '요청 내용을 처리할 수 없습니다. 입력값을 확인해 주세요.'
  }

  if (status === 404) {
    return '요청한 자료를 찾을 수 없습니다.'
  }

  return '처리 중 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.'
}

function formatWon(value) {
  if (value === null || value === undefined) {
    return '미입력'
  }

  return `${Number(value).toLocaleString('ko-KR')}원`
}

function formatBusinessNumber(value) {
  if (!value || value.length !== 10) {
    return value
  }

  return `${value.slice(0, 3)}-${value.slice(3, 5)}-${value.slice(5)}`
}

export default App
