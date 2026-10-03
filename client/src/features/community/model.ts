export type Activity = { id: string; semester: string; title: string; location: string; start: number; end: number; capacity: number; points: number; open: boolean; description: string }
export type Registration = { id: string; activityId: string; studentId: string; name: string; token: string; version: number; status: 'REGISTERED' | 'ATTENDED' | 'COMPLETED' | 'CANCELLED' }
export type Transfer = { id: string; studentId: string; semester: string; drl: number; status: 'PENDING' | 'CONFIRMED' | 'REJECTED' }
export type PointEntry = { id: string; studentId: string; semester: string; points: number; note: string; at: number }
export type Certificate = { id: string; studentId: string; semester: string; status: 'PENDING'; registrationIds: string[]; at: number }
export type CommunityData = { activities: Activity[]; registrations: Registration[]; transfers: Transfer[]; ledger: PointEntry[]; certificates: Certificate[] }
export const registrationLabels = { REGISTERED: 'Đã đăng ký', ATTENDED: 'Đã điểm danh', COMPLETED: 'Đã hoàn thành', CANCELLED: 'Đã hủy' }
export function places(data: CommunityData, id: string) { return data.registrations.filter(r => r.activityId === id && r.status !== 'CANCELLED').length }
export function balance(data: CommunityData, studentId: string, semester: string) { return data.ledger.filter(p => p.studentId === studentId && p.semester === semester).reduce((sum, p) => sum + p.points, 0) }
export function converted(data: CommunityData, studentId: string, semester: string) { return data.transfers.filter(t => t.studentId === studentId && t.semester === semester && t.status !== 'REJECTED').reduce((sum, t) => sum + t.drl, 0) }
export function maxConversion(data: CommunityData, studentId: string, semester: string) { return Math.max(0, Math.min(Math.floor(balance(data, studentId, semester) / 10), 5 - converted(data, studentId, semester))) }
export function registrationIssue(data: CommunityData, activity: Activity | undefined, studentId: string, now: number) {
  if (!activity || !activity.open || activity.start <= now) return 'Hoạt động chưa mở đăng ký hoặc đã bắt đầu.'
  if (data.registrations.some(r => r.activityId === activity.id && r.studentId === studentId && r.status !== 'CANCELLED')) return 'Bạn đã đăng ký hoạt động này.'
  if (places(data, activity.id) >= activity.capacity) return 'Hoạt động đã hết chỗ.'
  if (data.registrations.some(r => r.studentId === studentId && r.status !== 'CANCELLED' && data.activities.some(a => a.id === r.activityId && a.start < activity.end && activity.start < a.end))) return 'Thời gian trùng với hoạt động bạn đã đăng ký.'
  return ''
}
export function registerPreview(data: CommunityData, activityId: string, studentId: string, name: string, token: string, now: number) {
  const error = registrationIssue(data, data.activities.find(a => a.id === activityId), studentId, now)
  if (error) return { data, error }
  return { data: { ...data, registrations: [...data.registrations, { id: token, token, studentId, name, activityId, version: 0, status: 'REGISTERED' as const }] } }
}
export function cancelIssue(data: CommunityData, r: Registration, now: number) {
  const activity = data.activities.find(a => a.id === r.activityId)
  return r.status !== 'REGISTERED' ? 'Chỉ hủy đăng ký chưa điểm danh.' : !activity || activity.start - now < 12 * 3600000 ? 'Chỉ hủy khi còn ít nhất 12 giờ trước khi bắt đầu.' : ''
}
export function cancelPreview(data: CommunityData, id: string, studentId: string, version: number, now: number) {
  const row = data.registrations.find(r => r.id === id), error = !row || row.studentId !== studentId || row.version !== version ? 'Đăng ký không thuộc về bạn hoặc đã thay đổi.' : cancelIssue(data, row, now)
  if (error) return { data, error }
  return { data: { ...data, registrations: data.registrations.map(r => r.id === id ? { ...r, status: 'CANCELLED' as const, version: r.version + 1 } : r) } }
}
export function attendPreview(data: CommunityData, token: string, activityId: string, admin: boolean) {
  const row = data.registrations.find(r => r.token === token && r.activityId === activityId)
  const error = !admin ? 'Chỉ Ban quản lý được điểm danh.' : !row ? 'Mã không thuộc hoạt động đang chọn.' : row.status !== 'REGISTERED' ? 'Đăng ký đã được điểm danh, hoàn thành hoặc hủy.' : ''
  if (error) return { data, error }
  return { data: { ...data, registrations: data.registrations.map(r => r.id === row!.id ? { ...r, status: 'ATTENDED' as const, version: r.version + 1 } : r) } }
}
export function completePreview(data: CommunityData, ids: string[], versions: Record<string, number>, admin: boolean, now: number) {
  const rows = ids.map(id => data.registrations.find(r => r.id === id))
  const error = !admin ? 'Chỉ Ban quản lý được xác nhận hoàn thành.' : !ids.length || new Set(ids).size !== ids.length ? 'Chọn các lượt đã điểm danh khác nhau.' : rows.some(r => !r || r.status !== 'ATTENDED' || r.version !== versions[r.id] || !data.activities.some(a => a.id === r.activityId)) ? 'Có lượt chưa điểm danh hoặc đã thay đổi; chưa cộng điểm nào.' : ''
  if (error) return { data, error }
  const entries = rows.map(r => { const a = data.activities.find(a => a.id === r!.activityId)!; return { id: `complete-${r!.id}`, studentId: r!.studentId, semester: a.semester, points: a.points, note: `Hoàn thành · ${a.title}`, at: now } })
  return { data: { ...data, registrations: data.registrations.map(r => ids.includes(r.id) ? { ...r, status: 'COMPLETED' as const, version: r.version + 1 } : r), ledger: [...data.ledger, ...entries] } }
}
export function convertPreview(data: CommunityData, studentId: string, semester: string, drl: number, key: string, now: number) {
  const previous = data.transfers.find(t => t.id === key)
  if (previous) return previous.studentId === studentId && previous.semester === semester && previous.drl === drl ? { data } : { data, error: 'Yêu cầu quy đổi đã tồn tại với nội dung khác.' }
  if (!Number.isInteger(drl) || drl < 1 || drl > maxConversion(data, studentId, semester)) return { data, error: 'Không đủ điểm hoặc vượt hạn mức 5 điểm rèn luyện mỗi học kỳ.' }
  return { data: { ...data, transfers: [...data.transfers, { id: key, studentId, semester, drl, status: 'PENDING' as const }], ledger: [...data.ledger, { id: `debit-${key}`, studentId, semester, points: -drl * 10, note: 'Quy đổi · Đang chờ xác nhận', at: now }] } }
}
export function transferResultPreview(data: CommunityData, id: string, accepted: boolean, now: number) {
  const transfer = data.transfers.find(t => t.id === id)
  if (!transfer || transfer.status !== 'PENDING') return data
  return { ...data, transfers: data.transfers.map(t => t.id === id ? { ...t, status: accepted ? 'CONFIRMED' as const : 'REJECTED' as const } : t), ledger: accepted ? data.ledger : [...data.ledger, { id: `refund-${id}`, studentId: transfer.studentId, semester: transfer.semester, points: transfer.drl * 10, note: 'Hoàn điểm · Quy đổi bị từ chối', at: now }] }
}
export function certificatePreview(data: CommunityData, studentId: string, semester: string, key: string, now: number) {
  const rows = data.registrations.filter(r => r.studentId === studentId && r.status === 'COMPLETED' && data.activities.some(a => a.id === r.activityId && a.semester === semester))
  if (!rows.length) return { data, error: 'Chưa có hoạt động hoàn thành trong học kỳ này.' }
  if (data.certificates.some(c => c.id === key)) return { data }
  return { data: { ...data, certificates: [...data.certificates, { id: key, studentId, semester, registrationIds: rows.map(r => r.id), status: 'PENDING' as const, at: now }] } }
}
export function createActivityPreview(data: CommunityData, activity: Activity, admin: boolean, now: number) {
  if (!admin || !activity.semester || !activity.title.trim() || !activity.location.trim() || !Number.isFinite(activity.start) || !Number.isFinite(activity.end) || activity.start <= now || activity.end <= activity.start || !Number.isInteger(activity.capacity) || activity.capacity <= 0 || !Number.isInteger(activity.points) || activity.points <= 0) return { data, error: 'Điền đủ học kỳ, tên, địa điểm, thời gian tương lai và số chỗ / điểm là số nguyên dương.' }
  return { data: { ...data, activities: [...data.activities, activity] } }
}
