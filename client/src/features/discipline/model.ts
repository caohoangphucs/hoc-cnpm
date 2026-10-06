import { instant, integer, list, object, text } from '../../shared/api/protocol.ts'
export type Decision = { id: number; revision: number; decision: string; reason: string; actorId: number; createdAt: string; evidenceFileId: string | null }
export type Violation = { id: number; studentId: string; semesterId: number; content: string; level: string; status: string; semesterCount: number; decisions: Decision[]; version: number }
export function parseViolation(value: unknown): Violation {
  const v = object(value)
  return { id: integer(v.id, 1), studentId: text(v.studentId), semesterId: integer(v.semesterId, 1), content: text(v.content), level: text(v.level), status: text(v.status), semesterCount: integer(v.semesterCount), version: integer(v.version, 1), decisions: list(v.decisions, item => { const d = object(item); return { id: integer(d.id, 1), revision: integer(d.revision, 1), decision: text(d.decision), reason: text(d.reason), actorId: integer(d.actorId, 1), createdAt: instant(d.createdAt), evidenceFileId: d.evidenceFileId == null ? null : text(d.evidenceFileId) } }) }
}
export const parseViolations = (v: unknown) => list(v, parseViolation)

/** Gửi version hiện tại để tránh ghi đè hồ sơ admin khác vừa xử lý. */
export function decisionPayload(violation: Violation, decision: string, reason: string, evidenceFileId: string) {
  decision = decision.trim(); reason = reason.trim()
  if (!decision || decision.length > 1000) throw new Error('Nhập quyết định, tối đa 1000 ký tự.')
  if (!reason || reason.length > 1000) throw new Error('Nhập lý do, tối đa 1000 ký tự.')
  if (!evidenceFileId) throw new Error('Tải ảnh minh chứng trước khi ra quyết định.')
  if (violation.status !== 'CHUA_XU_LY') throw new Error('Hồ sơ đã xử lý. Tải lại để xem quyết định.')
  return { violationIds: [violation.id], decision, reason, evidenceFileId, versions: { [violation.id]: violation.version } }
}
