import { useState } from 'react'
import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { Icon } from '@/shared/components/Icon'
import { TextField } from '@/shared/components/TextField'
import { DataTable, Facts, Feedback, NotesField, SelectField, Status, Summary, WorkflowPage } from '@/shared/workflow/Workflow'
import { useClock, usePreviewAction } from '@/shared/workflow/hooks'
import { dateTime, money, searchText, type WorkflowOptions } from '@/shared/workflow/model'
import { downloadDraft } from './exportDraft'
import { calculateInvoice, dueDate, extraIssue, formatDate, MAX_MONEY, type Calculation, type Draft, type ExtraCharge, type InvoiceRoom, type IssuanceResult } from './model'

type Props = WorkflowOptions & { rooms?: readonly InvoiceRoom[]; initialPeriod?: string; failedRoom?: string }
const noCharge: ExtraCharge = { amount: '0', note: '' }
const totalOf = (rows: Calculation[]) => rows.reduce((sum, row) => sum + BigInt(row.total), 0n)
const meterText = (meter: InvoiceRoom['electricity'], unit: string) => meter.current === null || meter.previous === null ? 'Thiếu chỉ số' : `${meter.previous} → ${meter.current} (${meter.current - meter.previous} ${unit})`

export function InvoiceIssuancePage({ rooms = [], initialPeriod, failedRoom, ...options }: Props) {
  const [items, setItems] = useState(() => [...rooms])
  const [period, setPeriod] = useState(() => initialPeriod ?? new Intl.DateTimeFormat('sv-SE', { year: 'numeric', month: '2-digit', timeZone: 'Asia/Ho_Chi_Minh' }).format(new Date()))
  const [query, setQuery] = useState(''), [building, setBuilding] = useState('')
  const [selection, setSelection] = useState(() => rooms.filter(room => !room.issued).map(room => room.id))
  const [extras, setExtras] = useState<Record<string, ExtraCharge>>({})
  const [skipInvalid, setSkipInvalid] = useState(false), [draft, setDraft] = useState<Draft | null>(null)
  const [revision, setRevision] = useState(0), [stale, setStale] = useState(false)
  const [results, setResults] = useState<IssuanceResult[]>([])
  const [detailId, setDetailId] = useState(''), [confirm, setConfirm] = useState(false)
  const [charge, setCharge] = useState<ExtraCharge>(noCharge), [chargeError, setChargeError] = useState('')
  const action = usePreviewAction(options), now = useClock()
  const calculations = items.map(room => calculateInvoice(room, period, extras[room.id]))
  const shown = calculations.filter(row => (!building || row.room.building === building) && searchText(`${row.room.id} ${row.room.building}`).includes(searchText(query)))
  const selected = calculations.filter(row => selection.includes(row.room.id))
  const eligible = selected.filter(row => !row.issue), invalid = selected.filter(row => row.issue)
  const detail = calculations.find(row => row.room.id === detailId)
  const selectable = shown.filter(row => !(row.room.issued && row.room.period === period))
  function invalidate() { if (draft) setStale(true); setDraft(null); action.setError('') }
  function changeSelection(ids: string[]) { setSelection(ids); invalidate() }
  function openDetail(row: Calculation) { setDetailId(row.room.id); setCharge(extras[row.room.id] ?? noCharge); setChargeError('') }
  function closeDetail() { setDetailId(''); setChargeError('') }
  function saveCharge() {
    if (!detail || !options.preview || detail.room.issued || detail.room.period !== period) return
    const issue = extraIssue(charge)
    if (issue) { setChargeError(issue); return }
    setExtras(current => ({ ...current, [detail.room.id]: { amount: String(Number(charge.amount)), note: charge.note.trim() } }))
    invalidate(); closeDetail()
  }
  async function createDraft() {
    if (!options.preview || !/^\d{4}-(0[1-9]|1[0-2])$/.test(period) || !eligible.length || (invalid.length && !skipInvalid)) return
    await action.run(() => {
      setDraft({ version: revision + 1, period, createdAt: Date.now(), rows: eligible, skipped: invalid })
      setRevision(value => value + 1); setStale(false); setResults([])
    }, 'Đã tạo bản nháp để kiểm tra')
  }
  async function issueDraft() {
    if (!draft || !options.preview) return
    const snapshot = draft, issuedAt = Date.now(), deadline = dueDate(issuedAt)
    const success = await action.run(() => {
      const issued = snapshot.rows.filter(row => row.room.id !== failedRoom).map(row => row.room.id)
      setItems(current => current.map(room => issued.includes(room.id) ? { ...room, issued: true } : room))
      setSelection(current => current.filter(id => !issued.includes(id)))
      setResults([
        ...snapshot.rows.map(row => ({ room: row.room.id, status: row.room.id === failedRoom ? 'error' as const : 'issued' as const, message: row.room.id === failedRoom ? 'Không lưu được hóa đơn phòng này (minh họa). Có thể tạo nháp để thử lại.' : `HD-MINH-HOA-${snapshot.period}-${row.room.id}`, total: row.total, due: row.room.id === failedRoom ? undefined : deadline })),
        ...snapshot.skipped.map(row => ({ room: row.room.id, status: 'skipped' as const, message: row.issue, total: 0 })),
      ])
      setDraft(null); setStale(false)
    }, 'Đã xử lý phát hành; xem kết quả từng phòng bên dưới')
    if (success) setConfirm(false)
    else if (options.outcome === 'conflict') { setDraft(null); setStale(true) }
  }
  function closeConfirm() { setConfirm(false); action.setError('') }
  const disabled = action.busy || !options.preview

  return <WorkflowPage title="Phát hành hóa đơn" description="Kiểm tra chi phí theo kỳ, xem bản nháp và phát hành hóa đơn cho từng phòng." view={options.view}>
    <div className="invoice-workflow">
      <Feedback notice={action.notice} error={!confirm ? action.error : undefined} />
      <Summary items={[{ label: 'Phòng đã chọn', value: selected.length }, { label: 'Đủ dữ liệu để lập nháp', value: eligible.length }, { label: 'Tổng dự kiến', value: money(totalOf(eligible)) }]} />

      <section className="wl-card" aria-labelledby="invoice-rooms-heading">
        <div className="wl-card-heading"><div><h2 id="invoice-rooms-heading">1. Chọn kỳ và phòng</h2><p>Chỉ số và đơn giá có hiệu lực tại ngày đầu kỳ</p></div><Status>{period ? `Kỳ ${period.split('-').reverse().join('/')}` : 'Chưa chọn kỳ'}</Status></div>
        <div className="wl-toolbar">
          <TextField label="Kỳ hóa đơn" type="month" min="2000-01" max="9999-12" value={period} disabled={disabled} onChange={event => { setPeriod(event.target.value); setExtras({}); setSelection(items.filter(room => !(room.issued && room.period === event.target.value)).map(room => room.id)); setResults([]); setSkipInvalid(false); invalidate() }} />
          <TextField label="Tìm phòng" placeholder="Tên phòng hoặc tòa nhà" value={query} disabled={action.busy} onChange={event => setQuery(event.target.value)} />
          <SelectField label="Tòa nhà" value={building} disabled={action.busy} onChange={event => setBuilding(event.target.value)}><option value="">Tất cả tòa</option>{[...new Set(items.map(room => room.building))].map(value => <option key={value}>{value}</option>)}</SelectField>
        </div>
        <DataTable label="Phòng lập hóa đơn" head={<tr><th><input type="checkbox" aria-label="Chọn tất cả phòng đang hiển thị" disabled={disabled || !selectable.length} checked={selectable.length > 0 && selectable.every(row => selection.includes(row.room.id))} onChange={event => changeSelection(event.target.checked ? [...new Set([...selection, ...selectable.map(row => row.room.id)])] : selection.filter(id => !selectable.some(row => row.room.id === id)))} /></th><th>Phòng / lưu trú</th><th>Điện (kWh)</th><th>Nước (m³)</th><th>Tổng dự kiến</th><th>Kiểm tra</th><th>Chi tiết</th></tr>} rows={shown.map(row => <tr key={row.room.id}>
          <td><input type="checkbox" aria-label={`Chọn phòng ${row.room.id}`} disabled={disabled || Boolean(row.room.issued && row.room.period === period)} checked={selection.includes(row.room.id)} onChange={event => changeSelection(event.target.checked ? [...selection, row.room.id] : selection.filter(id => id !== row.room.id))} /></td>
          <td><strong>{row.room.id}</strong><small>{row.room.building} · {row.room.period === period ? `${row.room.residents.length} sinh viên` : 'Chưa có dữ liệu lưu trú'}</small></td>
          <td>{row.room.period === period ? meterText(row.room.electricity, 'kWh') : 'Chưa có dữ liệu'}<small>{row.room.period === period && `${money(row.room.electricity.rate)}/kWh`}</small></td>
          <td>{row.room.period === period ? meterText(row.room.water, 'm³') : 'Chưa có dữ liệu'}<small>{row.room.period === period && `${money(row.room.water.rate)}/m³`}</small></td>
          <td>{row.issue ? '—' : money(row.total)}<small>{Number(extras[row.room.id]?.amount) > 0 && `Đã thêm ${money(Number(extras[row.room.id].amount))}`}</small></td>
          <td><Status tone={!row.issue ? 'primary' : row.room.issued && row.room.period === period ? 'neutral' : 'danger'}>{!row.issue ? 'Sẵn sàng' : row.room.issued && row.room.period === period ? 'Đã phát hành' : 'Cần kiểm tra'}</Status>{row.issue && <small>{row.issue}</small>}</td>
          <td><Button variant="text" disabled={action.busy} onClick={() => openDetail(row)}>Xem chi phí</Button></td>
        </tr>)} />
        <div className="invoice-selection-footer">
          <p>{selection.length} phòng đã chọn, kể cả phòng ngoài bộ lọc hiện tại.</p>
          {invalid.length > 0 && <div className="invoice-warning"><Icon name="info" /><div><strong>{invalid.length} phòng cần kiểm tra</strong><p>Thiếu dữ liệu, chỉ số không hợp lệ hoặc đã có hóa đơn. Các phòng này không thể phát hành.</p><label className="wl-check"><input type="checkbox" checked={skipInvalid} disabled={disabled} onChange={event => { setSkipInvalid(event.target.checked); invalidate() }} />Bỏ qua các phòng chưa đủ điều kiện và tiếp tục với phòng hợp lệ</label></div></div>}
          {stale && <p className="wl-note" role="status">Bản nháp không còn hiệu lực. Tạo lại nháp để kiểm tra dữ liệu mới nhất.</p>}
          <div className="invoice-footer-actions"><p>Giảm giá chỉ tính trên tiền phòng; điện nước tính theo mức sử dụng.</p><Button disabled={disabled || !eligible.length || !period || (invalid.length > 0 && !skipInvalid)} pending={action.busy && !confirm} onClick={() => void createDraft()}><Icon name="document" />{draft ? 'Tạo lại bản nháp' : 'Tạo bản nháp'}</Button></div>
        </div>
      </section>

      <section className="wl-card" aria-labelledby="invoice-draft-heading">
        <div className="wl-card-heading"><div><h2 id="invoice-draft-heading">2. Kiểm tra và phát hành</h2><p>{draft ? `Nháp v${draft.version} · ${dateTime(new Date(draft.createdAt).toISOString())}` : 'Tạo bản nháp sau khi chọn phòng và kiểm tra các cảnh báo'}</p></div>{draft && <Status>Chưa phát hành</Status>}</div>
        {draft ? <>
          <DataTable label="Bản nháp hóa đơn" head={<tr><th>Phòng</th><th>Tiền phòng</th><th>Điện</th><th>Nước</th><th>Giảm giá</th><th>Thu thêm</th><th>Tổng tiền</th></tr>} rows={draft.rows.map(row => <tr key={row.room.id}><td><strong>{row.room.id}</strong></td><td>{money(row.roomFee)}</td><td>{money(row.electricity)}</td><td>{money(row.water)}</td><td>−{money(row.discount)}</td><td>{money(row.extra)}{row.extraNote && <small>{row.extraNote}</small>}</td><td><strong>{money(row.total)}</strong></td></tr>)} />
          <div className="invoice-draft-footer"><Facts items={[["Số phòng phát hành", draft.rows.length], ["Phòng bỏ qua", draft.skipped.length], ["Tổng tiền", money(totalOf(draft.rows))], ["Hạn thanh toán dự kiến", formatDate(dueDate(now))]]} /><p className="wl-note">Hạn thanh toán bằng ngày phát hành + 15 ngày. Sau phát hành, chỉ số và đơn giá được giữ nguyên; điều chỉnh bằng chứng từ riêng.</p><div className="invoice-footer-actions"><Button variant="outlined" disabled={disabled} onClick={() => downloadDraft(draft)}><Icon name="document" />Tải Excel nháp</Button><Button disabled={disabled} onClick={() => { action.setError(''); setConfirm(true) }}>Phát hành {draft.rows.length} hóa đơn</Button></div></div>
        </> : <div className="invoice-draft-empty"><Icon name="document" /><h3>Chưa có bản nháp</h3><p>Bản nháp sẽ hiển thị chi tiết từng khoản thu trước khi phát hành.</p></div>}
      </section>

      {results.length > 0 && <section className="wl-card" aria-labelledby="invoice-results-heading"><div className="wl-card-heading"><div><h2 id="invoice-results-heading">Kết quả phát hành</h2><p>Mỗi phòng được xử lý riêng; phòng lỗi có thể tạo nháp để thử lại</p></div><Status>Minh họa</Status></div><DataTable label="Kết quả phát hành hóa đơn" head={<tr><th>Phòng</th><th>Kết quả</th><th>Hóa đơn / lý do</th><th>Số tiền</th><th>Hạn thanh toán</th></tr>} rows={results.map(result => <tr key={result.room}><td><strong>{result.room}</strong></td><td><Status tone={result.status === 'issued' ? 'primary' : result.status === 'error' ? 'danger' : 'neutral'}>{result.status === 'issued' ? 'Đã phát hành' : result.status === 'error' ? 'Lỗi phát hành' : 'Bỏ qua'}</Status></td><td>{result.message}</td><td>{result.status === 'skipped' ? '—' : money(result.total)}</td><td>{result.due ? formatDate(result.due) : '—'}</td></tr>)} /><p className="invoice-result-note">Chỉ mô phỏng kết quả trên giao diện. Chưa lưu hóa đơn, tạo QR thanh toán hoặc gửi thông báo thật.</p></section>}
    </div>

    {detail && <Dialog title={`Chi phí phòng ${detail.room.id}`} panel onClose={closeDetail} actions={<><Button variant="text" onClick={closeDetail}>Đóng</Button><Button disabled={!options.preview || Boolean(detail.room.issued) || detail.room.period !== period} onClick={saveCharge}>Lưu chi phí thêm</Button></>}><div className="wl-form">
      <Status>Kỳ {period}</Status>{detail.issue && <p className="wl-note">{detail.issue}</p>}
      {detail.room.period === period && <>
        <Facts items={[["Tiền phòng / kỳ", money(detail.room.roomPrice)], ["Điện", meterText(detail.room.electricity, 'kWh')], ["Đơn giá điện", `${money(detail.room.electricity.rate)}/kWh`], ["Nước", meterText(detail.room.water, 'm³')], ["Đơn giá nước", `${money(detail.room.water.rate)}/m³`], ["Tổng dự kiến", detail.issue ? 'Chưa tính được' : money(detail.total)]]} />
        <h3>Phân bổ tiền phòng và giảm giá</h3><p>Chia cho sinh viên lưu trú tại ngày đầu kỳ; số dư chia theo MSSV tăng dần. Mỗi sinh viên dùng một chính sách hợp lệ có mức giảm cao nhất, làm tròn xuống.</p>
        <ul className="invoice-residents">{detail.room.residents.map(resident => { const share = detail.shares.find(value => value.resident.code === resident.code); return <li key={resident.code}><div><strong>{resident.name}</strong><small>{resident.code} · Giảm {resident.discount}% tiền phòng</small></div>{share && <span>{money(share.roomFee)}<small>Giảm {money(share.discount)}</small></span>}</li> })}</ul>
        <h3>Chi phí riêng của phòng</h3><div className="invoice-charge"><TextField label="Số tiền thu thêm (VND)" type="number" min="0" max={MAX_MONEY} step="1" value={charge.amount} disabled={!options.preview || Boolean(detail.room.issued)} errorText={chargeError && (extraIssue(charge).startsWith('Nhập số tiền') ? chargeError : undefined)} onChange={event => { setCharge(current => ({ ...current, amount: event.target.value })); setChargeError('') }} /></div><NotesField label="Căn cứ / ghi chú khoản thu thêm" value={charge.note} disabled={!options.preview || Boolean(detail.room.issued)} error={chargeError && !extraIssue(charge).startsWith('Nhập số tiền') ? chargeError : undefined} onChange={event => { setCharge(current => ({ ...current, note: event.target.value })); setChargeError('') }} /><p className="wl-note">Nhập 0 để bỏ khoản thu thêm. Lưu thay đổi sẽ yêu cầu tạo lại bản nháp.</p>
      </>}
    </div></Dialog>}

    {confirm && <Dialog title="Xác nhận phát hành hóa đơn?" busy={action.busy} onClose={closeConfirm} actions={<><Button variant="text" disabled={action.busy} onClick={closeConfirm}>{draft ? 'Quay lại kiểm tra' : 'Quay lại tạo nháp'}</Button><Button pending={action.busy} disabled={!draft} onClick={() => void issueDraft()}>Xác nhận phát hành</Button></>}><div className="wl-form"><Feedback error={action.error && !draft && stale ? 'Dữ liệu hóa đơn đã thay đổi (minh họa). Chưa phát hành; hãy quay lại tạo bản nháp mới.' : action.error} />{draft && <><Facts items={[["Kỳ hóa đơn", draft.period], ["Phiên bản nháp", `v${draft.version}`], ["Phòng phát hành / bỏ qua", `${draft.rows.length} / ${draft.skipped.length}`], ["Tổng tiền", money(totalOf(draft.rows))], ["Hạn thanh toán dự kiến", formatDate(dueDate(now))]]} /><p className="wl-note">Hóa đơn được phát hành theo từng phòng, kiểm tra lại phiên bản và chống trùng kỳ. Bản xem trước chỉ mô phỏng; chưa ghi vào hệ thống thật.</p></>}</div></Dialog>}
  </WorkflowPage>
}
