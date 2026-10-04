import { useState } from 'react'
import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { useApiAction, useResource } from '@/shared/api/hooks'
import { command } from '@/shared/api/protocol'
import { DataTable, Facts, Feedback, NotesField, SelectField, ServerPagination, Status, WorkflowPage } from '@/shared/workflow/Workflow'
import { ConfirmAction } from '@/shared/workflow/ConfirmAction'
import { dateTime } from '@/shared/workflow/model'
import { showSystemFeedback } from '@/shared/components/feedback'
import { parseVisitor, parseVisitors, type Visitor } from './model'
const labels: Record<string, string> = { PENDING: 'Chờ duyệt', APPROVED: 'Đã duyệt', REJECTED: 'Từ chối' }
export function VisitorList({ admin = false, security = false, revision = 0, onSelect }: { admin?: boolean; security?: boolean; revision?: number; onSelect?: (id: number) => void }) {
  const [page, setPage] = useState(1), [detail, setDetail] = useState<number | null>(null), [deleting, setDeleting] = useState<Visitor | null>(null), [notice, setNotice] = useState('')
  const resource = useResource(`visitors?page=${page}&size=8`, parseVisitors, revision), rows = resource.data ?? []
  return <><WorkflowPage title={admin ? 'Duyệt khách thăm' : 'Phiếu khách thăm'} description={security ? 'Các phiếu đã được duyệt trong phạm vi kiểm soát của bạn.' : 'Theo dõi khai báo và khoảng thời gian thăm được phép.'} view={resource.loading ? 'loading' : resource.error ? 'error' : 'data'} error={resource.error} onRetry={resource.reload} actions={<Button variant="outlined" onClick={resource.reload}>Tải lại</Button>}><Feedback notice={notice} /><section className="wl-card"><DataTable pagination={false} label="Danh sách khách thăm" head={<tr><th>Phiếu</th><th>MSSV tiếp đón</th><th>Thời gian thăm</th><th>Trạng thái</th><th>Thao tác</th></tr>} rows={rows.map(v => <tr key={v.id} className="clickable-row" onClick={() => setDetail(v.id)} tabIndex={0} onKeyDown={e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); setDetail(v.id) } }} title="Nhấp vào dòng để xem chi tiết phiếu khách thăm"><td><strong>KT-{v.id}</strong></td><td>{v.hostStudentId}</td><td>{dateTime(v.validFrom)}<small>Đến {dateTime(v.validTo)}</small></td><td><Status tone={v.state === 'APPROVED' ? 'success' : v.state === 'PENDING' ? 'warning' : 'neutral'}>{labels[v.state] || v.state}</Status></td><td onClick={e => e.stopPropagation()}><Button variant="text" onClick={() => setDetail(v.id)}>Chi tiết</Button>{security && onSelect && v.state === 'APPROVED' && <Button variant="outlined" onClick={() => onSelect(v.id)}>Chọn phiếu KT-{v.id}</Button>}{!security && v.state === 'PENDING' && <Button variant="text" onClick={() => setDeleting(v)}>Hủy khai báo</Button>}</td></tr>)} /><ServerPagination page={page} count={rows.length} onPage={setPage} /></section><p className="wl-note">Thông tin giấy tờ và họ tên khách được bảo vệ; tra cứu hiện chỉ cung cấp mã phiếu, MSSV và thời gian thăm.</p></WorkflowPage>{detail && <VisitorDetail key={detail} id={detail} admin={admin} security={security} onClose={() => setDetail(null)} onSave={() => { resource.reload(); setNotice('Đã lưu thao tác khách thăm.') }} />}{deleting && <ConfirmAction title="Hủy khai báo khách?" description="Chỉ khai báo đang chờ duyệt mới có thể hủy." path={`visitors/${deleting.id}`} onClose={() => setDeleting(null)} onSave={() => { setDeleting(null); resource.reload(); setNotice('Đã hủy khai báo.'); showSystemFeedback('Đã hủy phiếu khai báo khách thành công.') }} />}</>
}
function VisitorDetail({ id, admin, security, onClose, onSave }: { id: number; admin: boolean; security: boolean; onClose: () => void; onSave: () => void }) {
  const resource = useResource(`visitors/${id}`, parseVisitor), action = useApiAction(), [decision, setDecision] = useState(''), [reason, setReason] = useState(''), [verified, setVerified] = useState(false)
  const visitor = resource.data
  async function act(direction?: 'check-in' | 'check-out') {
    if (!visitor) return
    if (direction && !verified) { action.setError('Đối chiếu giấy tờ gốc và phiếu đã duyệt trước khi ghi lượt.'); return }
    if (!direction && (!decision || decision === 'TU_CHOI' && !reason.trim())) { action.setError('Chọn kết quả và nhập lý do từ chối.'); return }
    if (await action.run(`visitors/${id}/${direction || 'approval'}`, direction ? undefined : { decision, reason: reason.trim() || null, version: visitor.version }, command)) {
      onSave()
      setVerified(false)
      onClose()
      showSystemFeedback(
        direction
          ? (direction === 'check-in' ? `Đã ghi nhận lượt vào cho phiếu KT-${id} thành công!` : `Đã ghi nhận lượt ra cho phiếu KT-${id} thành công!`)
          : (decision === 'DUYET' ? `Đã phê duyệt phiếu khách KT-${id} thành công!` : `Đã từ chối phiếu khách KT-${id}.`),
        decision === 'TU_CHOI' ? 'warning' : 'success'
      )
    }
  }
  return <Dialog title={`Phiếu khách KT-${id}`} panel onClose={onClose} busy={action.busy} actions={<><Button variant="text" disabled={action.busy} onClick={onClose}>Đóng</Button>{admin && visitor?.state === 'PENDING' && <Button disabled={!decision} pending={action.busy} variant={decision === 'DUYET' ? 'approve' : decision === 'TU_CHOI' ? 'reject' : 'filled'} onClick={() => void act()}>Lưu xét duyệt</Button>}{security && visitor?.state === 'APPROVED' && <><Button variant="outlined" disabled={!verified || action.busy} onClick={() => void act('check-out')}>Ghi lượt ra</Button><Button disabled={!verified} pending={action.busy} onClick={() => void act('check-in')}>Ghi lượt vào</Button></>}</>}><WorkflowPage title="Chi tiết khai báo" description="Kiểm tra phiếu hiện tại trước khi thực hiện." view={resource.loading ? 'loading' : resource.error ? 'error' : 'data'} error={resource.error} onRetry={() => { resource.reload(); setVerified(false) }}>{visitor && <><Facts items={[[ 'Mã khai báo khách', `KT-${visitor.id}` ], [ 'MSSV tiếp đón', visitor.hostStudentId ], ['Bắt đầu', dateTime(visitor.validFrom) ], ['Kết thúc', dateTime(visitor.validTo) ], ['Trạng thái', labels[visitor.state] || visitor.state ]]} />{admin && visitor.state === 'PENDING' && <div className="wl-form"><SelectField label="Kết quả xét duyệt" disabled={action.busy} value={decision} onChange={e => setDecision(e.target.value)}><option value="">Chọn kết quả</option><option value="DUYET">Duyệt</option><option value="TU_CHOI">Từ chối</option></SelectField><NotesField label="Lý do / ghi chú" disabled={action.busy} value={reason} onChange={e => setReason(e.target.value)} required={decision === 'TU_CHOI'} /></div>}{security && visitor.state === 'APPROVED' && <label className="wl-check"><input type="checkbox" disabled={action.busy} checked={verified} onChange={e => setVerified(e.target.checked)} />Đã đối chiếu giấy tờ gốc và phiếu khai báo</label>}</>}</WorkflowPage><Feedback error={action.error} /></Dialog>
}
export function AdminVisitorsPage() { return <VisitorList admin /> }
