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
import { PaymentsPage } from '@/features/payments/PaymentsPage'
import { MaintenancePage, StudentRepairsPage } from '@/features/maintenance/MaintenancePage'
import { StudentVisitorDeclarationPage } from '@/features/gate/GatePage'
import { featurePages, type FeaturePageModule } from './featurePages'

const WorkspacePreview = import.meta.env.DEV ? lazy(() => import('./WorkspacePreview')) : null
const pages = { 'admin/accounts': AccountsPage, 'admin/bookings': RegistrationReviewPage, 'student/stay': BookingPage, 'accountant/invoices': InvoiceIssuancePage, 'student/invoices': PaymentsPage, 'technician/repairs': MaintenancePage, 'student/repairs': StudentRepairsPage, 'student/visitors': StudentVisitorDeclarationPage, ...featurePages(import.meta.glob<FeaturePageModule>('../features/*/route.tsx', { eager: true })) }

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
            {workspace.navigation.filter((item) => item.path).map((item) => {
              const Page = pages[`${workspace.id}/${item.path}` as keyof typeof pages]
              return <Route key={item.path} path={item.path} element={Page ? <Page /> : <FeatureFrame workspace={workspace} item={item} />} />
            })}
            <Route path="*" element={<NotFoundPage home={workspacePath(workspace)} />} />
          </Route>
        </Route>
      ))}
      <Route path="*" element={<NotFoundPage standalone />} />
    </Routes>
  )
}
