import { command, enumValue, instant, integer, invalidResponse, list, object, text, vietnamTime } from '../../shared/api/protocol.ts'
export type Activity = { id: number; semesterId: number; title: string; startAt: string; endAt: string; capacity: number; remaining: number; points: number; status: string; version: number }
export function parseActivity(value: unknown): Activity {
  const v = object(value), capacity = integer(v.capacity, 1), remaining = integer(v.remaining), startAt = instant(v.startAt), endAt = instant(v.endAt)
  if (remaining > capacity || Date.parse(endAt) <= Date.parse(startAt)) throw invalidResponse()
  return { id: integer(v.id, 1), semesterId: integer(v.semesterId, 1), title: text(v.title), startAt, endAt, capacity, remaining, points: integer(v.points, 1), status: text(v.status), version: integer(v.version, 1) }
}
export const parseActivities = (v: unknown) => list(v, parseActivity)
export type Registration = { id: number; activityId: string; status: 'REGISTERED' | 'ATTENDED' | 'COMPLETED' | 'CANCELLED'; qrToken: string; version: number }
export function parseRegistration(value: unknown): Registration { const v = object(value); return { id: integer(v.id, 1), activityId: text(v.activityId), status: enumValue(v.status, ['REGISTERED', 'ATTENDED', 'COMPLETED', 'CANCELLED']), qrToken: typeof v.qrToken === 'string' ? v.qrToken : text(v.qrToken), version: integer(v.version, 1) } }
export function cancellationRequest(registration: Pick<Registration, 'id' | 'version'>) { return { path: `activities/${registration.id}/registrations`, body: { version: registration.version }, method: 'DELETE' } }
export function parsePoints(value: unknown) {
  const v = object(value)
  return { semesterId: integer(v.semesterId, 1), earned: integer(v.earned), available: integer(v.available), convertedDrl: integer(v.convertedDrl), ledger: list(v.ledger, item => {
    const x = object(item); if (typeof x.delta !== 'number' || !Number.isSafeInteger(x.delta)) throw invalidResponse()
    return { id: integer(x.id, 1), delta: x.delta, kind: text(x.kind), time: instant(x.time) }
  }) }
}
export function parseConversion(value: unknown) { const v = object(value); return { id: text(v.id), status: enumValue(v.status, ['PENDING']), pointsSpent: integer(v.pointsSpent, 1), drl: integer(v.drl, 1), externalState: enumValue(v.externalState, ['PENDING']) } }
export function parseCertificate(value: unknown) { const v = object(value); return { id: text(v.id), status: enumValue(v.status, ['PENDING']) } }
export const parseCommands = (v: unknown) => list(v, command)
export function activityPayload(semesterId: string, title: string, start: string, end: string, capacity: string, points: string) {
  const startAt = vietnamTime(start), endAt = vietnamTime(end)
  if (!startAt || !endAt || Date.parse(endAt) <= Date.parse(startAt)) throw new Error('Chọn khoảng thời gian hoạt động hợp lệ theo giờ Việt Nam.')
  if (![semesterId, capacity, points].every(s => /^[1-9]\d*$/.test(s) && Number.isSafeInteger(Number(s)) && Number(s) <= 2147483647)) throw new Error('Mã học kỳ, số chỗ và điểm phải là số nguyên dương hợp lệ.')
  if (!title.trim() || title.trim().length > 1000) throw new Error('Nhập tên hoạt động từ 1 đến 1.000 ký tự.')
  return { semesterId: Number(semesterId), title: title.trim(), startAt, endAt, capacity: Number(capacity), points: Number(points) }
}
export function conversionPayload(semesterId: number, drl: string, points: Pick<ReturnType<typeof parsePoints>, 'available' | 'convertedDrl'>) {
  const max = Math.max(0, Math.min(5 - points.convertedDrl, Math.floor(points.available / 10))), amount = Number(drl)
  if (!/^[1-5]$/.test(drl) || amount > max) throw new Error(`Có thể quy đổi thêm tối đa ${max} điểm rèn luyện.`)
  return { semesterId, drl: amount }
}

export type Ticket = { activity: Activity; receipt: Registration; cancelled: boolean }
export const parseTickets = (value: unknown): Ticket[] => list(value, item => {
  const v = object(item), receipt = parseRegistration(v.receipt)
  return { activity: parseActivity(v.activity), receipt, cancelled: receipt.status === 'CANCELLED' }
})
