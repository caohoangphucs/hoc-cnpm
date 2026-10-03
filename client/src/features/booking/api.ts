import { instant, integer, list, nullableText, object, paged, text } from '../../shared/api/protocol.ts'
export type Booking = { id: number; bedId: string; studentId: string; status: string; expiresAt: string; profileState: string; version: number; fileIds: string[]; reason: string | null }
export function parseBooking(value: unknown): Booking { const v = object(value); return { id: integer(v.id, 1), bedId: text(v.bedId), studentId: text(v.studentId), status: text(v.status), expiresAt: instant(v.expiresAt), profileState: text(v.profileState), version: integer(v.version, 1), fileIds: v.fileIds == null ? [] : list(v.fileIds, text), reason: nullableText(v.reason) } }
export const parseBookings = (value: unknown) => paged(value, parseBooking)
export const bookingLabels: Record<string, string> = { CHO_DUYET: 'Chờ duyệt', DA_DUYET: 'Đã duyệt', TU_CHOI: 'Từ chối', HET_HAN: 'Hết hạn', HUY: 'Đã hủy' }
export function validProfile(record: Pick<Booking, 'profileState'>) { return record.profileState === 'VALID' }
export const profileLabels: Record<string, string> = { VALID: 'Hồ sơ hợp lệ', PENDING: 'Chờ kiểm tra', MISSING: 'Thiếu minh chứng', INVALID: 'Hồ sơ chưa hợp lệ', COMPLETE: 'Đủ hồ sơ, cần kiểm tra tính hợp lệ' }
