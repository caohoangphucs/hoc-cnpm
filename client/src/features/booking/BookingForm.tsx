import { BookingBedPicker } from './BookingBedPicker'
import { Dialog } from '@/shared/components/Dialog'
import { useId, useState } from 'react'
import { Button } from '@/shared/components/Button'
import { Feedback, SelectField } from '@/shared/workflow/Workflow'
import { useAuth } from '@/features/auth/context'
import { apiFetch } from '@/shared/api/client'
import { useApiAction, useResource } from '@/shared/api/hooks'
import { command, errorMessage, readJson } from '@/shared/api/protocol'
import { EvidenceFiles } from './EvidenceFiles'
import { evidenceKinds, evidenceLabels, parseEvidence, parseWindows, uploadProblem, type EvidenceFile, type EvidenceKind } from './evidence'

/** Gửi hồ sơ với đợt đang mở, giường đã chọn và ba ảnh thực sự tải lên. */
export function BookingForm({ bedId, onSelectBed, onComplete, onClose }: { onClose: () => void; onSelectBed: (id: string | null) => void; bedId: string | null; onComplete: () => void }) {
  const formId = useId()
  const windows = useResource('booking-windows', parseWindows), action = useApiAction(), auth = useAuth()
  const [selectedWindow, setSelectedWindow] = useState(''), [accepted, setAccepted] = useState(false)
  const [files, setFiles] = useState<Partial<Record<EvidenceKind, EvidenceFile>>>({})
  const [uploading, setUploading] = useState(false), [uploadError, setUploadError] = useState('')
  const windowId = selectedWindow || String(windows.data?.[0]?.id ?? '')
  const fileIds = evidenceKinds.flatMap(kind => files[kind] ? [files[kind]!.id] : [])
  const ready = !!bedId && !!windowId && accepted && fileIds.length === 3 && !uploading && !action.busy

  // Tải từng ảnh bằng multipart, có CSRF và không tự đặt Content-Type/boundary.
  async function upload(kind: EvidenceKind, file?: File) {
    if (!file || uploading || action.busy) return
    setUploadError('')
    const problem = uploadProblem(file)
    if (problem) { setUploadError(problem); return }
    setUploading(true)
    try {
      const session = await auth.refresh()
      const form = new FormData()
      form.set('kind', kind); form.set('file', file)
      const response = await apiFetch('files', { method: 'POST', csrfToken: session.csrfToken, body: form })
      const saved = await readJson(response, parseEvidence)
      setFiles(previous => ({ ...previous, [kind]: saved }))
    } catch (error) { setUploadError(errorMessage(error)) }
    finally { setUploading(false) }
  }

  // Sau thành công chuyển sang hồ sơ của tôi; backend là nơi quyết định giữ giường.
  async function submit() {
    if (!ready) return
    const result = await action.run('bookings', { bedId, windowId: Number(windowId), fileIds, acceptedTerms: accepted }, command)
    if (result) onComplete()
  }

  return <Dialog title="Nộp hồ sơ đăng ký phòng" className="booking-dialog" busy={uploading || action.busy} onClose={onClose} actions={<><Button variant="text" disabled={uploading || action.busy} onClick={onClose}>Đóng</Button><Button type="submit" form={formId} pending={action.busy} disabled={!ready}>Gửi hồ sơ đăng ký</Button></>}>
    <form id={formId} className="booking-registration" onSubmit={event => { event.preventDefault(); void submit() }}>
    <ol className="booking-progress" aria-label="Các bước nộp hồ sơ"><li data-complete={!!bedId}><span>1</span>Chọn giường</li><li data-complete={fileIds.length === 3}><span>2</span>Minh chứng</li><li data-complete={accepted}><span>3</span>Xác nhận</li></ol>
    <p>Hoàn thành ba bước để gửi hồ sơ. Mỗi ảnh PNG/JPEG có dung lượng tối đa 5 MiB.</p>
    <Feedback error={windows.error || uploadError || action.error} />
    <h3>1. Chọn phòng & giường</h3>
    <BookingBedPicker bedId={bedId} onSelect={onSelectBed} disabled={uploading || action.busy} />
    <h3>2. Đợt đăng ký & minh chứng</h3>
    {windows.loading ? <p role="status">Đang tải đợt đăng ký…</p> : windows.data?.length ? <SelectField label="Đợt đăng ký" value={windowId} onChange={event => setSelectedWindow(event.target.value)} disabled={uploading || action.busy}>{windows.data.map(window => <option key={window.id} value={window.id}>{window.name} — {window.semesterName}</option>)}</SelectField> : <p>Hiện chưa có đợt đăng ký đang mở.</p>}
    <div className="booking-upload-grid">{evidenceKinds.map(kind => <label className="booking-upload-card" key={kind}>{evidenceLabels[kind]}<input type="file" accept="image/png,image/jpeg" disabled={uploading || action.busy} onChange={event => void upload(kind, event.target.files?.[0])} /><span className="booking-upload-button">{files[kind] ? 'Thay ảnh' : 'Chọn ảnh'}</span><small>{files[kind] ? 'Đã tải lên' : 'Chưa tải ảnh'}</small></label>)}</div>
    {uploading && <p role="status">Đang tải ảnh…</p>}
    <EvidenceFiles ids={fileIds} />
    <h3>3. Xác nhận & gửi hồ sơ</h3>
    {!bedId && <p className="wl-note">Chọn một giường trống ở bước 1 để tiếp tục.</p>}
    <label className="wl-check"><input type="checkbox" checked={accepted} onChange={event => setAccepted(event.target.checked)} disabled={uploading || action.busy} />Tôi xác nhận thông tin hồ sơ và đồng ý nội quy ký túc xá.</label>
    </form>
  </Dialog>
}
