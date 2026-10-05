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
export const draftWarningLabels: Record<string, string> = { MISSING_METER: 'Thiếu chỉ số điện nước', MISSING_PRICE: 'Thiếu biểu giá đã duyệt', ALREADY_ISSUED: 'Đã phát hành hóa đơn', QUANTITY_OUT_OF_RANGE: 'Mức tiêu thụ điện/nước vượt giới hạn phát hành' }
export function parseDraft(value: unknown): Draft { const v = object(value); return { id: text(v.id), version: integer(v.version, 1), month: text(v.month), inputHash: text(v.inputHash), rooms: list(v.rooms, item => { const r = object(item); return { roomId: text(r.roomId), oldElectricity: integer(r.oldElectricity), newElectricity: integer(r.newElectricity), oldWater: integer(r.oldWater), newWater: integer(r.newWater), electricityPrice: integer(r.electricityPrice), waterPrice: integer(r.waterPrice), roomPrice: integer(r.roomPrice), discount: integer(r.discount), extras: integer(r.extras), total: integer(r.total), warnings: list(r.warnings, text) } }) } }
export const parseIssue = (value: unknown) => list(value, item => { const v = object(item); return { roomId: text(v.roomId), status: text(v.status), invoiceId: nullableText(v.invoiceId) } })

// Invoice line quantities are DECIMAL(10,2); cumulative meter readings remain INT.
export const maxMeterConsumption = 99999999
export function draftRoomWarnings(room: Pick<DraftRoom, 'oldElectricity' | 'newElectricity' | 'oldWater' | 'newWater' | 'warnings'>) {
  const amounts = [room.newElectricity - room.oldElectricity, room.newWater - room.oldWater]
  return amounts.some(amount => amount < 0 || amount > maxMeterConsumption) ? [...new Set([...room.warnings, 'QUANTITY_OUT_OF_RANGE'])] : room.warnings
}
export function meterPayload(values: Record<'oldElectricity' | 'newElectricity' | 'oldWater' | 'newWater', string>, revision: string) {
  const number = (value: string) => {
    if (!/^\d+$/.test(value) || !Number.isSafeInteger(Number(value)) || Number(value) > 2147483647) throw new Error('Nhập đủ bốn số nguyên từ 0 đến 2147483647.')
    return Number(value)
  }
  const body = { oldElectricity: number(values.oldElectricity), newElectricity: number(values.newElectricity), oldWater: number(values.oldWater), newWater: number(values.newWater) }
  if (body.newElectricity < body.oldElectricity || body.newWater < body.oldWater) throw new Error('Chỉ số mới phải lớn hơn hoặc bằng chỉ số cũ.')
  if (draftRoomWarnings({ ...body, warnings: [] }).length) throw new Error('Mức tiêu thụ điện/nước mỗi kỳ tối đa 99.999.999. Kiểm tra lại chỉ số trước khi lưu.')
  return { ...body, revision }
}

// A failed batch can commit earlier rooms. Absence from unbilled rooms makes a
// room ineligible for retry, but does not prove which operation issued/deleted it.
export function reconcileIssuance(roomIds: string[], unbilled: { id: string }[]) {
  const remaining = new Set(unbilled.map(room => room.id))
  return roomIds.map(roomId => ({ roomId, status: remaining.has(roomId) ? 'NOT_ISSUED' : 'CHECK_INVOICES', invoiceId: null }))
}

/** Phòng chưa phát hành trong tháng đã chọn. */
export const parseUnbilledRooms = (value: unknown) => list(value, item => { const room = object(item); return { id: text(room.id), buildingId: text(room.buildingId), number: text(room.number) } })

/** Nguồn số đo có thể chưa tồn tại; không coi giá trị 0 là số đo đã nhập. */
export function parseMeter(value: unknown) {
  const v = object(value), reading = v.reading == null ? null : object(v.reading)
  if (typeof v.exists !== 'boolean' || typeof v.editable !== 'boolean') throw invalidResponse()
  return { exists: v.exists, editable: v.editable, revision: text(v.revision),
    oldElectricity: reading ? integer(reading.oldElectricity) : 0,
    newElectricity: reading ? integer(reading.newElectricity) : 0,
    oldWater: reading ? integer(reading.oldWater) : 0,
    newWater: reading ? integer(reading.newWater) : 0 }
}
