import { Link, Route, Routes } from 'react-router'
import OverviewPage from '@/features/overview/OverviewPage'

export default function AppRoutes() {
  return (
    <Routes>
      <Route
        path="/"
        element={<OverviewPage />}
      />
      <Route
        path="*"
        element={
          <main>
            <h1>Không tìm thấy trang</h1>
            <Link to="/">Về trang chủ</Link>
          </main>
        }
      />
    </Routes>
  )
}
