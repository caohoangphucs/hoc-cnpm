import { useState } from 'react'
import { PreviewWorkflow } from '@/shared/workflow/Workflow'
import type { WorkflowOptions } from '@/shared/workflow/model'
import { PaymentsPage } from './PaymentsPage'
import type { Invoice, PreviewReply } from './model'

const invoices: Invoice[] = [
  { id: 104, code: 'HD-DEMO-202610-A101', room: 'A101', month: '2026-10', dueDate: '2026-10-18', total: 2_100_000, paid: 0, discount: 300_000, status: 'CHUA_TT', lines: [{ label: 'Tiền phòng', amount: 1_800_000 }, { label: 'Tiền điện · 120 kWh × 3.500 đ', amount: 420_000 }, { label: 'Tiền nước · 12 m³ × 15.000 đ', amount: 180_000 }] },
  { id: 103, code: 'HD-DEMO-202609-A101', room: 'A101', month: '2026-09', dueDate: '2026-09-18', total: 1_950_000, paid: 500_000, discount: 300_000, status: 'TT_MOT_PHAN', lines: [{ label: 'Tiền phòng', amount: 1_800_000 }, { label: 'Tiền điện · 90 kWh × 3.500 đ', amount: 315_000 }, { label: 'Tiền nước · 9 m³ × 15.000 đ', amount: 135_000 }] },
  { id: 102, code: 'HD-DEMO-202608-A101', room: 'A101', month: '2026-08', dueDate: '2026-08-18', total: 1_800_000, paid: 0, discount: 300_000, status: 'QUA_HAN', lines: [{ label: 'Tiền phòng', amount: 1_800_000 }, { label: 'Tiền điện · 60 kWh × 3.500 đ', amount: 210_000 }, { label: 'Tiền nước · 6 m³ × 15.000 đ', amount: 90_000 }] },
  { id: 101, code: 'HD-DEMO-202607-A101', room: 'A101', month: '2026-07', dueDate: '2026-07-18', total: 1_800_000, paid: 1_800_000, discount: 300_000, status: 'DA_TT', lines: [{ label: 'Tiền phòng và dịch vụ', amount: 2_100_000 }] },
  { id: 100, code: 'HD-DEMO-202606-A101', room: 'A101', month: '2026-06', dueDate: '2026-06-18', total: 1_780_000, paid: 0, discount: 300_000, status: 'HUY', lines: [{ label: 'Tiền phòng và dịch vụ', amount: 2_080_000 }] },
]
export default function PaymentsPreview() { return <PreviewWorkflow title="UC-08 · Thanh toán hóa đơn">{options => <PreviewPayments {...options} />}</PreviewWorkflow> }
function PreviewPayments(options: WorkflowOptions) {
  const [reply, setReply] = useState<PreviewReply>('success')
  return <><div className="payment-demo-controls"><label>Kết quả khi kiểm tra trạng thái<select aria-label="Kết quả đối soát minh họa" value={reply} onChange={event => setReply(event.target.value as PreviewReply)}><option value="success">Đã xác nhận thành công</option><option value="pending">Vẫn chờ xác nhận</option><option value="failed">Không thành công</option><option value="canceled">Đã hủy tại cổng</option><option value="review">Cần đối soát</option><option value="invalid">Kết quả không hợp lệ</option><option value="wrongAmount">Sai số tiền</option><option value="late">Thành công đến muộn</option></select></label><p>Chỉ mô phỏng phản hồi từ hệ thống; không kết nối cổng thanh toán.</p></div><PaymentsPage {...options} invoices={invoices} reply={reply} /></>
}
