import { Link, Route, Routes } from 'react-router'

export default function AppRoutes() {
  return (
    <Routes>
      <Route
        path="/"
        element={
          <main>
            <h1>KTX Smart Hub</h1>
            <p>Hệ thống quản lý ký túc xá.</p>
          </main>
        }
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
