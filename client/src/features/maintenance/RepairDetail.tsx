import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { Facts, Status } from '@/shared/workflow/Workflow'
import { priorityLabels, statusLabels, type Repair } from './model'
export function RepairDetail({ repair, onClose }: { repair: Repair; onClose: () => void }) {
  return <Dialog title={`Phiếu sửa chữa SC-${repair.id}`} panel onClose={onClose} actions={<Button variant="outlined" onClick={onClose}>Đóng</Button>}><div className="repair-detail"><h3>{repair.title}</h3><Status tone={repair.status==='HOAN_THANH'?'success':'primary'}>{statusLabels[repair.status]}</Status><Facts items={[["Phòng",repair.roomId],["Sinh viên",repair.studentId],["Ưu tiên",priorityLabels[repair.priority]]]} /><p className="wl-note">Mô tả, lịch hẹn và lịch sử xử lý chi tiết chưa được hệ thống cung cấp trong phần tra cứu.</p></div></Dialog>
}
