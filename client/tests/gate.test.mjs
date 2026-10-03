import test from 'node:test'
import assert from 'node:assert/strict'
import { lateReturn, recordPreviewEntry, recordPreviewVisitor, updatePreviewBarrier } from '../src/features/gate/model.ts'
const at = value => Date.parse(`2026-10-03T${value}+07:00`)
const now = at('23:01:00'), input = { eventId: 'event-1', studentId: 'SV-1', type: 'VAO', note: '' }
const data = { students: [{ id: 'SV-1', name: 'SV', room: 'A101', card: 'CARD', active: true, resident: true, validFrom: now - 1000, validTo: now + 1000 }], visitors: [{ id: 'guest-1', approved: true, from: now - 1000, to: now + 1000, inside: false, name: 'Khách', host: 'SV-1' }], entries: [], visitorEntries: [] }
test('UC-25 Vietnam-time boundaries: 23:00 and 05:00 are allowed; late returns only apply to entry', () => {
  for (const time of ['05:00:00', '23:00:00', '12:00:00']) assert.equal(lateReturn('VAO', at(time)), false)
  for (const time of ['23:00:01', '00:00:00', '04:59:59']) assert.equal(lateReturn('VAO', at(time)), true)
  assert.equal(lateReturn('RA', at('23:30:00')), false)
})
test('UC-25 repeated event preserves original time and creates no extra entry/violation; changed payload is rejected', () => {
  const first = recordPreviewEntry(data, input, now)
  assert.equal(first.entry.late, true)
  const repeat = recordPreviewEntry(first.data, input, now + 5000)
  assert.equal(repeat.data.entries.length, 1)
  assert.equal(repeat.entry.at, now)
  assert.ok(recordPreviewEntry(first.data, { ...input, type: 'RA' }, now).error)
  assert.equal(recordPreviewEntry(first.data, { ...input, eventId: 'event-2' }, now).data.entries.length, 2)
})
test('UC-25 expired, locked and non-resident cards do not create a log or open gate', () => {
  for (const patch of [{ validTo: now }, { active: false }, { resident: false }, { validFrom: now + 1 }]) {
    const denied = { ...data, students: [{ ...data.students[0], ...patch }] }
    const result = recordPreviewEntry(denied, input, now)
    assert.ok(result.error); assert.deepEqual(result.data, denied)
  }
})
test('UC-25 device failure preserves log; device retry does not create another entry', () => {
  const first = recordPreviewEntry(data, input, now)
  assert.equal(first.entry.barrier, 'PENDING')
  const failed = updatePreviewBarrier(first.data, input.eventId, 'FAILED')
  assert.equal(failed.entries.length, 1)
  const opened = updatePreviewBarrier(failed, input.eventId, 'OPENED')
  assert.equal(opened.entries.length, 1); assert.equal(opened.entries[0].barrier, 'OPENED')
})
test('UC-25 guests require approved valid declaration and document check, cannot enter twice', () => {
  assert.ok(recordPreviewVisitor(data, 'guest-1', 'VAO', false, 'g1', now).error)
  assert.ok(recordPreviewVisitor({ ...data, visitors: [{ ...data.visitors[0], approved: false }] }, 'guest-1', 'VAO', true, 'g1', now).error)
  assert.ok(recordPreviewVisitor(data, 'guest-1', 'VAO', true, 'g1', now + 1000).error)
  const entered = recordPreviewVisitor(data, 'guest-1', 'VAO', true, 'g1', now)
  assert.equal(entered.data.visitorEntries.length, 1)
  assert.equal(recordPreviewVisitor(entered.data, 'guest-1', 'VAO', true, 'g1', now).data.visitorEntries.length, 1)
  assert.ok(recordPreviewVisitor(entered.data, 'guest-1', 'VAO', true, 'g2', now).error)
  assert.equal(recordPreviewVisitor(entered.data, 'guest-1', 'RA', true, 'g2', now).data.visitors[0].inside, false)
})
