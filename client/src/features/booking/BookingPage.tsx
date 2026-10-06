import { useState } from 'react'
import { Button } from '@/shared/components/Button'
import { useResource } from '@/shared/api/hooks'
import { RoomCatalog } from '@/features/rooms/RoomCatalog'
import { BookingRecords } from './BookingRecords'
import { BookingForm } from './BookingForm'
import { parseBookingEligibility } from './api'

/** Shows room registration only while the student has no active room assignment. */
export function BookingPage() {
  const [section, setSection] = useState('rooms'), [bedId, setBedId] = useState<string | null>(null), [registering, setRegistering] = useState(false)
  const eligibility = useResource('bookings/eligibility', parseBookingEligibility)
  const canRegister = !eligibility.loading && !eligibility.error && !eligibility.data?.hasActiveRoom

  function chooseBed(id: string) {
    if (!canRegister) return
    setBedId(id)
    setRegistering(true)
  }

  return <>
    <div className="wl-segments"><Button variant="outlined" aria-pressed={section === 'rooms'} onClick={() => setSection('rooms')}>Tra cứu phòng</Button><Button variant="outlined" aria-pressed={section === 'bookings'} onClick={() => setSection('bookings')}>Hồ sơ của tôi</Button></div>
    {eligibility.error && <div className="wl-note" role="alert">Không thể kiểm tra tình trạng chỗ ở. Tải lại để thử tiếp tục. <Button variant="text" onClick={eligibility.reload}>Tải lại</Button></div>}
    {eligibility.data?.hasActiveRoom && <p className="wl-note" role="status">Bạn đã có phòng lưu trú. Chức năng đăng ký phòng hiện không khả dụng.</p>}
    {canRegister && <div className="booking-launch"><div><h2>Đăng ký chỗ ở</h2><p>Chọn giường phù hợp và chuẩn bị ba ảnh minh chứng để nộp hồ sơ.</p></div><Button onClick={() => setRegistering(true)}>Đăng ký phòng</Button></div>}
    {section === 'rooms' ? <RoomCatalog onSelectBed={canRegister ? chooseBed : undefined} /> : <BookingRecords />}
    {registering && canRegister && <BookingForm onClose={() => setRegistering(false)} onSelectBed={setBedId} bedId={bedId} onComplete={() => { setRegistering(false); setBedId(null); setSection('bookings'); eligibility.reload() }} />}
  </>
}
