import AppRoutes from './routes'
import { AuthProvider } from '@/features/auth/AuthProvider'

// component gốc: bọc auth provider quản lý phiên đăng nhập cho toàn bộ tuyến đường
export default function App() {
  return <AuthProvider><AppRoutes /></AuthProvider>
}
