import { useState } from 'react'
import { useAuth } from './context'
import type { AuthError } from './api'
import { Button } from '@/shared/components/Button'
import { Icon } from '@/shared/components/Icon'

export function AuthFeedback({ error }: { error: AuthError | null }) {
  if (!error) return null
  return <div className="auth-feedback auth-feedback--error" role="alert">
    <Icon name="info" /><div><p>{error.message}</p>{error.requestId && <p className="request-reference">Mã tra cứu: {error.requestId}</p>}</div>
  </div>
}

export function SessionFeedback() {
  const { state, notice, refresh } = useAuth()
  const [retrying, setRetrying] = useState(false)
  async function retry() {
    setRetrying(true)
    try { await refresh() } catch { /* The provider renders the connection error. */ }
    finally { setRetrying(false) }
  }
  return <>
    {notice && <p className="auth-notice" role="status">{notice}</p>}
    {state.status === 'loading' && <p className="auth-notice" role="status">Đang kiểm tra phiên đăng nhập…</p>}
    {state.status === 'error' && <div className="session-feedback">
      <AuthFeedback error={state.error} />
      <Button variant="outlined" pending={retrying} onClick={retry}>{retrying ? 'Đang kết nối…' : 'Thử kết nối lại'}</Button>
    </div>}
  </>
}
