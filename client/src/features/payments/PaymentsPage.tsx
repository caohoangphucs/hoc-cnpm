import { useState } from 'react'
import { useSearchParams } from 'react-router'
import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { useApiAction, useResource } from '@/shared/api/hooks'
import { DataTable, Facts, Feedback, SelectField, ServerPagination, Status, WorkflowPage } from '@/shared/workflow/Workflow'
import { ConfirmAction } from '@/shared/workflow/ConfirmAction'
import { dateTime, money } from '@/shared/workflow/model'
import { InvoiceList } from '@/features/invoice-issuance/InvoiceList'
import type { Invoice } from '@/features/invoice-issuance/api'
import { parsePayment, parsePayments, paymentLabels, paymentSelection, type Payment } from './api'
import { useClock } from '@/shared/workflow/hooks'

export function PaymentsPage() {
  const [params] = useSearchParams()
  const returnedSession = params.get('paymentSession')
  const initialSession = returnedSession && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(returnedSession) ? returnedSession : null
  const [section, setSection] = useState(initialSession ? 'sessions' : 'invoices'), [invoices, setInvoices] = useState<Invoice[]>([]), [gateway, setGateway] = useState('VNPAY'), [receipt, setReceipt] = useState<string | null>(initialSession)
  const action = useApiAction()
  async function create() {
    let selection: ReturnType<typeof paymentSelection>
    try { selection = paymentSelection(invoices) } catch (error) { action.setError((error as Error).message); return }
    const result = await action.run('payment-sessions', { invoiceIds: selection.invoiceIds, gateway }, parsePayment)
    if (result) { setReceipt(result.id); setInvoices([]); setSection('sessions') }
  }
  return <><div className="wl-segments"><Button variant="outlined" disabled={action.busy} aria-pressed={section === 'invoices'} onClick={() => setSection('invoices')}>Hóa đơn của tôi</Button><Button variant="outlined" disabled={action.busy} aria-pressed={section === 'sessions'} onClick={() => setSection('sessions')}>Phiên thanh toán</Button></div>{section === 'invoices' ? <InvoiceList onSelect={r => { action.setError(''); setInvoices([r]) }} onSelectMany={rows => { action.setError(''); setInvoices(rows) }} /> : <PaymentSessions />}
    {!!invoices.length && <Dialog title={invoices.length === 1 ? `Thanh toán HĐ-${invoices[0].id}` : `Thanh toán gộp ${invoices.length} hóa đơn`} onClose={() => setInvoices([])} busy={action.busy} actions={<><Button variant="text" onClick={() => setInvoices([])} disabled={action.busy}>Quay lại</Button><Button pending={action.busy} onClick={() => void create()}>Tạo phiên thanh toán</Button></>}>
      <ul className="payment-allocations">{invoices.map(invoice => <li key={invoice.id}><span>HĐ-{invoice.id}<small>Phòng {invoice.roomId} · {invoice.month}</small></span><strong>{money(invoice.balance)}</strong></li>)}</ul>
      <div className="payment-total"><span>Tổng còn phải đóng</span><strong>{money(invoices.reduce((sum, invoice) => sum + BigInt(invoice.balance), 0n))}</strong></div>
      <SelectField label="Cổng thanh toán" value={gateway} onChange={e => setGateway(e.target.value)} disabled={action.busy}><option value="VNPAY">VNPAY</option></SelectField><p className="wl-note">Hệ thống kiểm tra lại số tiền và quyền thanh toán của từng hóa đơn trước khi tạo phiên. Chỉ mở cổng thanh toán sau khi nhận được liên kết hợp lệ.</p><Feedback error={action.error} />
    </Dialog>}
    {receipt && <PaymentDetail key={receipt} id={receipt} onClose={() => setReceipt(null)} />}
  </>
}
export function PaymentSessions() {
  const [page, setPage] = useState(1), [selected, setSelected] = useState<string | null>(null), [deleting, setDeleting] = useState<Payment | null>(null)
  const resource = useResource(`payment-sessions?page=${page}&pageSize=8`, parsePayments), rows = resource.data ?? []
  return <><WorkflowPage title="Phiên thanh toán" description="Kiểm tra kết quả xác nhận từ máy chủ sau khi thanh toán." view={resource.loading ? 'loading' : resource.error ? 'error' : 'data'} error={resource.error} onRetry={resource.reload} actions={<Button variant="outlined" onClick={resource.reload}>Tải lại trạng thái</Button>}><section className="wl-card"><DataTable pagination={false} label="Các phiên thanh toán" head={<tr><th>Phiên / cổng</th><th>Hóa đơn</th><th>Số tiền</th><th>Trạng thái</th><th>Hết hạn</th><th>Thao tác</th></tr>} rows={rows.map(p => <tr key={p.id} className="clickable-row" onClick={() => setSelected(p.id)} tabIndex={0} onKeyDown={e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); setSelected(p.id) } }} title="Nhấp vào dòng để kiểm tra chi tiết phiên"><td><strong>{p.gateway}</strong><small>{p.id}</small></td><td>{p.invoiceIds.map(id => `HĐ-${id}`).join(', ')}</td><td>{money(p.amount)}</td><td><Status tone={p.status === 'SUCCESS' ? 'success' : p.status === 'NEEDS_REVIEW' ? 'warning' : 'neutral'}>{paymentLabels[p.status] || p.status}</Status></td><td>{dateTime(p.expiresAt)}</td><td onClick={e => e.stopPropagation()}><Button variant="text" onClick={() => setSelected(p.id)}>Kiểm tra</Button>{p.status === 'PENDING' && !p.payUrl && <Button variant="text" onClick={() => setDeleting(p)}>Hủy phiên</Button>}</td></tr>)} /><ServerPagination page={page} count={rows.length} onPage={setPage} /></section><p className="wl-note">Quay lại từ cổng thanh toán chưa xác nhận đã đóng tiền. Kiểm tra trạng thái ở đây và số dư trên hóa đơn.</p></WorkflowPage>{selected && <PaymentDetail key={selected} id={selected} onClose={() => { setSelected(null); resource.reload() }} />}{deleting && <ConfirmAction title="Hủy phiên thanh toán?" description="Chỉ phiên chờ xử lý, chưa có liên kết cổng và chưa có phản hồi giao dịch mới có thể hủy." path={`payment-sessions/${deleting.id}`} onClose={() => setDeleting(null)} onSave={() => { setDeleting(null); resource.reload() }} />}</>
}
function PaymentDetail({ id, onClose }: { id: string; onClose: () => void }) {
  const resource = useResource(`payment-sessions/${encodeURIComponent(id)}`, parsePayment), now = useClock(), p = resource.data
  const active = p?.status === 'PENDING' && Date.parse(p.expiresAt) > now
  return <Dialog title="Kết quả thanh toán" onClose={onClose} actions={<><Button variant="outlined" onClick={resource.reload}>Kiểm tra lại</Button><Button variant="text" onClick={onClose}>Đóng</Button></>}><WorkflowPage title="Thông tin phiên" description="Trạng thái do hệ thống xác nhận." view={resource.loading ? 'loading' : resource.error ? 'error' : 'data'} error={resource.error} onRetry={resource.reload}>{p && <><Facts items={[[ 'Mã phiên', p.id ], ['Cổng', p.gateway ], ['Số tiền', money(p.amount) ], ['Hóa đơn', p.invoiceIds.map(id => `HĐ-${id}`).join(', ') ], ['Hết hạn', dateTime(p.expiresAt) ], ['Trạng thái', paymentLabels[p.status] || p.status ]]} />{active && p.payUrl && <><a className="m3-button m3-button--filled" href={p.payUrl} target="_blank" rel="noopener noreferrer">Mở {p.gateway}</a><p className="wl-note">Đi đến {new URL(p.payUrl).hostname}. Sau khi thanh toán, trở lại và kiểm tra trạng thái.</p></>}{p.status === 'PENDING' && !active && <p className="wl-note">Phiên đã qua thời hạn mở thanh toán. Kiểm tra lại trạng thái trước khi tạo phiên khác.</p>}</>}</WorkflowPage></Dialog>
}
