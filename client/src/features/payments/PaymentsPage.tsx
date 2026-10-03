import { useRef, useState } from 'react'
import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { Icon } from '@/shared/components/Icon'
import { TextField } from '@/shared/components/TextField'
import { DataTable, Facts, Feedback, SelectField, Status, Summary, WorkflowPage } from '@/shared/workflow/Workflow'
import { useClock, usePreviewAction } from '@/shared/workflow/hooks'
import { dateTime, money, searchText, type WorkflowOptions } from '@/shared/workflow/model'
import { PaymentReceipt } from './PaymentReceipt'
import { PaymentSessionCard } from './PaymentSessionCard'
import { applyPreviewReply, balance, blocksNewPayment, createPreviewSession, gatewayLabels, invoiceLabel, isOverdue, payableIssue, paymentLabels, paymentState, selectionIssue, type Gateway, type Invoice, type PaymentSession, type PreviewReply } from './model'

type Props = WorkflowOptions & { invoices?: readonly Invoice[]; reply?: PreviewReply }
const calendarDate = (date: string) => date.split('-').reverse().join('/')
export function PaymentsPage({ invoices = [], reply = 'pending', ...options }: Props) {
  const [items, setItems] = useState(() => [...invoices]), [selection, setSelection] = useState<number[]>([])
  const [query, setQuery] = useState(''), [filter, setFilter] = useState('outstanding'), [period, setPeriod] = useState('')
  const [gateway, setGateway] = useState<Gateway>('VNPAY')
  const [sessions, setSessions] = useState<PaymentSession[]>([]), [activeId, setActiveId] = useState('')
  const [detailId, setDetailId] = useState<number | null>(null), [confirm, setConfirm] = useState(false), [providerId, setProviderId] = useState(''), [receiptId, setReceiptId] = useState('')
  const [returned, setReturned] = useState<Record<string, boolean>>({}), [replyOverrides, setReplyOverrides] = useState<Record<string, PreviewReply>>({})
  const attempt = useRef<{ key: string; payload: string } | null>(null)
  const action = usePreviewAction(options), now = useClock(1000)
  const outstanding = items.filter(invoice => !payableIssue(invoice))
  const shown = items.filter(invoice => (!period || invoice.month === period) && searchText(`${invoice.code} ${invoice.room} ${invoice.month}`).includes(searchText(query)) && (filter === 'all' || filter === 'outstanding' && !payableIssue(invoice) || filter === 'paid' && invoice.status === 'DA_TT' || filter === 'overdue' && isOverdue(invoice, now)))
  const selected = items.filter(invoice => selection.includes(invoice.id))
  const selectionTotal = selected.reduce((sum, invoice) => sum + BigInt(balance(invoice)), 0n)
  const issue = selectionIssue(items, selection)
  const active = sessions.find(session => session.id === activeId)
  const blocker = sessions.find(session => blocksNewPayment(session, now))
  const detail = items.find(invoice => invoice.id === detailId), provider = sessions.find(session => session.id === providerId), receipt = sessions.find(session => session.id === receiptId)
  const selectable = shown.filter(invoice => !payableIssue(invoice))
  const selectionDisabled = action.busy || !options.preview || Boolean(blocker)

  function closeConfirm() { setConfirm(false); action.setError('') }
  async function createPayment() {
    if (!options.preview || blocker || issue) return
    const ids = [...selection].sort((a, b) => a - b), payload = JSON.stringify({ ids, gateway })
    if (attempt.current?.payload !== payload) attempt.current = { payload, key: crypto.randomUUID() }
    const key = attempt.current.key
    if (await action.run(() => {
      const session = createPreviewSession(items, ids, gateway, Date.now(), key)
      setSessions(current => [session, ...current]); setActiveId(session.id); attempt.current = null
    }, 'Đã tạo phiên chờ xác nhận; dư nợ chưa thay đổi')) setConfirm(false)
  }
  async function checkStatus() {
    if (!active || !options.preview) return
    const result = applyPreviewReply(items, active, replyOverrides[active.id] ?? reply, Date.now())
    if (await action.run(() => {
      setItems(result.invoices); setSessions(current => current.map(session => session.id === active.id ? result.session : session))
      if (result.session.status === 'SUCCESS') setSelection(current => current.filter(id => !result.session.allocations.some(allocation => allocation.invoiceId === id)))
    }, 'Đã kiểm tra trạng thái phiên')) { if (result.error) action.setError(result.error) }
  }
  function leaveProvider(canceled = false) {
    if (!provider) return
    setReturned(current => ({ ...current, [provider.id]: true }))
    if (canceled) setReplyOverrides(current => ({ ...current, [provider.id]: 'canceled' }))
    setProviderId('')
  }
  function expirePreview() {
    if (!options.preview || !active || action.busy) return
    setSessions(current => current.map(session => session.id === active.id ? { ...session, expiresAt: now - 1 } : session))
  }
  function selectAgain() {
    setActiveId(''); setSelection(current => current.filter(id => { const invoice = items.find(value => value.id === id); return invoice && !payableIssue(invoice) }))
    action.setError('')
  }

  return <WorkflowPage title="Hóa đơn & thanh toán" description="Xem khoản thu của phòng lưu trú, chọn hóa đơn và theo dõi kết quả thanh toán." view={options.view}>
    <div className="payment-workflow"><Feedback notice={action.notice} error={!confirm ? action.error : undefined} />
      <Summary items={[{ label: 'Hóa đơn còn nợ', tone: 'warning', value: outstanding.length }, { label: 'Hóa đơn quá hạn', tone: 'danger', value: outstanding.filter(invoice => isOverdue(invoice, now)).length }, { label: 'Tổng còn phải trả', tone: 'primary', value: money(outstanding.reduce((sum, invoice) => sum + BigInt(balance(invoice)), 0n)) }]} />
      <div className="payment-layout">
        <section className="wl-card" aria-labelledby="payment-list-heading"><div className="wl-card-heading"><div><h2 id="payment-list-heading">Hóa đơn của bạn</h2><p>Theo phòng lưu trú và kỳ phát hành</p></div></div>
          <div className="wl-toolbar"><TextField label="Tìm hóa đơn" placeholder="Mã hóa đơn, phòng hoặc kỳ" value={query} onChange={event => setQuery(event.target.value)} /><SelectField label="Trạng thái hóa đơn" value={filter} onChange={event => setFilter(event.target.value)}><option value="outstanding">Còn phải trả</option><option value="overdue">Quá hạn</option><option value="paid">Đã thanh toán</option><option value="all">Tất cả</option></SelectField><SelectField label="Kỳ hóa đơn" value={period} onChange={event => setPeriod(event.target.value)}><option value="">Tất cả kỳ</option>{[...new Set(items.map(invoice => invoice.month))].sort().reverse().map(value => <option key={value} value={value}>{value.split('-').reverse().join('/')}</option>)}</SelectField></div>
          <div className="payment-list-selection"><label className="wl-check"><input type="checkbox" aria-label="Chọn tất cả hóa đơn có thể thanh toán đang hiển thị" disabled={selectionDisabled || !selectable.length} checked={selectable.length > 0 && selectable.every(invoice => selection.includes(invoice.id))} onChange={event => setSelection(current => event.target.checked ? [...new Set([...current, ...selectable.map(invoice => invoice.id)])] : current.filter(id => !selectable.some(invoice => invoice.id === id)))} />Chọn tất cả trong danh sách</label><span>{selection.length} đã chọn</span></div>
          <ul className="payment-invoice-list" aria-label="Danh sách hóa đơn của bạn">{shown.map(invoice => <li key={invoice.id}><article aria-label={`Hóa đơn ${invoice.code}`}><div className="payment-invoice-heading"><label className="payment-invoice-select"><input type="checkbox" aria-label={`Chọn hóa đơn ${invoice.code}`} checked={selection.includes(invoice.id)} disabled={selectionDisabled || Boolean(payableIssue(invoice))} onChange={event => setSelection(current => event.target.checked ? [...current, invoice.id] : current.filter(id => id !== invoice.id))} /><strong>{invoice.code}</strong></label><Status tone={invoice.status === 'DA_TT' ? 'success' : isOverdue(invoice, now) ? 'danger' : 'warning'}>{invoiceLabel(invoice, now)}</Status></div><p className="payment-invoice-meta">Phòng {invoice.room} · Kỳ {invoice.month.split('-').reverse().join('/')}</p><dl className="payment-invoice-amounts"><div><dt>Tổng hóa đơn</dt><dd>{money(invoice.total)}</dd></div><div><dt>Đã thanh toán</dt><dd>{money(invoice.paid)}</dd></div><div><dt>Còn phải trả</dt><dd>{money(balance(invoice))}</dd></div></dl><div className="payment-invoice-footer"><span className={isOverdue(invoice, now) ? 'wl-error' : undefined}>Hạn trả: {calendarDate(invoice.dueDate)}</span><Button variant="text" onClick={() => setDetailId(invoice.id)}>Xem chi tiết</Button></div></article></li>)}</ul>
          {!shown.length && <div className="wl-empty"><Icon name="search" /><h3>Không có hóa đơn phù hợp</h3><p>Thử thay đổi từ khóa, kỳ hoặc trạng thái hóa đơn.</p></div>}
          {selection.length > 0 && <div className="payment-list-footer"><p>{selection.length} hóa đơn đã chọn, kể cả hóa đơn ngoài bộ lọc hiện tại.</p><a className="filled-link state-layer" href="#payment-checkout">Đến phần thanh toán<Icon name="arrow" /></a></div>}
        </section>
        <aside className="wl-card payment-checkout" id="payment-checkout" tabIndex={-1} aria-label="Thanh toán hóa đơn">
          {active ? <PaymentSessionCard session={active} now={now} busy={action.busy} returned={Boolean(returned[active.id])} preview={options.preview} onCheck={() => void checkStatus()} onGateway={() => { if (options.preview && paymentState(active, Date.now()) === 'PENDING') setProviderId(active.id) }} onReceipt={() => setReceiptId(active.id)} onAgain={selectAgain} onExpire={expirePreview} /> : <>
            <h2>Thanh toán</h2><p>Chọn một hoặc nhiều hóa đơn để thanh toán toàn bộ số còn nợ.</p>
            {blocker && <div className="wl-note"><p>{paymentState(blocker, now) === 'NEEDS_REVIEW' ? 'Có giao dịch cần đối soát. Chờ Kế toán xử lý trước khi thanh toán lại.' : 'Bạn đang có một phiên chờ xác nhận. Kiểm tra phiên hiện tại trước khi tạo phiên mới.'}</p><Button variant="text" onClick={() => setActiveId(blocker.id)}>Xem phiên hiện tại</Button></div>}
            <ul className="payment-allocations" aria-label="Hóa đơn đã chọn">{selected.map(invoice => <li key={invoice.id}><span>{invoice.month.split('-').reverse().join('/')} · {invoice.room}<small>{invoice.code}</small></span><strong>{money(balance(invoice))}</strong></li>)}</ul>
            {!selected.length && <div className="payment-selection-empty"><Icon name="wallet" /><p>Chưa chọn hóa đơn</p></div>}
            <fieldset className="payment-methods" disabled={selectionDisabled}><legend>Phương thức thanh toán</legend>{(['VNPAY', 'MOMO'] as const).map(method => <label key={method} className={`payment-method ${gateway === method ? 'payment-method--selected' : ''}`}><input type="radio" name="payment-method" value={method} checked={gateway === method} onChange={() => setGateway(method)} /><span className="payment-method-mark" aria-hidden="true">{method === 'VNPAY' ? 'VN' : 'M'}</span><span><strong>{gatewayLabels[method]}</strong><small>{method === 'VNPAY' ? 'ATM, tài khoản ngân hàng qua cổng' : 'Thanh toán bằng ví MoMo'}</small></span></label>)}</fieldset>
            <div className="payment-total"><span>Tổng thanh toán</span><strong>{money(selectionTotal)}</strong></div>
            {selection.length > 0 && issue && <p className="wl-error" role="alert">{issue}</p>}
            <Button disabled={selectionDisabled || Boolean(issue)} pending={action.busy} onClick={() => { action.setError(''); setConfirm(true) }}>Thanh toán {selected.length || ''} hóa đơn</Button>
            <p className="payment-helper">Số tiền được hệ thống xác định từ dư nợ. Trang KTX không thu thập thông tin thẻ hoặc mật khẩu ngân hàng.</p>
          </>}
        </aside>
      </div>
      {sessions.length > 0 && <section className="wl-card" aria-labelledby="payment-history-heading"><div className="wl-card-heading"><div><h2 id="payment-history-heading">Lịch sử phiên thanh toán</h2><p>Theo dõi giao dịch và mở biên lai sau khi được xác nhận</p></div></div><DataTable label="Lịch sử phiên thanh toán" head={<tr><th>Mã giao dịch</th><th>Thời điểm</th><th>Phương thức</th><th>Số tiền</th><th>Trạng thái</th><th>Chi tiết</th></tr>} rows={sessions.map(session => <tr key={session.id}><td>GD-MINH-HOA-{session.id.slice(0, 8)}</td><td>{dateTime(new Date(session.createdAt).toISOString())}</td><td>{gatewayLabels[session.gateway]}</td><td>{money(session.amount)}</td><td><Status tone={paymentState(session, now) === 'SUCCESS' ? 'success' : paymentState(session, now) === 'NEEDS_REVIEW' ? 'danger' : 'neutral'}>{paymentLabels[paymentState(session, now)]}</Status></td><td><Button variant="text" disabled={action.busy} onClick={() => { setActiveId(session.id); action.setError('') }}>Xem giao dịch</Button></td></tr>)} /></section>}
    </div>
    {detail && <Dialog title={`Chi tiết hóa đơn ${detail.code}`} panel onClose={() => setDetailId(null)} actions={<Button variant="outlined" onClick={() => setDetailId(null)}>Đóng</Button>}><div className="wl-form"><Status tone={isOverdue(detail, now) ? 'danger' : detail.status === 'DA_TT' ? 'success' : 'warning'}>{invoiceLabel(detail, now)}</Status><Facts items={[["Phòng / kỳ", `${detail.room} / ${detail.month.split('-').reverse().join('/')}`], ["Hạn thanh toán", calendarDate(detail.dueDate)]]} /><h3>Chi tiết khoản thu</h3><Facts items={[...detail.lines.map(line => [line.label, money(line.amount)] as [string, string]), ["Giảm tiền phòng", `−${money(detail.discount)}`], ["Tổng hóa đơn", money(detail.total)], ["Đã thanh toán", money(detail.paid)], ["Còn phải trả", money(balance(detail))]]} /><p className="wl-note">Hóa đơn theo phòng lưu trú. Phí và giảm giá đã được xác định khi phát hành; số tiền thanh toán là khoản còn nợ của hóa đơn.</p></div></Dialog>}
    {confirm && <Dialog title="Xác nhận tạo phiên thanh toán?" busy={action.busy} onClose={closeConfirm} actions={<><Button variant="text" disabled={action.busy} onClick={closeConfirm}>Quay lại</Button><Button pending={action.busy} disabled={Boolean(blocker) || Boolean(issue)} onClick={() => void createPayment()}>Tạo phiên thanh toán</Button></>}><div className="wl-form"><Feedback error={action.error} /><Facts items={[["Số hóa đơn", selected.length], ["Phương thức", gatewayLabels[gateway]], ["Số tiền", money(selectionTotal)], ["Thời hạn phiên", '15 phút']]} /><p className="wl-note">Tạo phiên chưa ghi nhận tiền đã trả. Chỉ cập nhật hóa đơn khi hệ thống nhận và xác minh kết quả thành công từ cổng thanh toán.</p>{options.preview && <p>Bản xem trước chỉ mô phỏng giao diện, không chuyển tiền thật.</p>}</div></Dialog>}
    {provider && <Dialog title={`${gatewayLabels[provider.gateway]} · Cổng thanh toán minh họa`} onClose={() => leaveProvider()} actions={<><Button variant="text" onClick={() => leaveProvider(true)}>Hủy tại cổng (minh họa)</Button><Button onClick={() => leaveProvider()}>Quay về KTX (minh họa)</Button></>}><div className="wl-form"><Status>MINH HỌA</Status><Facts items={[["Mã giao dịch", `GD-MINH-HOA-${provider.id.slice(0, 8)}`], ["Số tiền", money(provider.amount)], ["Thời điểm hết hạn", dateTime(new Date(provider.expiresAt).toISOString())]]} /><p>Ở bản tích hợp, bạn sẽ được chuyển đến URL bảo mật do {gatewayLabels[provider.gateway]} cung cấp để hoàn tất thanh toán.</p><p className="wl-note">Quay về KTX không xác nhận thanh toán thành công. Cần kiểm tra trạng thái do hệ thống trả về. Bản xem trước không có URL hoặc QR chuyển tiền thật.</p></div></Dialog>}
    {receipt && <PaymentReceipt session={receipt} onClose={() => setReceiptId('')} />}
  </WorkflowPage>
}
