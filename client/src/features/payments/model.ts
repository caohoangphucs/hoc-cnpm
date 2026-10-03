export const MAX_AMOUNT = 999_999_999_999_999
export type Gateway = 'VNPAY' | 'MOMO'
export type InvoiceStatus = 'CHUA_TT' | 'TT_MOT_PHAN' | 'QUA_HAN' | 'DA_TT' | 'HUY'
export type Invoice = {
  id: number; code: string; room: string; month: string; dueDate: string
  total: number; paid: number; discount: number; status: InvoiceStatus
  lines: readonly { label: string; amount: number }[]
}
export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'EXPIRED' | 'NEEDS_REVIEW'
export type Allocation = { invoiceId: number; code: string; room: string; month: string; amount: number }
export type PaymentSession = {
  id: string; gateway: Gateway; amount: number; createdAt: number; expiresAt: number
  status: PaymentStatus; allocations: readonly Allocation[]; confirmedAt?: number; failure?: 'canceled' | 'declined'
}
export type PreviewReply = 'return' | 'pending' | 'success' | 'failed' | 'canceled' | 'review' | 'invalid' | 'wrongAmount' | 'late'
export const paymentLabels: Record<PaymentStatus, string> = { PENDING: 'Chờ xác nhận', SUCCESS: 'Thành công', FAILED: 'Không thành công', EXPIRED: 'Hết hạn', NEEDS_REVIEW: 'Cần đối soát' }
export const gatewayLabels: Record<Gateway, string> = { VNPAY: 'VNPAY', MOMO: 'MoMo' }
const validAmount = (value: number) => Number.isSafeInteger(value) && value >= 0 && value <= MAX_AMOUNT
export function balance(invoice: Invoice) { return invoice.status === 'HUY' ? 0 : invoice.total - invoice.paid }
export function payableIssue(invoice: Invoice) {
  if (!validAmount(invoice.total) || !validAmount(invoice.paid) || invoice.paid > invoice.total) return 'Số tiền hóa đơn không hợp lệ.'
  if (!['CHUA_TT', 'TT_MOT_PHAN', 'QUA_HAN'].includes(invoice.status) || balance(invoice) <= 0) return 'Hóa đơn không còn khoản phải thanh toán.'
  return ''
}
export function vietnamDate(now: number) { return new Date(now + 7 * 60 * 60 * 1000).toISOString().slice(0, 10) }
export function isOverdue(invoice: Invoice, now: number) { return !payableIssue(invoice) && invoice.dueDate < vietnamDate(now) }
export function invoiceLabel(invoice: Invoice, now: number) {
  if (invoice.status === 'HUY') return 'Đã hủy'
  if (balance(invoice) === 0) return 'Đã thanh toán'
  if (isOverdue(invoice, now)) return 'Quá hạn'
  return invoice.paid > 0 ? 'Thanh toán một phần' : 'Chưa thanh toán'
}
export function selectionIssue(invoices: readonly Invoice[], ids: readonly number[]) {
  if (!ids.length) return 'Chọn ít nhất một hóa đơn.'
  if (ids.length > 100 || new Set(ids).size !== ids.length || ids.some(id => !Number.isSafeInteger(id) || id <= 0)) return 'Danh sách hóa đơn không hợp lệ (tối đa 100 hóa đơn, không trùng).'
  const selected = ids.map(id => invoices.find(invoice => invoice.id === id))
  if (selected.some(invoice => !invoice || payableIssue(invoice))) return 'Hóa đơn đã thay đổi hoặc không thể thanh toán. Hãy kiểm tra lại danh sách.'
  const total = selected.reduce((sum, invoice) => sum + BigInt(balance(invoice!)), 0n)
  return total > BigInt(MAX_AMOUNT) ? 'Tổng tiền vượt giới hạn một phiên thanh toán.' : ''
}
export function createPreviewSession(invoices: readonly Invoice[], ids: readonly number[], gateway: Gateway, now: number, id: string): PaymentSession {
  const issue = selectionIssue(invoices, ids)
  if (issue) throw new Error(issue)
  const allocations = [...ids].sort((a, b) => a - b).map(invoiceId => {
    const invoice = invoices.find(value => value.id === invoiceId)!
    return { invoiceId, code: invoice.code, room: invoice.room, month: invoice.month, amount: balance(invoice) }
  })
  return { id, gateway, amount: Number(allocations.reduce((sum, allocation) => sum + BigInt(allocation.amount), 0n)), createdAt: now, expiresAt: now + 15 * 60 * 1000, status: 'PENDING', allocations }
}
export function paymentState(session: PaymentSession, now: number): PaymentStatus {
  return session.status === 'PENDING' && now >= session.expiresAt ? 'EXPIRED' : session.status
}
export function countdown(session: PaymentSession, now: number) {
  const seconds = Math.max(0, Math.ceil((session.expiresAt - now) / 1000))
  return `${Math.floor(seconds / 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}`
}
export function blocksNewPayment(session: PaymentSession, now: number) { return ['PENDING', 'NEEDS_REVIEW'].includes(paymentState(session, now)) }

// This models a SERVER status response only in the development preview.
// It does not verify provider signatures or process real payments in the browser.
export function applyPreviewReply(invoices: readonly Invoice[], session: PaymentSession, reply: PreviewReply, now: number) {
  const unchanged = { invoices: [...invoices], session, error: '' }
  if (session.status === 'SUCCESS' || session.status === 'NEEDS_REVIEW') return unchanged
  if (reply === 'invalid' || reply === 'wrongAmount') return { ...unchanged, error: reply === 'invalid' ? 'Kết quả không hợp lệ (minh họa). Chưa ghi nhận thanh toán; cần kiểm tra lại với cổng.' : 'Số tiền không khớp (minh họa). Chưa ghi nhận thanh toán; dư nợ được giữ nguyên.' }
  if (reply === 'return' || reply === 'pending') return unchanged
  if (reply === 'canceled' || reply === 'failed') return { ...unchanged, session: { ...session, status: paymentState(session, now) === 'EXPIRED' ? 'EXPIRED' as const : 'FAILED' as const, failure: reply === 'canceled' ? 'canceled' as const : 'declined' as const } }
  const allocationsValid = validAmount(session.amount) && session.amount > 0 && new Set(session.allocations.map(a => a.invoiceId)).size === session.allocations.length && session.allocations.length > 0 && session.allocations.every(a => validAmount(a.amount) && a.amount > 0) && session.allocations.reduce((sum, a) => sum + BigInt(a.amount), 0n) === BigInt(session.amount)
  const changed = session.allocations.some(allocation => { const invoice = invoices.find(i => i.id === allocation.invoiceId); return !invoice || Boolean(payableIssue(invoice)) || balance(invoice) < allocation.amount })
  if (reply === 'review' || reply === 'late' || now >= session.expiresAt || !allocationsValid || changed) return { ...unchanged, session: { ...session, status: 'NEEDS_REVIEW' as const } }
  return {
    invoices: invoices.map(invoice => { const allocation = session.allocations.find(a => a.invoiceId === invoice.id); return allocation ? { ...invoice, paid: invoice.paid + allocation.amount, status: invoice.paid + allocation.amount === invoice.total ? 'DA_TT' as const : 'TT_MOT_PHAN' as const } : invoice }),
    session: { ...session, status: 'SUCCESS' as const, confirmedAt: now }, error: '',
  }
}
