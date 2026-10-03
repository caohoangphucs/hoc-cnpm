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
import { RegistrationReviewPage } from '@/features/registration-review/RegistrationReviewPage'
import { InvoiceIssuancePage } from '@/features/invoice-issuance/InvoiceIssuancePage'

const WorkspacePreview = import.meta.env.DEV ? lazy(() => import('./WorkspacePreview')) : null

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/login/2fa" element={<TwoFactorPage />} />
      <Route path="/login/help" element={<LoginHelpPage />} />
      {WorkspacePreview && <Route path="/preview/:workspaceId/*" element={<Suspense fallback={<main className="standalone-page" role="status">Đang mở bản xem trước…</main>}><WorkspacePreview /></Suspense>} />}
      {workspaces.map((workspace) => (
        <Route key={workspace.id} element={<RequireSession workspace={workspace.id} />}>
          <Route path={workspacePath(workspace)} element={<AppShell workspace={workspace} />}>
            <Route index element={<OverviewPage workspace={workspace} />} />
            {workspace.navigation.filter((item) => item.path).map((item) => (
              <Route key={item.path} path={item.path} element={workspace.id === 'admin' && item.path === 'accounts' ? <AccountsPage /> : workspace.id === 'admin' && item.path === 'bookings' ? <RegistrationReviewPage /> : workspace.id === 'student' && item.path === 'stay' ? <BookingPage /> : workspace.id === 'accountant' && item.path === 'invoices' ? <InvoiceIssuancePage /> : <FeatureFrame workspace={workspace} item={item} />} />
            ))}
            <Route path="*" element={<NotFoundPage home={workspacePath(workspace)} />} />
          </Route>
        </Route>
      ))}
      <Route path="*" element={<NotFoundPage standalone />} />
    </Routes>
  )
}
