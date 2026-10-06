import { test } from 'node:test'
import assert from 'node:assert/strict'
import { parseEntry, parseEntries, parseVisitor, entryPayload, manualEntryPayload, visitorPayload } from '../src/features/gate/model.ts'
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
test('visitor list and detail preserve approval state and version without inventing decrypted identity', () => {
  const visitor = parseVisitor({ id: 3, hostStudentId: '21110001', state: 'APPROVED', validFrom: '2026-10-03T08:00:00+07:00', validTo: '2026-10-03T12:00:00+07:00', version: 2 })
  assert.equal(visitor.state, 'APPROVED'); assert.equal(visitor.version, 2)
  assert.equal(Object.hasOwn(visitor, 'name'), false); assert.equal(Object.hasOwn(visitor, 'identity'), false)
  assert.throws(() => parseVisitor({ ...visitor, version: 0 }))
})
test('entry history uses paginated server results and keeps audit event IDs', () => {
  const page = parseEntries({ items: [{ eventId: 'audit-uuid', studentId: '21110001', type: 'RA', time: '2026-10-03T08:00:00Z', barrierStatus: 'OPENED', violationId: null }], page: 1, pageSize: 8, total: 18 })
  assert.equal(page.total, 18); assert.equal(page.items[0].eventId, 'audit-uuid')
})

test('visitor name and document type validation support Vietnamese and reject numeric names', () => {
  const args = ['21110001', '  Nguyễn   Văn An ', '012345678901', '2030-01-01T09:00', '2030-01-01T10:00']
  const valid = visitorPayload(...args, 'CCCD')
  assert.equal(valid.name, 'Nguyễn Văn An')
  assert.throws(() => visitorPayload(args[0], '12345', ...args.slice(2), 'CCCD'))
  assert.throws(() => visitorPayload(args[0], 'x', ...args.slice(2), 'CCCD'))
  assert.throws(() => visitorPayload(args[0], args[1], '123', ...args.slice(3), 'CCCD'))
  assert.equal(visitorPayload(args[0], args[1], 'A1234567', ...args.slice(3), 'PASSPORT').identityType, 'PASSPORT')
})

test('manual gate flow requires physical verification and keeps MSSV separate from card tokens', () => {
  const body = manualEntryPayload('event', ' SV01 ', 'RA', 'gate', '', true)
  assert.equal(body.studentId, 'SV01')
  assert.equal(Object.hasOwn(body, 'cardToken'), false)
  assert.throws(() => manualEntryPayload('event', 'SV01', 'RA', 'gate', '', false))
  assert.equal(entryPayload('event', 'card', 'RA', 'gate', 'A'.repeat(255)).note.length, 255)
  assert.throws(() => entryPayload('event', 'card', 'RA', 'gate', 'A'.repeat(256)))
})
