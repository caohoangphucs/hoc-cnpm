import { test } from 'node:test'
import assert from 'node:assert/strict'
import { parseInvoice, parseDraft, parseIssue, draftRoomWarnings, meterPayload, reconcileIssuance } from '../src/features/invoice-issuance/api.ts'
const invoice = { id: 3, roomId: 'A101', month: '2026-10', lines: [{ name: 'Điện', quantity: 1.5, unitPrice: 3000, total: 4500 }], discount: 0, total: 4500, paid: 1000, balance: 3500, dueDate: '2026-10-15', status: 'TT_MOT_PHAN' }
test('invoice preserves authoritative decimal quantities and balances', () => {
  const r = parseInvoice(invoice)
  assert.equal(r.lines[0].quantity, 1.5); assert.equal(r.balance, 3500)
  assert.throws(() => parseInvoice({ ...invoice, balance: -1 }))
  assert.throws(() => parseInvoice({ ...invoice, dueDate: '2026-02-30' }))
})
test('draft warnings, input hash and version remain linked to the server snapshot', () => {
  const r = parseDraft({ id: 'draft-uuid', version: 2, month: '2026-10', inputHash: 'hash', rooms: [{ roomId: 'A101', oldElectricity: 1, newElectricity: 2, oldWater: 1, newWater: 2, electricityPrice: 3000, waterPrice: 1000, roomPrice: 100000, discount: 0, extras: 0, total: 104000, warnings: ['READING_MISSING'] }] })
  assert.equal(r.version, 2); assert.deepEqual(r.rooms[0].warnings, ['READING_MISSING']); assert.equal(r.inputHash, 'hash')
})
test('issuance displays the actual per-room results, including unsuccessful rooms', () => {
  assert.deepEqual(parseIssue([{ roomId: 'A101', status: 'SUCCESS', invoiceId: '3', message: 'Invoice issued' }, { roomId: 'B101', status: 'FAILED', invoiceId: null, message: 'failed' }]).map(r => r.invoiceId), ['3', null])
})

test('meter limits apply to consumption, not the cumulative readings, and block unsafe drafts', () => {
  const values = { oldElectricity: '2047483648', newElectricity: '2147483647', oldWater: '0', newWater: '0' }
  assert.equal(meterPayload(values, 'rev').newElectricity, 2147483647)
  assert.throws(() => meterPayload({ ...values, oldElectricity: '2047483647' }, 'rev'))
  assert.throws(() => meterPayload({ ...values, newElectricity: '2147483648' }, 'rev'))
  assert.throws(() => meterPayload({ ...values, newWater: '-1' }, 'rev'))
  assert.throws(() => meterPayload({ ...values, oldWater: '1', newWater: '0' }, 'rev'))
  assert.deepEqual(draftRoomWarnings({ oldElectricity: 0, newElectricity: 100000000, oldWater: 0, newWater: 0, warnings: [] }), ['QUANTITY_OUT_OF_RANGE'])
  assert.deepEqual(draftRoomWarnings({ oldElectricity: 0, newElectricity: 1, oldWater: 0, newWater: 0, warnings: ['MISSING_PRICE'] }), ['MISSING_PRICE'])
})

test('reconciliation after partial failure uses current room eligibility without inventing invoice IDs', () => {
  assert.deepEqual(reconcileIssuance(['QA-A', 'QA-B', 'QA-C'], [{ id: 'QA-B' }, { id: 'QA-C' }]), [
    { roomId: 'QA-A', status: 'CHECK_INVOICES', invoiceId: null },
    { roomId: 'QA-B', status: 'NOT_ISSUED', invoiceId: null },
    { roomId: 'QA-C', status: 'NOT_ISSUED', invoiceId: null },
  ])
})
