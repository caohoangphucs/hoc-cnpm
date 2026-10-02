import { lazy, Suspense } from 'react'
import { Route, Routes, useParams } from 'react-router'
import { AppShell } from './layout/AppShell'
import { workspaces } from './navigation'
import { OverviewPage } from './pages/OverviewPage'
import { FeatureFrame } from './pages/FeatureFrame'
import { NotFoundPage } from './pages/NotFoundPage'
const Accounts = lazy(()=>import('@/features/accounts/AccountsPreview').then(module=>({default:module.PreviewAccounts})))
const Booking = lazy(()=>import('@/features/booking/BookingPreview').then(module=>({default:module.PreviewRegistration})))
const Registration = lazy(()=>import('@/features/registration-review/RegistrationReviewPreview'))
const pages = { 'admin/accounts': Accounts, 'student/stay': Booking, 'admin/bookings': Registration }
export default function WorkspacePreview() {
  const {workspaceId}=useParams(), workspace=workspaces.find(w=>w.id===workspaceId)
  if(!workspace)return <NotFoundPage standalone />
  return <Routes><Route element={<AppShell workspace={workspace} preview />}><Route index element={<OverviewPage workspace={workspace} prefix="/preview" />} />{workspace.navigation.filter(item=>item.path).map(item=>{const Page=pages[`${workspace.id}/${item.path}` as keyof typeof pages];return <Route key={item.path} path={item.path} element={Page?<Suspense fallback={<p role="status">Đang mở giao diện…</p>}><Page /></Suspense>:<FeatureFrame workspace={workspace} item={item} prefix="/preview" />} />})}<Route path="*" element={<NotFoundPage home={`/preview/${workspace.id}`} />} /></Route></Routes>
}
