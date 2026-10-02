export type Registration = {
  id: string; student: string; studentCode: string; room: string; bed: string; submittedAt: string; expiresAt: string
  priority: number; profile: 'VALID' | 'MISSING'; verified: boolean; bedStatus: 'GIU_CHO' | 'KHOA' | 'DANG_O'
  status: 'CHO_DUYET' | 'DA_DUYET' | 'TU_CHOI' | 'HUY'; reason?: string; contract?: string
}
export const registrationLabels = { CHO_DUYET: 'Chờ duyệt', DA_DUYET: 'Đã duyệt', TU_CHOI: 'Từ chối', HUY: 'Đã hủy' }
export function approvalIssue(record: Registration, now = Date.now()) {
  if (record.status !== 'CHO_DUYET') return 'Đơn không còn ở trạng thái chờ duyệt.'
  if (Date.parse(record.expiresAt) <= now) return 'Đơn đã hết hạn giữ chỗ 48 giờ.'
  if (record.bedStatus !== 'GIU_CHO') return 'Giường đã khóa hoặc không còn được giữ cho đơn.'
  if (record.profile !== 'VALID') return 'Hồ sơ còn thiếu minh chứng bắt buộc.'
  if (!record.verified) return 'Cần kiểm tra và xác nhận nội dung hồ sơ trước khi duyệt.'
  return ''
}
