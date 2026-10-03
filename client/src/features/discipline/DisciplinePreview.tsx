import { PreviewWorkflow } from '@/shared/workflow/Workflow'
import { DisciplinePage } from './DisciplinePage'
import type { DisciplineData, Violation } from './model'
const at = (day: string) => Date.parse(`${day}T23:10:00+07:00`)
const row = (id: string, studentId: string, name: string, day: string, done = false): Violation => ({ id, studentId, name, room: 'A-203', at: at(day), source: 'Về trễ', detail: 'Nhật ký cổng chính ghi nhận sinh viên về lúc 23:10. Cần đối chiếu lý do và hồ sơ ca trực.', version: done ? 1 : 0, revisions: done ? [{ id: `${id}-R1`, at: at(day) + 12 * 3600000, decision: 'Nhắc nhở bằng văn bản', reason: 'Đã đối chiếu nhật ký và thông tin sinh viên.', evidence: 'bien-ban-demo.pdf (minh họa)', reviewer: 'Ban quản lý' }] : [], appeals: [] })
function sampleDiscipline(): DisciplineData {
  const appeal = row('VP-204', 'SV-DEMO-102', 'Trần Minh Anh', '2026-09-29', true)
  appeal.appeals = [{ id: 'KN-01', at: at('2026-09-30'), content: 'Em có lịch trực thực tập kết thúc muộn. Đề nghị Ban quản lý xem xét lại quyết định.', status: 'OPEN', version: 0 }]
  return { semesters: [{ id: 'HK1-26', name: 'HK1 · 2026–2027', from: at('2026-08-01') - 23 * 3600000 - 10 * 60000, to: Date.parse('2027-01-01T00:00:00+07:00') }, { id: 'HK2-25', name: 'HK2 · 2025–2026', from: Date.parse('2026-01-01T00:00:00+07:00'), to: Date.parse('2026-08-01T00:00:00+07:00') }], violations: [row('VP-205', 'SV-DEMO-103', 'Lê Quốc Huy', '2026-10-02'), row('VP-206', 'SV-DEMO-104', 'Phạm Ngọc Mai', '2026-10-01'), row('VP-207', 'SV-DEMO-101', 'Nguyễn Hoàng Nam', '2026-10-02'), appeal, row('VP-201', 'SV-DEMO-101', 'Nguyễn Hoàng Nam', '2026-09-05', true), row('VP-202', 'SV-DEMO-101', 'Nguyễn Hoàng Nam', '2026-09-15', true), row('VP-203', 'SV-DEMO-101', 'Nguyễn Hoàng Nam', '2026-09-25', true), row('VP-OLD', 'SV-DEMO-103', 'Lê Quốc Huy', '2026-07-21', true)] }
}
export default function DisciplinePreview() { return <PreviewWorkflow title="UC-14 · Xử lý kỷ luật">{options => <DisciplinePage {...options} initialData={sampleDiscipline()} />}</PreviewWorkflow> }
export function StudentDisciplinePreview() { return <PreviewWorkflow title="UC-14 · Quyết định & khiếu nại">{options => <DisciplinePage {...options} initialData={sampleDiscipline()} studentId="SV-DEMO-101" />}</PreviewWorkflow> }
