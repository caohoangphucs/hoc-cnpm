import { useState } from 'react'
import { useResource } from '@/shared/api/hooks'
import { Icon } from '@/shared/components/Icon'
import { ServerPagination, WorkflowPage } from '@/shared/workflow/Workflow'
import { bedLabels, parseBeds } from '@/features/rooms/model'

/** Hiển thị giường thực của phòng; xét duyệt giữ nguyên giường trong hồ sơ. */
export function BookingBedMap({ roomId, bedId }: { roomId: string; bedId: string }) {
  const [page, setPage] = useState(1)
  const resource = useResource(roomId ? `rooms/${encodeURIComponent(roomId)}/beds?page=${page}&pageSize=8` : null, parseBeds)
  return <section className="bed-selection-section">
    <div className="bed-section-header"><div className="bed-section-title"><Icon name="bed" /><span>Sơ đồ giường (Phòng {roomId || 'Chưa xác định'})</span></div></div>
    <p>Giường đăng ký: {bedId}. Xét duyệt hồ sơ không đổi giường.</p>
    <WorkflowPage title="Giường trong phòng" description="Số lượng và trạng thái lấy từ dữ liệu hệ thống." view={!roomId || resource.error ? 'error' : resource.loading ? 'loading' : 'data'} error={resource.error || (!roomId ? 'Không xác định được phòng từ hồ sơ. Vui lòng tải lại sau khi backend cập nhật.' : undefined)} onRetry={resource.reload}>
      <div className="bed-grid">{resource.data?.items.map(bed => <div key={bed.id} className={`bed-card-tile ${bed.id === bedId ? 'selected requested' : bed.status === 'TRONG' ? 'available' : 'occupied'}`}>
        <Icon name="bed" /><span className="bed-number">Giường {bed.number}</span><span className="bed-status-text">{bedLabels[bed.status] || bed.status}{bed.id === bedId ? ' · Hồ sơ này' : ''}</span>
      </div>)}</div>
      <ServerPagination page={page} count={resource.data?.items.length ?? 0} total={resource.data?.total} onPage={setPage} />
    </WorkflowPage>
  </section>
}
