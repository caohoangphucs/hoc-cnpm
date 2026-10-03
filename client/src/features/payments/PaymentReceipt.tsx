import { createPortal } from 'react-dom'
import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { Facts, Status } from '@/shared/workflow/Workflow'
import { dateTime, money } from '@/shared/workflow/model'
import { gatewayLabels, type PaymentSession } from './model'

function ReceiptContent({ session }: { session: PaymentSession }) {
  return <article className="payment-receipt"><header><p className="eyebrow">KTX SMART HUB</p><h2>Biên lai thanh toán</h2><Status>MINH HỌA · KHÔNG CÓ GIÁ TRỊ THU TIỀN</Status></header><Facts items={[["Số biên lai", `BL-MINH-HOA-${session.id.slice(0, 8)}`], ["Mã giao dịch", `GD-MINH-HOA-${session.id.slice(0, 8)}`], ["Phương thức", gatewayLabels[session.gateway]], ["Ngày xác nhận", dateTime(new Date(session.confirmedAt!).toISOString())]]} /><h3>Khoản thanh toán</h3><ul className="payment-allocations">{session.allocations.map(allocation => <li key={allocation.invoiceId}><span>{allocation.code}<small>Phòng {allocation.room} · Kỳ {allocation.month.split('-').reverse().join('/')}</small></span><strong>{money(allocation.amount)}</strong></li>)}</ul><div className="payment-receipt-total"><strong>Tổng đã thanh toán</strong><strong>{money(session.amount)}</strong></div><p>Biên lai xem trước dùng dữ liệu minh họa. Chưa có giao dịch thật, chữ ký số hoặc QR kiểm chứng. Biên lai chính thức sẽ được lấy từ hệ thống sau khi thanh toán được xác nhận.</p></article>
}
export function PaymentReceipt({ session, onClose }: { session: PaymentSession; onClose: () => void }) {
  if (session.status !== 'SUCCESS' || session.confirmedAt === undefined) return null
  return <><Dialog title="Biên lai thanh toán" panel onClose={onClose} actions={<><Button variant="text" onClick={onClose}>Đóng</Button><Button onClick={() => window.print()}>In biên lai minh họa</Button></>}><ReceiptContent session={session} /></Dialog>{createPortal(<div className="payment-print-root"><ReceiptContent session={session} /></div>, document.body)}</>
}
