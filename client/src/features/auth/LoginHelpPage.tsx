import { Link } from 'react-router'
import { AuthLayout } from './AuthLayout'
import { Icon } from '@/shared/components/Icon'

export function LoginHelpPage() {
  return <AuthLayout title="Bạn cần hỗ trợ?" description="Liên hệ Ban quản lý ký túc xá để được hỗ trợ tài khoản.">
    <div className="auth-help">
      <h2>Quên mật khẩu hoặc mất mã xác thực</h2>
      <p>Chuẩn bị tên đăng nhập và thông tin định danh của bạn để Ban quản lý xác minh và hướng dẫn khôi phục quyền truy cập.</p>
      <p>Không gửi mật khẩu hoặc mã xác thực cho người khác.</p>
    </div>
    <Link className="filled-link state-layer auth-submit" to="/login"><Icon name="back" />Về đăng nhập</Link>
  </AuthLayout>
}
