import { useState } from 'react'
import type { Role } from '@/features/auth/model'
import { PageHeader } from '@/shared/components/PageHeader'
import { TextField } from '@/shared/components/TextField'
import { Button } from '@/shared/components/Button'
import { Icon } from '@/shared/components/Icon'
import { AccountDetail, AccountRoleDialog, AccountStatusDialog, AccountAvatar, AccountStatus } from './AccountDialogs'
import { accountRoles, filterAccounts, formatAccountDate, roleLabel, type Account, type AccountView } from './model'

export function AccountsPage({ initialAccounts = [], preview = false, view = 'error', onReset }: {
  initialAccounts?: readonly Account[]; preview?: boolean; view?: AccountView; onReset?: () => void
}) {
  const [accounts, setAccounts] = useState(() => [...initialAccounts])
  const [query, setQuery] = useState('')
  const [role, setRole] = useState('')
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(1)
  const [size, setSize] = useState(6)
  const [sort, setSort] = useState<'name' | 'lastLoginAt'>('name')
  const [descending, setDescending] = useState(false)
  const [dialog, setDialog] = useState<{ id: string; mode: 'detail' | 'role' | 'status' } | null>(null)
  const [notice, setNotice] = useState('')
  const data = view === 'data' ? accounts : []
  const filtered = filterAccounts(data, query, role, status).sort((left, right) => {
    if (sort === 'lastLoginAt') {
      if (!left.lastLoginAt) return right.lastLoginAt ? 1 : 0
      if (!right.lastLoginAt) return -1
      return (Date.parse(left.lastLoginAt) - Date.parse(right.lastLoginAt)) * (descending ? -1 : 1)
    }
    return left.name.localeCompare(right.name, 'vi') * (descending ? -1 : 1)
  })
  const pages = Math.max(1, Math.ceil(filtered.length / size))
  const currentPage = Math.min(page, pages)
  const rows = filtered.slice((currentPage - 1) * size, currentPage * size)
  const selected = dialog ? accounts.find((item) => item.id === dialog.id) : null
  const hasFilters = Boolean(query || role || status)
  function clearFilters() { setQuery(''); setRole(''); setStatus(''); setPage(1) }
  function changeSort(value: typeof sort) { setDescending(sort === value ? !descending : false); setSort(value); setPage(1) }
  function apply(update: Partial<Account>, message: string) {
    if (!preview || !selected) return
    setAccounts((current) => current.map((account) => account.id === selected.id ? { ...account, ...update } : account))
    setDialog(null)
    setNotice(`${message} (chỉ trong bản xem trước).`)
  }
  const available = view === 'data'
  return <>
    <PageHeader title="Tài khoản & vai trò" description="Quản lý quyền truy cập và trạng thái tài khoản trong ký túc xá." actions={preview ? <Button variant="outlined" onClick={onReset}><Icon name="refresh" />Khôi phục dữ liệu mẫu</Button> : undefined} />
    <div className="accounts-notice" role="status">{notice && <><Icon name="check" /><p>{notice}</p><button className="icon-button state-layer" aria-label="Đóng thông báo" onClick={() => setNotice('')}><Icon name="close" /></button></>}</div>
    {available && <dl className="accounts-summary">
      <div><dt>Tổng tài khoản</dt><dd>{data.length}</dd><Icon name="people" /></div>
      <div><dt>Đang hoạt động</dt><dd>{data.filter((item) => item.status === 'ACTIVE').length}</dd><Icon name="check" /></div>
      <div><dt>Đã khóa</dt><dd>{data.filter((item) => item.status === 'LOCKED').length}</dd><Icon name="lock" /></div>
    </dl>}
    <section className="accounts-list" aria-labelledby="account-list-title" aria-busy={view === 'loading'}>
      <div className="accounts-list-heading"><h2 id="account-list-title">Danh sách tài khoản</h2>{available && <span>{filtered.length} kết quả</span>}</div>
      <div className="accounts-filters">
        <TextField label="Tìm tài khoản" name="account-search" type="search" autoComplete="off" placeholder="Tên, tên đăng nhập hoặc email" value={query} disabled={!available} onChange={(event) => { setQuery(event.target.value); setPage(1) }} trailing={<Icon name="search" />} />
        <label className="accounts-select"><span>Vai trò</span><select aria-label="Lọc theo vai trò" value={role} disabled={!available} onChange={(event) => { setRole(event.target.value); setPage(1) }}><option value="">Tất cả vai trò</option>{accountRoles.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select></label>
        <label className="accounts-select"><span>Trạng thái</span><select aria-label="Lọc theo trạng thái" value={status} disabled={!available} onChange={(event) => { setStatus(event.target.value); setPage(1) }}><option value="">Tất cả trạng thái</option><option value="ACTIVE">Hoạt động</option><option value="LOCKED">Đã khóa</option></select></label>
        <Button variant="text" disabled={!hasFilters} onClick={clearFilters}>Xóa bộ lọc</Button>
      </div>
      {view === 'loading' ? <div className="accounts-loading" role="status"><p>Đang tải danh sách tài khoản…</p><div aria-hidden="true">{Array.from({ length: 5 }, (_, index) => <span key={index} />)}</div></div> : view === 'error' ?
        <div className="accounts-empty" role="alert"><span className="empty-symbol"><Icon name="info" /></span><h3>Chưa thể tải danh sách tài khoản</h3><p>Danh sách hiện chưa sẵn sàng. Vui lòng thử lại sau.</p></div> : !rows.length ?
          <div className="accounts-empty"><span className="empty-symbol"><Icon name={hasFilters ? 'search' : 'people'} /></span><h3>{hasFilters ? 'Không tìm thấy tài khoản phù hợp' : 'Chưa có tài khoản'}</h3><p>{hasFilters ? 'Thử tìm bằng thông tin khác hoặc xóa bộ lọc.' : 'Các tài khoản sẽ xuất hiện tại đây khi có dữ liệu.'}</p>{hasFilters && <Button variant="outlined" onClick={clearFilters}>Xóa bộ lọc</Button>}</div> :
          <><p className="accounts-scroll-hint">Cuộn ngang để xem đầy đủ thông tin.</p><div className="accounts-table-scroll" tabIndex={0} role="region" aria-label="Bảng danh sách tài khoản, có thể cuộn ngang">
            <table className="accounts-table"><caption className="sr-only">Danh sách tài khoản: {filtered.length} kết quả</caption><thead><tr>
              <th scope="col" aria-sort={sort === 'name' ? descending ? 'descending' : 'ascending' : 'none'}><button onClick={() => changeSort('name')}>Tài khoản<Icon name={sort === 'name' && descending ? 'sortDown' : 'sortUp'} /></button></th>
              <th scope="col">Vai trò</th><th scope="col">Trạng thái</th>
              <th scope="col" aria-sort={sort === 'lastLoginAt' ? descending ? 'descending' : 'ascending' : 'none'}><button onClick={() => changeSort('lastLoginAt')}>Đăng nhập gần nhất<Icon name={sort === 'lastLoginAt' && descending ? 'sortDown' : 'sortUp'} /></button></th>
              <th scope="col"><span className="sr-only">Thao tác</span></th>
            </tr></thead><tbody>{rows.map((account) => <tr key={account.id}>
              <td><button className="account-person" aria-label={`Xem tài khoản ${account.name}`} onClick={() => setDialog({ id: account.id, mode: 'detail' })}><AccountAvatar name={account.name} /><span><strong>{account.name}</strong><small>@{account.username}</small></span></button></td>
              <td><span className="account-role-tag">{roleLabel(account.role)}</span></td><td><AccountStatus status={account.status} /></td><td className="account-last-login">{formatAccountDate(account.lastLoginAt)}</td>
              <td><button className="icon-button state-layer" aria-label={`Chi tiết tài khoản ${account.username}`} onClick={() => setDialog({ id: account.id, mode: 'detail' })}><Icon name="more" /></button></td>
            </tr>)}</tbody></table>
          </div></>}
      {available && filtered.length > 0 && <div className="accounts-pagination">
        <label>Số dòng mỗi trang<select aria-label="Số dòng mỗi trang" value={size} onChange={(event) => { setSize(Number(event.target.value)); setPage(1) }}><option value="6">6</option><option value="12">12</option><option value="24">24</option></select></label>
        <p role="status">{(currentPage - 1) * size + 1}–{Math.min(currentPage * size, filtered.length)} / {filtered.length} tài khoản</p>
        <div><button className="icon-button state-layer" aria-label="Trang trước" disabled={currentPage === 1} onClick={() => setPage(currentPage - 1)}><Icon name="back" /></button><span>Trang {currentPage}/{pages}</span><button className="icon-button state-layer" aria-label="Trang sau" disabled={currentPage === pages} onClick={() => setPage(currentPage + 1)}><Icon name="arrow" /></button></div>
      </div>}
    </section>
    {selected && dialog?.mode === 'detail' && <AccountDetail account={selected} onClose={() => setDialog(null)} onRole={() => setDialog({ id: selected.id, mode: 'role' })} onStatus={() => setDialog({ id: selected.id, mode: 'status' })} />}
    {selected && dialog?.mode === 'role' && <AccountRoleDialog key={selected.id} account={selected} onClose={() => setDialog(null)} onApply={(next: Role) => apply({ role: next }, `Đã đổi vai trò của ${selected.name} thành ${roleLabel(next)}`)} />}
    {selected && dialog?.mode === 'status' && <AccountStatusDialog account={selected} onClose={() => setDialog(null)} onApply={() => apply({ status: selected.status === 'ACTIVE' ? 'LOCKED' : 'ACTIVE' }, `Đã ${selected.status === 'ACTIVE' ? 'khóa' : 'mở khóa'} tài khoản ${selected.username}`)} />}
  </>
}
