export type EntryType = 'VAO' | 'RA'
export type BarrierStatus = 'PENDING' | 'OPENED' | 'FAILED'
export type GateStudent = { id: string; name: string; room: string; card: string; active: boolean; resident: boolean; validFrom: number; validTo: number }
export type Visitor = { id: string; name: string; identity: string; host: string; room: string; approved: boolean; from: number; to: number; inside: boolean }
export type Entry = { eventId: string; studentId: string; name: string; room: string; type: EntryType; at: number; late: boolean; barrier: BarrierStatus; note: string; fingerprint: string }
export type VisitorEntry = { eventId: string; visitorId: string; name: string; host: string; type: EntryType; at: number }
export type GateData = { students: GateStudent[]; visitors: Visitor[]; entries: Entry[]; visitorEntries: VisitorEntry[] }
export const barrierLabels: Record<BarrierStatus, string> = { PENDING: 'Chờ thiết bị', OPENED: 'Đã mở', FAILED: 'Thiết bị lỗi' }
export function lateReturn(type: EntryType, at: number) {
  const local = new Date(at + 7 * 3600000)
  const second = local.getUTCHours() * 3600 + local.getUTCMinutes() * 60 + local.getUTCSeconds() + local.getUTCMilliseconds() / 1000
  return type === 'VAO' && (second > 23 * 3600 || second < 5 * 3600)
}
export function studentIssue(student: GateStudent | undefined, at: number) {
  if (!student) return 'Không tìm thấy thẻ hoặc sinh viên. Kiểm tra lại mã vừa nhập.'
  if (!student.active || at < student.validFrom || at >= student.validTo) return 'Thẻ chưa có hiệu lực, hết hạn hoặc đã bị khóa. Chưa cho qua cổng.'
  if (!student.resident) return 'Sinh viên không còn lưu trú. Liên hệ Ban quản lý để kiểm tra.'
  return ''
}
export function visitorIssue(visitor: Visitor | undefined, type: EntryType, at: number, verified: boolean) {
  if (!visitor) return 'Chưa có khai báo khách phù hợp. Liên hệ sinh viên hoặc Ban quản lý.'
  if (!visitor.approved) return 'Khai báo chưa được duyệt. Chưa cho khách vào cổng.'
  if (at < visitor.from || at >= visitor.to) return 'Ngoài thời gian thăm đã được duyệt. Liên hệ Ban quản lý.'
  if (!verified) return 'Đối chiếu giấy tờ gốc với thông tin khai báo trước khi xác nhận.'
  if ((type === 'VAO') === visitor.inside) return type === 'VAO' ? 'Khách đã được ghi nhận vào; không ghi thêm lượt vào.' : 'Khách chưa có lượt vào đang hiệu lực.'
  return ''
}
export function recordPreviewEntry(data: GateData, input: { eventId: string; studentId: string; type: EntryType; note: string }, at: number): { data: GateData; entry?: Entry; error?: string } {
  const fingerprint = JSON.stringify([input.studentId, input.type, input.note.trim()])
  const previous = data.entries.find(e => e.eventId === input.eventId)
  if (previous) return previous.fingerprint === fingerprint ? { data, entry: previous } : { data, error: 'Mã sự kiện đã dùng với dữ liệu khác; chưa ghi lượt mới.' }
  const student = data.students.find(s => s.id === input.studentId), issue = studentIssue(student, at)
  if (issue || !student) return { data, error: issue }
  if (input.note.trim().length > 1000) return { data, error: 'Ghi chú tối đa 1.000 ký tự.' }
  const entry: Entry = { eventId: input.eventId, studentId: student.id, name: student.name, room: student.room, type: input.type, note: input.note.trim(), at, late: lateReturn(input.type, at), barrier: 'PENDING', fingerprint }
  return { data: { ...data, entries: [entry, ...data.entries] }, entry }
}
export function recordPreviewVisitor(data: GateData, id: string, type: EntryType, verified: boolean, eventId: string, at: number): { data: GateData; error?: string } {
  const prior = data.visitorEntries.find(e => e.eventId === eventId)
  if (prior) return prior.visitorId === id && prior.type === type ? { data } : { data, error: 'Mã sự kiện khách đã được dùng cho dữ liệu khác.' }
  const visitor = data.visitors.find(v => v.id === id), issue = visitorIssue(visitor, type, at, verified)
  if (issue || !visitor) return { data, error: issue }
  return { data: { ...data, visitors: data.visitors.map(v => v.id === id ? { ...v, inside: type === 'VAO' } : v), visitorEntries: [{ eventId, visitorId: id, name: visitor.name, host: visitor.host, type, at }, ...data.visitorEntries] } }
}
export function updatePreviewBarrier(data: GateData, eventId: string, status: BarrierStatus) {
  return { ...data, entries: data.entries.map(e => e.eventId === eventId && e.barrier !== 'OPENED' ? { ...e, barrier: status } : e) }
}
