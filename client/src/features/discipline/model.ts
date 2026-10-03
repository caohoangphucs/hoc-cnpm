export type Semester = { id: string; name: string; from: number; to: number }
export type Revision = { id: string; at: number; decision: string; reason: string; evidence: string; reviewer: string }
export type Appeal = { id: string; at: number; content: string; evidence?: string; version: number; status: 'OPEN' | 'ACCEPTED' | 'REJECTED'; response?: string }
export type Violation = { id: string; studentId: string; name: string; room: string; at: number; source: string; detail: string; version: number; revisions: Revision[]; appeals: Appeal[] }
export type DisciplineData = { semesters: Semester[]; violations: Violation[] }
export function inSemester(v: Violation, semester: Semester) { return v.at >= semester.from && v.at < semester.to }
export function confirmedCount(data: DisciplineData, studentId: string, semester: Semester) { return data.violations.filter(v => v.studentId === studentId && inSemester(v, semester) && v.revisions.length > 0).length }
export function evidenceIssue(file: { name: string; size: number; type: string }) {
  return !file.size || file.size > 5 * 1024 * 1024 || !['image/jpeg', 'image/png', 'application/pdf'].includes(file.type) ? 'Chọn minh chứng JPG, PNG hoặc PDF, dung lượng từ 1 byte đến 5 MB.' : ''
}
export function decidePreview(data: DisciplineData, ids: string[], versions: Record<string, number>, decision: string, reason: string, evidence: string, now: number) {
  const rows = ids.map(id => data.violations.find(v => v.id === id))
  const error = !ids.length || ids.length > 100 || new Set(ids).size !== ids.length ? 'Chọn từ 1 đến 100 vi phạm khác nhau.' : rows.some(v => !v || v.revisions.length || v.version !== versions[v.id]) ? 'Có vi phạm đã được xử lý hoặc thay đổi. Chưa lưu quyết định nào; hãy kiểm tra lại.' : !decision.trim() || decision.length > 1000 || !reason.trim() || reason.length > 1000 ? 'Nhập hình thức xử lý và lý do, tối đa 1.000 ký tự mỗi nội dung.' : !evidence ? 'Cần minh chứng trước khi ra quyết định.' : ''
  if (error) return { data, error }
  return { data: { ...data, violations: data.violations.map(v => ids.includes(v.id) ? { ...v, version: v.version + 1, revisions: [...v.revisions, { id: `${v.id}-R1`, at: now, decision: decision.trim(), reason: reason.trim(), evidence, reviewer: 'Ban quản lý' }] } : v) } }
}
export function appealPreview(data: DisciplineData, id: string, studentId: string, content: string, now: number, evidence = '') {
  const row = data.violations.find(v => v.id === id)
  const error = !row || row.studentId !== studentId || !row.revisions.length ? 'Không thể khiếu nại quyết định này.' : row.appeals.some(a => a.status === 'OPEN') ? 'Đã có khiếu nại đang chờ xét lại.' : !content.trim() || content.length > 1000 ? 'Nhập nội dung khiếu nại từ 1 đến 1.000 ký tự.' : ''
  if (error) return { data, error }
  return { data: { ...data, violations: data.violations.map(v => v.id === id ? { ...v, appeals: [...v.appeals, { id: `${id}-A${v.appeals.length + 1}`, at: now, content: content.trim(), evidence, status: 'OPEN' as const, version: 0 }] } : v) } }
}
export function respondPreview(data: DisciplineData, id: string, appealId: string, version: number, accepted: boolean, response: string, decision: string, now: number) {
  const row = data.violations.find(v => v.id === id), appeal = row?.appeals.find(a => a.id === appealId)
  const error = !appeal || appeal.status !== 'OPEN' || appeal.version !== version ? 'Khiếu nại đã được phản hồi hoặc thay đổi; chưa lưu phản hồi.' : !response.trim() || response.length > 1000 || accepted && (!decision.trim() || decision.length > 1000) ? 'Nhập lý do phản hồi và quyết định xét lại khi chấp nhận, tối đa 1.000 ký tự.' : ''
  if (error) return { data, error }
  return { data: { ...data, violations: data.violations.map(v => v.id === id ? { ...v, version: accepted ? v.version + 1 : v.version, appeals: v.appeals.map(a => a.id === appealId ? { ...a, status: accepted ? 'ACCEPTED' as const : 'REJECTED' as const, response: response.trim(), version: a.version + 1 } : a), revisions: accepted ? [...v.revisions, { id: `${id}-R${v.revisions.length + 1}`, at: now, decision: decision.trim(), reason: response.trim(), evidence: v.revisions.at(-1)!.evidence, reviewer: 'Ban quản lý · Xét lại' }] : v.revisions } : v) } }
}
