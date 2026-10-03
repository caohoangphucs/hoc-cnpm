import { instant, integer, list, object, text } from '../../shared/api/protocol.ts'
export type Decision = { id: number; revision: number; decision: string; reason: string; actorId: number; createdAt: string }
export type Violation = { id: number; studentId: string; semesterId: number; content: string; level: string; status: string; semesterCount: number; decisions: Decision[]; version: number }
export function parseViolation(value: unknown): Violation {
  const v = object(value)
  return { id: integer(v.id, 1), studentId: text(v.studentId), semesterId: integer(v.semesterId, 1), content: text(v.content), level: text(v.level), status: text(v.status), semesterCount: integer(v.semesterCount), version: integer(v.version, 1), decisions: list(v.decisions, item => { const d = object(item); return { id: integer(d.id, 1), revision: integer(d.revision, 1), decision: text(d.decision), reason: text(d.reason), actorId: integer(d.actorId, 1), createdAt: instant(d.createdAt) } }) }
}
export const parseViolations = (v: unknown) => list(v, parseViolation)
