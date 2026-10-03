import { Facts, Status } from '@/shared/workflow/Workflow'
import { dateTime } from '@/shared/workflow/model'
import { priorityLabels, statusLabels, type Repair, type Supply } from './model'

export function RepairDetail({ repair, supplies, purchaseCount }: { repair: Repair; supplies: readonly Supply[]; purchaseCount: number }) {
  return <div className="repair-detail">
    <div className="repair-detail-title"><span className="eyebrow">{repair.code} · Phòng {repair.room}</span><h3>{repair.title}</h3><div><Status tone={repair.status === 'DANG_XU_LY' ? 'primary' : 'neutral'}>{statusLabels[repair.status]}</Status><Status tone={repair.priority === 'KHAN_CAP' ? 'danger' : 'neutral'}>{priorityLabels[repair.priority]}</Status></div></div>
    <Facts items={[
      ['Người báo', repair.student], ['Nhóm sự cố', repair.category], ['Thời điểm báo', dateTime(repair.createdAt)],
      ['Người phụ trách', repair.technician ?? 'Chưa tiếp nhận'], ['Lịch xử lý', repair.schedule ? dateTime(repair.schedule) : 'Chưa có lịch'],
      ...(repair.completedAt ? [['Hoàn thành lúc', dateTime(repair.completedAt)] as [string, string]] : []),
      ...(repair.warrantyUntil ? [['Bảo hành đến', repair.warrantyUntil.split('-').reverse().join('/')] as [string, string]] : []),
    ]} />
    <section><h3>Mô tả sự cố</h3><p className="repair-description">{repair.description}</p><p className="repair-muted">Chưa có ảnh minh chứng trong phiếu này.</p></section>
    {repair.category !== 'Thiết bị' && <p className="wl-note">{repair.category === 'Điện' ? 'Ngắt cầu dao và kiểm tra an toàn điện trước khi sửa chữa.' : 'Khóa van cấp nước trước khi kiểm tra, thay thế đường ống.'}</p>}
    {repair.warranty && <section className="wl-note"><h3>Đang chờ bảo hành ngoài</h3><p>{repair.warranty.provider} · Dự kiến bàn giao {dateTime(repair.warranty.expectedAt)}.</p><p>Phiếu vẫn đang xử lý. Chỉ hoàn thành sau khi thiết bị được bàn giao và kiểm tra.</p></section>}
    {repair.note && <section><h3>Ghi chú kỹ thuật</h3><p className="repair-description">{repair.note}</p></section>}
    {purchaseCount > 0 && <p className="wl-note">Có {purchaseCount} dòng đề nghị mua bổ sung đang chờ xử lý. Tồn kho chưa được tăng.</p>}
    {repair.status === 'HOAN_THANH' && <section><h3>Vật tư đã xuất</h3>{repair.usedSupplies.length ? <ul className="repair-used">{repair.usedSupplies.map(line => { const supply = supplies.find(s => s.id === line.supplyId); return <li key={line.supplyId}><span>{supply?.name ?? line.supplyId}</span><strong>{line.quantity} {supply?.unit}</strong></li> })}</ul> : <p className="repair-muted">Không sử dụng vật tư từ kho.</p>}<p className="wl-note">Thông báo nghiệm thu và lời mời đánh giá sẽ được hệ thống xử lý sau khi lưu thành công.</p></section>}
    <section><h3>Lịch sử xử lý</h3><ol className="wl-timeline">{repair.history.map((event, index) => <li key={`${event.at}-${index}`}><p>{event.note}</p><small>{dateTime(event.at)} · {event.actor}</small></li>)}</ol></section>
  </div>
}
