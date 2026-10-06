import { invalidResponse, nullableText, object, text } from '../../shared/api/protocol.ts'

export type StudentProfile = {
  studentId: string
  name: string
  dateOfBirth: string | null
  gender: string
  email: string
  phone: string
  className: string | null
  faculty: string | null
  hometown: string | null
  roomId: string | null
  status: string
}

/** Decodes the fields returned by the self-only student profile endpoint. */
export function parseStudentProfile(value: unknown): StudentProfile {
  const profile = object(value)
  const dateOfBirth = nullableText(profile.dateOfBirth)
  if (dateOfBirth !== null && !/^\d{4}-\d{2}-\d{2}$/.test(dateOfBirth)) throw invalidResponse()
  return {
    studentId: text(profile.studentId),
    name: text(profile.name),
    dateOfBirth,
    gender: text(profile.gender),
    email: text(profile.email),
    phone: text(profile.phone),
    className: nullableText(profile.className),
    faculty: nullableText(profile.faculty),
    hometown: nullableText(profile.hometown),
    roomId: nullableText(profile.roomId),
    status: text(profile.status),
  }
}

export const studentStatusLabels: Record<string, string> = {
  DANG_O: 'Đang lưu trú',
  DA_ROI_KTX: 'Đã rời ký túc xá',
  CHO_DUYET: 'Chờ xét duyệt',
}
