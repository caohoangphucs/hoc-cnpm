import { Button } from '@/shared/components/Button'
import { Facts, Status } from '@/shared/workflow/Workflow'
import { dateTime, money } from '@/shared/workflow/model'
import { countdown, gatewayLabels, paymentLabels, paymentState, type PaymentSession } from './model'

export function PaymentSessionCard({ session, now, busy, returned, preview, onCheck, onGateway, onReceipt, onAgain, onExpire }: {
  session: PaymentSession; now: number; busy: boolean; returned: boolean; preview?: boolean
  onCheck: () => void; onGateway: () => void; onReceipt: () => void; onAgain: () => void; onExpire: () => void
}) {
  const status = paymentState(session, now)
  return <div className="payment-session" aria-labelledby="payment-session-heading">
    <div className="payment-card-heading"><h2 id="payment-session-heading">Phiên thanh toán</h2><Status tone={status === 'SUCCESS' ? 'primary' : ['FAILED', 'NEEDS_REVIEW'].includes(status) ? 'danger' : 'neutral'}>{paymentLabels[status]}</Status></div>
    <Facts items={[["Mã giao dịch", `GD-MINH-HOA-${session.id.slice(0, 8)}`], ["Phương thức", gatewayLabels[session.gateway]], ["Số tiền", money(session.amount)], ["Số hóa đơn", session.allocations.length], ["Thời điểm tạo", dateTime(new Date(session.createdAt).toISOString())]]} />
    {status === 'PENDING' && <><div className="payment-countdown"><span>Thời gian còn lại</span><time dateTime={new Date(session.expiresAt).toISOString()}>{countdown(session, now)}</time></div><p className="wl-note" role="status">{returned ? 'Đã quay về KTX. Hệ thống đang chờ xác nhận kết quả; dư nợ chưa được cập nhật.' : 'Phiên có hiệu lực 15 phút. Hoàn tất tại cổng thanh toán, sau đó kiểm tra kết quả ở đây.'}</p></>}
    {status === 'SUCCESS' && <p className="wl-note" role="status">Đã xác nhận thanh toán thành công. Các khoản đã chọn được ghi nhận và biên lai đã sẵn sàng trong bản xem trước.</p>}
    {status === 'FAILED' && <p className="wl-note" role="status">{session.failure === 'canceled' ? 'Đã hủy tại cổng thanh toán.' : 'Cổng thanh toán báo giao dịch không thành công.'} Dư nợ giữ nguyên; bạn có thể chọn hóa đơn để thử lại.</p>}
    {status === 'EXPIRED' && <p className="wl-note" role="status">Phiên đã hết hạn, dư nợ chưa được cập nhật. Nếu đã bị trừ tiền, kiểm tra trạng thái hoặc liên hệ Kế toán trước khi thanh toán lại.</p>}
    {status === 'NEEDS_REVIEW' && <p className="wl-note" role="status">Giao dịch cần đối soát, dư nợ chưa được cập nhật. Liên hệ Kế toán và cung cấp mã giao dịch; chờ xử lý trước khi tạo phiên mới.</p>}
    <ul className="payment-allocations" aria-label="Phân bổ phiên thanh toán">{session.allocations.map(allocation => <li key={allocation.invoiceId}><span>{allocation.code}<small>Phòng {allocation.room} · {allocation.month.split('-').reverse().join('/')}</small></span><strong>{money(allocation.amount)}</strong></li>)}</ul>
    <div className="payment-session-actions">
      {status === 'PENDING' && <Button disabled={busy} onClick={onGateway}>Tiếp tục tại {gatewayLabels[session.gateway]}</Button>}
      {status === 'SUCCESS' && <Button disabled={busy} onClick={onReceipt}>Xem biên lai</Button>}
      <Button variant="outlined" pending={busy} onClick={onCheck}>Kiểm tra trạng thái</Button>
      {['SUCCESS', 'FAILED', 'EXPIRED'].includes(status) && <Button variant="text" disabled={busy} onClick={onAgain}>{status === 'SUCCESS' ? 'Chọn hóa đơn khác' : 'Chọn hóa đơn để thử lại'}</Button>}
      {preview && status === 'PENDING' && <Button variant="text" disabled={busy} onClick={onExpire}>Mô phỏng hết hạn phiên</Button>}
    </div>
  </div>
}
