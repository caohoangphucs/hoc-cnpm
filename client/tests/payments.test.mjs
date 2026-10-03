import test from 'node:test'
import assert from 'node:assert/strict'
import { applyPreviewReply, balance, blocksNewPayment, countdown, createPreviewSession, MAX_AMOUNT, paymentState, selectionIssue } from '../src/features/payments/model.ts'
const now = Date.parse('2026-10-03T08:00:00Z')
const invoice = (id, total, paid = 0) => ({ id, code: `HD-${id}`, room: 'A101', month: '2026-10', dueDate: '2026-10-18', total, paid, discount: 0, status: paid ? 'TT_MOT_PHAN' : 'CHUA_TT', lines: [] })
const invoices = [invoice(2, 200000, 50000), invoice(1, 100000)]
const session = createPreviewSession(invoices, [2, 1], 'VNPAY', now, 'demo-request')
test('UC-08 freezes remaining balance allocations, sorts IDs and expires at exactly 15 minutes', () => {
  assert.equal(session.amount, 250000)
  assert.deepEqual(session.allocations.map(a => [a.invoiceId, a.amount]), [[1, 100000], [2, 150000]])
  assert.equal(session.expiresAt, now + 900000)
  assert.equal(countdown(session, now), '15:00')
  assert.equal(paymentState(session, now + 899999), 'PENDING')
  assert.equal(paymentState(session, now + 900000), 'EXPIRED')
  assert.equal(blocksNewPayment(session, now), true)
  assert.equal(blocksNewPayment(session, now + 900000), false)
})
test('UC-08 rejects missing/duplicate/nonpayable IDs and integer amount overflow', () => {
  for (const ids of [[], [1, 1], [999], [-1], [0], [1.5]]) assert.notEqual(selectionIssue(invoices, ids), '')
  assert.notEqual(selectionIssue([invoice(1, MAX_AMOUNT), invoice(2, MAX_AMOUNT)], [1, 2]), '')
  for (const patch of [{ status: 'HUY' }, { status: 'DA_TT', paid: 100000 }, { total: 1.5 }, { paid: 100001 }]) assert.notEqual(selectionIssue([{ ...invoices[1], ...patch }], [1]), '')
})
test('UC-08 return/pending/failed/canceled/invalid results preserve balances', () => {
  for (const reply of ['return', 'pending', 'failed', 'canceled', 'invalid', 'wrongAmount']) {
    const result = applyPreviewReply(invoices, session, reply, now + 1000)
    assert.deepEqual(result.invoices, invoices)
    assert.notEqual(result.session.status, 'SUCCESS')
  }
})
test('UC-08 only confirmed success clears balances and duplicate confirmation never increments twice', () => {
  const result = applyPreviewReply(invoices, session, 'success', now + 1000)
  assert.equal(result.session.status, 'SUCCESS')
  assert.deepEqual(result.invoices.map(balance), [0, 0])
  const replay = applyPreviewReply(result.invoices, result.session, 'success', now + 2000)
  assert.deepEqual(replay.invoices, result.invoices)
  assert.equal(replay.session.confirmedAt, now + 1000)
  assert.deepEqual(applyPreviewReply(result.invoices, result.session, 'invalid', now + 2000).session, result.session)
})
test('UC-08 late, overpaid or canceled invoice success requires review without changing debts', () => {
  for (const [data, reply, at] of [[invoices, 'success', session.expiresAt], [invoices, 'late', now], [invoices.map(i => ({ ...i, paid: i.total })), 'success', now], [invoices.map(i => ({ ...i, status: 'HUY' })), 'success', now]]) {
    const result = applyPreviewReply(data, session, reply, at)
    assert.equal(result.session.status, 'NEEDS_REVIEW')
    assert.deepEqual(result.invoices, data)
    assert.equal(blocksNewPayment(result.session, at), true)
  }
})
