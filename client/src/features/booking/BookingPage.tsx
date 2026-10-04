import { useState } from 'react'
import { Button } from '@/shared/components/Button'
import { RoomCatalog } from '@/features/rooms/RoomCatalog'
import { BookingRecords } from './BookingRecords'
import { BookingForm } from './BookingForm'

export function BookingPage() {
  const [section, setSection] = useState('rooms'), [bedId, setBedId] = useState<string | null>(null), [registering, setRegistering] = useState(false)
  return <>
    <div className="wl-segments"><Button variant="outlined" aria-pressed={section === 'rooms'} onClick={() => setSection('rooms')}>Tra cứu phòng</Button><Button variant="outlined" aria-pressed={section === 'bookings'} onClick={() => setSection('bookings')}>Hồ sơ của tôi</Button></div>
    <div className="booking-launch"><div><h2>Đăng ký chỗ ở</h2><p>Chọn giường phù hợp và chuẩn bị ba ảnh minh chứng để nộp hồ sơ.</p></div><Button onClick={() => setRegistering(true)}>Đăng ký phòng</Button></div>
    {section === 'rooms' ? <RoomCatalog onSelectBed={id => { setBedId(id); setRegistering(true) }} /> : <BookingRecords />}
    {registering && <BookingForm onClose={() => setRegistering(false)} onSelectBed={setBedId} bedId={bedId} onComplete={() => { setRegistering(false); setBedId(null); setSection('bookings') }} />}
  </>
}
