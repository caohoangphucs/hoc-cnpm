import { Navigate, Route, Routes } from 'react-router'
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
import { MaterialsPage } from '@/features/maintenance/MaterialsPage'
import { MaintenancePage, StudentRepairsPage } from '@/features/maintenance/MaintenancePage'
import { StudentVisitorDeclarationPage } from '@/features/gate/GatePage'
import { AdminVisitorsPage } from '@/features/gate/VisitorList'
import { EntryHistory } from '@/features/gate/EntryHistory'
import { InvoiceList } from '@/features/invoice-issuance/InvoiceList'
import { PaymentSessions } from '@/features/payments/PaymentsPage'
import { featurePages, type FeaturePageModule } from './featurePages'

// nạp lười màn hình xem trước chỉ trong môi trường phát triển (dev)
const WorkspacePreview = import.meta.env.DEV ? lazy(() => import('./WorkspacePreview')) : null
// component bọc danh sách hóa đơn cho vai trò quản lý
function AdminInvoices() { return <InvoiceList manage /> }

// bảng ánh xạ đường dẫn nghiệp vụ sang component giao diện thực tế
const pages = { 'technician/materials': MaterialsPage, 'admin/materials': MaterialsPage, 'admin/invoices': AdminInvoices, 'admin/visitors': AdminVisitorsPage, 'admin/repairs': MaintenancePage, 'admin/entries': EntryHistory, 'student/entries': EntryHistory, 'accountant/payments': PaymentSessions, 'admin/accounts': AccountsPage, 'admin/bookings': RegistrationReviewPage, 'student/stay': BookingPage, 'accountant/invoices': InvoiceIssuancePage, 'student/invoices': PaymentsPage, 'technician/repairs': MaintenancePage, 'student/repairs': StudentRepairsPage, 'student/visitors': StudentVisitorDeclarationPage, ...featurePages(import.meta.glob<FeaturePageModule>('../features/*/route.tsx', { eager: true })) }

// định tuyến chính của ứng dụng
export default function AppRoutes() {
  return (
    <Routes>
      {/* điều hướng trang chủ và các trang xác thực đăng nhập */}
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/login/2fa" element={<TwoFactorPage />} />
      <Route path="/login/help" element={<LoginHelpPage />} />

      {/* đường dẫn xem trước giao diện dành cho lập trình viên */}
      {WorkspacePreview && <Route path="/preview/:workspaceId/*" element={<Suspense fallback={<main className="standalone-page" role="status">Đang mở bản xem trước…</main>}><WorkspacePreview /></Suspense>} />}

      {/* các tuyến đường theo không gian làm việc, được bảo vệ bằng session */}
      {workspaces.map((workspace) => (
        <Route key={workspace.id} element={<RequireSession workspace={workspace.id} />}>
          <Route path={workspacePath(workspace)} element={<AppShell workspace={workspace} />}>
            {/* trang tổng quan của vai trò */}
            <Route index element={<OverviewPage workspace={workspace} />} />

            {/* chuyển hướng thông báo của sinh viên về trang kỷ luật */}
            {workspace.id === 'student' && <Route path="notifications" element={<Navigate to="../discipline" replace />} />}

            {/* sinh tự động các trang con: có trang thật thì render, chưa có thì rơi vào feature frame */}
            {workspace.navigation.filter((item) => item.path).map((item) => {
              const Page = pages[`${workspace.id}/${item.path}` as keyof typeof pages]
              return <Route key={item.path} path={item.path} element={Page ? <Page /> : <FeatureFrame workspace={workspace} item={item} />} />
            })}

            {/* trang 404 bên trong layout khi gõ sai đường dẫn con */}
            <Route path="*" element={<NotFoundPage home={workspacePath(workspace)} />} />
          </Route>
        </Route>
      ))}

      {/* trang 404 độc lập ngoài layout */}
      <Route path="*" element={<NotFoundPage standalone />} />
    </Routes>
  )
}
