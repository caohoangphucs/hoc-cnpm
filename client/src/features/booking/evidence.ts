import { integer, list, object, text } from '../../shared/api/protocol.ts'
export const evidenceKinds = ['CCCD_FRONT', 'CCCD_BACK', 'STUDENT_CARD'] as const
export type EvidenceKind = typeof evidenceKinds[number]
export const evidenceLabels: Record<string, string> = { CCCD_FRONT: 'CCCD mặt trước', CCCD_BACK: 'CCCD mặt sau', STUDENT_CARD: 'Thẻ sinh viên', EVIDENCE: 'Bằng chứng', REPAIR: 'Ảnh sửa chữa', POLICY: 'Minh chứng chính sách' }
export type EvidenceFile = { id: string; kind: string; mime: string; sizeBytes: number; sha256: string }
export function parseEvidence(value: unknown): EvidenceFile {
  const v = object(value)
  return { id: text(v.id), kind: text(v.kind), mime: text(v.mime), sizeBytes: integer(v.sizeBytes, 1), sha256: text(v.sha256) }
}
export function parseWindows(value: unknown) {
  return list(value, row => { const v = object(row); return { id: integer(v.id, 1), name: text(v.name), semesterName: text(v.semesterName) } })
}
// Kiểm tra sớm để người dùng không gửi tệp sai loại/kích thước; backend vẫn kiểm tra nội dung thật.
export function uploadProblem(file: Pick<File, 'type' | 'size'>): string | null {
  if (!['image/png', 'image/jpeg'].includes(file.type)) return 'Chọn ảnh PNG hoặc JPEG.'
  if (file.size < 1) return 'Tệp ảnh đang trống.'
  if (file.size > 5 * 1024 * 1024) return 'Mỗi ảnh tối đa 5 MiB.'
  return null
}
