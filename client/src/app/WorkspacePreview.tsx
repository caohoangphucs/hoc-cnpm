import { lazy, Suspense } from 'react'
import { Navigate, Route, Routes, useParams } from 'react-router'
import { AppShell } from './layout/AppShell'
import { workspaces } from './navigation'
import { OverviewPage } from './pages/OverviewPage'
import { FeatureFrame } from './pages/FeatureFrame'
import { NotFoundPage } from './pages/NotFoundPage'
import { featurePages, type FeaturePageModule } from './featurePages'
const Accounts = lazy(()=>import('@/features/accounts/AccountsPreview').then(module=>({default:module.PreviewAccounts})))
const Booking = lazy(()=>import('@/features/booking/BookingPreview').then(module=>({default:module.PreviewRegistration})))
const Registration = lazy(()=>import('@/features/registration-review/RegistrationReviewPreview'))
const InvoiceIssuance = lazy(()=>import('@/features/invoice-issuance/InvoiceIssuancePreview'))
const Payments = lazy(()=>import('@/features/payments/PaymentsPreview'))
const liveRoutes = new Set(['technician/repairs', 'student/repairs', 'student/visitors', 'security/gate', 'security/visitors', 'student/community', 'admin/community', 'admin/discipline', 'student/notifications'])
const pages = { 'admin/accounts': Accounts, 'student/stay': Booking, 'admin/bookings': Registration, 'accountant/invoices': InvoiceIssuance, 'student/invoices': Payments, ...featurePages(import.meta.glob<FeaturePageModule>('../features/*/preview-route.tsx', { eager: true })) }
export default function WorkspacePreview() {
  const {workspaceId}=useParams(), workspace=workspaces.find(w=>w.id===workspaceId)
  if(!workspace)return <NotFoundPage standalone />
  return <Routes><Route element={<AppShell workspace={workspace} preview />}><Route index element={<OverviewPage workspace={workspace} prefix="/preview" />} />{workspace.navigation.filter(item=>item.path).map(item=>{const Page=pages[`${workspace.id}/${item.path}` as keyof typeof pages];return <Route key={item.path} path={item.path} element={liveRoutes.has(`${workspace.id}/${item.path}`)?<Navigate replace to={`/${workspace.id}/${item.path}`} />:Page?<Suspense fallback={<p role="status">Đang mở giao diện…</p>}><Page /></Suspense>:<FeatureFrame workspace={workspace} item={item} prefix="/preview" />} />})}<Route path="*" element={<NotFoundPage home={`/preview/${workspace.id}`} />} /></Route></Routes>
}
