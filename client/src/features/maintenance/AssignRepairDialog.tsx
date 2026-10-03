import { useState } from 'react'
import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { TextField } from '@/shared/components/TextField'
import { Feedback } from '@/shared/workflow/Workflow'
import { useApiAction } from '@/shared/api/hooks'
import { command, vietnamTime } from '@/shared/api/protocol'
import type { Repair } from './model'
export function AssignRepairDialog({ repair, onClose, onSave }: { repair: Repair; onClose: () => void; onSave: () => void }) {
  const [technician, setTechnician] = useState(''), [schedule, setSchedule] = useState(''), action = useApiAction()
  async function submit() {
    const scheduledAt = vietnamTime(schedule)
    if (!/^[1-9]\d*$/.test(technician) || !Number.isSafeInteger(Number(technician)) || !scheduledAt) { action.setError('Nhập mã tài khoản kỹ thuật viên và thời gian hợp lệ.'); return }
    if (await action.run(`repairs/${repair.id}/assignment`, { technicianAccountId: Number(technician), scheduledAt, version: repair.version }, command)) onSave()
  }
  return <Dialog title={`Phân công SC-${repair.id}`} onClose={onClose} busy={action.busy} actions={<><Button variant="text" disabled={action.busy} onClick={onClose}>Hủy</Button><Button type="submit" form="repair-assignment" pending={action.busy}>Phân công</Button></>}><Feedback error={action.error} /><form id="repair-assignment" className="wl-form" onSubmit={e => { e.preventDefault(); void submit() }}><TextField label="Mã tài khoản kỹ thuật viên" value={technician} onChange={e => setTechnician(e.target.value)} disabled={action.busy} inputMode="numeric" required supportingText="Dùng mã tài khoản do Ban quản lý cung cấp. Hệ thống kiểm tra tài khoản đang hoạt động và có vai trò kỹ thuật." /><TextField label="Lịch xử lý (giờ Việt Nam)" type="datetime-local" value={schedule} onChange={e => setSchedule(e.target.value)} disabled={action.busy} required /></form></Dialog>
}
