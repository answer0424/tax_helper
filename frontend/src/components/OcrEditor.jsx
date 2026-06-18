import { useState } from 'react'
import { ocrStatusLabels } from '../constants/labels'

const emptyOcrForm = {
  transactionDate: '',
  supplierName: '',
  businessRegistrationNumber: '',
  totalAmount: '',
  supplyAmount: '',
  vatAmount: '',
  itemName: '',
  paymentMethod: '',
  approvalNumber: '',
  rawText: '',
  confidence: '',
  reviewRequired: false,
}

export function OcrEditor({ evidence, loading, onRunOcr, onSave }) {
  const [form, setForm] = useState(() => buildOcrForm(evidence))

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }))
  }

  function submit(event) {
    event.preventDefault()
    onSave({
      transactionDate: form.transactionDate || null,
      supplierName: form.supplierName || null,
      businessRegistrationNumber: form.businessRegistrationNumber || null,
      totalAmount: form.totalAmount === '' ? null : Number(form.totalAmount),
      supplyAmount: form.supplyAmount === '' ? null : Number(form.supplyAmount),
      vatAmount: form.vatAmount === '' ? null : Number(form.vatAmount),
      itemName: form.itemName || null,
      paymentMethod: form.paymentMethod || null,
      approvalNumber: form.approvalNumber || null,
      rawText: form.rawText || null,
      confidence: form.confidence === '' ? null : Number(form.confidence),
      reviewRequired: form.reviewRequired,
    })
  }

  return (
    <form className="panel ocr-panel" onSubmit={submit}>
      <div className="panel-header">
        <h2>OCR 추출 결과</h2>
        <span>{evidence ? ocrStatusLabels[evidence.ocrStatus] ?? 'OCR 대기' : '미선택'}</span>
      </div>

      {!evidence ? (
        <p className="empty">증빙을 선택하면 OCR 결과를 확인할 수 있습니다.</p>
      ) : (
        <>
          {evidence.ocrErrorMessage && <p className="inline-error">{evidence.ocrErrorMessage}</p>}

          <div className="ocr-actions">
            <button className="secondary-button" type="button" onClick={onRunOcr} disabled={loading}>
              OCR 실행/재처리
            </button>
            <label className="review-check">
              <input
                type="checkbox"
                checked={form.reviewRequired}
                onChange={(event) => updateField('reviewRequired', event.target.checked)}
              />
              검토필요
            </label>
          </div>

          <div className="form-grid ocr-grid">
            <label>
              거래일
              <input
                type="date"
                value={form.transactionDate}
                onChange={(event) => updateField('transactionDate', event.target.value)}
              />
            </label>
            <label>
              공급자명
              <input value={form.supplierName} onChange={(event) => updateField('supplierName', event.target.value)} />
            </label>
            <label>
              사업자등록번호
              <input
                value={form.businessRegistrationNumber}
                onChange={(event) => updateField('businessRegistrationNumber', event.target.value)}
              />
            </label>
            <label>
              총액
              <input
                type="number"
                value={form.totalAmount}
                onChange={(event) => updateField('totalAmount', event.target.value)}
              />
            </label>
            <label>
              공급가액
              <input
                type="number"
                value={form.supplyAmount}
                onChange={(event) => updateField('supplyAmount', event.target.value)}
              />
            </label>
            <label>
              부가세
              <input type="number" value={form.vatAmount} onChange={(event) => updateField('vatAmount', event.target.value)} />
            </label>
            <label>
              품목명
              <input value={form.itemName} onChange={(event) => updateField('itemName', event.target.value)} />
            </label>
            <label>
              결제수단
              <input value={form.paymentMethod} onChange={(event) => updateField('paymentMethod', event.target.value)} />
            </label>
            <label>
              승인번호
              <input value={form.approvalNumber} onChange={(event) => updateField('approvalNumber', event.target.value)} />
            </label>
            <label>
              신뢰도
              <input
                type="number"
                min="0"
                max="100"
                value={form.confidence}
                onChange={(event) => updateField('confidence', event.target.value)}
              />
            </label>
          </div>

          <label className="raw-text-field">
            OCR 원문
            <textarea value={form.rawText} onChange={(event) => updateField('rawText', event.target.value)} />
          </label>

          <button className="primary-button" type="submit" disabled={loading}>
            OCR 결과 저장
          </button>
        </>
      )}
    </form>
  )
}

function buildOcrForm(evidence) {
  if (!evidence) {
    return emptyOcrForm
  }

  return {
    transactionDate: evidence.extractedTransactionDate ?? '',
    supplierName: evidence.extractedSupplierName ?? '',
    businessRegistrationNumber: evidence.extractedBusinessRegistrationNumber ?? '',
    totalAmount: evidence.extractedTotalAmount ?? '',
    supplyAmount: evidence.extractedSupplyAmount ?? '',
    vatAmount: evidence.extractedVatAmount ?? '',
    itemName: evidence.extractedItemName ?? '',
    paymentMethod: evidence.extractedPaymentMethod ?? '',
    approvalNumber: evidence.extractedApprovalNumber ?? '',
    rawText: evidence.ocrRawText ?? '',
    confidence: evidence.ocrConfidence ?? '',
    reviewRequired: evidence.ocrReviewRequired ?? false,
  }
}
