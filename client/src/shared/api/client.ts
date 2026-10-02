const baseUrl = (import.meta.env.VITE_API_BASE_URL || '/api/v1').replace(/\/$/, '')

export type ApiRequestOptions = RequestInit & { csrfToken?: string }

// Trả Response để từng tính năng xử lý JSON, file hoặc response không có body.
export function apiFetch(path: string, options: ApiRequestOptions = {}) {
  const { csrfToken, headers: initialHeaders, ...requestOptions } = options
  const headers = new Headers(initialHeaders)
  headers.set('Accept', 'application/json')

  if (csrfToken) {
    headers.set('X-CSRF-TOKEN', csrfToken)
  }

  return fetch(`${baseUrl}/${path.replace(/^\/+/, '')}`, {
    ...requestOptions,
    headers,
    credentials: 'include',
  })
}
