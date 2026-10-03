import { useState } from 'react'
import { Button } from '@/shared/components/Button'
import { TextField } from '@/shared/components/TextField'
import { Dialog } from '@/shared/components/Dialog'
import { Feedback } from '@/shared/workflow/Workflow'
import { useApiAction } from '@/shared/api/hooks'
import { activityPayload, parseActivity } from './model'
export function ActivityCreateDialog({ onClose, onSaved }: { onClose: () => void; onSaved: () => void }) {
  const [semester, setSemester] = useState(''), [title, setTitle] = useState(''), [start, setStart] = useState(''), [end, setEnd] = useState(''), [capacity, setCapacity] = useState(''), [points, setPoints] = useState('')
  const action = useApiAction()
  async function submit() {
    let body: ReturnType<typeof activityPayload>
    try { body = activityPayload(semester, title, start, end, capacity, points) } catch (error) { action.setError((error as Error).message); return }
    if (await action.run('activities', body, parseActivity)) { onSaved(); onClose() }
  }
  return <Dialog title="Mở hoạt động công ích" busy={action.busy} onClose={onClose} actions={<><Button variant="text" disabled={action.busy} onClick={onClose}>Hủy</Button><Button pending={action.busy} onClick={() => void submit()}>Mở đăng ký</Button></>}><Feedback error={action.error} /><form className="wl-form community-create" onSubmit={e => e.preventDefault()}><fieldset disabled={action.busy}><TextField label="Mã học kỳ" inputMode="numeric" value={semester} onChange={e => setSemester(e.target.value)} required /><TextField label="Tên hoạt động" value={title} onChange={e => setTitle(e.target.value)} maxLength={1000} required /><TextField label="Bắt đầu (giờ Việt Nam)" type="datetime-local" value={start} onChange={e => setStart(e.target.value)} required /><TextField label="Kết thúc (giờ Việt Nam)" type="datetime-local" value={end} onChange={e => setEnd(e.target.value)} required /><TextField label="Số chỗ" type="number" min={1} step={1} value={capacity} onChange={e => setCapacity(e.target.value)} required /><TextField label="Điểm công ích" type="number" min={1} step={1} value={points} onChange={e => setPoints(e.target.value)} required /></fieldset></form></Dialog>
}
