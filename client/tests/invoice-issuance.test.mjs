import test from 'node:test'
import assert from 'node:assert/strict'
import { calculateInvoice, dueDate, extraIssue, MAX_MONEY } from '../src/features/invoice-issuance/model.ts'
const room = { id: 'A101', period: '2026-10', roomPrice: 500000, residents: [{ code: '02', name: 'B', discount: 0 }, { code: '01', name: 'A', discount: 50 }], electricity: { previous: 100, current: 120, rate: 3500 }, water: { previous: 10, current: 12, rate: 15000 } }
test('UC-17 discounts only each resident room share and preserves utility charges', () => {
  const bill = calculateInvoice(room, '2026-10', { amount: '10000', note: 'Thay khóa phòng' })
  assert.equal(bill.issue, '')
  assert.equal(bill.discount, 125000)
  assert.equal(bill.electricity, 70000)
  assert.equal(bill.water, 30000)
  assert.equal(bill.total, 485000)
  assert.equal(bill.shares[0].resident.code, '01')
  const remainder = calculateInvoice({ ...room, roomPrice: 500003 }, '2026-10')
  assert.deepEqual(remainder.shares.map(s => [s.roomFee, s.discount]), [[250002, 125001], [250001, 0]])
  const fractional = calculateInvoice({ ...room, residents: [{ ...room.residents[0], discount: 0 }, { ...room.residents[1], discount: 12.5 }] }, '2026-10')
  assert.equal(fractional.discount, 31250)
  assert.equal(fractional.total, 568750)
})
test('UC-17 rejects missing, negative, duplicate, stale-period and overflowing input', () => {
  for (const patch of [{ issued: true }, { residents: [] }, { residents: [room.residents[0], room.residents[0]] }, { electricity: { ...room.electricity, current: null } }, { water: { ...room.water, current: 9 } }, { electricity: { previous: 0, current: MAX_MONEY, rate: MAX_MONEY } }, { roomPrice: 0.5 }, { residents: [{ ...room.residents[0], discount: 101 }] }]) {
    assert.notEqual(calculateInvoice({ ...room, ...patch }, '2026-10').issue, '')
  }
  assert.notEqual(calculateInvoice(room, '2026-11').issue, '')
  assert.notEqual(extraIssue({ amount: '1', note: '  ' }), '')
  for (const amount of ['-1', '1.5', '1e3', '', '1000000000000000']) assert.notEqual(extraIssue({ amount, note: 'Fee' }), '')
  assert.equal(extraIssue({ amount: '0', note: '' }), '')
})
test('UC-17 due dates add 15 Vietnam calendar days across month and year boundaries', () => {
  assert.equal(dueDate(Date.parse('2026-10-03T16:59:59Z')), '2026-10-18')
  assert.equal(dueDate(Date.parse('2026-10-03T17:00:00Z')), '2026-10-19')
  assert.equal(dueDate(Date.parse('2026-12-31T18:00:00Z')), '2027-01-16')
})
