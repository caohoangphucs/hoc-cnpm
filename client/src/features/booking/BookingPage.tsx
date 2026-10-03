import { useState } from 'react'
import { Button } from '@/shared/components/Button'
import { RoomCatalog } from '@/features/rooms/RoomCatalog'
import { BookingRecords } from './BookingRecords'
export function BookingPage() {
  const [section, setSection] = useState('rooms')
  return <><div className="wl-segments"><Button variant="outlined" aria-pressed={section === 'rooms'} onClick={() => setSection('rooms')}>Tra cứu phòng</Button><Button variant="outlined" aria-pressed={section === 'bookings'} onClick={() => setSection('bookings')}>Hồ sơ của tôi</Button></div>{section === 'rooms' ? <><RoomCatalog /><section className="wl-card wl-panel"><h2>Đăng ký phòng</h2><p>Chức năng gửi hồ sơ cần thông tin đợt đăng ký và minh chứng đã tải lên. Hệ thống chưa hỗ trợ các bước này; vui lòng liên hệ Ban quản lý để được hướng dẫn.</p><Button disabled>Gửi hồ sơ đăng ký</Button></section></> : <BookingRecords />}</>
}
