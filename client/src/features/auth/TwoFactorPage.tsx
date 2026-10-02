import { useRef, useState, type FormEvent } from 'react'
import { Navigate, useLocation } from 'react-router'
import { useAuth } from './context'
import { asAuthError, type AuthError } from './api'
import { loginDestination, needsTwoFactor, roleWorkspaces } from './model'
import { workspaces } from '@/app/navigation'
import { AuthLayout } from './AuthLayout'
import { AuthFeedback, SessionFeedback } from './AuthFeedback'
import { TextField } from '@/shared/components/TextField'
import { Button } from '@/shared/components/Button'

export function TwoFactorPage() {
  const { state, verify, logout } = useAuth()
  const location = useLocation()
  const requested: unknown = location.state?.returnTo
  const [code, setCode] = useState('')
  const [busy, setBusy] = useState<'verify' | 'logout' | null>(null)
  const [error, setError] = useState<AuthError | null>(null)
  const [fieldError, setFieldError] = useState('')
  const codeRef = useRef<HTMLInputElement>(null)
  const session = state.status === 'ready' ? state.session : null
  if (session && !session.authenticated) return <Navigate to="/login" state={{ returnTo: requested }} replace />
  if (session?.authenticated && !needsTwoFactor(session)) return <Navigate to={loginDestination(session, requested)} replace />
  const role = session?.authenticated ? workspaces.find((workspace) => workspace.id === roleWorkspaces[session.role])?.label : ''
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (busy) return
    setError(null)
    if (!/^\d{6}$/.test(code)) { setFieldError('Nhập đủ 6 chữ số của mã xác thực.'); codeRef.current?.focus(); return }
    setFieldError('')
    setBusy('verify')
    const secret = code
    setCode('')
    try { await verify(secret) }
    catch (issue) { const failure = asAuthError(issue); setError(failure); setFieldError(failure.fieldErrors.code || ''); codeRef.current?.focus() }
    finally { setBusy(null) }
  }
  async function changeAccount() {
    if (busy) return
    setBusy('logout')
    setError(null)
    setCode('')
    try { await logout() } catch (issue) { setError(asAuthError(issue)) }
    finally { setBusy(null) }
  }
  return <AuthLayout title="Xác minh đăng nhập" description="Thêm một bước xác thực để bảo vệ tài khoản của bạn.">
    <SessionFeedback />
    {role && <div className="auth-account"><span>Đang đăng nhập với vai trò</span><strong>{role}</strong></div>}
    <AuthFeedback error={error} />
    <form className="auth-form" onSubmit={submit} noValidate aria-label="Xác thực hai bước" aria-busy={busy === 'verify'}>
      <TextField ref={codeRef} className="otp-field" label="Mã xác thực" name="code" type="text" inputMode="numeric" autoComplete="one-time-code" pattern="[0-9]{6}" maxLength={6} value={code} readOnly={Boolean(busy)} required errorText={fieldError} supportingText="Nhập mã 6 chữ số từ phương thức xác thực được cấp cho tài khoản." onChange={(event) => { setCode(event.target.value.replace(/\D/g, '').slice(0, 6)); setFieldError('') }} />
      <Button className="auth-submit" type="submit" pending={busy === 'verify'} disabled={state.status !== 'ready' || Boolean(busy)}>{busy === 'verify' ? 'Đang xác minh…' : 'Xác minh và tiếp tục'}</Button>
      <p className="form-status" role="status">{busy === 'verify' ? 'Đang kiểm tra mã xác thực.' : ''}</p>
    </form>
    <Button variant="text" className="auth-submit" pending={busy === 'logout'} disabled={state.status !== 'ready' || Boolean(busy)} onClick={changeAccount}>{busy === 'logout' ? 'Đang đăng xuất…' : 'Dùng tài khoản khác'}</Button>
    <p className="auth-support">Không lấy được mã? Liên hệ Ban quản lý để được hỗ trợ xác thực tài khoản.</p>
  </AuthLayout>
}
