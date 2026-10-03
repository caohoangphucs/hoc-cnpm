import { test } from 'node:test'
import assert from 'node:assert/strict'
import { parseRepairs, parseCompletion, repairPayload } from '../src/features/maintenance/model.ts'
const repair = { id: 41, roomId: 'P101', studentId: 'SV01', title: 'Quạt hỏng', priority: 'CAO', status: 'DANG_XU_LY', version: 2 }
test('repair endpoint raw list is decoded without invented description or assignments', () => {
  assert.deepEqual(parseRepairs([repair]), [repair]); assert.deepEqual(parseRepairs([]), [])
  assert.throws(() => parseRepairs({ items: [repair] }))
  assert.throws(() => parseRepairs([{ ...repair, version: 0 }]))
  assert.throws(() => parseRepairs([{ ...repair, status: 'UNKNOWN' }]))
})
test('completion includes version and exact material codes; rejects duplicate or fractional consumption', () => {
  const result = repairPayload('complete', 2, 'Đã sửa', '', '', [{ supplyId: ' VT-01 ', quantity: 3 }])
  assert.deepEqual(result.body, { version: 2, note: 'Đã sửa', warrantyUntil: null, supplies: [{ supplyId: 'VT-01', quantity: 3 }] })
  for (const supplies of [[{ supplyId: 'A', quantity: 0 }], [{ supplyId: 'A', quantity: 1.5 }], [{ supplyId: 'A', quantity: 1 }, { supplyId: ' A ', quantity: 2 }]]) assert.ok(repairPayload('complete', 2, 'Sửa', '', '', supplies).errors.supplies)
})
test('shortage response preserves in-progress status and is not interpreted as completed', () => {
  const result = parseCompletion({ status: 'INSUFFICIENT_STOCK', result: { id: '41', status: 'DANG_XU_LY', version: 2, requestId: 'request' }, shortages: ['VT-01'] })
  assert.equal(result.status, 'INSUFFICIENT_STOCK'); assert.equal(result.result.version, 2)
  assert.deepEqual(result.shortages, ['VT-01'])
})
test('reschedule rejects rollover dates and sends Vietnam offset with current version', () => {
  assert.ok(repairPayload('schedule', 2, 'Đổi lịch', '2026-02-30T09:00', '', []).errors.schedule)
  assert.equal(repairPayload('schedule', 2, 'Đổi lịch', '2026-12-01T09:00', '', []).body.scheduledAt, '2026-12-01T09:00:00+07:00')
})
