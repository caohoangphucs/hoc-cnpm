import { useState } from 'react'
import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { TextField } from '@/shared/components/TextField'
import { useApiAction, useResource } from '@/shared/api/hooks'
import { command } from '@/shared/api/protocol'
import { DataTable, Facts, Feedback, ServerPagination, Status, WorkflowPage } from '@/shared/workflow/Workflow'
import { ConfirmAction } from '@/shared/workflow/ConfirmAction'
import { money, searchText } from '@/shared/workflow/model'
import { showSystemFeedback } from '@/shared/components/feedback'
import { payableInvoice, paymentSelection } from '@/features/payments/api'
import { invoiceLabels, parseInvoice, parseInvoices, type Invoice } from './api'

export function InvoiceList({ manage = false, onSelect, onSelectMany }: { manage?: boolean; onSelect?: (invoice: Invoice) => void; onSelectMany?: (invoices: Invoice[]) => void }) {
  const [page, setPage] = useState(1), [search, setSearch] = useState(''), [detail, setDetail] = useState<number | null>(null), [deleting, setDeleting] = useState<Invoice | null>(null), [notice, setNotice] = useState('')
  const [selected, setSelected] = useState<Invoice[]>([]), [selectionError, setSelectionError] = useState('')
  function toggle(invoice: Invoice, checked: boolean) {
    setSelected(previous => checked ? [...previous.filter(i => i.id !== invoice.id), invoice] : previous.filter(i => i.id !== invoice.id))
    setSelectionError('')
  }
  function checkout() {
    try { paymentSelection(selected); onSelectMany?.(selected) }
    catch (error) { setSelectionError((error as Error).message) }
  }
  function reload() { setSelected([]); setSelectionError(''); resource.reload() }
  const resource = useResource(`invoices?page=${page}&pageSize=8`, parseInvoices), rows = resource.data?.items ?? []
  const shown = rows.filter(r => searchText(`${r.id} HĐ-${r.id} HD-${r.id} hd-${r.id} ${r.roomId} ${r.month}`).includes(searchText(search)))
  return <><WorkflowPage title={manage ? 'Danh sách hóa đơn' : 'Hóa đơn của tôi'} description="Các khoản phải thu, đã thu và còn lại do hệ thống xác nhận." view={resource.loading ? 'loading' : resource.error ? 'error' : 'data'} error={resource.error} onRetry={reload} actions={<Button variant="outlined" onClick={reload}>Tải lại</Button>}>
    <Feedback notice={notice} />
    {onSelectMany && <section className="wl-card wl-panel" aria-label="Hóa đơn đã chọn">
      <div className="payment-total" role="status"><span>Đã chọn {selected.length}/100 hóa đơn</span><strong>{money(selected.reduce((sum, invoice) => sum + BigInt(invoice.balance), 0n))}</strong></div>
      <p className="wl-note">Có thể chọn hóa đơn ở nhiều trang. Tải lại sẽ xóa lựa chọn để cập nhật số dư mới nhất.</p>
      {!!selected.length && <ul className="payment-allocations">{selected.map(invoice => <li key={invoice.id}><span>HĐ-{invoice.id}<small>{invoice.roomId} · {invoice.month} · Còn {money(invoice.balance)}</small></span><Button variant="text" aria-label={`Bỏ chọn hóa đơn HĐ-${invoice.id}`} onClick={() => toggle(invoice, false)}>Bỏ chọn</Button></li>)}</ul>}
      <Feedback error={selectionError} />
      <div className="wl-actions"><Button disabled={!selected.length || resource.loading || !!resource.error} onClick={checkout}>Thanh toán đã chọn</Button><Button variant="text" disabled={!selected.length} onClick={() => { setSelected([]); setSelectionError('') }}>Bỏ chọn tất cả</Button></div>
    </section>}
    <section className="wl-card"><div className="wl-toolbar"><TextField label="Tìm hóa đơn trên trang" placeholder="Mã hóa đơn (HĐ-...), phòng hoặc tháng" type="search" value={search} onChange={e => setSearch(e.target.value)} /></div><DataTable pagination={false} label="Danh sách hóa đơn" head={<tr>{onSelectMany && <th>Chọn</th>}<th>Hóa đơn / phòng</th><th>Tháng</th><th>Tổng tiền</th><th>Đã thu / còn lại</th><th>Hạn đóng</th><th>Trạng thái</th><th>Thao tác</th></tr>} rows={shown.map(r => <tr key={r.id} className="clickable-row" onClick={() => setDetail(r.id)} tabIndex={0} onKeyDown={e => { if (e.target === e.currentTarget && (e.key === 'Enter' || e.key === ' ')) { e.preventDefault(); setDetail(r.id) } }} title="Nhấp vào dòng để xem chi tiết hóa đơn">{onSelectMany && <td onClick={e => e.stopPropagation()}><label className="payment-invoice-select"><input type="checkbox" aria-label={`Chọn hóa đơn HĐ-${r.id}`} checked={selected.some(i => i.id === r.id)} disabled={!payableInvoice(r) || selected.length >= 100 && !selected.some(i => i.id === r.id)} onChange={e => toggle(r, e.target.checked)} /></label></td>}<td><strong>HĐ-{r.id}</strong><small>{r.roomId}</small></td><td>{r.month}</td><td>{money(r.total)}</td><td>{money(r.paid)}<small>Còn {money(r.balance)}</small></td><td>{r.dueDate.split('-').reverse().join('/')}</td><td><Status tone={r.status === 'DA_TT' ? 'success' : r.status === 'QUA_HAN' ? 'danger' : 'warning'}>{invoiceLabels[r.status] || r.status}</Status></td><td onClick={e => e.stopPropagation()}><Button variant="text" onClick={() => setDetail(r.id)}>Chi tiết</Button>{onSelect && payableInvoice(r) && <Button variant="text" onClick={() => onSelect(r)}>Thanh toán</Button>}{manage && r.status === 'CHUA_TT' && !r.paid && <Button variant="text" onClick={() => setDeleting(r)}>Xóa</Button>}</td></tr>)} /><ServerPagination page={page} count={rows.length} total={resource.data?.total} onPage={p => { setPage(p); setSearch('') }} /></section>
  </WorkflowPage>{detail && <InvoiceDetail key={detail} id={detail} manage={manage} onClose={() => setDetail(null)} onSave={() => { resource.reload(); setNotice('Đã cập nhật hạn thanh toán.'); showSystemFeedback('Đã cập nhật hạn thanh toán thành công.', 'success') }} />}{deleting && <ConfirmAction title={`Xóa hóa đơn HĐ-${deleting.id}?`} description="Chỉ hóa đơn chưa thu tiền và chưa có giao dịch mới có thể xóa. Kiểm tra lại trước khi xác nhận." path={`invoices/${deleting.id}`} onClose={() => setDeleting(null)} onSave={() => { setDeleting(null); resource.reload(); setNotice('Đã xóa hóa đơn.'); showSystemFeedback('Đã xóa hóa đơn.', 'success') }} />}</>
}
function InvoiceDetail({ id, manage, onClose, onSave }: { id: number; manage: boolean; onClose: () => void; onSave: () => void }) {
  const resource = useResource(`invoices/${id}`, parseInvoice), action = useApiAction(), [dueDate, setDueDate] = useState('')
  const r = resource.data
  async function save() { if (!dueDate) { action.setError('Chọn hạn thanh toán mới.'); return } if (await action.run(`invoices/${id}`, { dueDate }, command, 'PUT')) { onSave(); resource.reload(); setDueDate('') } }
  return <Dialog title={`Hóa đơn HĐ-${id}`} panel onClose={onClose} busy={action.busy} actions={<><Button variant="text" onClick={onClose} disabled={action.busy}>Đóng</Button>{manage && r && !r.paid && ['CHUA_TT', 'QUA_HAN'].includes(r.status) && <Button disabled={!dueDate} pending={action.busy} onClick={() => void save()}>Đổi hạn thanh toán</Button>}</>}><WorkflowPage title="Chi tiết hóa đơn" description="Các dòng tiền được tính và lưu trên hệ thống." view={resource.loading ? 'loading' : resource.error ? 'error' : 'data'} error={resource.error} onRetry={resource.reload}>{r && <><Facts items={[[ 'Phòng', r.roomId ], ['Tháng', r.month ], ['Tổng tiền', money(r.total) ], ['Giảm trừ', money(r.discount) ], ['Đã thu', money(r.paid) ], ['Còn lại', money(r.balance) ], ['Hạn thanh toán', r.dueDate.split('-').reverse().join('/') ], ['Trạng thái', invoiceLabels[r.status] || r.status ]]} /><DataTable label="Chi tiết khoản thu" pagination={false} head={<tr><th>Khoản thu</th><th>Số lượng</th><th>Đơn giá</th><th>Thành tiền</th></tr>} rows={r.lines.map((l,i) => <tr key={i}><td>{l.name}</td><td>{l.quantity}</td><td>{money(l.unitPrice)}</td><td>{money(l.total)}</td></tr>)} />{manage && !r.paid && ['CHUA_TT', 'QUA_HAN'].includes(r.status) && <TextField label="Hạn thanh toán mới" type="date" value={dueDate} disabled={action.busy} onChange={e => setDueDate(e.target.value)} />}</>}</WorkflowPage><Feedback error={action.error} /></Dialog>
}
