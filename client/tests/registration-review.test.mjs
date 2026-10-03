import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readJson } from '../src/shared/api/protocol.ts'
import { parseBooking } from '../src/features/booking/api.ts'
test('stale booking decision returns a conflict with a trace code, never a fabricated approval', async () => {
  await assert.rejects(readJson(new Response(JSON.stringify({ code: 'VERSION_CONFLICT', requestId: 'trace-review' }), { status: 409 }), parseBooking), e => e.status === 409 && e.requestId === 'trace-review')
})
test('rejected booking retains the reason and missing-file state returned by the server', () => {
  const record = parseBooking({ id: 4, bedId: 'G1', studentId: 'S1', status: 'TU_CHOI', expiresAt: '2026-10-04T10:00:00Z', profileState: 'MISSING', version: 2, fileIds: null, reason: 'Thiếu giấy tờ' })
  assert.equal(record.status, 'TU_CHOI'); assert.equal(record.reason, 'Thiếu giấy tờ'); assert.deepEqual(record.fileIds, [])
})
