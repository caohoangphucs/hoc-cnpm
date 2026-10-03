import { useRef, useState } from 'react'
import { Button } from '@/shared/components/Button'
import { Icon } from '@/shared/components/Icon'
import { TextField } from '@/shared/components/TextField'
import { availableBeds, bedLabels, compatible, genderLabels, normalizeSearch, type Room, type Student } from './model'

export function RoomPicker({ rooms, student, selectedRoom, bedId, onRoom, onBed, onNext, allowed }: {
  rooms: readonly Room[]; student: Student; selectedRoom: Room | null; bedId: string; onRoom: (room: Room | null) => void; onBed: (id: string) => void; onNext: () => void; allowed: boolean
}) {
  const [query, setQuery] = useState('')
  const [building, setBuilding] = useState('')
  const [type, setType] = useState('')
  const [onlyAvailable, setOnlyAvailable] = useState(false)
  const detail = useRef<HTMLElement>(null)
  const terms = normalizeSearch(query).split(/\s+/).filter(Boolean)
  const filtered = rooms.filter((room) => (!building || room.buildingId === building) && (!type || room.roomType === type) && (!onlyAvailable || availableBeds(room) > 0) && terms.every((term) => normalizeSearch(`${room.name} ${room.buildingName}`).includes(term)))
  const buildings = [...new Map(rooms.map((room) => [room.buildingId, room.buildingName])).entries()]
  const types = [...new Set(rooms.map((room) => room.roomType))]
  const hasFilters = Boolean(query || building || type || onlyAvailable)
  function clear() { setQuery(''); setBuilding(''); setType(''); setOnlyAvailable(false) }
  function chooseRoom(room: Room) {
    onRoom(room)
    if (window.matchMedia('(max-width: 1199px)').matches) requestAnimationFrame(() => { detail.current?.scrollIntoView({ block: 'start' }); detail.current?.focus({ preventScroll: true }) })
  }
  return <>
    <section className={`booking-filter-card ${selectedRoom ? 'has-selection' : ''}`} aria-label="Tra cứu phòng">
      <div className="booking-filters"><TextField label="Tìm phòng" placeholder="Tên phòng hoặc tòa nhà" type="search" value={query} onChange={(event) => setQuery(event.target.value)} trailing={<Icon name="search" />} />
        <label className="booking-select"><span>Tòa nhà</span><select aria-label="Lọc theo tòa nhà" value={building} onChange={(event) => setBuilding(event.target.value)}><option value="">Tất cả tòa nhà</option>{buildings.map(([id, name]) => <option key={id} value={id}>{name}</option>)}</select></label>
        <label className="booking-select"><span>Loại phòng</span><select aria-label="Lọc theo loại phòng" value={type} onChange={(event) => setType(event.target.value)}><option value="">Tất cả loại phòng</option>{types.map((item) => <option key={item}>{item}</option>)}</select></label>
      </div><div className="booking-filter-bottom"><label className="booking-check"><input type="checkbox" checked={onlyAvailable} onChange={(event) => setOnlyAvailable(event.target.checked)} />Chỉ hiện phòng còn giường trống</label><Button variant="text" disabled={!hasFilters} onClick={clear}>Xóa bộ lọc</Button></div>
    </section>
    <div className={`booking-room-layout ${selectedRoom ? 'has-selection' : ''}`}><section aria-labelledby="room-results-title"><div className="booking-section-heading"><h2 id="room-results-title" tabIndex={-1}>Danh sách phòng</h2><p role="status">{filtered.length} phòng</p></div>
      {!filtered.length ? <div className="booking-empty"><Icon name="search" /><h3>Không tìm thấy phòng phù hợp</h3><p>Thử thay đổi tòa nhà, loại phòng hoặc từ khóa.</p><Button variant="outlined" onClick={clear}>Xóa bộ lọc</Button></div> :
        <div className="booking-room-grid">{filtered.map((room) => {
          const count = availableBeds(room), fits = compatible(room, student), chosen = room.id === selectedRoom?.id
          return <article key={room.id} className={`booking-room-card ${chosen ? 'is-selected' : ''}`}><div className="booking-room-card-top"><span className="booking-room-symbol"><Icon name="building" /></span><span className={`booking-pill ${!count ? 'booking-pill--muted' : ''}`}>{count ? `${count} giường trống` : 'Hết giường trống'}</span></div>
            <h3>Phòng {room.name}</h3><p>{room.buildingName}</p><dl className="booking-room-facts"><div><dt>Loại phòng</dt><dd>{room.roomType}</dd></div><div><dt>Sức chứa</dt><dd>{room.capacity} người</dd></div><div><dt>Đối tượng</dt><dd>{genderLabels[room.gender]}</dd></div></dl>
            {!fits && <p className="booking-room-note"><Icon name="info" />Không phù hợp giới tính hồ sơ của bạn.</p>}
            <Button variant={chosen ? 'filled' : 'outlined'} onClick={() => chooseRoom(room)} aria-pressed={chosen} aria-label={`Xem giường phòng ${room.name}`}>{chosen ? 'Đang xem giường' : 'Xem giường'}<Icon name="arrow" /></Button>
          </article>
        })}</div>}
    </section><section className="booking-bed-panel" ref={detail} tabIndex={-1} aria-labelledby="bed-panel-title">
      {selectedRoom && <Button variant="text" className="booking-back-rooms" onClick={() => { onRoom(null); requestAnimationFrame(() => document.querySelector<HTMLElement>('#room-results-title')?.focus()) }}><Icon name="back" />Danh sách phòng</Button>}
      <div className="booking-section-heading"><h2 id="bed-panel-title">Chọn giường</h2><Icon name="bed" /></div>
      {!selectedRoom ? <div className="booking-bed-placeholder"><span><Icon name="bed" /></span><h3>Bắt đầu từ một phòng</h3><p>Chọn “Xem giường” để xem các giường và tình trạng hiện tại.</p></div> : <>
        <h3>Phòng {selectedRoom.name}</h3><p className="booking-subtle">{selectedRoom.buildingName} · {selectedRoom.roomType}</p>
        <fieldset className="booking-beds"><legend className="sr-only">Giường phòng {selectedRoom.name}</legend>{selectedRoom.beds.map((bed) => <label key={bed.id} className={`booking-bed ${bed.status !== 'TRONG' ? 'is-unavailable' : ''} ${bedId === bed.id ? 'is-selected' : ''}`}>
          <input type="radio" name="booking-bed" checked={bedId === bed.id} onChange={() => onBed(bed.id)} disabled={!allowed || !compatible(selectedRoom, student) || bed.status !== 'TRONG'} value={bed.id} />
          <Icon name={bed.status === 'KHOA' ? 'lock' : 'bed'} /><strong>Giường {bed.number}</strong><span>{bedLabels[bed.status]}</span>
        </label>)}</fieldset>
        {(!compatible(selectedRoom, student) || !allowed) && <p className="booking-room-note"><Icon name="info" />{!compatible(selectedRoom, student) ? 'Tòa nhà không phù hợp giới tính hồ sơ của bạn.' : 'Bạn chưa đủ điều kiện đăng ký trong đợt này.'}</p>}
        <div className="booking-selected-bed" role="status">{bedId ? <><Icon name="check" />Đã chọn giường {selectedRoom.beds.find((bed) => bed.id === bedId)?.number}</> : 'Chọn một giường trống để tiếp tục.'}</div>
        <Button disabled={!bedId || !allowed} onClick={onNext}>Tiếp tục: Hồ sơ<Icon name="arrow" /></Button>
      </>}
      <p className="booking-bed-note"><Icon name="info" />Chọn giường chưa tạo giữ chỗ. Giường chỉ được giữ 48 giờ sau khi đăng ký được hệ thống tiếp nhận.</p>
    </section></div>
  </>
}
