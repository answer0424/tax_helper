export const initialHospitalForm = {
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

export const taxationLabels = {
  VAT_EXEMPT: '면세 중심',
  TAXABLE: '과세',
  MIXED: '과세/면세 겸영',
}

export const uploadStatusLabels = {
  UPLOADED: '업로드 완료',
  DUPLICATE_SUSPECTED: '중복 의심',
  UNSUPPORTED: '지원 안 됨',
}

export const ocrStatusLabels = {
  NOT_STARTED: 'OCR 대기',
  PROCESSING: 'OCR 처리 중',
  COMPLETED: 'OCR 완료',
  FAILED: 'OCR 실패',
}

export const reviewStatusLabels = {
  NOT_REVIEWED: '미검토',
  NEEDS_REVIEW: '검토필요',
  CONFIRMED: '확정',
  HOLD: '보류',
  EXCLUDED: '제외',
}

export const transactionTypeLabels = {
  REVENUE: '수입',
  EXPENSE: '비용',
}
