import type { WorkspaceId } from '@/app/navigation'

export const roleWorkspaces = {
  SINHVIEN: 'student', ADMIN: 'admin', KETOAN: 'accountant', KYTHUAT: 'technician', BAOVE: 'security',
} as const satisfies Record<string, WorkspaceId>
export type Role = keyof typeof roleWorkspaces
export type Session = { authenticated: false; csrfToken: string } | {
  authenticated: true
  csrfToken: string
  role: Role
  twoFactorVerified: boolean
  idleExpiresAt: string
  studentId?: string
}

export function parseSession(value: unknown): Session {
  if (!value || typeof value !== 'object') throw new Error('Invalid session')
  const data = value as Record<string, unknown>
  if (typeof data.csrfToken !== 'string' || !data.csrfToken.trim()) throw new Error('Invalid CSRF token')
  if (data.authenticated === false) return { authenticated: false, csrfToken: data.csrfToken }
  if (data.authenticated !== true || typeof data.role !== 'string' ||
    !Object.hasOwn(roleWorkspaces, data.role) || typeof data.twoFactorVerified !== 'boolean' ||
    typeof data.idleExpiresAt !== 'string' || !Number.isFinite(Date.parse(data.idleExpiresAt))) {
    throw new Error('Invalid authenticated session')
  }
  if (Date.parse(data.idleExpiresAt) <= Date.now()) return { authenticated: false, csrfToken: data.csrfToken }
  return {
    authenticated: true, csrfToken: data.csrfToken, role: data.role as Role,
    twoFactorVerified: data.twoFactorVerified, idleExpiresAt: data.idleExpiresAt,
    ...(typeof data.studentId === 'string' && data.studentId.trim() ? { studentId: data.studentId } : {}),
  }
}

export function needsTwoFactor(session: Session) {
  return session.authenticated && (session.role === 'ADMIN' || session.role === 'KETOAN') && !session.twoFactorVerified
}

export function sessionHome(session: Session) {
  return session.authenticated ? `/${roleWorkspaces[session.role]}` : '/login'
}

// Only restore a destination inside the server-confirmed workspace.
export function loginDestination(session: Session, requested: unknown) {
  const home = sessionHome(session)
  if (!session.authenticated || typeof requested !== 'string' || /[\\\r\n]/.test(requested)) return home
  try {
    const target = new URL(requested, 'https://ktx.invalid')
    return requested.startsWith('/') && target.origin === 'https://ktx.invalid' &&
      (target.pathname === home || target.pathname.startsWith(`${home}/`)) ? `${target.pathname}${target.search}` : home
  } catch { return home }
}
