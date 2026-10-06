import { test } from 'node:test'
import assert from 'node:assert/strict'
import { parsePayment, safePaymentUrl, payableInvoice, paymentSelection } from '../src/features/payments/api.ts'
import { readJson } from '../src/shared/api/protocol.ts'
const payment = { id: 'session-uuid', gateway: 'VNPAY', providerRef: 'ref', amount: 500000, invoiceIds: [1,2], status: 'PENDING', payUrl: null, expiresAt: '2026-10-03T10:00:00Z' }
test('pending, expired and review sessions never become paid on the client', () => {
  for (const status of ['PENDING', 'EXPIRED', 'NEEDS_REVIEW', 'SUCCESS']) assert.equal(parsePayment({ ...payment, status }).status, status)
  assert.equal(parsePayment(payment).payUrl, null)
})
test('payment links require HTTPS and reject script, relative or credential-bearing URLs', () => {
  assert.equal(safePaymentUrl('https://sandbox.vnpayment.vn/paymentv2/vpcpay.html'), 'https://sandbox.vnpayment.vn/paymentv2/vpcpay.html')
  for (const url of ['javascript:alert(1)', '//evil.test', 'http://example.test', 'https://user:pass@example.test']) assert.throws(() => safePaymentUrl(url))
})
test('unconfigured gateway returns unavailable instead of a simulated successful transaction', async () => {
  await assert.rejects(readJson(new Response(JSON.stringify({ code: 'PAYMENT_GATEWAY_UNAVAILABLE' }), { status: 503 }), parsePayment), e => e.status === 503 && e.code === 'PAYMENT_GATEWAY_UNAVAILABLE')
})

test('merged payment preserves invoice allocations, excludes settled invoices and respects limits', () => {
  const a = { id: 1, balance: 475000, status: 'CHUA_TT' }, b = { id: 2, balance: 125000, status: 'TT_MOT_PHAN' }
  assert.deepEqual(paymentSelection([a, b]), { invoiceIds: [1, 2], total: 600000 })
  assert.equal(payableInvoice({ ...a, status: 'QUA_HAN' }), true)
  for (const invoices of [[], [a, a], [{ ...a, status: 'HUY' }], [{ ...a, balance: 0 }], [{ ...a, balance: 10000000000 }], [{ ...a, id: 2147483648 }], Array.from({ length: 101 }, (_, id) => ({ ...a, id: id + 1 }))]) assert.throws(() => paymentSelection(invoices))
  assert.equal(paymentSelection([{ ...a, balance: 9999999999 }]).total, 9999999999)
  assert.throws(() => paymentSelection([{ ...a, balance: 9999999999 }, b]))
})
