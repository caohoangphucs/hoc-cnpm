import { useState } from 'react'
import type { Role } from '@/features/auth/model'
import { Dialog } from '@/shared/components/Dialog'
import { Button } from '@/shared/components/Button'
import { Icon } from '@/shared/components/Icon'
import { accountRoles, formatAccountDate, roleLabel, type Account } from './model'

export function AccountStatus({ status }: { status: Account['status'] }) {
  return <span className={`account-status account-status--${status.toLowerCase()}`}><Icon name={status === 'ACTIVE' ? 'check' : 'lock'} />{status === 'ACTIVE' ? 'Hoạt động' : 'Đã khóa'}</span>
}
export function AccountAvatar({ name }: { name: string }) {
  return <span className="account-avatar" aria-hidden="true">{name.split(' ').filter(Boolean).slice(-2).map((word) => word[0]).join('')}</span>
}
export function AccountDetail({ account, onClose, onRole, onStatus }: {
  account: Account; onClose: () => void; onRole: () => void; onStatus: () => void
}) {
  return <Dialog title="Chi tiết tài khoản" panel onClose={onClose} actions={<Button variant="outlined" onClick={onClose}>Đóng</Button>}>
    <div className="account-detail-person"><AccountAvatar name={account.name} /><h3>{account.name}</h3><p>@{account.username}</p><AccountStatus status={account.status} /></div>
    <dl className="account-facts">
      <div><dt>Mã tài khoản</dt><dd>{account.id}</dd></div>
      <div><dt>Email</dt><dd>{account.email}</dd></div>
      <div><dt>Vai trò</dt><dd>{roleLabel(account.role)}</dd></div>
      <div><dt>Ngày tạo</dt><dd>{formatAccountDate(account.createdAt)}</dd></div>
      <div><dt>Đăng nhập gần nhất</dt><dd>{formatAccountDate(account.lastLoginAt)}</dd></div>
    </dl>
    <section className="account-role-summary"><h3>Phạm vi công việc</h3><p>{accountRoles.find((role) => role.value === account.role)!.description}</p></section>
    <div className="account-detail-actions"><Button variant="outlined" onClick={onRole}><Icon name="shield" />Đổi vai trò</Button><Button variant="text" className={account.status === 'ACTIVE' ? 'account-danger-text' : ''} onClick={onStatus}><Icon name={account.status === 'ACTIVE' ? 'lock' : 'unlock'} />{account.status === 'ACTIVE' ? 'Khóa tài khoản' : 'Mở khóa tài khoản'}</Button></div>
  </Dialog>
}
export function AccountRoleDialog({ account, onClose, onApply }: { account: Account; onClose: () => void; onApply: (role: Role) => void }) {
  const [role, setRole] = useState(account.role)
  return <Dialog title="Đổi vai trò" onClose={onClose} actions={<><Button variant="text" onClick={onClose}>Hủy</Button><Button disabled={role === account.role} onClick={() => onApply(role)}>Lưu vai trò</Button></>}>
    <p className="account-dialog-description">Chọn vai trò cho <strong>{account.name}</strong> (@{account.username}). Vai trò quyết định các khu vực tài khoản được truy cập.</p>
    <fieldset className="account-role-options"><legend>Vai trò truy cập</legend>{accountRoles.map((item) => <label key={item.value} className={`account-role-option ${role === item.value ? 'is-selected' : ''}`}>
      <input type="radio" name="account-role" value={item.value} checked={role === item.value} onChange={() => setRole(item.value)} />
      <span><strong>{item.label}</strong><small>{item.description}</small></span>
    </label>)}</fieldset>
    <p className="account-dialog-note"><Icon name="info" />Các vai trò Ban quản lý và Kế toán yêu cầu xác thực hai bước.</p>
  </Dialog>
}
export function AccountStatusDialog({ account, onClose, onApply }: { account: Account; onClose: () => void; onApply: () => void }) {
  const locking = account.status === 'ACTIVE'
  return <Dialog title={locking ? 'Khóa tài khoản?' : 'Mở khóa tài khoản?'} onClose={onClose} actions={<><Button variant="text" onClick={onClose}>Hủy</Button><Button className={locking ? 'account-danger-button' : ''} onClick={onApply}>{locking ? 'Khóa tài khoản' : 'Mở khóa tài khoản'}</Button></>}>
    <div className={`account-confirm-symbol ${locking ? 'is-danger' : ''}`}><Icon name={locking ? 'lock' : 'unlock'} /></div>
    <p className="account-dialog-description"><strong>{account.name}</strong> (@{account.username})</p>
    <p className="account-dialog-description">{locking ? 'Tài khoản bị khóa sẽ không thể đăng nhập. Thông tin tài khoản và các dữ liệu liên quan vẫn được giữ lại.' : 'Tài khoản sẽ có thể đăng nhập lại với vai trò hiện tại. Mật khẩu và vai trò được giữ nguyên.'}</p>
  </Dialog>
}
