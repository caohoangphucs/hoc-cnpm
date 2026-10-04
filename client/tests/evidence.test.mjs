import test from 'node:test'
import assert from 'node:assert/strict'
import { uploadProblem, parseEvidence, parseWindows } from '../src/features/booking/evidence.ts'

test('image upload accepts the inclusive size boundary and rejects empty, oversized and unsupported files', () => {
  assert.equal(uploadProblem({ type: 'image/png', size: 1 }), null)
  assert.equal(uploadProblem({ type: 'image/jpeg', size: 5 * 1024 * 1024 }), null)
  assert.ok(uploadProblem({ type: 'image/png', size: 5 * 1024 * 1024 + 1 }))
  assert.ok(uploadProblem({ type: 'image/png', size: 0 }))
  assert.ok(uploadProblem({ type: 'application/pdf', size: 100 }))
})
test('evidence and registration windows must satisfy the actual API response contract', () => {
  assert.equal(parseEvidence({ id: 'opaque-id', kind: 'CCCD_FRONT', mime: 'image/png', sizeBytes: 68, sha256: 'hash' }).kind, 'CCCD_FRONT')
  assert.throws(() => parseEvidence({ id: 'id', sizeBytes: -1 }))
  assert.equal(parseWindows([{ id: 900001, name: 'Đợt demo', semesterName: 'Học kỳ 1' }])[0].id, 900001)
  assert.throws(() => parseWindows([{ id: 0, name: 'Demo', semesterName: 'Demo' }]))
})
