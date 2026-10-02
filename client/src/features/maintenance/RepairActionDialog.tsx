import { useEffect, useId, useRef, useState } from 'react'
import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { Icon } from '@/shared/components/Icon'
import { TextField } from '@/shared/components/TextField'
import { Feedback, NotesField, SelectField } from '@/shared/workflow/Workflow'
import { useClock, usePreviewAction } from '@/shared/workflow/hooks'
import type { WorkflowOptions } from '@/shared/workflow/model'
import { applyPreviewRepair, draftErrors, editableIssue, emptyDraft, localInput, shortages, vietnamToday, type RepairData, type RepairDraft, type RepairErrors, type RepairMode } from './model'

const titles: Record<RepairMode, string> = { accept: 'Tiếp nhận phiếu', schedule: 'Hẹn lại lịch sửa chữa', warranty: 'Ghi nhận bảo hành ngoài', complete: 'Hoàn thành sửa chữa', cancel: 'Hủy phiếu sửa chữa' }
export function RepairActionDialog({ data, id, mode, actor, options, onClose, onSave }: {
  data: RepairData; id: number; mode: RepairMode; actor: { id: number; name: string }; options: WorkflowOptions
  onClose: () => void; onSave: (data: RepairData, message: string, close: boolean) => void
}) {
  const repair = data.repairs.find(r => r.id === id)!
  const version = useRef(repair.version), request = useRef<{ payload: string; key: string } | null>(null)
  const [draft, setDraft] = useState<RepairDraft>(() => ({ ...emptyDraft(), schedule: localInput(repair.schedule), provider: repair.warranty?.provider ?? '', warrantyUntil: repair.warrantyUntil ?? '' }))
  const [errors, setErrors] = useState<RepairErrors>({}), [requested, setRequested] = useState(false)
  const action = usePreviewAction(options), formId = useId()
  const now = useClock()
  const shortageFeedback = useRef<HTMLDivElement>(null)
  useEffect(() => { if (requested) shortageFeedback.current?.focus() }, [requested])
  const missing = mode === 'complete' ? shortages(draft.supplies, data.supplies) : []
  const issue = editableIssue(repair, actor.id, mode)
  function change<K extends keyof RepairDraft>(field: K, value: RepairDraft[K]) { setDraft(current => ({ ...current, [field]: value })); setErrors(current => ({ ...current, [field]: undefined })); setRequested(false); action.setError('') }
  async function submit() {
    if (!options.preview || action.busy) return
    const errors = draftErrors(repair, mode, draft, data.supplies, Date.now())
    setErrors(errors)
    if (Object.keys(errors).length) { action.setError('Kiểm tra các trường được đánh dấu trước khi gửi.'); return }
    const payload = JSON.stringify({ id, version: version.current, mode, draft })
    if (!request.current || request.current.payload !== payload) request.current = { payload, key: crypto.randomUUID() }
    let decision: ReturnType<typeof applyPreviewRepair> | undefined
    const saved = await action.run(() => {
      decision = applyPreviewRepair(data, id, version.current, mode, draft, actor, request.current!.key, Date.now())
    }, 'Đã xử lý yêu cầu')
    if (!saved || !decision) return
    if (decision.error) { action.setError(decision.error); return }
    if (decision.shortage?.length) { setRequested(true); onSave(decision.data, 'Đã tạo đề nghị mua bổ sung; phiếu giữ trạng thái đang xử lý, chưa xuất vật tư.', false); return }
    onSave(decision.data, mode === 'complete' ? 'Đã hoàn thành phiếu và ghi nhận xuất vật tư.' : mode === 'accept' ? 'Đã tiếp nhận phiếu và lên lịch kiểm tra.' : mode === 'cancel' ? 'Đã hủy phiếu; không xuất vật tư.' : 'Đã cập nhật lịch xử lý và lịch sử phiếu.', true)
  }
  const primary = mode === 'complete' && missing.length ? requested ? 'Đã gửi đề nghị mua' : 'Gửi đề nghị mua bổ sung' : mode === 'complete' ? 'Xác nhận hoàn thành' : mode === 'cancel' ? 'Xác nhận hủy' : mode === 'accept' ? 'Xác nhận tiếp nhận' : 'Lưu lịch xử lý'
  return <Dialog title={titles[mode]} busy={action.busy} onClose={onClose} actions={<><Button variant="text" onClick={onClose} disabled={action.busy}>Quay lại</Button><Button type="submit" form={formId} pending={action.busy} disabled={!!issue || requested || !options.preview} className={mode === 'cancel' ? 'account-danger-button' : ''}>{primary}</Button></>}>
    <p className="repair-dialog-intro"><strong>{repair.code} · Phòng {repair.room}</strong><br />{repair.title}</p>
    <Feedback error={action.error || issue} />
    <form id={formId} className="wl-form repair-form" noValidate aria-busy={action.busy} onSubmit={event => { event.preventDefault(); void submit() }}>
      <fieldset disabled={action.busy} className="repair-fieldset">
        {mode === 'accept' && <p className="wl-note">Bạn sẽ phụ trách phiếu này. Chọn lịch đến phòng kiểm tra; thời gian dùng múi giờ Việt Nam (UTC+7).</p>}
        {['accept', 'schedule', 'warranty'].includes(mode) && <TextField label={mode === 'warranty' ? 'Dự kiến bàn giao thiết bị' : 'Lịch đến phòng'} type="datetime-local" value={draft.schedule} onChange={event => change('schedule', event.target.value)} errorText={errors.schedule} supportingText="Giờ Việt Nam · UTC+7" required />}
        {mode === 'warranty' && <><TextField label="Đơn vị bảo hành" value={draft.provider} onChange={event => change('provider', event.target.value)} errorText={errors.provider} maxLength={200} required /><p className="wl-note">Ghi nhận đơn vị và ngày dự kiến. Phiếu tiếp tục ở trạng thái đang xử lý.</p></>}
        {mode !== 'accept' && <NotesField label={mode === 'cancel' ? 'Lý do hủy' : mode === 'complete' ? 'Nội dung công việc đã thực hiện' : 'Lý do / ghi chú'} value={draft.note} onChange={event => change('note', event.target.value)} error={errors.note} required placeholder={mode === 'schedule' ? 'Ví dụ: Phòng khóa, đã thống nhất lịch mới với sinh viên.' : undefined} />}
        {mode === 'complete' && <>
          {repair.warranty && <div><label className="wl-check"><input type="checkbox" checked={draft.received} onChange={event => change('received', event.target.checked)} aria-describedby={errors.received ? `${formId}-received` : undefined} aria-invalid={!!errors.received} />Thiết bị đã được đơn vị bảo hành bàn giao và kiểm tra hoạt động</label>{errors.received && <p className="wl-error" id={`${formId}-received`}>{errors.received}</p>}</div>}
          <TextField label="Bảo hành sau sửa chữa đến (tùy chọn)" type="date" min={vietnamToday(now)} value={draft.warrantyUntil} onChange={event => change('warrantyUntil', event.target.value)} errorText={errors.warrantyUntil} />
          <section className="repair-materials" aria-labelledby={`${formId}-materials`}>
            <h3 id={`${formId}-materials`}>Vật tư sử dụng</h3><p className="repair-muted">Chỉ xuất kho khi hoàn thành thành công. Có thể hoàn thành không dùng vật tư.</p>
            {!draft.supplies.length && <p className="wl-note">Không sử dụng vật tư từ kho.</p>}
            {draft.supplies.map((line, index) => { const supply = data.supplies.find(s => s.id === line.supplyId); return <div className="repair-material-row" key={index}>
              <SelectField label={`Vật tư ${index + 1}`} value={line.supplyId} onChange={event => change('supplies', draft.supplies.map((item, i) => i === index ? { ...item, supplyId: event.target.value } : item))} aria-describedby={errors.supplies ? `${formId}-supplies-error` : undefined} aria-invalid={!!errors.supplies}><option value="">Chọn vật tư</option>{data.supplies.map(s => <option key={s.id} value={s.id} disabled={draft.supplies.some((other, i) => i !== index && other.supplyId === s.id)}>{s.name}</option>)}</SelectField>
              <TextField label={`Số lượng ${index + 1}`} type="number" min={1} max={2147483647} step={1} value={Number.isFinite(line.quantity) ? line.quantity : ''} onChange={event => change('supplies', draft.supplies.map((item, i) => i === index ? { ...item, quantity: event.target.value === '' ? NaN : Number(event.target.value) } : item))} aria-describedby={errors.supplies ? `${formId}-supplies-error` : undefined} errorText={errors.supplies && (!Number.isInteger(line.quantity) || line.quantity < 1 || line.quantity > 2147483647) ? 'Nhập số nguyên dương.' : undefined} />
              <button className="icon-button state-layer" type="button" aria-label={`Xóa vật tư ${index + 1}`} onClick={() => change('supplies', draft.supplies.filter((_, i) => i !== index))}><Icon name="close" /></button>
              <small className="repair-stock">{supply ? `Tồn kho: ${supply.stock} ${supply.unit}${line.quantity > supply.stock ? ` · Thiếu ${line.quantity - supply.stock} ${supply.unit}` : ''}` : 'Chọn vật tư để xem tồn kho.'}</small>
            </div> })}
            {errors.supplies && <p className="wl-error" role="alert" id={`${formId}-supplies-error`}>{errors.supplies}</p>}
            <Button variant="outlined" disabled={draft.supplies.length >= data.supplies.length || draft.supplies.length >= 100} onClick={() => change('supplies', [...draft.supplies, { supplyId: '', quantity: 1 }])}><Icon name="box" />Thêm vật tư</Button>
          </section>
          {!!missing.length && <div className="repair-shortage" ref={shortageFeedback} tabIndex={-1} role={requested ? 'status' : undefined}><h3>{requested ? 'Đã gửi đề nghị mua bổ sung' : 'Kho chưa đủ vật tư'}</h3><ul>{missing.map(s => <li key={s.id}>{s.name}: cần {s.needed}, còn {s.stock}, thiếu {s.missing} {s.unit}.</li>)}</ul><p>{requested ? 'Đề nghị đã được lưu trong bản xem trước. Phiếu chưa hoàn thành, toàn bộ tồn kho được giữ nguyên.' : 'Gửi đề nghị mua phần thiếu. Phiếu giữ trạng thái đang xử lý; không xuất bất kỳ vật tư nào.'}</p></div>}
          {!missing.length && <p className="wl-note">Xác nhận sẽ hoàn thành phiếu và xuất các vật tư đã chọn một lần.</p>}
        </>}
        {mode === 'cancel' && <p className="wl-note">Hủy phiếu trước khi hoàn thành. Lý do sẽ được giữ trong lịch sử; không xuất kho vật tư.</p>}
      </fieldset>
    </form>
  </Dialog>
}
