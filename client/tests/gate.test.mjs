import { test } from 'node:test'
import assert from 'node:assert/strict'
import { parseEntry, entryPayload, visitorPayload } from '../src/features/gate/model.ts'
test('server timestamp, violation and pending barrier are authoritative', () => {
  const result = parseEntry({ eventId: 'uuid', studentId: 'SV01', type: 'VAO', time: '2026-10-03T23:30:00+07:00', barrierStatus: 'PENDING', violationId: 17 })
  assert.equal(result.barrierStatus, 'PENDING'); assert.equal(result.violationId, 17)
  assert.equal(result.time, '2026-10-03T23:30:00+07:00')
  assert.throws(() => parseEntry({ ...result, barrierStatus: 'FAKE_OPEN' }))
})
test('entry command sends card token and device, never a client-derived student or event time', () => {
  const body = entryPayload('event', ' Card-01 ', 'RA', ' gate-01 ', '')
  assert.deepEqual(body, { eventId: 'event', cardToken: 'Card-01', type: 'RA', deviceId: 'gate-01', note: null })
  assert.throws(() => entryPayload('event', '', 'VAO', 'gate', ''))
})
test('visitor declaration rejects invalid intervals and preserves document value', () => {
  const body = visitorPayload(' SV01 ', 'Khách', '000123456', '2026-12-01T09:00', '2026-12-01T11:00')
  assert.equal(body.identity, '000123456'); assert.equal(body.validFrom, '2026-12-01T09:00:00+07:00')
  assert.throws(() => visitorPayload('SV01', 'Khách', 'ID', '2026-12-01T11:00', '2026-12-01T09:00'))
})
