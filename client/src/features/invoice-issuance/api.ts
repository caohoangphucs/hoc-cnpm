import { integer, invalidResponse, list, nullableText, object, paged, text } from '../../shared/api/protocol.ts'
export type Invoice = { id: number; roomId: string; month: string; lines: { name: string; quantity: number; unitPrice: number; total: number }[]; discount: number; total: number; paid: number; balance: number; dueDate: string; status: string }
export function parseInvoice(value: unknown): Invoice {
  const v = object(value), dueDate = text(v.dueDate)
  if (!/^\d{4}-\d{2}-\d{2}$/.test(dueDate) || !Number.isFinite(Date.parse(dueDate)) || new Date(dueDate).toISOString().slice(0, 10) !== dueDate) throw invalidResponse()
  return { id: integer(v.id, 1), roomId: text(v.roomId), month: text(v.month), lines: list(v.lines, item => { const l = object(item); if (typeof l.quantity !== 'number' || !Number.isFinite(l.quantity) || l.quantity < 0) throw invalidResponse(); return { name: text(l.name), quantity: l.quantity, unitPrice: integer(l.unitPrice), total: integer(l.total) } }), discount: integer(v.discount), total: integer(v.total), paid: integer(v.paid), balance: integer(v.balance), dueDate, status: text(v.status) }
}
export const parseInvoices = (v: unknown) => paged(v, parseInvoice)
export const invoiceLabels: Record<string, string> = { CHUA_TT: 'Chưa thanh toán', TT_MOT_PHAN: 'Thanh toán một phần', QUA_HAN: 'Quá hạn', DA_TT: 'Đã thanh toán', HUY: 'Đã hủy' }
export type DraftRoom = { roomId: string; oldElectricity: number; newElectricity: number; oldWater: number; newWater: number; electricityPrice: number; waterPrice: number; roomPrice: number; discount: number; extras: number; total: number; warnings: string[] }
export type Draft = { id: string; version: number; month: string; rooms: DraftRoom[]; inputHash: string }
export const draftWarningLabels: Record<string, string> = { MISSING_METER: 'Thiếu chỉ số điện nước', MISSING_PRICE: 'Thiếu biểu giá đã duyệt', ALREADY_ISSUED: 'Đã phát hành hóa đơn' }
export function parseDraft(value: unknown): Draft { const v = object(value); return { id: text(v.id), version: integer(v.version, 1), month: text(v.month), inputHash: text(v.inputHash), rooms: list(v.rooms, item => { const r = object(item); return { roomId: text(r.roomId), oldElectricity: integer(r.oldElectricity), newElectricity: integer(r.newElectricity), oldWater: integer(r.oldWater), newWater: integer(r.newWater), electricityPrice: integer(r.electricityPrice), waterPrice: integer(r.waterPrice), roomPrice: integer(r.roomPrice), discount: integer(r.discount), extras: integer(r.extras), total: integer(r.total), warnings: list(r.warnings, text) } }) } }
export const parseIssue = (value: unknown) => list(value, item => { const v = object(item); return { roomId: text(v.roomId), status: text(v.status), invoiceId: nullableText(v.invoiceId) } })
