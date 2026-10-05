import { instant, integer, invalidResponse, list, object, text } from '../../shared/api/protocol.ts'
import type { Invoice } from '../invoice-issuance/api.ts'
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
export function payableInvoice(invoice: Pick<Invoice, 'balance' | 'status'>) {
  return invoice.balance > 0 && ['CHUA_TT', 'TT_MOT_PHAN', 'QUA_HAN'].includes(invoice.status)
}
export function paymentSelection(invoices: Invoice[]) {
  if (!invoices.length || invoices.length > 100 || new Set(invoices.map(i => i.id)).size !== invoices.length) throw new Error('Chọn từ 1 đến 100 hóa đơn khác nhau.')
  if (invoices.some(i => !payableInvoice(i) || !Number.isSafeInteger(i.balance) || !Number.isInteger(i.id) || i.id < 1 || i.id > 2147483647)) throw new Error('Có hóa đơn không còn đủ điều kiện thanh toán. Tải lại và chọn lại.')
  const total = invoices.reduce((sum, i) => sum + BigInt(i.balance), 0n)
  if (total > 9999999999n) throw new Error('Tổng thanh toán VNPAY tối đa 9.999.999.999 đồng. Chọn ít hóa đơn hơn.')
  return { invoiceIds: invoices.map(i => i.id), total: Number(total) }
}
