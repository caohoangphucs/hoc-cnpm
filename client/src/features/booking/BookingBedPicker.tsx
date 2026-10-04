import { useState } from 'react'
import { useResource } from '@/shared/api/hooks'
import { Button } from '@/shared/components/Button'
import { Icon } from '@/shared/components/Icon'
import { Feedback, SelectField, ServerPagination } from '@/shared/workflow/Workflow'
import { bedLabels, parseBeds, parseRooms } from '@/features/rooms/model'

/** Chọn phòng và giường ngay trong form, chỉ dùng trạng thái từ API. */
export function BookingBedPicker({ bedId, onSelect, disabled }: { bedId: string | null; onSelect: (id: string | null) => void; disabled: boolean }) {
  const [roomId, setRoomId] = useState(''), [roomPage, setRoomPage] = useState(1), [bedPage, setBedPage] = useState(1)
  const rooms = useResource(`rooms?page=${roomPage}&pageSize=8`, parseRooms)
  const beds = useResource(roomId ? `rooms/${encodeURIComponent(roomId)}/beds?page=${bedPage}&pageSize=8` : null, parseBeds)
  const room = rooms.data?.items.find(item => item.id === roomId)
  return <div className="booking-bed-picker">
    <Feedback error={rooms.error || beds.error} />
    <SelectField label="Chọn phòng" value={roomId} disabled={disabled || rooms.loading} onChange={event => { setRoomId(event.target.value); setBedPage(1); onSelect(null) }}>
      <option value="">Chọn phòng để xem giường</option>
      {rooms.data?.items.map(item => <option key={item.id} value={item.id} disabled={!item.availableBeds}>{item.id} · {item.gender === 'NAM' ? 'Nam' : item.gender === 'NU' ? 'Nữ' : 'Tất cả'} · {item.availableBeds} giường trống</option>)}
    </SelectField>
    <ServerPagination page={roomPage} count={rooms.data?.items.length ?? 0} total={rooms.data?.total} onPage={page => { setRoomPage(page); setRoomId(''); onSelect(null) }} busy={disabled} />
    {beds.loading && <p role="status">Đang tải giường…</p>}
    {roomId && <div className="booking-bed-grid">{beds.data?.items.map(bed => <Button key={bed.id} variant="outlined" aria-pressed={bed.id === bedId} disabled={disabled || bed.status !== 'TRONG' || !room?.availableBeds} onClick={() => onSelect(bed.id)}><Icon name="bed" /><strong>{bed.number}</strong><span>{bed.id === bedId ? 'Đã chọn' : bedLabels[bed.status] || bed.status}</span></Button>)}</div>}
    {roomId && <ServerPagination page={bedPage} count={beds.data?.items.length ?? 0} total={beds.data?.total} onPage={setBedPage} busy={disabled} />}
    <p className="wl-note">Giường đã chọn: <strong>{bedId || 'Chưa chọn giường'}</strong>. Hệ thống kiểm tra giới tính và tình trạng giường khi gửi hồ sơ.</p>
  </div>
}
