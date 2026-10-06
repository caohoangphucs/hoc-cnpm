import { useState } from 'react'
import { Button } from '@/shared/components/Button'
import { TextField } from '@/shared/components/TextField'
import { Dialog } from '@/shared/components/Dialog'
import { Feedback } from '@/shared/workflow/Workflow'
import { useApiAction } from '@/shared/api/hooks'
import { activityPayload, parseActivity, type Activity } from './model'
const localTime = (value: string) => new Date(Date.parse(value) + 7 * 3600000).toISOString().slice(0, 16)
export function ActivityCreateDialog({ activity, onClose, onSaved }: { activity?: Activity; onClose: () => void; onSaved: () => void }) {
  const [semester, setSemester] = useState(activity?.semesterId.toString() ?? ''), [title, setTitle] = useState(activity?.title ?? ''), [start, setStart] = useState(activity ? localTime(activity.startAt) : ''), [end, setEnd] = useState(activity ? localTime(activity.endAt) : ''), [capacity, setCapacity] = useState(activity?.capacity.toString() ?? ''), [points, setPoints] = useState(activity?.points.toString() ?? '')
  const action = useApiAction()
  async function submit() {
    let body: ReturnType<typeof activityPayload>
    try { body = activityPayload(semester, title, start, end, capacity, points) } catch (error) { action.setError((error as Error).message); return }
    if (await action.run(activity ? `activities/${activity.id}` : 'activities', body, parseActivity, activity ? 'PUT' : 'POST')) { onSaved(); onClose() }
  }
  return <Dialog title={activity ? 'Sửa hoạt động công ích' : 'Mở hoạt động công ích'} busy={action.busy} onClose={onClose} actions={<><Button variant="text" disabled={action.busy} onClick={onClose}>Hủy</Button><Button type="submit" form="activity-edit" pending={action.busy}>{activity ? 'Lưu thay đổi' : 'Mở đăng ký'}</Button></>}><Feedback error={action.error} /><form id="activity-edit" className="wl-form community-create" onSubmit={e => { e.preventDefault(); void submit() }}><fieldset disabled={action.busy}><TextField label="Mã học kỳ" inputMode="numeric" value={semester} onChange={e => setSemester(e.target.value)} required /><TextField label="Tên hoạt động" value={title} onChange={e => setTitle(e.target.value)} maxLength={255} supportingText={`${title.length}/255 ký tự`} required /><TextField label="Bắt đầu (giờ Việt Nam)" type="datetime-local" value={start} onChange={e => setStart(e.target.value)} required /><TextField label="Kết thúc (giờ Việt Nam)" type="datetime-local" value={end} onChange={e => setEnd(e.target.value)} required /><TextField label="Số chỗ" type="number" min={1} max={2147483647} step={1} value={capacity} onChange={e => setCapacity(e.target.value)} required /><TextField label="Điểm công ích" type="number" min={1} max={2147483647} step={1} value={points} onChange={e => setPoints(e.target.value)} required /></fieldset></form></Dialog>
}
