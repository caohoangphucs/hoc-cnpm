import { useCallback, useEffect, useRef, useState, type ReactNode } from 'react'
import { AuthContext, type AuthState } from './context'
import { asAuthError, AuthError, fetchSession, submitLogin, submitLogout, submitTwoFactor } from './api'
import type { Session } from './model'
import { needsTwoFactor } from './model'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({ status: 'loading' })
  const [notice, setNotice] = useState('')
  const sessionRef = useRef<Session | null>(null)
  const generation = useRef(0)
  const mutation = useRef(false)
  const refreshTask = useRef<Promise<Session> | null>(null)

  const accept = useCallback((session: Session) => {
    if (sessionRef.current?.authenticated && !session.authenticated) setNotice('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
    sessionRef.current = session
    setState({ status: 'ready', session })
  }, [])

  const refresh = useCallback(() => {
    if (refreshTask.current) return refreshTask.current
    const version = generation.current
    const task = fetchSession().then((session) => {
      if (version === generation.current) accept(session)
      return session
    }).catch((error: unknown) => {
      if (version === generation.current) setState({ status: 'error', error: asAuthError(error) })
      throw error
    }).finally(() => { if (refreshTask.current === task) refreshTask.current = null })
    refreshTask.current = task
    return task
  }, [accept])

  useEffect(() => {
    const controller = new AbortController()
    const version = generation.current
    void fetchSession(controller.signal).then((session) => {
      if (!controller.signal.aborted && version === generation.current) accept(session)
    }).catch((error: unknown) => {
      if (!controller.signal.aborted && version === generation.current) setState({ status: 'error', error: asAuthError(error) })
    })
    return () => controller.abort()
  }, [accept])

  useEffect(() => {
    function revalidate() {
      if (document.visibilityState === 'visible' && sessionRef.current?.authenticated && !mutation.current) void refresh().catch(() => {})
    }
    window.addEventListener('focus', revalidate)
    window.addEventListener('ktx:unauthorized', revalidate)
    document.addEventListener('visibilitychange', revalidate)
    const expires = state.status === 'ready' && state.session.authenticated ? Date.parse(state.session.idleExpiresAt) : 0
    // Read the server again: other API activity may have extended the session.
    const timer = expires ? window.setTimeout(revalidate, Math.min(Math.max(0, expires - Date.now()), 2_147_483_647)) : undefined
    return () => {
      window.removeEventListener('focus', revalidate)
      window.removeEventListener('ktx:unauthorized', revalidate)
      document.removeEventListener('visibilitychange', revalidate)
      window.clearTimeout(timer)
    }
  }, [state, refresh])

  async function run(action: 'login' | 'verify' | 'logout', username = '', secret = '') {
    if (mutation.current) throw new AuthError('Yêu cầu đang được xử lý. Vui lòng chờ.')
    mutation.current = true
    try {
      // Re-read before a mutation to get a current CSRF token and cross-tab session.
      const current = await refresh()
      generation.current += 1
      if (action === 'login') {
        if (current.authenticated) return
        const next = await submitLogin(username, secret, current.csrfToken)
        if (!next.authenticated) throw new AuthError('Không thể đăng nhập. Vui lòng thử lại.')
        setNotice('')
        accept(next)
      } else if (action === 'verify') {
        if (!current.authenticated) throw new AuthError('Phiên xác thực đã hết hạn. Vui lòng đăng nhập lại.', 401)
        if (!needsTwoFactor(current)) return
        const next = await submitTwoFactor(secret, current.csrfToken)
        if (!next.authenticated || !next.twoFactorVerified) throw new AuthError('Chưa xác nhận được mã. Vui lòng thử lại.')
        setNotice('')
        accept(next)
      } else {
        const fallback: Session = { authenticated: false, csrfToken: current.csrfToken || '' }
        try {
          if (current.authenticated) await submitLogout(current.csrfToken)
        } catch {
          // Ignore server logout failures and continue clearing local auth state
        }
        sessionRef.current = null
        accept(fallback)
        setNotice('Bạn đã đăng xuất.')
        try {
          const fresh = await fetchSession()
          accept(fresh)
        } catch {
          // Fallback remains accepted
        }
      }
    } catch (error) {
      const issue = asAuthError(error)
      if (issue.status === 401 || issue.status === 403) await refresh().catch(() => {})
      throw issue
    } finally { mutation.current = false }
  }

  return <AuthContext value={{ state, notice, refresh, login: (username, password) => run('login', username, password), verify: (code) => run('verify', '', code), logout: () => run('logout') }}>{children}</AuthContext>
}
