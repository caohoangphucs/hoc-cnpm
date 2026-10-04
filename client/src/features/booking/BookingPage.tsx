import { useState } from 'react'
import { Button } from '@/shared/components/Button'
import { RoomCatalog } from '@/features/rooms/RoomCatalog'
import { BookingRecords } from './BookingRecords'
import { BookingForm } from './BookingForm'

export function BookingPage() {
  const [section, setSection] = useState('rooms'), [bedId, setBedId] = useState<string | null>(null)
  return <>
    <div className="wl-segments"><Button variant="outlined" aria-pressed={section === 'rooms'} onClick={() => setSection('rooms')}>Tra cứu phòng</Button><Button variant="outlined" aria-pressed={section === 'bookings'} onClick={() => setSection('bookings')}>Hồ sơ của tôi</Button></div>
    {section === 'rooms' ? <><RoomCatalog onSelectBed={setBedId} /><BookingForm onSelectBed={setBedId} bedId={bedId} onComplete={() => { setBedId(null); setSection('bookings') }} /></> : <BookingRecords />}
  </>
}
