import { enumValue, instant, integer, object, text, vietnamTime } from '../../shared/api/protocol.ts'
export type EntryType = 'VAO' | 'RA'
export type BarrierStatus = 'PENDING' | 'OPENED' | 'FAILED'
export type Entry = { eventId: string; studentId: string; type: EntryType; time: string; barrierStatus: BarrierStatus; violationId?: number }
export const barrierLabels: Record<BarrierStatus, string> = { PENDING: 'Chờ thiết bị', OPENED: 'Đã mở', FAILED: 'Thiết bị lỗi' }
export function parseEntry(value: unknown): Entry {
  const v = object(value)
  return { eventId: text(v.eventId), studentId: text(v.studentId), type: enumValue(v.type, ['VAO', 'RA']), time: instant(v.time), barrierStatus: enumValue(v.barrierStatus, ['PENDING', 'OPENED', 'FAILED']), violationId: v.violationId == null ? undefined : integer(v.violationId, 1) }
}
export function entryPayload(eventId: string, cardToken: string, type: EntryType, deviceId: string, note: string) {
  if (!cardToken.trim() || !deviceId.trim()) throw new Error('Nhập mã thẻ và mã thiết bị tại cổng.')
  if ([cardToken, deviceId, note].some(s => s.trim().length > 1000)) throw new Error('Mỗi trường tối đa 1.000 ký tự.')
  return { eventId, cardToken: cardToken.trim(), type, deviceId: deviceId.trim(), note: note.trim() || null }
}
export function visitorPayload(hostStudentId: string, name: string, identity: string, from: string, to: string) {
  if ([hostStudentId, name, identity].some(s => !s.trim() || s.trim().length > 1000)) throw new Error('Điền đầy đủ thông tin khách và MSSV tiếp đón, tối đa 1.000 ký tự mỗi trường.')
  const validFrom = vietnamTime(from), validTo = vietnamTime(to)
  if (!validFrom || !validTo || Date.parse(validTo) <= Date.parse(validFrom)) throw new Error('Chọn khoảng thời gian thăm hợp lệ theo giờ Việt Nam.')
  return { hostStudentId: hostStudentId.trim(), name: name.trim(), identity: identity.trim(), validFrom, validTo }
}
