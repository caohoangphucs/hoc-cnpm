import { Navigate, Outlet, useLocation } from 'react-router'
import type { WorkspaceId } from '@/app/navigation'
import { useAuth } from './context'
import { needsTwoFactor, roleWorkspaces, sessionHome } from './model'
import { AuthLayout } from './AuthLayout'
import { SessionFeedback } from './AuthFeedback'

// bảo vệ các tuyến đường yêu cầu đăng nhập và phân quyền đúng vai trò
export function RequireSession({ workspace }: { workspace: WorkspaceId }) {
  const { state } = useAuth()
  const location = useLocation()
  // lưu lại đường dẫn người dùng đang muốn vào để chuyển tiếp sau khi đăng nhập
  const returnTo = `${location.pathname}${location.search}`
  // đang kiểm tra phiên làm việc từ máy chủ
  if (state.status !== 'ready') return <AuthLayout title="Kết nối tài khoản" description="Kiểm tra phiên đăng nhập để mở không gian của bạn."><SessionFeedback /></AuthLayout>
  // chưa đăng nhập: chuyển hướng về trang login
  if (!state.session.authenticated) return <Navigate to="/login" state={{ returnTo }} replace />
  // cần xác thực 2 bước: chuyển hướng về trang nhập mã 2fa
  if (needsTwoFactor(state.session)) return <Navigate to="/login/2fa" state={{ returnTo }} replace />
  // vai trò không khớp với không gian hiện tại: chuyển về trang chủ của đúng vai trò
  if (roleWorkspaces[state.session.role] !== workspace) return <Navigate to={sessionHome(state.session)} replace />
  // phiên hợp lệ: cho phép render các trang con bên trong
  return <Outlet />
}

// điều hướng trang chủ gốc (/) dựa trên trạng thái phiên đăng nhập
export function HomeRedirect() {
  const { state } = useAuth()
  // đang kiểm tra phiên
  if (state.status !== 'ready') return <AuthLayout title="Kết nối tài khoản" description="Kiểm tra phiên đăng nhập để mở không gian của bạn."><SessionFeedback /></AuthLayout>
  // chuyển hướng về trang 2fa hoặc trang chủ của vai trò tương ứng
  return <Navigate to={needsTwoFactor(state.session) ? '/login/2fa' : sessionHome(state.session)} replace />
}
