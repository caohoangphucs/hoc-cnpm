import { useState } from 'react'
import { Button } from '@/shared/components/Button'
import { TextField } from '@/shared/components/TextField'
import { Icon } from '@/shared/components/Icon'
import { DataTable, Feedback, SelectField, ServerPagination, Status, Summary, WorkflowPage } from '@/shared/workflow/Workflow'
import { searchText } from '@/shared/workflow/model'
import { useResource } from '@/shared/api/hooks'
import { useAuth } from '@/features/auth/context'
import { parseRepairs, priorityLabels, priorityOrder, statusLabels, type Repair } from './model'
import { RepairActionDialog, ReportRepairDialog } from './RepairActionDialog'
import { RepairDetail } from './RepairDetail'
import { ConfirmAction } from '@/shared/workflow/ConfirmAction'
import { AssignRepairDialog } from './AssignRepairDialog'
import { showSystemFeedback } from '@/shared/components/feedback'

export function MaintenancePage({ student = false }: { student?: boolean }) {
  const { state } = useAuth(), technician = state.status === 'ready' && state.session.authenticated && state.session.role === 'KYTHUAT'
  const admin = state.status === 'ready' && state.session.authenticated && state.session.role === 'ADMIN'
  const [assigning, setAssigning] = useState<Repair | null>(null), [deleting, setDeleting] = useState<Repair | null>(null)
  const [page, setPage] = useState(1), [query, setQuery] = useState(''), [status, setStatus] = useState(''), [priority, setPriority] = useState('')
  const resource = useResource(`repairs?page=${page}&size=8`, parseRepairs)
  const [detail, setDetail] = useState<Repair | null>(null), [editing, setEditing] = useState<{ repair: Repair; mode: 'schedule' | 'complete' } | null>(null), [creating, setCreating] = useState(false), [notice, setNotice] = useState('')
  const rows = resource.data ?? []
  const shown = rows.filter(r => (!status || r.status === status) && (!priority || r.priority === priority) && searchText(`${r.id} ${r.roomId} ${r.studentId} ${r.title}`).includes(searchText(query))).sort((a,b) => priorityOrder[a.priority] - priorityOrder[b.priority])
  return <><WorkflowPage title={student ? 'Yêu cầu sửa chữa' : admin ? 'Phân công sửa chữa' : 'Công việc sửa chữa'} description={student ? 'Gửi sự cố trong phòng và theo dõi trạng thái xử lý.' : admin ? 'Theo dõi phiếu và giao công việc cho kỹ thuật viên.' : 'Theo dõi công việc được giao, cập nhật lịch và hoàn thành sửa chữa.'} view={resource.loading ? 'loading' : resource.error ? 'error' : 'data'} error={resource.error} onRetry={resource.reload} actions={<div className="wl-actions">{student && <Button onClick={() => setCreating(true)}><Icon name="tool" />Báo sự cố</Button>}<Button variant="outlined" onClick={resource.reload}><Icon name="refresh" />Tải lại</Button></div>}>
    <Feedback notice={notice} />
    <Summary items={[{ label: 'Phiếu trên trang', value: rows.length }, { label: 'Đang xử lý trên trang', tone: 'warning', value: rows.filter(r=>r.status==='DANG_XU_LY').length }, { label: 'Hoàn thành trên trang', tone: 'success', value: rows.filter(r=>r.status==='HOAN_THANH').length }]} />
    <section className="wl-card repair-list"><div className="wl-card-heading"><h2>{student ? 'Phiếu của bạn' : 'Danh sách công việc'}</h2><p>{technician ? 'Các phiếu được phân công cho bạn' : 'Theo phạm vi tài khoản được phép xem'}</p></div><div className="wl-toolbar"><TextField label="Tìm trên trang hiện tại" type="search" value={query} onChange={e=>setQuery(e.target.value)} placeholder="Mã phiếu, phòng, MSSV hoặc sự cố" /><SelectField label="Trạng thái" value={status} onChange={e=>setStatus(e.target.value)}><option value="">Tất cả</option>{Object.entries(statusLabels).map(([v,l])=><option key={v} value={v}>{l}</option>)}</SelectField><SelectField label="Ưu tiên" value={priority} onChange={e=>setPriority(e.target.value)}><option value="">Tất cả</option>{Object.entries(priorityLabels).map(([v,l])=><option key={v} value={v}>{l}</option>)}</SelectField></div>
    <DataTable pagination={false} label="Phiếu sửa chữa" head={<tr><th>Mã phiếu / phòng</th><th>Sự cố</th><th>Sinh viên</th><th>Ưu tiên</th><th>Trạng thái</th><th>Thao tác</th></tr>} rows={shown.map(r=><tr key={r.id} className="clickable-row" onClick={()=>setDetail(r)} tabIndex={0} onKeyDown={e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); setDetail(r) } }} title="Nhấp vào dòng để xem chi tiết phiếu sửa chữa"><td><strong>SC-{r.id}</strong><small>Phòng {r.roomId}</small></td><td>{r.title}</td><td>{r.studentId}</td><td><Status tone={r.priority==='KHAN_CAP'?'danger':r.priority==='CAO'?'warning':'neutral'}>{priorityLabels[r.priority]}</Status></td><td><Status tone={r.status==='HOAN_THANH'?'success':r.status==='DANG_XU_LY'?'primary':'neutral'}>{statusLabels[r.status]}</Status></td><td onClick={e=>e.stopPropagation()}><Button variant="text" onClick={()=>setDetail(r)}>Chi tiết</Button>{admin && r.status === 'MOI' && <Button variant="text" onClick={() => setAssigning(r)}>Phân công</Button>}{(student || admin) && r.status === 'MOI' && <Button variant="text" onClick={() => setDeleting(r)}>Hủy phiếu</Button>}{technician && r.status==='DANG_XU_LY' && <><Button variant="text" onClick={()=>setEditing({repair:r,mode:'schedule'})}>Đổi lịch</Button><Button variant="text" onClick={()=>setEditing({repair:r,mode:'complete'})}>Hoàn thành</Button></>}</td></tr>)} />
    <ServerPagination page={page} count={shown.length} onPage={p=>{setPage(p);setQuery('');setStatus('');setPriority('')}} /></section>
    <p className="wl-note">{student ? 'Đính kèm ảnh sẽ được bổ sung khi hệ thống hỗ trợ tải tệp.' : 'Danh mục tồn kho và lịch sử chi tiết chưa sẵn sàng. Khi hoàn thành, nhập mã vật tư thực tế đã dùng; hệ thống kiểm tra tồn và xuất kho.'}</p>
  </WorkflowPage>
  {detail && <RepairDetail repair={detail} onClose={()=>setDetail(null)} />}
  {editing && <RepairActionDialog key={`${editing.repair.id}:${editing.mode}`} {...editing} onClose={()=>setEditing(null)} onSave={message=>{setNotice(message);showSystemFeedback(message, 'success');setEditing(null);resource.reload()}} />}
  {creating && <ReportRepairDialog onClose={()=>setCreating(false)} onSave={()=>{setNotice('Đã gửi yêu cầu sửa chữa.');showSystemFeedback('Đã gửi yêu cầu sửa chữa thành công.', 'success');setCreating(false);resource.reload()}} />}
  {assigning && <AssignRepairDialog repair={assigning} onClose={() => setAssigning(null)} onSave={() => { setAssigning(null); setNotice('Đã phân công sửa chữa.'); showSystemFeedback('Đã phân công sửa chữa thành công.', 'success'); resource.reload() }} />}
  {deleting && <ConfirmAction title="Hủy phiếu sửa chữa?" description="Chỉ phiếu mới, chưa phân công và chưa có lịch sử xử lý mới có thể hủy." path={`repairs/${deleting.id}`} onClose={() => setDeleting(null)} onSave={() => { setDeleting(null); setNotice('Đã hủy phiếu sửa chữa.'); showSystemFeedback('Đã hủy phiếu sửa chữa.', 'success'); resource.reload() }} />}</>
}
export function StudentRepairsPage() { return <MaintenancePage student /> }
