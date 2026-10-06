import { useState } from 'react'
import { useAuth } from '@/features/auth/context'
import { EvidenceFiles } from '@/features/booking/EvidenceFiles'
import { parseEvidence, uploadProblem } from '@/features/booking/evidence'
import { apiFetch } from '@/shared/api/client'
import { useApiAction } from '@/shared/api/hooks'
import { errorMessage, readJson } from '@/shared/api/protocol'
import { Button } from '@/shared/components/Button'
import { Feedback, NotesField } from '@/shared/workflow/Workflow'
import { decisionPayload, parseViolations, type Violation } from './model'

/** Tải ảnh nháp trước; API quyết định gắn ảnh và cập nhật hồ sơ trong cùng transaction. */
export function DecisionForm({ violation, onSave }: { violation: Violation; onSave: () => void }) {
  const auth = useAuth(), action = useApiAction()
  const [decision, setDecision] = useState(''), [reason, setReason] = useState(''), [evidence, setEvidence] = useState('')
  const [uploading, setUploading] = useState(false), [uploadError, setUploadError] = useState('')
  const busy = uploading || action.busy

  async function upload(file?: File) {
    if (!file || busy) return
    const problem = uploadProblem(file)
    if (problem) { setUploadError(problem); return }
    setUploading(true); setUploadError('')
    try {
      const session = await auth.refresh(), form = new FormData()
      form.set('kind', 'EVIDENCE'); form.set('file', file)
      const saved = await readJson(await apiFetch('files', { method: 'POST', csrfToken: session.csrfToken, body: form }), parseEvidence)
      setEvidence(saved.id)
    } catch (error) { setUploadError(errorMessage(error)) }
    finally { setUploading(false) }
  }

  async function save() {
    try {
      const body = decisionPayload(violation, decision, reason, evidence)
      if (await action.run('violations/decisions', body, parseViolations)) onSave()
    } catch (error) { action.setError((error as Error).message) }
  }

  return <form className="wl-form" onSubmit={event => { event.preventDefault(); void save() }}>
    <h3>Ra quyết định xử lý</h3><Feedback error={action.error || uploadError} />
    <NotesField label="Quyết định xử lý" value={decision} onChange={event => setDecision(event.target.value)} required disabled={busy} maxLength={1000} />
    <NotesField label="Lý do và căn cứ" value={reason} onChange={event => setReason(event.target.value)} required disabled={busy} maxLength={1000} />
    <label className="booking-upload-card">{uploading ? 'Đang tải minh chứng…' : evidence ? 'Thay ảnh minh chứng' : 'Tải ảnh minh chứng'}
      <input type="file" accept="image/png,image/jpeg" disabled={busy} onChange={event => { void upload(event.target.files?.[0]); event.target.value = '' }} />
    </label><p className="wl-note">Một ảnh PNG/JPEG, tối đa 5 MiB. Kiểm tra căn cứ trước khi lưu quyết định.</p>
    {evidence && <EvidenceFiles ids={[evidence]} />}
    <Button type="submit" pending={action.busy} disabled={busy || !evidence || !decision.trim() || !reason.trim()}>Lưu quyết định</Button>
  </form>
}
