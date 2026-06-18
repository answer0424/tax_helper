import { useEffect, useMemo, useState } from 'react'
import { apiRequest, uploadEvidenceFiles } from './api/client'
import { AppHeader } from './components/AppHeader'
import { EvidenceList } from './components/EvidenceList'
import { EvidencePreview } from './components/EvidencePreview'
import { EvidenceUploadPanel } from './components/EvidenceUploadPanel'
import { HospitalList } from './components/HospitalList'
import { HospitalRegistrationForm } from './components/HospitalRegistrationForm'
import { Notice } from './components/Notice'
import { TaxYearPanel } from './components/TaxYearPanel'
import { WorkspaceSummary } from './components/WorkspaceSummary'
import { initialHospitalForm } from './constants/labels'
import './App.css'

function App() {
  const [form, setForm] = useState(initialHospitalForm)
  const [hospitals, setHospitals] = useState([])
  const [selectedHospitalId, setSelectedHospitalId] = useState(null)
  const [taxYears, setTaxYears] = useState([])
  const [selectedTaxYear, setSelectedTaxYear] = useState(null)
  const [taxYearInput, setTaxYearInput] = useState(new Date().getFullYear() - 1)
  const [workspaceCounts, setWorkspaceCounts] = useState({ transactions: 0, evidences: 0 })
  const [evidences, setEvidences] = useState([])
  const [selectedEvidenceId, setSelectedEvidenceId] = useState(null)
  const [uploadFiles, setUploadFiles] = useState([])
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const selectedHospital = useMemo(
    () => hospitals.find((hospital) => hospital.id === selectedHospitalId),
    [hospitals, selectedHospitalId],
  )

  const selectedEvidence = useMemo(
    () => evidences.find((evidence) => evidence.id === selectedEvidenceId),
    [evidences, selectedEvidenceId],
  )

  useEffect(() => {
    loadHospitals()
  }, [])

  useEffect(() => {
    if (selectedHospitalId) {
      loadTaxYears(selectedHospitalId)
    }
  }, [selectedHospitalId])

  useEffect(() => {
    if (selectedHospitalId && selectedTaxYear) {
      loadWorkspace(selectedHospitalId, selectedTaxYear)
    }
  }, [selectedHospitalId, selectedTaxYear])

  async function loadHospitals() {
    setLoading(true)
    setError('')
    try {
      const data = await apiRequest('/api/hospitals')
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
      const data = await apiRequest(`/api/hospitals/${hospitalId}/tax-years`)
      setTaxYears(data)
      setSelectedTaxYear(data[0]?.taxYear ?? null)
    } catch (event) {
      setError(event.message)
    }
  }

  async function loadWorkspace(hospitalId, taxYear) {
    setError('')
    try {
      const [transactions, evidenceData] = await Promise.all([
        apiRequest(`/api/hospitals/${hospitalId}/tax-years/${taxYear}/transactions`),
        apiRequest(`/api/hospitals/${hospitalId}/tax-years/${taxYear}/evidences`),
      ])
      setWorkspaceCounts({
        transactions: transactions.length,
        evidences: evidenceData.length,
      })
      setEvidences(evidenceData)
      setSelectedEvidenceId((currentId) => currentId ?? evidenceData[0]?.id ?? null)
    } catch (event) {
      setError(event.message)
    }
  }

  function updateForm(field, value) {
    setForm((current) => ({ ...current, [field]: value }))
  }

  function selectHospital(hospitalId) {
    setSelectedHospitalId(hospitalId)
    setTaxYears([])
    setSelectedTaxYear(null)
    clearWorkspace()
  }

  function selectTaxYear(taxYear) {
    setSelectedTaxYear(taxYear)
    clearWorkspace()
  }

  function clearWorkspace() {
    setWorkspaceCounts({ transactions: 0, evidences: 0 })
    setEvidences([])
    setSelectedEvidenceId(null)
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
      const created = await apiRequest('/api/hospitals', {
        method: 'POST',
        body: JSON.stringify(payload),
      })
      setForm(initialHospitalForm)
      setMessage('병원 기본정보를 등록했습니다.')
      await loadHospitals()
      selectHospital(created.id)
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
      const workspace = await apiRequest(`/api/hospitals/${selectedHospitalId}/tax-years`, {
        method: 'POST',
        body: JSON.stringify({ taxYear: Number(taxYearInput) }),
      })
      setMessage(`${taxYearInput}년 과세연도 작업공간을 준비했습니다.`)
      selectTaxYear(workspace.taxYear)
      await loadTaxYears(selectedHospitalId)
    } catch (event) {
      setError(event.message)
    } finally {
      setLoading(false)
    }
  }

  async function uploadEvidences(event) {
    event.preventDefault()
    if (!selectedHospitalId || !selectedTaxYear) {
      setError('병원과 과세연도를 먼저 선택해 주세요.')
      return
    }
    if (uploadFiles.length === 0) {
      setError('업로드할 파일을 선택해 주세요.')
      return
    }

    setLoading(true)
    setError('')
    setMessage('')

    try {
      const result = await uploadEvidenceFiles({
        hospitalId: selectedHospitalId,
        taxYear: selectedTaxYear,
        files: uploadFiles,
      })
      setUploadFiles([])
      setMessage(`증빙 ${result.uploadedCount}건 업로드 완료, 중복 의심 ${result.duplicateSuspectedCount}건`)
      await loadWorkspace(selectedHospitalId, selectedTaxYear)
    } catch (event) {
      setError(event.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="app-shell">
      <AppHeader loading={loading} />
      <Notice message={message} error={error} />

      <section className="workspace">
        <HospitalRegistrationForm form={form} loading={loading} onChange={updateForm} onSubmit={submitHospital} />
        <HospitalList hospitals={hospitals} selectedHospitalId={selectedHospitalId} onSelect={selectHospital} />
      </section>

      <section className="detail-layout">
        <WorkspaceSummary
          hospital={selectedHospital}
          selectedTaxYear={selectedTaxYear}
          workspaceCounts={workspaceCounts}
        />
        <TaxYearPanel
          taxYears={taxYears}
          taxYearInput={taxYearInput}
          selectedTaxYear={selectedTaxYear}
          loading={loading}
          selectedHospitalId={selectedHospitalId}
          onInputChange={setTaxYearInput}
          onSelect={selectTaxYear}
          onSubmit={createTaxYear}
        />
      </section>

      <section className="upload-layout">
        <EvidenceUploadPanel
          uploadFiles={uploadFiles}
          loading={loading}
          selectedHospitalId={selectedHospitalId}
          selectedTaxYear={selectedTaxYear}
          onFilesChange={setUploadFiles}
          onSubmit={uploadEvidences}
        />
        <EvidenceList evidences={evidences} selectedEvidenceId={selectedEvidenceId} onSelect={setSelectedEvidenceId} />
        <EvidencePreview evidence={selectedEvidence} />
      </section>
    </main>
  )
}

export default App
