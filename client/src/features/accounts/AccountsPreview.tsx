import { useState } from 'react'
import { Route, Routes } from 'react-router'
import { AppShell } from '@/app/layout/AppShell'
import { workspaces } from '@/app/navigation'
import { OverviewPage } from '@/app/pages/OverviewPage'
import { FeatureFrame } from '@/app/pages/FeatureFrame'
import { NotFoundPage } from '@/app/pages/NotFoundPage'
import { Icon } from '@/shared/components/Icon'
import { AccountsPage } from './AccountsPage'
import type { Account, AccountView } from './model'

// Fictional fixtures are only loaded by the Vite development preview route.
const fixtures: readonly Account[] = [
  { id: 'demo-01', name: 'Nguyễn Minh Anh', username: 'sv.minhanh', email: 'minhanh@example.test', role: 'SINHVIEN', status: 'ACTIVE', lastLoginAt: '2026-10-03T08:15:00+07:00', createdAt: '2026-08-01T08:00:00+07:00' },
  { id: 'demo-02', name: 'Trần Quốc Bảo', username: 'sv.quocbao', email: 'quocbao@example.test', role: 'SINHVIEN', status: 'ACTIVE', lastLoginAt: '2026-10-02T17:20:00+07:00', createdAt: '2026-08-02T08:00:00+07:00' },
  { id: 'demo-03', name: 'Lê Thu Hà', username: 'kt.thuha', email: 'thuha@example.test', role: 'KETOAN', status: 'ACTIVE', lastLoginAt: '2026-10-03T07:30:00+07:00', createdAt: '2026-07-01T08:00:00+07:00' },
  { id: 'demo-04', name: 'Phạm Hoàng Nam', username: 'kt.hoangnam', email: 'hoangnam@example.test', role: 'KYTHUAT', status: 'ACTIVE', lastLoginAt: '2026-10-02T15:10:00+07:00', createdAt: '2026-07-03T08:00:00+07:00' },
  { id: 'demo-05', name: 'Võ Ngọc Linh', username: 'sv.ngoclinh', email: 'ngoclinh@example.test', role: 'SINHVIEN', status: 'LOCKED', lastLoginAt: '2026-09-27T09:00:00+07:00', createdAt: '2026-08-02T08:00:00+07:00' },
  { id: 'demo-06', name: 'Đặng Tuấn Kiệt', username: 'bv.tuankiet', email: 'tuankiet@example.test', role: 'BAOVE', status: 'ACTIVE', lastLoginAt: '2026-10-03T06:00:00+07:00', createdAt: '2026-07-01T08:00:00+07:00' },
  { id: 'demo-07', name: 'Bùi Thanh Mai', username: 'bql.thanhmai', email: 'thanhmai@example.test', role: 'ADMIN', status: 'ACTIVE', lastLoginAt: '2026-10-03T08:00:00+07:00', createdAt: '2026-06-01T08:00:00+07:00' },
  { id: 'demo-08', name: 'Nguyễn Đức Huy', username: 'sv.duchuy', email: 'duchuy@example.test', role: 'SINHVIEN', status: 'LOCKED', lastLoginAt: null, createdAt: '2026-09-30T08:00:00+07:00' },
  { id: 'demo-09', name: 'Trần Khánh Vy', username: 'sv.khanhvy', email: 'khanhvy@example.test', role: 'SINHVIEN', status: 'ACTIVE', lastLoginAt: '2026-10-01T16:45:00+07:00', createdAt: '2026-08-03T08:00:00+07:00' },
  { id: 'demo-10', name: 'Phan Minh Quân', username: 'kt.minhquan', email: 'minhquan@example.test', role: 'KYTHUAT', status: 'ACTIVE', lastLoginAt: '2026-10-02T13:10:00+07:00', createdAt: '2026-07-04T08:00:00+07:00' },
  { id: 'demo-11', name: 'Lý Hoài Phương', username: 'kt.hoaiphuong', email: 'hoaiphuong@example.test', role: 'KETOAN', status: 'LOCKED', lastLoginAt: '2026-09-28T08:40:00+07:00', createdAt: '2026-07-01T08:00:00+07:00' },
  { id: 'demo-12', name: 'Đỗ Hải Sơn', username: 'bv.haison', email: 'haison@example.test', role: 'BAOVE', status: 'ACTIVE', lastLoginAt: '2026-10-02T22:00:00+07:00', createdAt: '2026-07-02T08:00:00+07:00' },
]
export function PreviewAccounts() {
  const [view, setView] = useState<AccountView>('data')
  const [revision, setRevision] = useState(0)
  return <>
    <div className="accounts-preview-bar"><Icon name="info" /><div><strong>Xem trước giao diện</strong><p>Dữ liệu minh họa. Thay đổi chỉ tồn tại trong phiên xem trước.</p></div>
      <label>Trạng thái hiển thị<select value={view} onChange={(event) => setView(event.target.value as AccountView)}><option value="data">Có dữ liệu</option><option value="loading">Đang tải</option><option value="empty">Danh sách trống</option><option value="error">Lỗi tải dữ liệu</option></select></label>
    </div>
    <AccountsPage key={revision} preview initialAccounts={fixtures} view={view} onReset={() => { setView('data'); setRevision((value) => value + 1) }} />
  </>
}
export default function AccountsPreview() {
  const admin = workspaces.find((workspace) => workspace.id === 'admin')!
  return <Routes><Route element={<AppShell workspace={admin} preview />}>
    <Route index element={<OverviewPage workspace={admin} prefix="/preview" />} />
    <Route path="accounts" element={<PreviewAccounts />} />
    {admin.navigation.filter((item) => item.path && item.path !== 'accounts').map((item) => <Route key={item.path} path={item.path} element={<FeatureFrame workspace={admin} item={item} prefix="/preview" />} />)}
    <Route path="*" element={<NotFoundPage home="/preview/admin" />} />
  </Route></Routes>
}
