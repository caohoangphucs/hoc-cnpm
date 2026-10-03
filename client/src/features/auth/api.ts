import { apiFetch } from '@/shared/api/client'
import { parseSession } from './model'

export class AuthError extends Error {
  status: number
  requestId?: string
  fieldErrors: Record<string, string>
  constructor(message: string, status = 0, requestId?: string, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.name = 'AuthError'
    this.status = status
    this.requestId = requestId
    this.fieldErrors = fieldErrors
  }
}

function errorMessage(status: number, action: string) {
  if (status === 401) return action === 'login' ? 'Tên đăng nhập hoặc mật khẩu không đúng.' :
    action === '2fa' ? 'Mã xác thực không đúng hoặc đã hết hạn. Vui lòng kiểm tra lại.' : 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'
  if (status === 403) return 'Không thể xác nhận yêu cầu. Vui lòng thử lại để cập nhật phiên.'
  if (status === 429) return 'Bạn đã thử quá nhiều lần. Vui lòng chờ một lúc rồi thử lại.'
  if (status === 400 || status === 422) return 'Thông tin chưa hợp lệ. Vui lòng kiểm tra các trường bên dưới.'
  if (status === 404 || status === 405 || status >= 500) return 'Dịch vụ đăng nhập hiện chưa sẵn sàng. Vui lòng thử lại sau.'
  return 'Không thể hoàn tất yêu cầu. Vui lòng thử lại.'
}

async function authRequest(action: string, options: RequestInit & { csrfToken?: string } = {}) {
  const timeout = AbortSignal.timeout(15_000)
  let response: Response
  try {
    response = await apiFetch(`auth/${action}`, {
      ...options, signal: options.signal ? AbortSignal.any([options.signal, timeout]) : timeout,
    })
  } catch (error) {
    if (options.signal?.aborted) throw error
    throw new AuthError('Không kết nối được đến dịch vụ đăng nhập. Kiểm tra kết nối và thử lại.')
  }
  if (!response.ok) {
    const body: unknown = await response.json().catch(() => null)
    const data = body && typeof body === 'object' ? body as Record<string, unknown> : {}
    const requestId = typeof data.requestId === 'string' && /^[\w-]{1,100}$/.test(data.requestId) ? data.requestId : undefined
    const fields: Record<string, string> = {}
    if (data.fieldErrors && typeof data.fieldErrors === 'object') {
      for (const name of ['username', 'password', 'code']) {
        if (Object.hasOwn(data.fieldErrors, name)) fields[name] = name === 'code' ? 'Nhập mã xác thực gồm 6 chữ số.' : 'Vui lòng kiểm tra lại thông tin này.'
      }
    }
    throw new AuthError(errorMessage(response.status, action), response.status, requestId, fields)
  }
  return response
}

async function sessionResponse(response: Response) {
  try { return parseSession(await response.json()) }
  catch { throw new AuthError('Chưa đọc được trạng thái đăng nhập. Vui lòng thử lại.') }
}

export async function fetchSession(signal?: AbortSignal) {
  return sessionResponse(await authRequest('session', { signal, cache: 'no-store' }))
}
export async function submitLogin(username: string, password: string, csrfToken: string) {
  return sessionResponse(await authRequest('login', {
    method: 'POST', csrfToken, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ username, password }),
  }))
}
export async function submitTwoFactor(code: string, csrfToken: string) {
  return sessionResponse(await authRequest('2fa', {
    method: 'POST', csrfToken, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ code }),
  }))
}
export async function submitLogout(csrfToken: string) {
  await authRequest('logout', { method: 'POST', csrfToken })
}
export function asAuthError(error: unknown) {
  return error instanceof AuthError ? error : new AuthError('Không thể hoàn tất yêu cầu. Vui lòng thử lại.')
}
