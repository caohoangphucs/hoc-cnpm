import { useState } from 'react'
import { useAuth } from '@/features/auth/context'
import { apiFetch } from '@/shared/api/client'
import { errorMessage, readJson } from '@/shared/api/protocol'
import { parseEvidence, uploadProblem } from '@/features/booking/evidence'
import { EvidenceFiles } from '@/features/booking/EvidenceFiles'

/** Upload ảnh nháp; service chỉ gắn ảnh khi thao tác nghiệp vụ thành công. */
export function RepairImages({ ids, onChange, busy, onBusy, max = 10 }: {
  ids: string[]; onChange: (ids: string[]) => void; busy: boolean; onBusy: (busy: boolean) => void; max?: number
}) {
  const auth = useAuth()
  const [error, setError] = useState('')

  async function upload(file?: File) {
    if (!file || busy) return
    const problem = uploadProblem(file)
    if (problem) { setError(problem); return }
    setError('')
    onBusy(true)
    try {
      const session = await auth.refresh()
      const form = new FormData()
      form.set('kind', 'REPAIR')
      form.set('file', file)
      const response = await apiFetch('files', { method: 'POST', csrfToken: session.csrfToken, body: form })
      const saved = await readJson(response, parseEvidence)
      onChange([...ids, saved.id])
    } catch (error) { setError(errorMessage(error)) }
    finally { onBusy(false) }
  }

  return <section className="wl-form"><h3>Ảnh minh chứng</h3><p>Ảnh PNG/JPEG, tối đa 5 MiB mỗi ảnh và {max} ảnh mỗi lần gửi.</p>
    <label className="booking-upload-card">Chọn ảnh<input type="file" accept="image/png,image/jpeg" disabled={busy || ids.length >= max} onChange={event => { void upload(event.target.files?.[0]); event.target.value = '' }} /></label>
    {error && <p className="wl-error" role="alert">{error}</p>}
    {ids.length > 0 && <><EvidenceFiles ids={ids} /><button type="button" className="m3-button" disabled={busy} onClick={() => onChange([])}>Bỏ các ảnh đã chọn</button></>}
  </section>
}
