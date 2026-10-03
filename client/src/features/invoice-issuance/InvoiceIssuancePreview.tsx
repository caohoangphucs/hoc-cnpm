import { useState } from 'react'
import { PreviewWorkflow } from '@/shared/workflow/Workflow'
import type { WorkflowOptions } from '@/shared/workflow/model'
import { InvoiceIssuancePage } from './InvoiceIssuancePage'
import type { InvoiceRoom } from './model'

const resident = (code: string, name: string, discount = 0) => ({ code, name, discount })
const base = { period: '2026-10', roomPrice: 1_800_000, electricity: { previous: 1200, current: 1320, rate: 3500 }, water: { previous: 150, current: 162, rate: 15000 } }
const rooms: InvoiceRoom[] = [
  { ...base, id: 'A101', building: 'Tòa A', residents: [resident('SV-DEMO-101', 'Nguyễn Minh An', 50), resident('SV-DEMO-102', 'Lê Quốc Bảo'), resident('SV-DEMO-103', 'Võ Hoàng Nam')] },
  { ...base, id: 'A102', building: 'Tòa A', roomPrice: 2_000_000, electricity: { previous: 900, current: 975, rate: 3500 }, water: { previous: 80, current: 88, rate: 15000 }, residents: [resident('SV-DEMO-104', 'Trần Khánh Vy', 25), resident('SV-DEMO-105', 'Phạm Thu Hà')] },
  { ...base, id: 'A201', building: 'Tòa A', water: { previous: 50, current: null, rate: 15000 }, residents: [resident('SV-DEMO-106', 'Đặng Gia Huy')] },
  { ...base, id: 'B101', building: 'Tòa B', electricity: { previous: 800, current: 790, rate: 3500 }, residents: [resident('SV-DEMO-107', 'Đỗ Ngọc Mai')] },
  { ...base, id: 'B201', building: 'Tòa B', issued: true, residents: [resident('SV-DEMO-108', 'Bùi Anh Tuấn')] },
]
export default function InvoiceIssuancePreview() {
  return <PreviewWorkflow title="UC-17 · Phát hành hóa đơn">{options => <PreviewInvoice {...options} />}</PreviewWorkflow>
}
function PreviewInvoice(options: WorkflowOptions) {
  const [partialFailure, setPartialFailure] = useState(false)
  return <><label className="wl-check invoice-simulation"><input type="checkbox" checked={partialFailure} onChange={event => setPartialFailure(event.target.checked)} />Mô phỏng A102 lỗi khi phát hành; các phòng khác vẫn được xử lý</label><InvoiceIssuancePage {...options} rooms={rooms} initialPeriod="2026-10" failedRoom={partialFailure ? 'A102' : undefined} /></>
}
