export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:18083'

export async function apiRequest(path, options = {}) {
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
      throw new Error('백엔드 서버에 연결할 수 없습니다. 서버 실행 상태와 포트를 확인해 주세요.', {
        cause: event,
      })
    }
    throw event
  }
}

export async function uploadEvidenceFiles({ hospitalId, taxYear, files }) {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))

  try {
    const response = await fetch(
      `${API_BASE_URL}/api/hospitals/${hospitalId}/tax-years/${taxYear}/evidences/upload`,
      {
        method: 'POST',
        body: formData,
      },
    )

    if (!response.ok) {
      const body = await response.json().catch(() => ({}))
      throw new Error(getSafeErrorMessage(response.status, body.code))
    }

    return response.json()
  } catch (event) {
    if (event instanceof TypeError) {
      throw new Error('백엔드 서버에 연결할 수 없습니다.', { cause: event })
    }
    throw event
  }
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
