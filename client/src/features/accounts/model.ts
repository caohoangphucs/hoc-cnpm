import type { Role } from '@/features/auth/model'

// View model for the UI. An HTTP contract has not been agreed yet.
export type Account = {
  id: string; username: string; name: string; email: string; role: Role
  status: 'ACTIVE' | 'LOCKED'; lastLoginAt: string | null; createdAt: string
}
export type AccountView = 'data' | 'loading' | 'empty' | 'error'
export const accountRoles: { value: Role; label: string; description: string }[] = [
  { value: 'SINHVIEN', label: 'Sinh viên', description: 'Lưu trú, hóa đơn và các yêu cầu của sinh viên.' },
  { value: 'ADMIN', label: 'Ban quản lý', description: 'Quản lý lưu trú, hồ sơ, chính sách và tài khoản.' },
  { value: 'KETOAN', label: 'Kế toán', description: 'Hóa đơn, thu tiền và báo cáo tài chính.' },
  { value: 'KYTHUAT', label: 'Kỹ thuật', description: 'Sửa chữa, chỉ số điện nước và vật tư.' },
  { value: 'BAOVE', label: 'Bảo vệ', description: 'Kiểm soát ra vào và khách thăm.' },
]
export function roleLabel(role: Role) { return accountRoles.find((item) => item.value === role)!.label }
export function searchable(value: string) {
  return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/đ/g, 'd').replace(/Đ/g, 'D').toLowerCase().trim()
}
export function filterAccounts(accounts: readonly Account[], query: string, role: string, status: string) {
  const terms = searchable(query).split(/\s+/).filter(Boolean)
  return accounts.filter((account) => (!role || account.role === role) && (!status || account.status === status) &&
    terms.every((term) => searchable(`${account.name} ${account.username} ${account.email}`).includes(term)))
}
export function formatAccountDate(value: string | null) {
  return value ? new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value)) : 'Chưa đăng nhập'
}
