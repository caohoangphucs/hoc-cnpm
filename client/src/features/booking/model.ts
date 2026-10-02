// UI models for UC-04. Extra display fields need mapping when the lookup APIs are available.
export type Gender = 'NAM' | 'NU' | 'TAT_CA'
export type Bed = { id: string; number: string; status: 'TRONG' | 'GIU_CHO' | 'DANG_O' | 'KHOA'; version: number }
export type Room = { id: string; name: string; buildingId: string; buildingName: string; roomType: string; gender: Gender; capacity: number; beds: readonly Bed[] }
export type Student = { name: string; code: string; gender: 'NAM' | 'NU' }
export type RegistrationWindow = { id: number; name: string; semester: string; opensAt: string; closesAt: string; open: boolean }
export type Eligibility = 'eligible' | 'pending' | 'resident'
export type BookingView = 'data' | 'loading' | 'empty' | 'error'
export type Documents = { identity: File | null; studentCard: File | null; evidence: readonly File[] }
export type DocumentErrors = Partial<Record<'identity' | 'studentCard' | 'evidence' | 'terms', string>>
export type BookingRequest = { bedId: string; windowId: number; documents: Documents; acceptedTerms: true }
export type BookingReceipt = { id: string; status: 'CHO_DUYET'; expiresAt: string; roomName: string; buildingName: string; bedNumber: string }
export type Submission = { ok: true; receipt: BookingReceipt } | { ok: false; kind: 'conflict' | 'unavailable' | 'validation'; message: string; fieldErrors?: DocumentErrors }

export const bedLabels: Record<Bed['status'], string> = { TRONG: 'Trống', GIU_CHO: 'Giữ chỗ', DANG_O: 'Đang ở', KHOA: 'Đã khóa' }
export const genderLabels: Record<Gender, string> = { NAM: 'Nam', NU: 'Nữ', TAT_CA: 'Nam / Nữ' }
export const maxFileBytes = 5 * 1024 * 1024
export function compatible(room: Room, student: Student) { return room.gender === 'TAT_CA' || room.gender === student.gender }
export function availableBeds(room: Room) { return room.beds.filter((bed) => bed.status === 'TRONG').length }
export function normalizeSearch(value: string) { return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[đĐ]/g, 'd').toLowerCase().trim() }
export function fileMetadataError(file: Pick<File, 'type' | 'size' | 'name'>): string | undefined {
  if (!['image/jpeg', 'image/png'].includes(file.type) || !/\.(jpe?g|png)$/i.test(file.name)) return 'Chỉ chọn ảnh JPG hoặc PNG.'
  if (file.size === 0) return 'Tệp ảnh đang trống. Vui lòng chọn lại.'
  if (file.size > maxFileBytes) return 'Mỗi ảnh tối đa 5 MB.'
}
export function validateDocuments(documents: Documents, accepted: boolean): DocumentErrors {
  const errors: DocumentErrors = {}
  if (!documents.identity) errors.identity = 'Vui lòng chọn ảnh CCCD.'
  if (!documents.studentCard) errors.studentCard = 'Vui lòng chọn ảnh thẻ sinh viên.'
  if (documents.evidence.length > 3) errors.evidence = 'Chọn tối đa 3 ảnh minh chứng bổ sung (tổng tối đa 5 ảnh).'
  if (!accepted) errors.terms = 'Bạn cần xác nhận cam kết trước khi gửi đăng ký.'
  return errors
}
export function formatBookingTime(value: string) { return new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short', timeStyle: 'short', timeZone: 'Asia/Ho_Chi_Minh' }).format(new Date(value)) }
