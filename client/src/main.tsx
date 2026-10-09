import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { createBrowserRouter, RouterProvider } from 'react-router'
import '@/theme/global.css'
import App from '@/app/App'
import { applyTheme, getPreferredTheme } from '@/theme/theme'

// khởi tạo giao diện sáng hoặc tối theo cấu hình người dùng
applyTheme(getPreferredTheme())
// A data router lets UC-04 block link/history navigation while its draft is unsent.
// tạo data router bao bọc toàn bộ ứng dụng
const router = createBrowserRouter([{ path: '*', element: <App /> }])

// gắn ứng dụng react vào phần tử root trong index.html
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>,
)
