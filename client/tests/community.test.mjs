import { test } from 'node:test'
import assert from 'node:assert/strict'
import { parseActivities, parseRegistration, cancellationRequest, parsePoints, parseConversion, activityPayload, conversionPayload } from '../src/features/community/model.ts'
const activity = { id: 71, semesterId: 8, title: 'Trồng cây', startAt: '2026-12-01T08:00:00+07:00', endAt: '2026-12-01T10:00:00+07:00', capacity: 20, remaining: 19, points: 10, status: 'MO_DANG_KY', version: 1 }
test('activities use public numeric IDs and server capacity; inconsistent records are rejected', () => {
  assert.deepEqual(parseActivities([activity]), [activity])
  assert.throws(() => parseActivities([{ ...activity, remaining: 21 }]))
  assert.throws(() => parseActivities([{ ...activity, id: 'ACT01' }]))
})
test('cancel endpoint uses registration ID, not public or internal activity ID', () => {
  const receipt = parseRegistration({ id: 203, activityId: 'ACT_INTERNAL', status: 'REGISTERED', qrToken: 'signed-token', version: 2 })
  assert.deepEqual(cancellationRequest(receipt), { path: 'activities/203/registrations', method: 'DELETE', body: { version: 2 } })
  assert.throws(() => parseRegistration({ ...receipt, qrToken: null }))
})
test('points ledger supports debits; conversion respects authoritative pending-inclusive allowance', () => {
  const points = parsePoints({ semesterId: 8, earned: 80, available: 40, convertedDrl: 4, ledger: [{ id: 3, delta: -40, kind: 'CONVERSION', time: '2026-10-03T10:00:00+07:00' }] })
  assert.equal(points.ledger[0].delta, -40)
  assert.deepEqual(conversionPayload(8, '1', points), { semesterId: 8, drl: 1 })
  assert.throws(() => conversionPayload(8, '2', points))
  assert.throws(() => conversionPayload(8, '1', { available: 9, convertedDrl: 0 }))
})
test('accepted conversion is pending, never assumed externally confirmed', () => {
  const receipt = parseConversion({ id: 'request', status: 'PENDING', pointsSpent: 10, drl: 1, externalState: 'PENDING' })
  assert.equal(receipt.externalState, 'PENDING')
  assert.throws(() => parseConversion({ ...receipt, externalState: 'CONFIRMED' }))
})
test('create payload contains implemented fields only and validates integers/date order', () => {
  assert.deepEqual(activityPayload('8', ' Trồng cây ', '2026-12-01T08:00', '2026-12-01T10:00', '20', '10'), { semesterId: 8, title: 'Trồng cây', startAt: activity.startAt, endAt: activity.endAt, capacity: 20, points: 10 })
  assert.throws(() => activityPayload('8', 'A', '2026-12-01T08:00', '2026-12-01T10:00', '2.5', '10'))
})
