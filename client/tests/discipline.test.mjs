import { test } from 'node:test'
import assert from 'node:assert/strict'
import { decisionPayload, parseViolations } from '../src/features/discipline/model.ts'
const violation = { id: 22, studentId: 'SV01', semesterId: 8, content: 'Về trễ', level: 'NHE', status: 'CHO_XU_LY', semesterCount: 3, decisions: [{ id: 14, revision: 2, decision: 'Nhắc nhở', reason: 'Đã xác minh', actorId: 4, createdAt: '2026-10-03T08:00:00+07:00' }], version: 4 }
test('violation count comes from semesterCount even if the current page contains only one record', () => {
  const [row] = parseViolations([violation]); assert.equal(row.semesterCount, 3); assert.equal(row.decisions[0].revision, 2)
})
test('reject malformed versions or missing decisions; empty queue is valid', () => {
  assert.deepEqual(parseViolations([]), [])
  assert.throws(() => parseViolations([{ ...violation, version: null }]))
  assert.throws(() => parseViolations([{ ...violation, decisions: null }]))
  assert.throws(() => parseViolations([{ ...violation, semesterCount: -1 }]))
})

test('decision requires evidence and current version; attached evidence survives parsing', () => {
  const [v] = parseViolations([{ ...violation, status: 'CHUA_XU_LY', decisions: [{ ...violation.decisions[0], evidenceFileId: 'file-123' }] }])
  assert.equal(v.decisions[0].evidenceFileId, 'file-123')
  const payload = decisionPayload(v, ' Nhắc nhở ', ' Đã xác minh ', 'file-123')
  assert.deepEqual(payload.versions, { 22: 4 })
  assert.equal(payload.decision, 'Nhắc nhở')
  assert.throws(() => decisionPayload(v, 'Nhắc nhở', 'Căn cứ', ''))
  assert.throws(() => decisionPayload(v, 'x'.repeat(1001), 'Căn cứ', 'file-123'))
  assert.throws(() => decisionPayload({ ...v, status: 'DA_XU_LY' }, 'Nhắc nhở', 'Căn cứ', 'file-123'))
})
