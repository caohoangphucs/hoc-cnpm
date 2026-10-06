import { MeterReadingDialog } from './MeterReadingDialog'
import { useState } from 'react'
import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { TextField } from '@/shared/components/TextField'
import { PageHeader } from '@/shared/components/PageHeader'
import { apiFetch } from '@/shared/api/client'
import { readJson } from '@/shared/api/protocol'
import { useApiAction, useResource } from '@/shared/api/hooks'
import { DataTable, Feedback, Status } from '@/shared/workflow/Workflow'
import { money } from '@/shared/workflow/model'
import { draftWarningLabels, draftRoomWarnings, reconcileIssuance, parseUnbilledRooms, parseDraft, parseIssue, type Draft } from './api'
export function InvoiceDraft() {
  const [month, setMonth] = useState(''), [roomIds, setRooms] = useState<string[]>([]), [draft, setDraft] = useState<Draft | null>(null), [confirming, setConfirming] = useState(false), [reconciling, setReconciling] = useState(false), [recoveryNotice, setRecoveryNotice] = useState(''), [issued, setIssued] = useState<ReturnType<typeof parseIssue> | null>(null), action = useApiAction()
  const eligible = useResource(/^\d{4}-(0[1-9]|1[0-2])$/.test(month) ? `invoices/unbilled-rooms?month=${month}` : null, parseUnbilledRooms)
  const [search, setSearch] = useState(''), [editingMeter, setEditingMeter] = useState<string | null>(null)
  const busy = action.busy || reconciling
  const rooms = eligible.data ?? []
  const shown = rooms.filter(room => `${room.id} ${room.buildingId}`.toLowerCase().includes(search.toLowerCase()))
  async function preview() {
    if (busy) return
    const ids = roomIds
    if (!/^\d{4}-(0[1-9]|1[0-2])$/.test(month) || !ids.length || ids.length > 100 || new Set(ids).size !== ids.length) { action.setError('Chọn tháng và tích chọn từ 1 đến 100 phòng khác nhau.'); return }
    const result = await action.run('invoice-drafts', { month, roomIds: ids }, parseDraft)
    if (result) { setDraft(result); setIssued(null); setRecoveryNotice('') }
  }
  async function issue() {
    if (!draft || busy) return
    if (draft.rooms.some(room => draftRoomWarnings(room).length)) { action.setError('Bản nháp còn cảnh báo. Kiểm tra và tính lại trước khi phát hành.'); return }
    const current = draft
    const result = await action.run('invoices/issue', { draftId: current.id, version: current.version }, parseIssue)
    if (result) { eligible.reload(); setRooms([]); setIssued(result); setConfirming(false); setDraft(null); setRecoveryNotice(''); return }
    setReconciling(true)
    setRecoveryNotice('Yêu cầu phát hành chưa hoàn tất. Đang đối chiếu lại từng phòng với hệ thống…')
    setConfirming(false)
    setDraft(null)
    try {
      const unbilled = await readJson(await apiFetch(`invoices/unbilled-rooms?month=${current.month}`), parseUnbilledRooms)
      const reconciled = reconcileIssuance(current.rooms.map(room => room.roomId), unbilled)
      setIssued(reconciled)
      setRooms(reconciled.filter(room => room.status === 'NOT_ISSUED').map(room => room.roomId))
      setRecoveryNotice('Đã đối chiếu trạng thái hiện tại. Phòng không còn trong danh sách chưa phát hành được loại khỏi lựa chọn. Kiểm tra Danh sách hóa đơn; tính bản nháp mới cho các phòng còn lại trước khi phát hành.')
    } catch {
      setRooms([])
      setIssued(current.rooms.map(room => ({ roomId: room.roomId, status: 'UNCONFIRMED', invoiceId: null })))
      setRecoveryNotice('Chưa thể đối chiếu kết quả. Kiểm tra Danh sách hóa đơn hoặc tải lại phòng trước khi chọn và tính bản nháp mới; một số phòng có thể đã phát hành.')
    } finally { eligible.reload(); setReconciling(false) }
  }
  return <><PageHeader title="Lập hóa đơn tháng" description="Kiểm tra bản nháp trước khi phát hành hóa đơn." /><section className="wl-card wl-panel"><h2>Phạm vi lập hóa đơn</h2><p className="wl-note">Chọn tháng rồi tích các phòng chưa có hóa đơn. Chỉ số, biểu giá và khoản giảm trừ được hệ thống tính từ dữ liệu đã duyệt.</p><form className="wl-form" onSubmit={e => { e.preventDefault(); void preview() }}><TextField label="Tháng lập hóa đơn" type="month" value={month} onChange={e => { setMonth(e.target.value); setRooms([]); setDraft(null); setIssued(null); setRecoveryNotice(''); action.setError('') }} required disabled={busy} /><div className="invoice-room-picker">
      <TextField label="Tìm phòng / tòa" value={search} onChange={event => setSearch(event.target.value)} disabled={!month || busy} />
      <Feedback error={eligible.error} />
      {!month ? <p>Chọn tháng để hiển thị phòng chưa có hóa đơn.</p> : eligible.loading ? <p role="status">Đang tải phòng…</p> : !rooms.length && !eligible.error ? <p>Không có phòng chưa phát hành hóa đơn trong tháng này.</p> : <>
        <div className="wl-actions"><Button variant="outlined" disabled={busy || eligible.loading} onClick={() => { setRooms(shown.slice(0, 100).map(room => room.id)); setDraft(null) }}>Chọn phòng đang hiển thị (tối đa 100)</Button><Button variant="text" disabled={busy} onClick={() => { setRooms([]); setDraft(null) }}>Bỏ chọn</Button><Button variant="text" disabled={busy} onClick={() => { eligible.reload(); setRooms([]); setDraft(null) }}>Tải lại</Button></div>
        <p>Đã chọn {roomIds.length}/100 phòng</p>
        <div className="invoice-room-grid">{shown.map(room => <label key={room.id} className="invoice-room-option"><input type="checkbox" checked={roomIds.includes(room.id)} disabled={busy || eligible.loading || roomIds.length >= 100 && !roomIds.includes(room.id)} onChange={event => { setRooms(previous => event.target.checked ? [...previous, room.id] : previous.filter(id => id !== room.id)); setDraft(null) }} /><span><strong>{room.id}</strong><small>{room.buildingId}</small></span></label>)}</div>
        {!shown.length && <p>Không có phòng khớp từ khóa.</p>}
      </>}
    </div><Button type="submit" pending={busy} disabled={!roomIds.length || eligible.loading || !!eligible.error}>Tính bản nháp</Button></form><Feedback error={!confirming ? action.error : undefined} /></section>
    {draft && <section className="wl-card wl-panel"><h2>Bản nháp tháng {draft.month}</h2><DataTable pagination={false} label="Bản nháp hóa đơn" head={<tr><th>Phòng</th><th>Điện cũ → mới</th><th>Nước cũ → mới</th><th>Đơn giá điện / nước</th><th>Tiền phòng / phụ thu</th><th>Giảm trừ</th><th>Tổng tiền</th><th>Kiểm tra</th><th>Số liệu</th></tr>} rows={draft.rooms.map(r => <tr key={r.roomId}><td>{r.roomId}</td><td>{r.oldElectricity} → {r.newElectricity}</td><td>{r.oldWater} → {r.newWater}</td><td>{money(r.electricityPrice)}<small>{money(r.waterPrice)}</small></td><td>{money(r.roomPrice)}<small>{money(r.extras)}</small></td><td>{money(r.discount)}</td><td><strong>{money(r.total)}</strong></td><td>{draftRoomWarnings(r).length ? <Status tone="warning">Cần rà soát: {draftRoomWarnings(r).map(w => draftWarningLabels[w] || 'Cần đối chiếu dữ liệu phòng').join(', ')}</Status> : <Status tone="success">Không có cảnh báo</Status>}</td><td><Button variant="text" disabled={busy || r.warnings.includes('ALREADY_ISSUED')} onClick={() => setEditingMeter(r.roomId)}>Nhập / sửa chỉ số</Button></td></tr>)} /><Button disabled={busy || !draft.rooms.length || draft.rooms.some(r => draftRoomWarnings(r).length)} onClick={() => setConfirming(true)}>Phát hành hóa đơn</Button><p className="wl-note">Thiếu chỉ số: bấm Nhập / sửa chỉ số. Thiếu biểu giá: cần bổ sung giá đã duyệt.</p></section>}
    {recoveryNotice && <p className="wl-note" role="status">{recoveryNotice}</p>}
    {issued && <section className="wl-card wl-panel"><h2>Kết quả phát hành</h2><DataTable label="Kết quả từng phòng" pagination={false} head={<tr><th>Phòng</th><th>Kết quả</th><th>Hóa đơn</th></tr>} rows={issued.map(r => <tr key={r.roomId}><td>{r.roomId}</td><td><Status tone={r.status === 'SUCCESS' ? 'success' : 'warning'}>{r.status === 'SUCCESS' ? 'Đã phát hành' : r.status === 'CHECK_INVOICES' ? 'Không còn trong danh sách chưa phát hành; kiểm tra hóa đơn' : r.status === 'UNCONFIRMED' ? 'Chưa xác nhận; kiểm tra danh sách hóa đơn' : 'Chưa phát hành; kiểm tra dữ liệu và tính lại'}</Status></td><td>{r.invoiceId ? `HĐ-${r.invoiceId}` : '—'}</td></tr>)} /></section>}
    {editingMeter && <MeterReadingDialog roomId={editingMeter} month={month} onClose={() => setEditingMeter(null)} onSaved={() => { setEditingMeter(null); setDraft(null); void preview() }} />}
    {confirming && <Dialog title="Phát hành hóa đơn?" busy={busy} onClose={() => setConfirming(false)} actions={<><Button variant="text" disabled={busy} onClick={() => setConfirming(false)}>Quay lại</Button><Button pending={busy} onClick={() => void issue()}>Phát hành</Button></>}><p>Xác nhận số liệu bản nháp tháng {draft?.month}. Hệ thống sẽ kiểm tra lại dữ liệu trước khi tạo hóa đơn cho từng phòng.</p><Feedback error={action.error} /></Dialog>}
  </>
}
