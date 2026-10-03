import { useState } from 'react'
import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { Icon } from '@/shared/components/Icon'
import { TextField } from '@/shared/components/TextField'
import { DataTable, Feedback, SelectField, Status, Summary, WorkflowPage } from '@/shared/workflow/Workflow'
import { dateTime, searchText, type WorkflowOptions } from '@/shared/workflow/model'
import { useClock } from '@/shared/workflow/hooks'
import { RepairActionDialog } from './RepairActionDialog'
import { RepairDetail } from './RepairDetail'
import { editableIssue, priorityLabels, priorityOrder, statusLabels, type RepairData, type RepairMode } from './model'

export function MaintenancePage({ initialData, actor = { id: 0, name: '' }, ...options }: WorkflowOptions & { initialData?: RepairData; actor?: { id: number; name: string } }) {
  const [data, setData] = useState<RepairData>(() => initialData ?? { repairs: [], supplies: [], purchases: [] })
  const [query, setQuery] = useState(''), [status, setStatus] = useState(''), [priority, setPriority] = useState(''), [owner, setOwner] = useState(''), [sort, setSort] = useState('priority')
  const [dialog, setDialog] = useState<{ id: number; mode: 'detail' | RepairMode } | null>(null), [stockOpen, setStockOpen] = useState(false), [notice, setNotice] = useState('')
  const now = useClock()
  const shown = data.repairs.filter(r => (!status || r.status === status) && (!priority || r.priority === priority) && (!owner || (owner === 'mine' ? r.assignedTo === actor.id : !r.assignedTo)) && searchText(`${r.code} ${r.title} ${r.room} ${r.student}`).includes(searchText(query))).sort((a, b) => (sort === 'priority' ? priorityOrder[a.priority] - priorityOrder[b.priority] : 0) || Date.parse(a.createdAt) - Date.parse(b.createdAt))
  const selected = data.repairs.find(r => r.id === dialog?.id)
  function resetFilters() { setQuery(''); setStatus(''); setPriority(''); setOwner(''); setSort('priority') }
  function open(id: number, mode: 'detail' | RepairMode) { setDialog({ id, mode }); setNotice('') }
  return <WorkflowPage title="Công việc sửa chữa" description="Tiếp nhận phiếu, theo dõi lịch xử lý và ghi nhận vật tư sử dụng." view={options.view} actions={<Button variant="outlined" onClick={() => setStockOpen(true)}><Icon name="box" />Xem tồn vật tư</Button>}>
    <Feedback notice={notice} />
    <Summary items={[
      { label: 'Chưa tiếp nhận', tone: 'warning', value: data.repairs.filter(r => r.status === 'MOI').length },
      { label: 'Tôi đang xử lý', tone: 'primary', value: data.repairs.filter(r => r.status === 'DANG_XU_LY' && r.assignedTo === actor.id).length },
      { label: 'Đã hoàn thành', tone: 'success', value: data.repairs.filter(r => r.status === 'HOAN_THANH').length },
    ]} />
    <section className="wl-card repair-list" aria-labelledby="repair-list-heading">
      <div className="wl-card-heading"><div><h2 id="repair-list-heading">Danh sách phiếu sửa chữa</h2><p className="repair-muted">Ưu tiên sự cố khẩn cấp, sau đó phiếu được báo sớm hơn.</p></div><Status tone="primary">{shown.length} phiếu</Status></div>
      <div className="wl-toolbar repair-filters">
        <TextField label="Tìm phiếu sửa chữa" type="search" placeholder="Mã phiếu, phòng, sự cố hoặc người báo" value={query} onChange={event => setQuery(event.target.value)} trailing={<Icon name="search" />} />
        <SelectField label="Trạng thái" value={status} onChange={event => setStatus(event.target.value)}><option value="">Tất cả trạng thái</option>{Object.entries(statusLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</SelectField>
        <SelectField label="Mức ưu tiên" value={priority} onChange={event => setPriority(event.target.value)}><option value="">Tất cả mức ưu tiên</option>{Object.entries(priorityLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</SelectField>
        <SelectField label="Người phụ trách" value={owner} onChange={event => setOwner(event.target.value)}><option value="">Tất cả người phụ trách</option><option value="mine">Phiếu của tôi</option><option value="unassigned">Chưa có người nhận</option></SelectField>
        <SelectField label="Sắp xếp" value={sort} onChange={event => setSort(event.target.value)}><option value="priority">Ưu tiên cao trước</option><option value="oldest">Báo sớm trước</option></SelectField>
      </div>
      {(query || status || priority || owner || sort !== 'priority') && <div className="repair-filter-reset"><Button variant="text" onClick={resetFilters}>Xóa bộ lọc</Button></div>}
      <p className="repair-scroll-hint">Cuộn ngang để xem đủ thông tin phiếu và thao tác.</p>
      <DataTable key={`${query}-${status}-${priority}-${owner}-${sort}`} label="Phiếu sửa chữa" head={<tr><th scope="col">Phiếu / phòng</th><th scope="col">Sự cố</th><th scope="col">Ưu tiên</th><th scope="col">Trạng thái</th><th scope="col">Phụ trách / lịch hẹn</th><th scope="col">Thao tác</th></tr>} rows={shown.map(repair => <tr key={repair.id}>
        <td><button className="wl-link-button" onClick={() => open(repair.id, 'detail')} aria-label={`Xem phiếu ${repair.code}`}>{repair.code}</button><small>Phòng {repair.room}</small></td>
        <td className="repair-title-cell"><strong>{repair.title}</strong><small>{repair.category} · {dateTime(repair.createdAt)}</small></td>
        <td><Status tone={repair.priority === 'KHAN_CAP' ? 'danger' : repair.priority === 'CAO' ? 'warning' : 'neutral'}>{priorityLabels[repair.priority]}</Status></td>
        <td><Status tone={repair.status === 'HOAN_THANH' ? 'success' : repair.status === 'DANG_XU_LY' ? 'primary' : repair.status === 'MOI' ? 'warning' : 'neutral'}>{statusLabels[repair.status]}</Status>{repair.warranty && <small>Chờ bảo hành ngoài</small>}</td>
        <td><span>{repair.assignedTo === actor.id ? 'Tôi' : repair.technician ?? 'Chưa có'}</span><small>{repair.schedule ? dateTime(repair.schedule) : 'Chưa có lịch'}</small>{repair.status === 'DANG_XU_LY' && repair.schedule && Date.parse(repair.schedule) < now && <small className="repair-late">Lịch hẹn đã qua</small>}</td>
        <td><Button variant="text" onClick={() => open(repair.id, repair.status === 'MOI' ? 'accept' : 'detail')}>{repair.status === 'MOI' ? 'Tiếp nhận' : 'Chi tiết'}</Button></td>
      </tr>)} />
    </section>
    {selected && dialog?.mode === 'detail' && <Dialog title="Chi tiết phiếu sửa chữa" panel onClose={() => setDialog(null)} actions={<>
      {selected.status === 'MOI' && <Button onClick={() => open(selected.id, 'accept')}>Tiếp nhận phiếu</Button>}
      {selected.status === 'DANG_XU_LY' && selected.assignedTo === actor.id && <><Button variant="text" className="account-danger-text" onClick={() => open(selected.id, 'cancel')}>Hủy phiếu</Button><Button variant="outlined" onClick={() => open(selected.id, 'schedule')}>Hẹn lại</Button><Button variant="outlined" onClick={() => open(selected.id, 'warranty')}>Bảo hành ngoài</Button><Button onClick={() => open(selected.id, 'complete')}>Hoàn thành</Button></>}
      {(selected.status === 'HOAN_THANH' || selected.status === 'HUY') && <Button variant="text" onClick={() => setDialog(null)}>Đóng</Button>}
    </>}><RepairDetail repair={selected} supplies={data.supplies} purchaseCount={data.purchases.filter(p => p.repairId === selected.id).length} />{selected.status === 'DANG_XU_LY' && selected.assignedTo !== actor.id && <p className="wl-note">{editableIssue(selected, actor.id, 'complete')}</p>}</Dialog>}
    {selected && dialog && dialog.mode !== 'detail' && <RepairActionDialog key={`${selected.id}-${dialog.mode}`} data={data} id={selected.id} mode={dialog.mode} actor={actor} options={options} onClose={() => setDialog({ id: selected.id, mode: 'detail' })} onSave={(next, message, close) => { setData(next); setNotice(`${message} (Chỉ trong bản xem trước.)`); if (close) setDialog(null) }} />}
    {stockOpen && <Dialog title="Tồn vật tư sửa chữa" panel onClose={() => setStockOpen(false)} actions={<Button variant="text" onClick={() => setStockOpen(false)}>Đóng</Button>}><p className="repair-dialog-intro">Tra cứu tồn kho để lập nội dung sửa chữa. Tồn hiển thị không giữ chỗ vật tư.</p><ul className="repair-stock-list">{data.supplies.map(s => <li key={s.id}><div><strong>{s.name}</strong><small>{s.id}</small></div><Status tone={s.stock === 0 ? 'danger' : 'neutral'}>{s.stock} {s.unit}</Status></li>)}</ul>{!data.supplies.length && <p className="wl-note">Chưa có dữ liệu vật tư.</p>}<h3>Đề nghị mua bổ sung</h3>{data.purchases.length ? <ul className="repair-stock-list">{data.purchases.map(p => <li key={`${p.key}-${p.supplyId}`}><div><strong>{data.supplies.find(s => s.id === p.supplyId)?.name ?? p.supplyId}</strong><small>{data.repairs.find(r => r.id === p.repairId)?.code} · {dateTime(p.at)}</small></div><span>{p.quantity} {data.supplies.find(s => s.id === p.supplyId)?.unit} · Chờ mua</span></li>)}</ul> : <p className="repair-muted">Chưa có đề nghị mua bổ sung.</p>}</Dialog>}
  </WorkflowPage>
}
