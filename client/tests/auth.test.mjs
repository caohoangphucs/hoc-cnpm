import test from 'node:test'
import assert from 'node:assert/strict'
import { parseSession, needsTwoFactor, sessionHome, loginDestination } from '../src/features/auth/model.ts'

const session = (role = 'SINHVIEN', twoFactorVerified = false) => ({
  authenticated: true, role, twoFactorVerified, csrfToken: 'csrf-from-server',
  idleExpiresAt: new Date(Date.now() + 60_000).toISOString(),
})

test('only server-confirmed roles map to their workspace', () => {
  for (const [role, home] of Object.entries({ SINHVIEN: '/student', ADMIN: '/admin', KETOAN: '/accountant', KYTHUAT: '/technician', BAOVE: '/security' })) {
    assert.equal(sessionHome(parseSession(session(role))), home)
  }
  for (const role of ['OWNER', 'student', 'toString', '__proto__']) assert.throws(() => parseSession(session(role)))
})
test('admin and accountant must finish 2FA before reaching protected routes', () => {
  for (const role of ['ADMIN', 'KETOAN']) {
    assert.equal(needsTwoFactor(parseSession(session(role))), true)
    assert.equal(needsTwoFactor(parseSession(session(role, true))), false)
  }
  for (const role of ['SINHVIEN', 'KYTHUAT', 'BAOVE']) assert.equal(needsTwoFactor(parseSession(session(role))), false)
})
test('missing or malformed session data cannot unlock a workspace', () => {
  for (const value of [null, {}, { ...session(), authenticated: 'true' }, { ...session(), csrfToken: '' }, { ...session(), twoFactorVerified: 'false' }, { ...session(), idleExpiresAt: 'invalid' }]) {
    assert.throws(() => parseSession(value))
  }
  assert.deepEqual(parseSession({ authenticated: false, csrfToken: 'fresh' }), { authenticated: false, csrfToken: 'fresh' })
  assert.equal(parseSession({ ...session(), idleExpiresAt: new Date(Date.now() - 1000).toISOString() }).authenticated, false)
})
test('return destination stays in the authenticated workspace and rejects external or traversed paths', () => {
  const student = parseSession(session())
  assert.equal(loginDestination(student, '/student/invoices?month=2026-10'), '/student/invoices?month=2026-10')
  for (const path of ['/admin', '//evil.test/student', 'https://evil.test', '/student/../../admin', '/student/%2e%2e/admin', '/student\\evil', '/student\n/evil', '/studentish', null, {}]) {
    assert.equal(loginDestination(student, path), '/student')
  }
})
