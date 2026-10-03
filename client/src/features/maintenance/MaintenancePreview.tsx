import { PreviewWorkflow } from '@/shared/workflow/Workflow'
import { useState } from 'react'
import type { WorkflowOptions } from '@/shared/workflow/model'
import { MaintenancePage } from './MaintenancePage'
import type { Repair, RepairData } from './model'

const actor = { id: 501, name: 'Trần Minh Khoa (minh họa)' }
function fixtures(): RepairData {
  const now = Date.now(), at = (hours: number) => new Date(now + hours * 3600000).toISOString()
  const repair = (id: number, patch: Partial<Repair>): Repair => ({ id, code: `SC-DEMO-${id}`, room: 'A101', student: 'Nguyễn Minh An (minh họa)', title: '', description: '', category: 'Thiết bị', priority: 'TRUNG_BINH', status: 'MOI', version: 1, createdAt: at(-24), usedSupplies: [], history: [{ at: at(-24), actor: 'Sinh viên', note: 'Gửi yêu cầu sửa chữa.' }], ...patch })
  return { supplies: [
    { id: 'VT-DEMO-01', name: 'Bóng đèn LED 18W', unit: 'cái', stock: 8 },
    { id: 'VT-DEMO-02', name: 'Vòi nước inox', unit: 'cái', stock: 1 },
    { id: 'VT-DEMO-03', name: 'Ổ cắm điện đôi', unit: 'cái', stock: 0 },
    { id: 'VT-DEMO-04', name: 'Dây cấp nước 60cm', unit: 'sợi', stock: 4 },
    { id: 'VT-DEMO-05', name: 'Bản lề cửa', unit: 'cái', stock: 12 },
  ], purchases: [], repairs: [
    repair(221, { title: 'Ổ cắm phát tia lửa', description: 'Ổ cắm gần bàn học phát tia lửa khi cắm sạc. Sinh viên đã ngắt sử dụng và báo Ban quản lý.', category: 'Điện', priority: 'KHAN_CAP' }),
    repair(222, { room: 'A102', title: 'Vòi nước rò rỉ liên tục', description: 'Hai vòi nước trong khu vệ sinh rò nước kể cả khi đã đóng. Cần kiểm tra và thay thế nếu cần.', category: 'Nước', priority: 'CAO', status: 'DANG_XU_LY', assignedTo: actor.id, technician: actor.name, schedule: at(-1), history: [{ at: at(-24), actor: 'Sinh viên', note: 'Gửi yêu cầu sửa chữa.' }, { at: at(-5), actor: actor.name, note: 'Tiếp nhận phiếu, lên lịch kiểm tra đường nước.' }] }),
    repair(223, { room: 'B201', title: 'Quạt trần không hoạt động', description: 'Quạt không khởi động sau khi bật công tắc. Đã chuyển thiết bị cho đơn vị bảo hành kiểm tra.', category: 'Điện', status: 'DANG_XU_LY', assignedTo: actor.id, technician: actor.name, schedule: at(48), warranty: { provider: 'Trung tâm bảo hành An Phát (minh họa)', expectedAt: at(48) }, history: [{ at: at(-48), actor: 'Sinh viên', note: 'Gửi yêu cầu sửa chữa.' }, { at: at(-24), actor: actor.name, note: 'Ghi nhận bảo hành ngoài, dự kiến bàn giao sau 2 ngày.' }] }),
    repair(224, { room: 'A201', title: 'Đèn khu vực bàn học bị hỏng', description: 'Đèn nhấp nháy và không đủ sáng, cần kiểm tra bóng đèn.', category: 'Điện', status: 'DANG_XU_LY', assignedTo: 502, technician: 'Lê Hoàng Nam (minh họa)', schedule: at(3) }),
    repair(225, { room: 'B102', title: 'Cửa tủ bị lệch bản lề', description: 'Cửa tủ khó đóng, cần chỉnh lại bản lề.', priority: 'THAP', status: 'DANG_XU_LY', assignedTo: actor.id, technician: actor.name, schedule: at(5) }),
    repair(226, { room: 'A103', title: 'Thay bóng đèn hành lang', description: 'Đèn trước cửa phòng không sáng.', category: 'Điện', status: 'HOAN_THANH', assignedTo: actor.id, technician: actor.name, completedAt: at(-3), note: 'Đã thay bóng đèn LED và kiểm tra hoạt động.', usedSupplies: [{ supplyId: 'VT-DEMO-01', quantity: 1 }], history: [{ at: at(-24), actor: 'Sinh viên', note: 'Gửi yêu cầu sửa chữa.' }, { at: at(-3), actor: actor.name, note: 'Hoàn thành: thay bóng đèn, kiểm tra hoạt động.' }] }),
    repair(227, { room: 'B101', title: 'Kiểm tra khóa cửa', description: 'Khóa cửa khó xoay chìa.', status: 'HUY', note: 'Sinh viên báo đã xử lý được; hủy theo yêu cầu.', history: [{ at: at(-24), actor: 'Sinh viên', note: 'Gửi yêu cầu sửa chữa.' }, { at: at(-2), actor: actor.name, note: 'Hủy theo yêu cầu sinh viên, đã xử lý được khóa.' }] }),
    repair(228, { room: 'A202', title: 'Ống thoát nước chậm', description: 'Nước thoát chậm tại bồn rửa, cần kiểm tra đường ống.', category: 'Nước' }),
    repair(229, { room: 'B202', title: 'Tay nắm cửa lỏng', description: 'Tay nắm cửa cần siết lại ốc.', priority: 'THAP' }),
  ] }
}
export default function MaintenancePreview() {
  return <PreviewWorkflow title="UC-22 · Xử lý sửa chữa">{options => <PreviewMaintenance {...options} />}</PreviewWorkflow>
}
function PreviewMaintenance(options: WorkflowOptions) {
  const [data] = useState(fixtures)
  return <MaintenancePage {...options} actor={actor} initialData={data} />
}
