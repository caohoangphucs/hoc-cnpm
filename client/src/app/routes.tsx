import { Route, Routes } from 'react-router'
import { workspaces, workspacePath } from './navigation'
import { AppShell } from './layout/AppShell'
import { OverviewPage } from './pages/OverviewPage'
import { FeatureFrame } from './pages/FeatureFrame'
import { NotFoundPage } from './pages/NotFoundPage'
import { LoginPage } from '@/features/auth/LoginPage'
import { TwoFactorPage } from '@/features/auth/TwoFactorPage'
import { LoginHelpPage } from '@/features/auth/LoginHelpPage'
import { HomeRedirect, RequireSession } from '@/features/auth/RouteGuards'
import { lazy, Suspense } from 'react'
import { AccountsPage } from '@/features/accounts/AccountsPage'
import { BookingPage } from '@/features/booking/BookingPage'

const AccountsPreview = import.meta.env.DEV ? lazy(() => import('@/features/accounts/AccountsPreview')) : null
const BookingPreview = import.meta.env.DEV ? lazy(() => import('@/features/booking/BookingPreview')) : null

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/login/2fa" element={<TwoFactorPage />} />
      <Route path="/login/help" element={<LoginHelpPage />} />
      {AccountsPreview && <Route path="/preview/admin/*" element={<Suspense fallback={<main className="standalone-page" role="status">Đang mở bản xem trước…</main>}><AccountsPreview /></Suspense>} />}
      {BookingPreview && <Route path="/preview/student/*" element={<Suspense fallback={<main className="standalone-page" role="status">Đang mở bản xem trước…</main>}><BookingPreview /></Suspense>} />}
      {workspaces.map((workspace) => (
        <Route key={workspace.id} element={<RequireSession workspace={workspace.id} />}>
          <Route path={workspacePath(workspace)} element={<AppShell workspace={workspace} />}>
            <Route index element={<OverviewPage workspace={workspace} />} />
            {workspace.navigation.filter((item) => item.path).map((item) => (
              <Route key={item.path} path={item.path} element={workspace.id === 'admin' && item.path === 'accounts' ? <AccountsPage /> : workspace.id === 'student' && item.path === 'stay' ? <BookingPage /> : <FeatureFrame workspace={workspace} item={item} />} />
            ))}
            <Route path="*" element={<NotFoundPage home={workspacePath(workspace)} />} />
          </Route>
        </Route>
      ))}
      <Route path="*" element={<NotFoundPage standalone />} />
    </Routes>
  )
}
