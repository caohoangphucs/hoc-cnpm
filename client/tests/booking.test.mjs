import { test } from 'node:test'
import assert from 'node:assert/strict'
import { parseRooms, parseBeds, parseRoom, parseBed, catalogPayload, maxRoomPrice } from '../src/features/rooms/model.ts'
import { parseBookings, parseBooking, validProfile, matchesBooking } from '../src/features/booking/api.ts'
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

test('booking search accepts the displayed accented code, ASCII code and raw ID', () => {
  const record = { id: 123, studentId: 'QA-STUDENT', bedId: 'QA-BED' }
  for (const query of ['ĐK-123', 'DK-123', 'dk-123', '123', ' qa-student ', 'QA-BED']) assert.equal(matchesBooking(record, query), true)
  assert.equal(matchesBooking(record, 'ĐK-999'), false)
})

test('missing admin configuration remains unavailable; explicit null is a known empty value', () => {
  const room = { id: 'QA-ROOM', buildingId: 'QA-BUILDING', roomType: 'Loại phòng', gender: 'NAM', capacity: 2, availableBeds: 1 }
  assert.equal(parseRoom(room).configuration, undefined)
  assert.equal(parseRoom({ ...room, number: '101', floor: 1, basePrice: 500000, status: 'CON_CHO' }).configuration, undefined)
  assert.deepEqual(parseRoom({ ...room, number: '101', floor: 1, basePrice: 500000, status: 'CON_CHO', roomTypeId: null }).configuration,
    { number: '101', floor: 1, basePrice: 500000, status: 'CON_CHO', roomTypeId: null })
  const bed = { id: 'QA-BED', number: '1', status: 'TRONG', version: 1 }
  assert.equal(Object.hasOwn(parseBed(bed), 'position'), false)
  assert.equal(Object.hasOwn(parseBed({ ...bed, position: null }), 'position'), true)
  assert.equal(parseBed({ ...bed, position: null }).position, null)
})

test('catalogue writes respect schema boundaries without truncating IDs, prices or positions', () => {
  const fields = { id: 'A'.repeat(20), number: '1', building: 'QA', type: '', floor: '1', capacity: '2', price: String(maxRoomPrice), position: 'A'.repeat(50), status: 'CON_CHO' }
  assert.equal(catalogPayload('room', fields, '').basePrice, maxRoomPrice)
  assert.equal(catalogPayload('room', fields, '').roomTypeId, null)
  for (const invalid of [{ id: 'A'.repeat(21) }, { price: String(maxRoomPrice + 1) }, { floor: '2147483648' }, { capacity: '0' }, { id: ' ' }, { status: 'UNKNOWN' }]) assert.throws(() => catalogPayload('room', { ...fields, ...invalid }, ''))
  assert.equal(catalogPayload('bed', { ...fields, status: 'TRONG' }, 'QA').position.length, 50)
  assert.throws(() => catalogPayload('bed', { ...fields, status: 'TRONG', position: 'A'.repeat(51) }, 'QA'))
})
