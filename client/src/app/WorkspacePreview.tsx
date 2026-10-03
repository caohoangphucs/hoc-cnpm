import { lazy, Suspense } from 'react'
import { Navigate, Route, Routes, useParams } from 'react-router'
import { AppShell } from './layout/AppShell'
import { workspaces } from './navigation'
import { OverviewPage } from './pages/OverviewPage'
import { FeatureFrame } from './pages/FeatureFrame'
import { NotFoundPage } from './pages/NotFoundPage'
import { featurePages, type FeaturePageModule } from './featurePages'
const Accounts = lazy(()=>import('@/features/accounts/AccountsPreview').then(module=>({default:module.PreviewAccounts})))
const liveRoutes = new Set(['technician/repairs', 'student/repairs', 'student/visitors', 'security/gate', 'security/visitors', 'student/community', 'admin/community', 'admin/discipline', 'student/notifications', 'admin/rooms', 'student/stay', 'admin/bookings', 'accountant/invoices', 'student/invoices', 'admin/visitors', 'admin/repairs', 'admin/invoices', 'admin/entries', 'student/entries', 'accountant/payments'])
const pages = { 'admin/accounts': Accounts, ...featurePages(import.meta.glob<FeaturePageModule>('../features/*/preview-route.tsx', { eager: true })) }
export default function WorkspacePreview() {
  const {workspaceId}=useParams(), workspace=workspaces.find(w=>w.id===workspaceId)
  if(!workspace)return <NotFoundPage standalone />
  return <Routes><Route element={<AppShell workspace={workspace} preview />}><Route index element={<OverviewPage workspace={workspace} prefix="/preview" />} />{workspace.navigation.filter(item=>item.path).map(item=>{const Page=pages[`${workspace.id}/${item.path}` as keyof typeof pages];return <Route key={item.path} path={item.path} element={liveRoutes.has(`${workspace.id}/${item.path}`)?<Navigate replace to={`/${workspace.id}/${item.path}`} />:Page?<Suspense fallback={<p role="status">Đang mở giao diện…</p>}><Page /></Suspense>:<FeatureFrame workspace={workspace} item={item} prefix="/preview" />} />})}<Route path="*" element={<NotFoundPage home={`/preview/${workspace.id}`} />} /></Route></Routes>
}
