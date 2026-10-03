import { enumValue, integer, list, object, text, vietnamTime, command } from '../../shared/api/protocol.ts'
export type Priority = 'THAP' | 'TRUNG_BINH' | 'CAO' | 'KHAN_CAP'
export type RepairStatus = 'MOI' | 'DANG_XU_LY' | 'HOAN_THANH' | 'HUY'
export type Repair = { id: number; roomId: string; studentId: string; title: string; priority: Priority; status: RepairStatus; version: number }
export type SupplyLine = { supplyId: string; quantity: number }
export const statusLabels: Record<RepairStatus, string> = { MOI: 'Chưa tiếp nhận', DANG_XU_LY: 'Đang xử lý', HOAN_THANH: 'Hoàn thành', HUY: 'Đã hủy' }
export const priorityLabels: Record<Priority, string> = { THAP: 'Thấp', TRUNG_BINH: 'Bình thường', CAO: 'Cao', KHAN_CAP: 'Khẩn cấp' }
export const priorityOrder: Record<Priority, number> = { KHAN_CAP: 0, CAO: 1, TRUNG_BINH: 2, THAP: 3 }
export function parseRepair(value: unknown): Repair {
  const v = object(value)
  return { id: integer(v.id, 1), roomId: text(v.roomId), studentId: text(v.studentId), title: text(v.title), priority: enumValue(v.priority, ['THAP', 'TRUNG_BINH', 'CAO', 'KHAN_CAP']), status: enumValue(v.status, ['MOI', 'DANG_XU_LY', 'HOAN_THANH', 'HUY']), version: integer(v.version, 1) }
}
export const parseRepairs = (v: unknown) => list(v, parseRepair)
export function repairPayload(mode: 'schedule' | 'complete', version: number, note: string, schedule: string, warrantyUntil: string, supplies: SupplyLine[]) {
  const errors: Record<string, string> = {}
  if (!note.trim() || note.trim().length > 1000) errors.note = 'Nhập nội dung từ 1 đến 1.000 ký tự.'
  if (mode === 'schedule' && !vietnamTime(schedule)) errors.schedule = 'Chọn thời gian hợp lệ theo giờ Việt Nam.'
  if (warrantyUntil && (!/^\d{4}-\d{2}-\d{2}$/.test(warrantyUntil) || !Number.isFinite(Date.parse(warrantyUntil)) || new Date(warrantyUntil).toISOString().slice(0, 10) !== warrantyUntil)) errors.warrantyUntil = 'Chọn ngày bảo hành hợp lệ.'
  const lines = supplies.map(s => ({ supplyId: s.supplyId.trim(), quantity: s.quantity }))
  if (mode === 'complete' && (lines.length > 100 || new Set(lines.map(s => s.supplyId)).size !== lines.length || lines.some(s => !s.supplyId || s.supplyId.length > 1000 || !Number.isInteger(s.quantity) || s.quantity < 1 || s.quantity > 2147483647))) errors.supplies = 'Mã vật tư không được trùng; số lượng phải là số nguyên dương hợp lệ.'
  return { errors, body: mode === 'schedule' ? { version, note: note.trim(), scheduledAt: vietnamTime(schedule), warrantyUntil: warrantyUntil || null } : { version, note: note.trim(), warrantyUntil: warrantyUntil || null, supplies: lines } }
}
export function parseCompletion(v: unknown) { const data = object(v); return { status: enumValue(data.status, ['HOAN_THANH', 'INSUFFICIENT_STOCK']), result: command(data.result), shortages: list(data.shortages, text) } }
