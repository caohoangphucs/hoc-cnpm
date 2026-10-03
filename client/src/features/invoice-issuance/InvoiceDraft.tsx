import { useState } from 'react'
import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { TextField } from '@/shared/components/TextField'
import { PageHeader } from '@/shared/components/PageHeader'
import { useApiAction } from '@/shared/api/hooks'
import { DataTable, Feedback, NotesField, Status } from '@/shared/workflow/Workflow'
import { money } from '@/shared/workflow/model'
import { draftWarningLabels, parseDraft, parseIssue, type Draft } from './api'
export function InvoiceDraft() {
  const [month, setMonth] = useState(''), [roomIds, setRooms] = useState(''), [draft, setDraft] = useState<Draft | null>(null), [confirming, setConfirming] = useState(false), [issued, setIssued] = useState<ReturnType<typeof parseIssue> | null>(null), action = useApiAction()
  async function preview() {
    const ids = roomIds.split(/[\s,;]+/).map(v => v.trim()).filter(Boolean)
    if (!/^\d{4}-(0[1-9]|1[0-2])$/.test(month) || !ids.length || ids.length > 100 || new Set(ids).size !== ids.length) { action.setError('Chọn tháng và nhập tối đa 100 mã phòng khác nhau.'); return }
    const result = await action.run('invoice-drafts', { month, roomIds: ids }, parseDraft)
    if (result) { setDraft(result); setIssued(null) }
  }
  async function issue() { if (!draft) return; const result = await action.run('invoices/issue', { draftId: draft.id, version: draft.version }, parseIssue); if (result) { setIssued(result); setConfirming(false); setDraft(null) } }
  return <><PageHeader title="Lập hóa đơn tháng" description="Kiểm tra bản nháp trước khi phát hành hóa đơn." /><section className="wl-card wl-panel"><h2>Phạm vi lập hóa đơn</h2><p className="wl-note">Nhập mã phòng cần lập hóa đơn. Chỉ số, biểu giá và khoản giảm trừ được hệ thống tính từ dữ liệu đã duyệt.</p><form className="wl-form" onSubmit={e => { e.preventDefault(); void preview() }}><TextField label="Tháng lập hóa đơn" type="month" value={month} onChange={e => { setMonth(e.target.value); setDraft(null) }} required disabled={action.busy} /><NotesField label="Mã phòng (cách nhau bằng dấu phẩy hoặc xuống dòng)" value={roomIds} onChange={e => { setRooms(e.target.value); setDraft(null) }} required disabled={action.busy} /><Button type="submit" pending={action.busy}>Tính bản nháp</Button></form><Feedback error={!confirming ? action.error : undefined} /></section>
    {draft && <section className="wl-card wl-panel"><h2>Bản nháp tháng {draft.month}</h2><DataTable pagination={false} label="Bản nháp hóa đơn" head={<tr><th>Phòng</th><th>Điện cũ → mới</th><th>Nước cũ → mới</th><th>Đơn giá điện / nước</th><th>Tiền phòng / phụ thu</th><th>Giảm trừ</th><th>Tổng tiền</th><th>Kiểm tra</th></tr>} rows={draft.rooms.map(r => <tr key={r.roomId}><td>{r.roomId}</td><td>{r.oldElectricity} → {r.newElectricity}</td><td>{r.oldWater} → {r.newWater}</td><td>{money(r.electricityPrice)}<small>{money(r.waterPrice)}</small></td><td>{money(r.roomPrice)}<small>{money(r.extras)}</small></td><td>{money(r.discount)}</td><td><strong>{money(r.total)}</strong></td><td>{r.warnings.length ? <Status tone="warning">Cần rà soát: {r.warnings.map(w => draftWarningLabels[w] || 'Cần đối chiếu dữ liệu phòng').join(', ')}</Status> : <Status tone="success">Không có cảnh báo</Status>}</td></tr>)} /><Button disabled={action.busy || !draft.rooms.length || draft.rooms.some(r => r.warnings.length)} onClick={() => setConfirming(true)}>Phát hành hóa đơn</Button><p className="wl-note">Nếu có cảnh báo, hãy cập nhật số liệu tại hệ thống rồi tính lại bản nháp.</p></section>}
    {issued && <section className="wl-card wl-panel"><h2>Kết quả phát hành</h2><DataTable label="Kết quả từng phòng" pagination={false} head={<tr><th>Phòng</th><th>Kết quả</th><th>Hóa đơn</th></tr>} rows={issued.map(r => <tr key={r.roomId}><td>{r.roomId}</td><td><Status tone={r.status === 'SUCCESS' ? 'success' : 'danger'}>{r.status === 'SUCCESS' ? 'Đã phát hành' : 'Chưa phát hành; kiểm tra dữ liệu và tính lại'}</Status></td><td>{r.invoiceId ? `HĐ-${r.invoiceId}` : '—'}</td></tr>)} /></section>}
    {confirming && <Dialog title="Phát hành hóa đơn?" busy={action.busy} onClose={() => setConfirming(false)} actions={<><Button variant="text" disabled={action.busy} onClick={() => setConfirming(false)}>Quay lại</Button><Button pending={action.busy} onClick={() => void issue()}>Phát hành</Button></>}><p>Xác nhận số liệu bản nháp tháng {draft?.month}. Hệ thống sẽ kiểm tra lại dữ liệu trước khi tạo hóa đơn cho từng phòng.</p><Feedback error={action.error} /></Dialog>}
  </>
}
