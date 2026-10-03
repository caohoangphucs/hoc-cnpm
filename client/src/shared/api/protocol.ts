export class ApiError extends Error {
  status: number
  code: string
  requestId?: string
  constructor(message: string, status = 0, code = 'NETWORK', requestId?: string) {
    super(message); this.name = 'ApiError'; this.status = status; this.code = code; this.requestId = requestId
  }
}
export function object(value: unknown): Record<string, unknown> {
  if (!value || typeof value !== 'object' || Array.isArray(value)) throw invalidResponse()
  return value as Record<string, unknown>
}
export function text(value: unknown): string { if (typeof value !== 'string' || !value.trim()) throw invalidResponse(); return value }
export function integer(value: unknown, min = 0): number { if (typeof value !== 'number' || !Number.isSafeInteger(value) || value < min) throw invalidResponse(); return value }
export function instant(value: unknown): string { const s = text(value); if (!Number.isFinite(Date.parse(s))) throw invalidResponse(); return s }
export function list<T>(value: unknown, decode: (item: unknown) => T): T[] { if (!Array.isArray(value)) throw invalidResponse(); return value.map(decode) }
export function enumValue<T extends string>(value: unknown, allowed: readonly T[]): T { if (!allowed.includes(value as T)) throw invalidResponse(); return value as T }
export function invalidResponse() { return new ApiError('Dữ liệu trả về chưa hợp lệ. Vui lòng tải lại hoặc liên hệ Ban quản lý.', 0, 'INVALID_RESPONSE') }
export function errorMessage(error: unknown): string { return error instanceof ApiError ? `${error.message}${error.requestId ? ` Mã tra cứu: ${error.requestId}.` : ''}` : 'Không thể kết nối dịch vụ. Vui lòng thử lại.' }
export type RequestIdentity = { signature: string; key: string }
export function requestIdentity(previous: RequestIdentity | null, signature: string, uuid: () => string): RequestIdentity {
  return previous?.signature === signature ? previous : { signature, key: uuid() }
}
// Do not display raw server errors, which may contain internal implementation details.
export async function readJson<T>(response: Response, decode: (value: unknown) => T): Promise<T> {
  const body: unknown = await response.json().catch(() => undefined)
  if (!response.ok) {
    const data = body && typeof body === 'object' ? body as Record<string, unknown> : {}
    const message = response.status === 401 ? 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'
      : response.status === 403 ? 'Bạn chưa có quyền thực hiện thao tác hoặc phiên xác thực đã thay đổi. Tải lại trang trước khi thử lại.'
      : response.status === 404 ? 'Không tìm thấy bản ghi. Vui lòng kiểm tra lại.'
      : response.status === 409 ? 'Dữ liệu đã thay đổi hoặc yêu cầu không còn phù hợp. Tải lại và kiểm tra trước khi thử lại.'
      : response.status === 400 || response.status === 422 ? 'Thông tin gửi chưa hợp lệ. Kiểm tra các trường nhập và điều kiện của yêu cầu.'
      : response.status === 429 ? 'Có quá nhiều yêu cầu. Vui lòng chờ một lát rồi thử lại.'
      : 'Dịch vụ đang gián đoạn. Kết quả chưa được xác nhận; hãy thử lại cùng yêu cầu.'
    throw new ApiError(message, response.status, typeof data.code === 'string' ? data.code : 'HTTP_ERROR', typeof data.requestId === 'string' ? data.requestId : undefined)
  }
  return decode(body)
}
export type CommandResult = { id: string; status: string; version: number; requestId: string }
export function command(value: unknown): CommandResult { const v = object(value); return { id: text(v.id), status: text(v.status), version: integer(v.version, 1), requestId: text(v.requestId) } }
// Browser datetime-local fields represent dormitory time, independent of the device timezone.
export function vietnamTime(value: string): string | undefined {
  if (!/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(value)) return undefined
  const at = Date.parse(`${value}:00+07:00`)
  return Number.isFinite(at) && new Date(at + 7 * 3600000).toISOString().slice(0, 16) === value ? `${value}:00+07:00` : undefined
}
