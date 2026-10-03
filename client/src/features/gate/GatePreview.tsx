import { useState } from 'react'
import { PreviewWorkflow, SelectField } from '@/shared/workflow/Workflow'
import { TextField } from '@/shared/components/TextField'
import type { WorkflowOptions } from '@/shared/workflow/model'
import { GatePage } from './GatePage'
import type { BarrierStatus, GateData } from './model'

function sample() {
  const today = new Date(Date.now() + 7 * 3600000).toISOString().slice(0, 10), now = Date.parse(`${today}T23:01:00+07:00`)
  const base = { name: 'Nguyễn Minh An (minh họa)', room: 'A101', active: true, resident: true, validFrom: now - 86400000, validTo: now + 365 * 86400000 }
  const data: GateData = { students: [
    { ...base, id: 'SV-DEMO-101', card: 'CARD-DEMO-101' }, { ...base, id: 'SV-DEMO-102', card: 'CARD-DEMO-102', name: 'Lê Quốc Bảo (minh họa)', validTo: now - 1000 }, { ...base, id: 'SV-DEMO-103', card: 'CARD-DEMO-103', name: 'Võ Hoàng Nam (minh họa)', resident: false },
  ], visitors: [
    { id: 'KH-DEMO-01', name: 'Nguyễn Văn Minh (minh họa)', identity: 'DEMO-ID-1001', host: 'SV-DEMO-101 · Nguyễn Minh An', room: 'A101', approved: true, from: now - 3600000, to: now + 3600000, inside: false },
    { id: 'KH-DEMO-02', name: 'Lê Thị Hoa (minh họa)', identity: 'DEMO-ID-1002', host: 'SV-DEMO-102 · Lê Quốc Bảo', room: 'A102', approved: false, from: now - 3600000, to: now + 3600000, inside: false },
  ], entries: [], visitorEntries: [] }
  return { today, now, data }
}
export default function GatePreview() { return <PreviewWorkflow title="UC-25 · Ghi nhận về KTX">{options => <PreviewGate {...options} />}</PreviewWorkflow> }
function PreviewGate({ initialSection = 'student', ...options }: WorkflowOptions & { initialSection?: 'student' | 'visitor' }) {
  const [initial] = useState(sample), [time, setTime] = useState(`${initial.today}T23:01`), [device, setDevice] = useState<BarrierStatus>('OPENED')
  const parsed = Date.parse(`${time}:00+07:00`)
  return <><div className="wl-preview-controls gate-simulation"><TextField label="Giờ server minh họa (UTC+7)" type="datetime-local" value={time} onChange={event => setTime(event.target.value)} errorText={!Number.isFinite(parsed) ? 'Chọn thời gian hợp lệ.' : undefined} /><SelectField label="Phản hồi thiết bị minh họa" value={device} onChange={event => setDevice(event.target.value as BarrierStatus)}><option value="OPENED">Đã mở cổng</option><option value="FAILED">Thiết bị lỗi</option><option value="PENDING">Chưa phản hồi</option></SelectField><p>Thẻ: CARD-DEMO-101 (hợp lệ), 102 (hết hạn), 103 (đã trả phòng). Khách: KH-DEMO-01 (đã duyệt), 02 (chờ duyệt).</p></div><GatePage {...options} initialSection={initialSection} preview={options.preview && Number.isFinite(parsed)} initialData={initial.data} serverNow={Number.isFinite(parsed) ? parsed : initial.now} deviceResult={device} /></>
}

export function VisitorGatePreview() { return <PreviewWorkflow title="UC-25 · Kiểm soát khách thăm">{options => <PreviewGate {...options} initialSection="visitor" />}</PreviewWorkflow> }
