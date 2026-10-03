import { Navigate, Outlet, useLocation } from 'react-router'
import type { WorkspaceId } from '@/app/navigation'
import { useAuth } from './context'
import { needsTwoFactor, roleWorkspaces, sessionHome } from './model'
import { AuthLayout } from './AuthLayout'
import { SessionFeedback } from './AuthFeedback'

export function RequireSession({ workspace }: { workspace: WorkspaceId }) {
  const { state } = useAuth()
  const location = useLocation()
  const returnTo = `${location.pathname}${location.search}`
  if (state.status !== 'ready') return <AuthLayout title="Kết nối tài khoản" description="Kiểm tra phiên đăng nhập để mở không gian của bạn."><SessionFeedback /></AuthLayout>
  if (!state.session.authenticated) return <Navigate to="/login" state={{ returnTo }} replace />
  if (needsTwoFactor(state.session)) return <Navigate to="/login/2fa" state={{ returnTo }} replace />
  if (roleWorkspaces[state.session.role] !== workspace) return <Navigate to={sessionHome(state.session)} replace />
  return <Outlet />
}

export function HomeRedirect() {
  const { state } = useAuth()
  if (state.status !== 'ready') return <AuthLayout title="Kết nối tài khoản" description="Kiểm tra phiên đăng nhập để mở không gian của bạn."><SessionFeedback /></AuthLayout>
  return <Navigate to={needsTwoFactor(state.session) ? '/login/2fa' : sessionHome(state.session)} replace />
}
