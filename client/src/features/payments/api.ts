import { instant, integer, invalidResponse, list, object, text } from '../../shared/api/protocol.ts'
export type Payment = { id: string; gateway: string; providerRef: string; amount: number; invoiceIds: number[]; status: string; payUrl: string | null; expiresAt: string }
export function safePaymentUrl(value: unknown): string | null {
  if (value == null) return null
  const url = new URL(text(value))
  if (url.protocol !== 'https:' || url.username || url.password) throw invalidResponse()
  return url.href
}
export function parsePayment(value: unknown): Payment { const v = object(value); return { id: text(v.id), gateway: text(v.gateway), providerRef: text(v.providerRef), amount: integer(v.amount), invoiceIds: list(v.invoiceIds, id => integer(id, 1)), status: text(v.status), payUrl: safePaymentUrl(v.payUrl), expiresAt: instant(v.expiresAt) } }
export const parsePayments = (value: unknown) => list(value, parsePayment)
export const paymentLabels: Record<string, string> = { PENDING: 'Chờ xác nhận', SUCCESS: 'Thành công', FAILED: 'Không thành công', EXPIRED: 'Hết hạn', NEEDS_REVIEW: 'Cần đối soát' }
