import test from 'node:test'
import assert from 'node:assert/strict'
import { applyPreviewRepair, draftErrors, emptyDraft, vietnamInstant } from '../src/features/maintenance/model.ts'

const now = Date.parse('2026-10-03T10:00:00+07:00'), actor = { id: 501, name: 'Kỹ thuật minh họa' }
const repair = { id: 1, code: 'SC-DEMO-1', room: 'A101', title: 'Rò nước', description: 'Rò hai vòi', student: 'SV', category: 'Nước', priority: 'CAO', status: 'DANG_XU_LY', version: 2, assignedTo: 501, createdAt: new Date(now).toISOString(), usedSupplies: [], history: [] }
const data = { repairs: [repair], supplies: [{ id: 'a', name: 'Vòi', stock: 1, unit: 'cái' }, { id: 'b', name: 'Ống', stock: 4, unit: 'sợi' }], purchases: [] }
const draft = { ...emptyDraft(), note: 'Đã thay vòi và kiểm tra đường nước.', supplies: [{ supplyId: 'a', quantity: 1 }, { supplyId: 'b', quantity: 2 }] }
const run = (input = data, values = draft, mode = 'complete', version = 2) => applyPreviewRepair(input, 1, version, mode, values, actor, 'same-key', now)

test('UC-22 ownership, terminal status and stale version never change repair or stock', () => {
  for (const patch of [{ assignedTo: 502 }, { status: 'HOAN_THANH' }, { status: 'HUY' }, { version: 3 }]) {
    const input = { ...data, repairs: [{ ...repair, ...patch }] }, result = run(input)
    assert.ok(result.error)
    assert.deepEqual(result.data, input)
  }
})
test('UC-22 rejects duplicate supplies, zero/fractional/oversized quantity and invalid notes', () => {
  for (const supplies of [[{ supplyId: 'a', quantity: 0 }], [{ supplyId: 'a', quantity: 1.5 }], [{ supplyId: 'a', quantity: 2147483648 }], [{ supplyId: 'missing', quantity: 1 }], [{ supplyId: 'a', quantity: 1 }, { supplyId: 'a', quantity: 1 }]]) {
    assert.ok(run(data, { ...draft, supplies }).error)
  }
  assert.ok(run(data, { ...draft, note: '  ' }).error)
})
test('UC-22 stock shortage creates purchase once, preserves ALL stock and keeps repair in progress', () => {
  const shortageDraft = { ...draft, supplies: [{ supplyId: 'a', quantity: 3 }, { supplyId: 'b', quantity: 2 }] }
  const result = run(data, shortageDraft)
  assert.equal(result.shortage[0].missing, 2)
  assert.deepEqual(result.data.repairs, data.repairs)
  assert.deepEqual(result.data.supplies, data.supplies)
  assert.equal(result.data.purchases[0].quantity, 2)
  assert.equal(run(result.data, shortageDraft).data.purchases.length, 1)
})
test('UC-22 completes atomically once, supports no supplies, and duplicate completion cannot deduct stock twice', () => {
  const result = run()
  assert.equal(result.data.repairs[0].status, 'HOAN_THANH')
  assert.equal(result.data.repairs[0].version, 3)
  assert.deepEqual(result.data.supplies.map(s => s.stock), [0, 2])
  assert.deepEqual(run(result.data).data, result.data)
  assert.equal(run(data, { ...draft, supplies: [] }).data.repairs[0].status, 'HOAN_THANH')
  assert.deepEqual(run(data, { ...draft, supplies: [] }).data.supplies, data.supplies)
})
test('UC-22 accepts only new requests and records future appointment in Vietnam timezone', () => {
  const fresh = { ...data, repairs: [{ ...repair, status: 'MOI', assignedTo: undefined, version: 1 }] }
  const result = run(fresh, { ...emptyDraft(), schedule: '2026-10-03T12:00' }, 'accept', 1)
  assert.equal(result.data.repairs[0].assignedTo, actor.id)
  assert.equal(result.data.repairs[0].schedule, '2026-10-03T12:00:00+07:00')
  assert.equal(vietnamInstant('2026-10-03T10:00'), now)
  assert.ok(Number.isNaN(vietnamInstant('2026-02-30T10:00')))
  assert.ok(run(fresh, { ...emptyDraft(), schedule: '2026-10-03T09:00' }, 'accept', 1).error)
})
test('UC-22 external warranty keeps processing state until handover; cancellation requires reason and never exports', () => {
  const warranty = run(data, { ...emptyDraft(), schedule: '2026-10-05T12:00', note: 'Gửi bảo hành motor.', provider: 'Trung tâm A' }, 'warranty')
  assert.equal(warranty.data.repairs[0].status, 'DANG_XU_LY')
  assert.ok(draftErrors(warranty.data.repairs[0], 'complete', draft, data.supplies, now).received)
  const received = run(warranty.data, { ...draft, received: true }, 'complete', 3)
  assert.equal(received.data.repairs[0].status, 'HOAN_THANH')
  const canceled = run(data, { ...emptyDraft(), note: 'Sinh viên đã xử lý được, yêu cầu hủy.' }, 'cancel')
  assert.equal(canceled.data.repairs[0].status, 'HUY')
  assert.deepEqual(canceled.data.supplies, data.supplies)
})
