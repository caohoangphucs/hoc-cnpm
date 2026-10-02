import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { createBrowserRouter, RouterProvider } from 'react-router'
import '@/theme/global.css'
import App from '@/app/App'
import { applyTheme, getPreferredTheme } from '@/theme/theme'

applyTheme(getPreferredTheme())
// A data router lets UC-04 block link/history navigation while its draft is unsent.
const router = createBrowserRouter([{ path: '*', element: <App /> }])

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>,
)
