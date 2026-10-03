import { test } from 'node:test'
import assert from 'node:assert/strict'
import { parseRooms, parseBeds } from '../src/features/rooms/model.ts'
import { parseBookings, parseBooking, validProfile } from '../src/features/booking/api.ts'
const booking = { id: 1, bedId: 'G-A101-1', studentId: '21110001', status: 'CHO_DUYET', expiresAt: '2026-10-05T10:00:00+07:00', profileState: 'COMPLETE', version: 3, fileIds: ['owned-file-id'], reason: null }
test('catalogue preserves public IDs, gender and server availability with page totals', () => {
  const page = parseRooms({ items: [{ id: 'A-101', buildingId: 'TOA-A', roomType: '4 người', gender: 'NAM', capacity: 4, availableBeds: 2 }], page: 2, pageSize: 8, total: 20 })
  assert.equal(page.total, 20); assert.equal(page.page, 2); assert.equal(page.items[0].availableBeds, 2)
  const beds = parseBeds({ items: [{ id: 'G-A101-1', number: 'G-01', status: 'GIU_CHO', version: 4 }], page: 1, pageSize: 8, total: 1 })
  assert.equal(beds.items[0].status, 'GIU_CHO'); assert.equal(beds.items[0].version, 4)
})
test('bookings use server profile state, files and optimistic version without synthesized student details', () => {
  const record = parseBooking(booking)
  assert.equal(record.version, 3); assert.deepEqual(record.fileIds, ['owned-file-id']); assert.equal(record.reason, null)
  const page = parseBookings({ items: [booking], page: 1, pageSize: 8, total: 1 })
  assert.equal(page.items[0].id, 1)
  assert.throws(() => parseBooking({ ...booking, version: undefined }))
})
test('malformed paginated responses are rejected rather than presented as empty data', () => {
  assert.throws(() => parseRooms([]))
  assert.throws(() => parseBookings({ items: [booking], page: 1, pageSize: 8, total: 0 }))
})


test('approval follows VALID from the actual service; COMPLETE alone is insufficient', () => {
  assert.equal(validProfile({ profileState: 'VALID' }), true)
  for (const profileState of ['PENDING','MISSING','INVALID','COMPLETE']) assert.equal(validProfile({ profileState }), false)
})
