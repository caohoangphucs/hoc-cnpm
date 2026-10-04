import { useState } from 'react'
import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { TextField } from '@/shared/components/TextField'
import { Feedback } from '@/shared/workflow/Workflow'
import { useApiAction, useResource } from '@/shared/api/hooks'
import { command } from '@/shared/api/protocol'
import { parseMeter } from './api'

const fields = [ ['oldElectricity', 'Điện cũ (kWh)'], ['newElectricity', 'Điện mới (kWh)'], ['oldWater', 'Nước cũ (m³)'], ['newWater', 'Nước mới (m³)'] ] as const

/** Sửa dữ liệu nguồn; đóng form sẽ tính lại bản nháp với số đo đã lưu. */
export function MeterReadingDialog({ roomId, month, onClose, onSaved }: { roomId: string; month: string; onClose: () => void; onSaved: () => void }) {
  const path = `rooms/${encodeURIComponent(roomId)}/meter-readings?month=${month}`
  const query = useResource(path, parseMeter), action = useApiAction()
  const [values, setValues] = useState<Partial<Record<typeof fields[number][0], string>>>({})
  async function save() {
    if (!query.data || !query.data.editable) return
    const entries = fields.map(([key]) => [key, values[key] ?? (query.data!.exists ? String(query.data![key]) : '')] as const)
    if (entries.some(([, value]) => !/^\d+$/.test(value) || !Number.isSafeInteger(Number(value)) || Number(value) > 2147483647)) { action.setError('Nhập đủ bốn số nguyên từ 0 đến 2147483647.'); return }
    const body = Object.fromEntries(entries.map(([key, value]) => [key, Number(value)]))
    if (body.newElectricity < body.oldElectricity || body.newWater < body.oldWater) { action.setError('Chỉ số mới phải lớn hơn hoặc bằng chỉ số cũ.'); return }
    if (await action.run(path, { ...body, revision: query.data.revision }, command, 'PUT')) onSaved()
  }
  return <Dialog title={`Chỉ số điện nước · ${roomId} · ${month}`} busy={action.busy} onClose={onClose} actions={<><Button variant="text" disabled={action.busy} onClick={onClose}>Đóng</Button><Button pending={action.busy} disabled={!query.data?.editable || query.loading} onClick={() => void save()}>Lưu & tính lại</Button></>}>
    <Feedback error={query.error || action.error} />
    {query.loading ? <p role="status">Đang tải số đo…</p> : query.data && <><p>{query.data.exists ? 'Kiểm tra và cập nhật số đo thực tế. Hóa đơn đã phát hành không bị thay đổi.' : 'Phòng chưa có số đo tháng này. Nhập đủ chỉ số trước khi phát hành.'}</p>{!query.data.editable && <p>Tháng này đã có hóa đơn, không thể sửa chỉ số.</p>}<div className="wl-form">{fields.map(([key, label]) => <TextField key={key} label={label} type="number" min={0} max={2147483647} step={1} required value={values[key] ?? (query.data!.exists ? String(query.data![key]) : '')} disabled={action.busy || !query.data!.editable} onChange={event => setValues(previous => ({ ...previous, [key]: event.target.value }))} />)}</div><Button variant="text" disabled={action.busy} onClick={() => { setValues({}); query.reload() }}>Tải lại số liệu</Button></>}
  </Dialog>
}
