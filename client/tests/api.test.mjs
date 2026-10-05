import { test } from 'node:test'
import assert from 'node:assert/strict'
import { ApiError, readJson, requestIdentity, vietnamTime, command } from '../src/shared/api/protocol.ts'
test('failed HTTP command does not masquerade as success or expose internal error details', async () => {
  const response = new Response(JSON.stringify({ code: 'VERSION_CONFLICT', message: 'SQL secret', requestId: 'trace-123' }), { status: 409 })
  await assert.rejects(readJson(response, command), e => e instanceof ApiError && e.status === 409 && e.requestId === 'trace-123' && !e.message.includes('SQL'))
})
test('invalid enrollment OTP is distinguished from an expired login session', async () => {
  await assert.rejects(readJson(new Response(JSON.stringify({ code: 'INVALID_OTP', message: 'internal details' }), { status: 401 }), command), e => e.code === 'INVALID_OTP' && e.message.includes('Mã xác thực') && !e.message.includes('internal details'))
})
test('HTML proxy failures and malformed success bodies are handled safely', async () => {
  await assert.rejects(readJson(new Response('<html>oops</html>', { status: 502 }), command), e => e.status === 502)
  await assert.rejects(readJson(new Response('{}'), command), e => e.code === 'INVALID_RESPONSE')
})
test('retry preserves request identity and changing payload rotates it', () => {
  let n = 0; const uuid = () => `request-${++n}`
  const first = requestIdentity(null, 'complete:1:version-2', uuid)
  assert.equal(requestIdentity(first, 'complete:1:version-2', uuid), first)
  assert.notEqual(requestIdentity(first, 'complete:1:version-3', uuid).key, first.key)
  assert.notEqual(requestIdentity(null, 'complete:1:version-2', uuid).key, first.key)
})
test('datetime-local is converted to Vietnam time without device timezone or date rollover', () => {
  assert.equal(vietnamTime('2026-12-01T08:30'), '2026-12-01T08:30:00+07:00')
  assert.equal(vietnamTime('2026-02-30T08:30'), undefined)
  assert.equal(vietnamTime('2026-12-01T25:30'), undefined)
})
test('visitor gate errors explain approval and visiting time separately', async () => {
  for (const [code, text] of [['VISITOR_NOT_APPROVED', 'chưa được duyệt'], ['VISITOR_NOT_STARTED', 'Chưa đến'], ['VISITOR_EXPIRED', 'hết hạn']]) {
    await assert.rejects(readJson(new Response(JSON.stringify({ code }), { status: 409 }), command), e => e.code === code && e.message.includes(text))
  }
})
