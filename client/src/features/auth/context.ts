import { createContext, useContext } from 'react'
import type { Session } from './model'
import type { AuthError } from './api'

export type AuthState = { status: 'loading' } | { status: 'error'; error: AuthError } | { status: 'ready'; session: Session }
export type AuthContextValue = {
  state: AuthState
  notice: string
  refresh: () => Promise<Session>
  login: (username: string, password: string) => Promise<void>
  verify: (code: string) => Promise<void>
  logout: () => Promise<void>
}
export const AuthContext = createContext<AuthContextValue | null>(null)
export function useAuth() {
  const auth = useContext(AuthContext)
  if (!auth) throw new Error('AuthProvider is required')
  return auth
}
