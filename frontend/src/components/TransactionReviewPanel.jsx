import { useEffect, useMemo, useState } from 'react'
import { reviewStatusLabels, riskTagLabels, transactionTypeLabels } from '../constants/labels'
import { formatWon } from '../utils/formatters'

const emptyReviewForm = {
  transactionDate: '',
  counterpartyName: '',
  counterpartyBusinessNumber: '',
  amount: '',
  supplyAmount: '',
  vatAmount: '',
  transactionType: 'EXPENSE',
  accountTitle: '',
  reviewStatus: 'NOT_REVIEWED',
  riskTags: [],
  memo: '',
}

const completedStatuses = new Set(['CONFIRMED', 'EXCLUDED'])
const initialAccountTitles = [
  '의약품비',
  '진료재료비',
  '소모품비',
  '지급수수료',
  '임차료',
  '관리비',
  '광고선전비',
  '급여',
  '복리후생비',
  '접대비',
  '차량유지비',
  '통신비',
  '세금과공과',
  '보험료',
  '수선비',
  '고정자산 후보',
]

export function TransactionReviewPanel({
  evidences,
  transactions,
  counterpartyAccountRules,
  selectedEvidence,
  selectedTransactionId,
  selectedHospitalId,
  loading,
  onSuggestAccountTitles,
  onSelectEvidence,
  onSelectTransaction,
  onSave,
}) {
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [tagFilter, setTagFilter] = useState('ALL')

  const filteredTransactions = useMemo(() => {
    return transactions.filter((transaction) => {
      const matchesStatus = statusFilter === 'ALL' || transaction.reviewStatus === statusFilter
      const matchesTag = tagFilter === 'ALL' || transaction.riskTags?.includes(tagFilter)
      const keyword = search.trim().toLowerCase()
      const matchesSearch =
        keyword === '' ||
        transaction.counterpartyName?.toLowerCase().includes(keyword) ||
        transaction.accountTitle?.toLowerCase().includes(keyword) ||
        transaction.memo?.toLowerCase().includes(keyword) ||
        transaction.riskTags?.some((tag) => riskTagLabels[tag]?.toLowerCase().includes(keyword))
      return matchesStatus && matchesTag && matchesSearch
    })
  }, [transactions, search, statusFilter, tagFilter])

  const selectedTransaction = useMemo(
    () => transactions.find((transaction) => transaction.id === selectedTransactionId),
    [transactions, selectedTransactionId],
  )

  const remainingCount = transactions.filter((transaction) => !completedStatuses.has(transaction.reviewStatus)).length
  const completedCount = transactions.length - remainingCount

  return (
    <section className="review-layout">
      <div className="panel transaction-list-panel">
        <div className="panel-header">
          <h2>거래 목록</h2>
          <span>
            남은 {remainingCount}건 · 완료 {completedCount}건
          </span>
        </div>

        <div className="review-filters">
          <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="거래처, 계정, 메모, 태그 검색" />
          <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
            <option value="ALL">전체 상태</option>
            {Object.entries(reviewStatusLabels).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
          <select value={tagFilter} onChange={(event) => setTagFilter(event.target.value)}>
            <option value="ALL">전체 태그</option>
            {Object.entries(riskTagLabels).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </div>

        <div className="transaction-list">
          {filteredTransactions.length === 0 && <p className="empty">표시할 거래가 없습니다.</p>}
          {filteredTransactions.map((transaction) => (
            <button
              className={`transaction-item ${transaction.id === selectedTransactionId ? 'selected' : ''}`}
              key={transaction.id}
              type="button"
              onClick={() => onSelectTransaction(transaction.id)}
            >
              <strong>{transaction.counterpartyName}</strong>
              <span>
                {transaction.transactionDate} · {formatWon(transaction.amount)} · {reviewStatusLabels[transaction.reviewStatus]}
              </span>
              <small>
                {transactionTypeLabels[transaction.transactionType]} · {transaction.accountTitle || '계정 미입력'}
              </small>
              <RiskTagChips tags={transaction.riskTags ?? []} />
            </button>
          ))}
        </div>

        <div className="counterparty-rule-list">
          <strong>거래처 사전</strong>
          {counterpartyAccountRules.length === 0 ? (
            <p className="empty">저장된 거래처 사전이 없습니다.</p>
          ) : (
            counterpartyAccountRules.slice(0, 5).map((rule) => (
              <div className="counterparty-rule-item" key={rule.id}>
                <span>{rule.counterpartyName}</span>
                <strong>{rule.accountTitle}</strong>
                <small>{rule.learnedCount}회 반영</small>
              </div>
            ))
          )}
        </div>
      </div>

      <div className="panel review-source-panel">
        <div className="panel-header">
          <h2>OCR 증빙 선택</h2>
          <span>{evidences.length}건</span>
        </div>

        <div className="transaction-list">
          {evidences.length === 0 && <p className="empty">검토할 증빙이 없습니다.</p>}
          {evidences.map((evidence) => (
            <button
              className={`transaction-item ${evidence.id === selectedEvidence?.id ? 'selected' : ''}`}
              key={evidence.id}
              type="button"
              onClick={() => onSelectEvidence(evidence.id)}
            >
              <strong>{evidence.extractedSupplierName || evidence.originalFileName}</strong>
              <span>
                {evidence.extractedTransactionDate || '날짜 없음'} · {formatWon(evidence.extractedTotalAmount)}
              </span>
              <small>{evidence.transactionId ? '거래 연결됨' : '거래 미생성'}</small>
            </button>
          ))}
        </div>
      </div>

      <ReviewForm
        key={`${selectedEvidence?.id ?? 'none'}-${selectedTransaction?.id ?? 'new'}`}
        evidence={selectedEvidence}
        transaction={selectedTransaction}
        selectedHospitalId={selectedHospitalId}
        loading={loading}
        onSuggestAccountTitles={onSuggestAccountTitles}
        onSave={onSave}
      />
    </section>
  )
}

function ReviewForm({ evidence, transaction, selectedHospitalId, loading, onSuggestAccountTitles, onSave }) {
  const [form, setForm] = useState(() => buildReviewForm(evidence, transaction))
  const [suggestions, setSuggestions] = useState([])
  const [suggestionLoading, setSuggestionLoading] = useState(false)
  const suggestionItemName = evidence?.extractedItemName ?? ''
  const canRequestSuggestions = Boolean(
    selectedHospitalId &&
      onSuggestAccountTitles &&
      (form.counterpartyName.trim() || suggestionItemName || form.amount !== ''),
  )

  useEffect(() => {
    if (!canRequestSuggestions) {
      return undefined
    }
    const counterpartyName = form.counterpartyName.trim()
    const amount = form.amount

    const timeoutId = window.setTimeout(async () => {
      setSuggestionLoading(true)
      try {
        const data = await onSuggestAccountTitles({
          hospitalId: selectedHospitalId,
          counterpartyName,
          itemName: suggestionItemName,
          amount,
        })
        setSuggestions(data)
        if (!form.accountTitle && data[0]?.accountTitle) {
          setForm((current) => (current.accountTitle ? current : { ...current, accountTitle: data[0].accountTitle }))
        }
      } catch {
        setSuggestions([])
      } finally {
        setSuggestionLoading(false)
      }
    }, 250)

    return () => window.clearTimeout(timeoutId)
  }, [
    canRequestSuggestions,
    form.accountTitle,
    form.amount,
    form.counterpartyName,
    onSuggestAccountTitles,
    selectedHospitalId,
    suggestionItemName,
  ])

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }))
  }

  function submit(event, overrideStatus) {
    event.preventDefault()
    onSave({
      transactionDate: form.transactionDate || null,
      counterpartyName: form.counterpartyName,
      counterpartyBusinessNumber: form.counterpartyBusinessNumber || null,
      amount: form.amount === '' ? null : Number(form.amount),
      supplyAmount: form.supplyAmount === '' ? null : Number(form.supplyAmount),
      vatAmount: form.vatAmount === '' ? null : Number(form.vatAmount),
      transactionType: form.transactionType,
      accountTitle: form.accountTitle || null,
      reviewStatus: overrideStatus || form.reviewStatus,
      riskTags: form.riskTags,
      memo: form.memo || null,
    })
  }

  function handleKeyDown(event) {
    if (event.ctrlKey && event.key === 'Enter') {
      submit(event)
    }
    if (event.altKey && event.key.toLowerCase() === 'c') {
      submit(event, 'CONFIRMED')
    }
    if (event.altKey && event.key.toLowerCase() === 'h') {
      submit(event, 'HOLD')
    }
    if (event.altKey && event.key.toLowerCase() === 'x') {
      submit(event, 'EXCLUDED')
    }
  }

  return (
    <form className="panel transaction-review-panel" onSubmit={submit} onKeyDown={handleKeyDown}>
      <div className="panel-header">
        <h2>수동 검토</h2>
        <span>{transaction ? `거래 #${transaction.id}` : evidence ? 'OCR에서 거래 생성' : '미선택'}</span>
      </div>

      {!evidence && !transaction ? (
        <p className="empty">증빙 또는 거래를 선택해 주세요.</p>
      ) : (
        <>
          <div className="form-grid review-grid">
            <label>
              거래일
              <input
                type="date"
                value={form.transactionDate}
                onChange={(event) => updateField('transactionDate', event.target.value)}
              />
            </label>
            <label>
              거래처
              <input
                required
                value={form.counterpartyName}
                onChange={(event) => updateField('counterpartyName', event.target.value)}
              />
            </label>
            <label>
              사업자등록번호
              <input
                value={form.counterpartyBusinessNumber}
                onChange={(event) => updateField('counterpartyBusinessNumber', event.target.value)}
              />
            </label>
            <label>
              총액
              <input required type="number" value={form.amount} onChange={(event) => updateField('amount', event.target.value)} />
            </label>
            <label>
              공급가액
              <input type="number" value={form.supplyAmount} onChange={(event) => updateField('supplyAmount', event.target.value)} />
            </label>
            <label>
              부가세
              <input type="number" value={form.vatAmount} onChange={(event) => updateField('vatAmount', event.target.value)} />
            </label>
            <label>
              수입/비용
              <select value={form.transactionType} onChange={(event) => updateField('transactionType', event.target.value)}>
                <option value="EXPENSE">비용</option>
                <option value="REVENUE">수입</option>
              </select>
            </label>
            <label>
              계정과목
              <input
                list="account-title-options"
                value={form.accountTitle}
                onChange={(event) => updateField('accountTitle', event.target.value)}
              />
              <datalist id="account-title-options">
                {initialAccountTitles.map((title) => (
                  <option key={title} value={title} />
                ))}
              </datalist>
            </label>
            <label>
              검토상태
              <select value={form.reviewStatus} onChange={(event) => updateField('reviewStatus', event.target.value)}>
                {Object.entries(reviewStatusLabels).map(([value, label]) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </select>
            </label>
          </div>

          <AccountTitleSuggestions
            loading={suggestionLoading}
            suggestions={canRequestSuggestions ? suggestions : []}
            onApply={(accountTitle) => updateField('accountTitle', accountTitle)}
          />

          <RiskTagEditor selectedTags={form.riskTags} onChange={(riskTags) => updateField('riskTags', riskTags)} />

          <label className="raw-text-field">
            메모
            <textarea value={form.memo} onChange={(event) => updateField('memo', event.target.value)} />
          </label>

          <div className="review-actions">
            <button className="primary-button" type="submit" disabled={loading}>
              저장
            </button>
            <button className="secondary-button" type="button" disabled={loading} onClick={(event) => submit(event, 'CONFIRMED')}>
              확정
            </button>
            <button className="secondary-button" type="button" disabled={loading} onClick={(event) => submit(event, 'HOLD')}>
              보류
            </button>
            <button className="secondary-button danger-button" type="button" disabled={loading} onClick={(event) => submit(event, 'EXCLUDED')}>
              제외
            </button>
          </div>
        </>
      )}
    </form>
  )
}

function AccountTitleSuggestions({ loading, suggestions, onApply }) {
  if (loading) {
    return <p className="account-suggestion-status">계정과목 추천 확인 중</p>
  }

  if (suggestions.length === 0) {
    return <p className="account-suggestion-status">아직 추천할 계정과목이 없습니다.</p>
  }

  return (
    <div className="account-suggestions">
      {suggestions.map((suggestion) => (
        <button
          className={`account-suggestion ${suggestion.fixedAssetCandidate ? 'asset-candidate' : ''}`}
          key={`${suggestion.source}-${suggestion.accountTitle}`}
          type="button"
          onClick={() => onApply(suggestion.accountTitle)}
        >
          <strong>{suggestion.accountTitle}</strong>
          <span>{suggestion.reason}</span>
          <small>
            {suggestion.source} · 신뢰도 {suggestion.confidence}%
          </small>
        </button>
      ))}
    </div>
  )
}

function RiskTagEditor({ selectedTags, onChange }) {
  const selected = new Set(selectedTags)

  function toggle(tag) {
    const next = new Set(selected)
    if (next.has(tag)) {
      next.delete(tag)
    } else {
      next.add(tag)
    }
    onChange([...next])
  }

  return (
    <div className="risk-tag-editor">
      <strong>위험 태그</strong>
      <div className="risk-tag-options">
        {Object.entries(riskTagLabels).map(([tag, label]) => (
          <label className="risk-tag-option" key={tag}>
            <input type="checkbox" checked={selected.has(tag)} onChange={() => toggle(tag)} />
            {label}
          </label>
        ))}
      </div>
    </div>
  )
}

function RiskTagChips({ tags }) {
  if (tags.length === 0) {
    return null
  }

  return (
    <div className="risk-tag-chips">
      {tags.map((tag) => (
        <span className="risk-tag-chip" key={tag}>
          {riskTagLabels[tag] ?? tag}
        </span>
      ))}
    </div>
  )
}

function buildReviewForm(evidence, transaction) {
  if (transaction) {
    return {
      transactionDate: transaction.transactionDate ?? '',
      counterpartyName: transaction.counterpartyName ?? '',
      counterpartyBusinessNumber: transaction.counterpartyBusinessNumber ?? '',
      amount: transaction.amount ?? '',
      supplyAmount: transaction.supplyAmount ?? '',
      vatAmount: transaction.vatAmount ?? '',
      transactionType: transaction.transactionType ?? 'EXPENSE',
      accountTitle: transaction.accountTitle ?? '',
      reviewStatus: transaction.reviewStatus ?? 'NOT_REVIEWED',
      riskTags: transaction.riskTags ?? [],
      memo: transaction.memo ?? '',
    }
  }

  if (evidence) {
    return {
      transactionDate: evidence.extractedTransactionDate ?? '',
      counterpartyName: evidence.extractedSupplierName ?? '',
      counterpartyBusinessNumber: evidence.extractedBusinessRegistrationNumber ?? '',
      amount: evidence.extractedTotalAmount ?? '',
      supplyAmount: evidence.extractedSupplyAmount ?? '',
      vatAmount: evidence.extractedVatAmount ?? '',
      transactionType: 'EXPENSE',
      accountTitle: '',
      reviewStatus: evidence.ocrReviewRequired ? 'NEEDS_REVIEW' : 'NOT_REVIEWED',
      riskTags: evidence.ocrReviewRequired ? ['OCR_REVIEW_REQUIRED'] : [],
      memo: '',
    }
  }

  return emptyReviewForm
}
