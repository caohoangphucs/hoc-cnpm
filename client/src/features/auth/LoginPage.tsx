import { useRef, useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation } from 'react-router'
import { useAuth } from './context'
import { asAuthError, type AuthError } from './api'
import { loginDestination, needsTwoFactor } from './model'
import { AuthLayout } from './AuthLayout'
import { AuthFeedback, SessionFeedback } from './AuthFeedback'
import { TextField } from '@/shared/components/TextField'
import { Button } from '@/shared/components/Button'
import { Icon } from '@/shared/components/Icon'

// màn hình đăng nhập tài khoản người dùng
export function LoginPage() {
  const { state, login } = useAuth()
  const location = useLocation()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<AuthError | null>(null)
  const [fields, setFields] = useState<Record<string, string>>({})
  const usernameRef = useRef<HTMLInputElement>(null)
  const passwordRef = useRef<HTMLInputElement>(null)
  const requested: unknown = location.state?.returnTo

  // nếu đã có phiên hợp lệ: chuyển hướng ngay đến trang đích hoặc 2fa
  if (state.status === 'ready' && state.session.authenticated) {
    return <Navigate to={needsTwoFactor(state.session) ? '/login/2fa' : loginDestination(state.session, requested)} state={{ returnTo: requested }} replace />
  }

  // xử lý gửi biểu mẫu đăng nhập
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (busy) return
    const issues: Record<string, string> = {}
    // kiểm tra hợp lệ tên đăng nhập
    if (!username.trim()) issues.username = 'Nhập tên đăng nhập của bạn.'
    else if (username.trim().length > 50) issues.username = 'Tên đăng nhập không quá 50 ký tự.'
    // kiểm tra hợp lệ mật khẩu
    if (!password.trim()) issues.password = 'Nhập mật khẩu của bạn.'
    else if (password.length > 128) issues.password = 'Mật khẩu không quá 128 ký tự.'
    setFields(issues)
    setError(null)
    if (Object.keys(issues).length) {
      if (issues.username) usernameRef.current?.focus()
      else passwordRef.current?.focus()
      return
    }
    setBusy(true)
    const secret = password
    setPassword('')
    try { await login(username.trim(), secret) }
    catch (issue) {
      const failure = asAuthError(issue)
      setError(failure)
      setFields(failure.fieldErrors)
      if (failure.fieldErrors.username) usernameRef.current?.focus()
      else passwordRef.current?.focus()
    } finally { setBusy(false) }
  }

  return <AuthLayout title="Chào mừng trở lại" description="Đăng nhập để tiếp tục sử dụng KTX Smart Hub.">
    <SessionFeedback />
    <AuthFeedback error={error} />
    {/* biểu mẫu đăng nhập */}
    <form className="auth-form" onSubmit={submit} noValidate aria-label="Đăng nhập" aria-busy={busy}>
      <TextField ref={usernameRef} label="Tên đăng nhập" name="username" autoComplete="username" autoCapitalize="none" spellCheck={false} value={username} readOnly={busy} required maxLength={50} errorText={fields.username} onChange={(event) => { setUsername(event.target.value); setFields((current) => ({ ...current, username: '' })) }} />
      <TextField ref={passwordRef} label="Mật khẩu" name="password" autoComplete="current-password" type={showPassword ? 'text' : 'password'} value={password} readOnly={busy} required maxLength={128} errorText={fields.password} onChange={(event) => { setPassword(event.target.value); setFields((current) => ({ ...current, password: '' })) }} trailing={
        <button className="icon-button state-layer" type="button" aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'} aria-pressed={showPassword} onClick={() => setShowPassword((shown) => !shown)}><Icon name={showPassword ? 'eyeOff' : 'eye'} /></button>
      } />
      <div className="auth-form-links"><Link className="text-link" to="/login/help">Quên mật khẩu?</Link></div>
      <Button type="submit" pending={busy} disabled={state.status === 'loading'} className="auth-submit">{busy ? 'Đang đăng nhập…' : 'Đăng nhập'}{!busy && <Icon name="arrow" />}</Button>
      <p className="form-status" role="status">{busy ? 'Đang xác nhận thông tin đăng nhập.' : ''}</p>
    </form>
    <p className="auth-support">Chưa có tài khoản? Liên hệ Ban quản lý ký túc xá để được hỗ trợ.</p>
  </AuthLayout>
}
