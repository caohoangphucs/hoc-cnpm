import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { Facts, Status, WorkflowPage } from '@/shared/workflow/Workflow'
import { useResource } from '@/shared/api/hooks'
import { findStudent } from '@/shared/data/students'
import { parseRepair, priorityLabels, statusLabels, type Repair } from './model'
export function RepairDetail({ repair, onClose }: { repair: Repair; onClose: () => void }) {
  const resource = useResource(`repairs/${repair.id}`, parseRepair)
  if (!resource.data) return <Dialog title="Chi tiết phiếu sửa chữa" onClose={onClose}><WorkflowPage title="Thông tin phiếu" description="Đang lấy thông tin mới nhất." view={resource.loading ? 'loading' : 'error'} error={resource.error} onRetry={resource.reload} /></Dialog>
  repair = resource.data
  const student = findStudent(repair.studentId)
  return <Dialog title={`Phiếu sửa chữa SC-${repair.id}`} panel onClose={onClose} actions={<Button variant="outlined" onClick={onClose}>Đóng</Button>}><div className="repair-detail"><h3>{repair.title}</h3><Status tone={repair.status==='HOAN_THANH'?'success':'primary'}>{statusLabels[repair.status]}</Status><Facts items={[["Phòng",repair.roomId],["Sinh viên",student ? `${student.fullName} (${repair.studentId})` : repair.studentId],["Ưu tiên",priorityLabels[repair.priority]]]} /><p className="wl-note">Mô tả, lịch hẹn và lịch sử xử lý chi tiết chưa được hệ thống cung cấp trong phần tra cứu.</p></div></Dialog>
}
