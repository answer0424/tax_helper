export function formatWon(value) {
  if (value === null || value === undefined) {
    return '미입력'
  }
  return `${Number(value).toLocaleString('ko-KR')}원`
}

export function formatBytes(value) {
  if (!value) {
    return '0 B'
  }
  if (value < 1024) {
    return `${value} B`
  }
  if (value < 1024 * 1024) {
    return `${(value / 1024).toFixed(1)} KB`
  }
  return `${(value / 1024 / 1024).toFixed(1)} MB`
}

export function formatBusinessNumber(value) {
  if (!value || value.length !== 10) {
    return value
  }
  return `${value.slice(0, 3)}-${value.slice(3, 5)}-${value.slice(5)}`
}
