import { useCallback, useEffect, useRef, useState } from 'react'
import { useBeforeUnload, useBlocker } from 'react-router'
import { PageHeader } from '@/shared/components/PageHeader'
import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { Icon } from '@/shared/components/Icon'
import { RoomPicker } from './RoomPicker'
import { DocumentField } from './DocumentField'
import { availableBeds, compatible, formatBookingTime, genderLabels, validateDocuments, type BookingReceipt, type BookingRequest, type BookingView, type DocumentErrors, type Documents, type Eligibility, type RegistrationWindow, type Room, type Student, type Submission } from './model'

const steps = ['Chọn giường', 'Hồ sơ đăng ký', 'Kiểm tra & gửi']
const blankDocuments: Documents = { identity: null, studentCard: null, evidence: [] }

export function BookingPage({ initialRooms = [], student = null, registration = null, eligibility = 'eligible', view = 'error', preview = false, onSubmit }: {
  initialRooms?: readonly Room[]; student?: Student | null; registration?: RegistrationWindow | null; eligibility?: Eligibility; view?: BookingView; preview?: boolean
  onSubmit?: (request: BookingRequest, room: Room) => Promise<Submission>
}) {
  const [rooms, setRooms] = useState(() => [...initialRooms])
  const [roomId, setRoomId] = useState('')
  const [bedId, setBedId] = useState('')
  const [step, setStep] = useState(1)
  const [documents, setDocuments] = useState<Documents>(blankDocuments)
  const [checking, setChecking] = useState<Partial<Record<keyof Documents, boolean>>>({})
  const onChecking = useCallback((field: keyof Documents, value: boolean) => { setChecking((current) => current[field] === value ? current : { ...current, [field]: value }) }, [])
  const checkingImages = Object.values(checking).some(Boolean)
  const [accepted, setAccepted] = useState(false)
  const [errors, setErrors] = useState<DocumentErrors>({})
  const [message, setMessage] = useState('')
  const [sending, setSending] = useState(false)
  const [receipt, setReceipt] = useState<BookingReceipt | null>(null)
  const [termsOpen, setTermsOpen] = useState(false)
  const panel = useRef<HTMLDivElement>(null)
  const previousStage = useRef('1')
  const generation = useRef(0)
  const submitLock = useRef(false)
  useEffect(() => () => { generation.current++ }, [])
  const room = rooms.find((item) => item.id === roomId) ?? null
  const bed = room?.beds.find((item) => item.id === bedId)
  const allowed = Boolean(registration?.open && eligibility === 'eligible' && !receipt)
  const dirty = !receipt && Boolean(bedId || documents.identity || documents.studentCard || documents.evidence.length || accepted)
  const blocker = useBlocker(({ currentLocation, nextLocation }) => dirty && (currentLocation.pathname !== nextLocation.pathname || currentLocation.search !== nextLocation.search))
  useBeforeUnload(useCallback((event) => { if (dirty) { event.preventDefault(); event.returnValue = '' } }, [dirty]))
  const stage = receipt ? 'receipt' : String(step)
  useEffect(() => {
    if (previousStage.current !== stage) { panel.current?.scrollIntoView({ block: 'start' }); panel.current?.querySelector<HTMLElement>('h2')?.focus({ preventScroll: true }); previousStage.current = stage }
  }, [stage])

  function setDocument(key: keyof Documents, files: readonly File[]) {
    setDocuments((current) => ({ ...current, [key]: key === 'evidence' ? files : files[0] ?? null }))
    setErrors((current) => ({ ...current, [key]: undefined }))
  }
  function checkDocuments() {
    if (checkingImages) { setMessage('Vui lòng chờ kiểm tra ảnh hoàn tất.'); return false }
    const issues = validateDocuments(documents, accepted)
    setErrors(issues)
    if (Object.values(issues).some(Boolean)) {
      setMessage('Vui lòng kiểm tra các mục hồ sơ được đánh dấu.')
      requestAnimationFrame(() => panel.current?.querySelector<HTMLElement>('[aria-invalid="true"]:not([type="file"])')?.focus())
      return false
    }
    setMessage(''); return true
  }
  async function send() {
    if (submitLock.current || !preview || !onSubmit || !student || !registration || !room || !bed || !allowed) return
    if (!compatible(room, student) || bed.status !== 'TRONG') { setBedId(''); setStep(1); setMessage('Giường này không còn phù hợp. Vui lòng chọn lại.'); return }
    if (!checkDocuments()) { setStep(2); return }
    submitLock.current = true
    setSending(true); setMessage('')
    const run = generation.current
    try {
      const result = await onSubmit({ bedId, windowId: registration.id, documents, acceptedTerms: true }, room)
      if (run !== generation.current) return
      if (result.ok) { setReceipt(result.receipt); setDocuments(blankDocuments); setAccepted(false); setErrors({}) }
      else {
        setMessage(result.message)
        if (result.kind === 'conflict') {
          setRooms((current) => current.map((item) => ({ ...item, beds: item.beds.map((itemBed) => itemBed.id === bedId ? { ...itemBed, status: 'GIU_CHO' as const } : itemBed) })))
          setBedId(''); setStep(1)
        } else if (result.kind === 'validation') { setErrors(result.fieldErrors ?? {}); setStep(2) }
      }
    } catch { if (run === generation.current) setMessage('Chưa gửi được đăng ký. Hồ sơ đã chọn vẫn được giữ trong trang để bạn thử lại.') }
    finally { submitLock.current = false; if (run === generation.current) setSending(false) }
  }

  return <>
    <PageHeader title="Đăng ký phòng" description="Tra cứu phòng, chọn giường và chuẩn bị hồ sơ đăng ký ký túc xá." />
    {view !== 'data' || !student || !registration ? <section className="booking-empty booking-surface" aria-busy={view === 'loading'}>
      <Icon name={view === 'loading' ? 'refresh' : 'building'} /><h2>{view === 'loading' ? 'Đang tải thông tin đăng ký…' : view === 'empty' ? 'Chưa có phòng trong đợt này' : 'Chưa thể tải thông tin đăng ký'}</h2>
      <p>{view === 'loading' ? 'Vui lòng chờ danh sách phòng và thông tin đợt đăng ký.' : view === 'empty' ? 'Bạn có thể quay lại khi có phòng được mở đăng ký.' : preview ? 'Thông tin phòng và đợt đăng ký hiện chưa sẵn sàng. Vui lòng thử lại sau.' : 'Tra cứu phòng, giường, đợt đăng ký và tải hồ sơ chưa sẵn sàng. Chưa thể gửi đăng ký từ dữ liệu thực.'}</p>
    </section> : <>
      <section className="booking-window" aria-label="Đợt đăng ký"><span className="booking-window-symbol"><Icon name="community" /></span><div><h2>{registration.name}</h2><p>{registration.semester} · {formatBookingTime(registration.opensAt)} – {formatBookingTime(registration.closesAt)}</p></div><span className={`booking-pill ${!registration.open ? 'booking-pill--muted' : ''}`}>{registration.open ? 'Đang mở đăng ký' : 'Đợt đăng ký đã đóng'}</span></section>
      {!allowed && !receipt && <div className="booking-info" role="status"><Icon name="info" /><div><strong>{eligibility === 'pending' ? 'Bạn đang có đơn chờ duyệt' : eligibility === 'resident' ? 'Bạn đang có lưu trú còn hiệu lực' : 'Hiện chưa thể gửi đăng ký'}</strong><p>{eligibility === 'pending' ? 'Mỗi sinh viên chỉ có một đơn chờ duyệt còn hạn. Vui lòng theo dõi đơn hiện tại; chưa thể tạo thêm đơn.' : eligibility === 'resident' ? 'Mỗi sinh viên chỉ có một lưu trú đang ở. Bạn chưa thể đăng ký thêm giường.' : 'Đợt đăng ký đã đóng. Bạn vẫn có thể tra cứu thông tin phòng.'}</p></div></div>}
      {!receipt && <ol className="booking-steps" aria-label="Tiến trình đăng ký">{steps.map((name, index) => <li key={name} className={step === index + 1 ? 'is-current' : step > index + 1 ? 'is-complete' : ''}><button disabled={sending || index + 1 > step} aria-current={step === index + 1 ? 'step' : undefined} onClick={() => { setStep(index + 1); setMessage('') }}><span>{step > index + 1 ? <Icon name="check" /> : index + 1}</span><strong>{name}</strong></button></li>)}</ol>}
      {message && <div className="booking-alert" role="alert"><Icon name="info" /><p>{message}</p></div>}
      <div ref={panel} className="booking-flow-panel">
        {receipt ? <section className="booking-receipt booking-surface"><span className="booking-receipt-symbol"><Icon name="check" /></span><span className="booking-pill">Chờ duyệt</span><h2 tabIndex={-1}>Đã gửi đăng ký{preview ? ' minh họa' : ''}</h2><p>Đơn đang chờ Ban quản lý xem xét. Giữ chỗ chưa đồng nghĩa hồ sơ đã được duyệt hoặc đã có hợp đồng lưu trú.</p><dl className="booking-review-facts"><div><dt>Mã đăng ký</dt><dd>{receipt.id}</dd></div><div><dt>Phòng / tòa</dt><dd>{receipt.roomName} · {receipt.buildingName}</dd></div><div><dt>Giường</dt><dd>{receipt.bedNumber}</dd></div><div><dt>Giữ chỗ đến</dt><dd>{formatBookingTime(receipt.expiresAt)} (giờ Việt Nam)</dd></div></dl><div className="booking-info"><Icon name="info" /><p>Giường được giữ 48 giờ kể từ khi tiếp nhận đơn. Nếu đơn chưa được duyệt khi hết hạn, hệ thống hủy đơn và trả giường trống.</p></div>{preview && <p className="booking-subtle">Đây là kết quả minh họa. Không có đơn hoặc giữ chỗ nào được tạo trên hệ thống thật.</p>}</section>
          : step === 1 ? <RoomPicker rooms={rooms} student={student} selectedRoom={room} bedId={bedId} allowed={allowed} onRoom={(next) => { if (next?.id !== roomId) setBedId(''); setRoomId(next?.id ?? '') }} onBed={setBedId} onNext={() => { setStep(2); setMessage('') }} />
            : step === 2 ? <div className="booking-form-layout"><form noValidate className="booking-surface booking-documents" onSubmit={(event) => { event.preventDefault(); if (checkDocuments()) setStep(3) }}>
              <h2 tabIndex={-1}>Hồ sơ đăng ký</h2><p className="booking-subtle">Chuẩn bị ảnh rõ nét, đúng thông tin của bạn. Các mục có dấu * là bắt buộc.</p>
              <div className="booking-student"><Icon name="people" /><div><strong>{student.name}</strong><p>MSSV: {student.code} · {genderLabels[student.gender]}</p></div><span className="booking-pill">Thông tin sinh viên</span></div>
              <DocumentField title="CCCD" field="identity" onChecking={onChecking} files={documents.identity ? [documents.identity] : []} onChange={(files) => setDocument('identity', files)} error={errors.identity} />
              <DocumentField title="Thẻ sinh viên" field="studentCard" onChecking={onChecking} files={documents.studentCard ? [documents.studentCard] : []} onChange={(files) => setDocument('studentCard', files)} error={errors.studentCard} />
              <DocumentField title="Minh chứng bổ sung (không bắt buộc)" field="evidence" onChecking={onChecking} max={3} files={documents.evidence} onChange={(files) => setDocument('evidence', files)} error={errors.evidence} />
              <div className="booking-terms"><label className="booking-check"><input type="checkbox" checked={accepted} onChange={(event) => { setAccepted(event.target.checked); setErrors((current) => ({ ...current, terms: undefined })) }} aria-invalid={errors.terms ? true : undefined} aria-describedby="booking-terms-message" />Tôi xác nhận hồ sơ đúng thông tin và đồng ý cam kết đăng ký phòng. *</label><Button variant="text" onClick={() => setTermsOpen(true)}>Xem cam kết</Button><p id="booking-terms-message" className="booking-error-text">{errors.terms}</p></div>
              <div className="booking-form-actions"><Button variant="outlined" onClick={() => setStep(1)}><Icon name="back" />Quay lại chọn giường</Button><Button type="submit" disabled={checkingImages}>Kiểm tra hồ sơ<Icon name="arrow" /></Button></div>
            </form><SelectionSummary room={room} bedNumber={bed?.number} /></div>
              : <div className="booking-form-layout"><section className="booking-surface booking-review"><h2 tabIndex={-1}>Kiểm tra trước khi gửi</h2><p className="booking-subtle">Xem lại chỗ ở và hồ sơ. Bạn có thể chỉnh sửa trước khi gửi đăng ký.</p><dl className="booking-review-facts"><div><dt>Sinh viên</dt><dd>{student.name}</dd></div><div><dt>Mã số sinh viên</dt><dd>{student.code}</dd></div><div><dt>Đợt đăng ký</dt><dd>{registration.name}</dd></div><div><dt>Phòng / tòa</dt><dd>{room?.name} · {room?.buildingName}</dd></div><div><dt>Giường đã chọn</dt><dd>{bed?.number}</dd></div></dl><h3>Ảnh hồ sơ</h3><ul className="booking-review-files">{[documents.identity, documents.studentCard, ...documents.evidence].filter((file): file is File => Boolean(file)).map((file, index) => <li key={`${file.name}-${index}`}><Icon name="document" /><span>{file.name}<small>{index === 0 ? 'CCCD' : index === 1 ? 'Thẻ sinh viên' : 'Minh chứng bổ sung'}</small></span><Icon name="check" /></li>)}</ul><p className="booking-accepted"><Icon name="check" />Đã xác nhận cam kết đăng ký.</p><div className="booking-form-actions"><Button variant="outlined" disabled={sending} onClick={() => { setStep(2); setMessage('') }}>Chỉnh sửa hồ sơ</Button><Button pending={sending} disabled={!preview || !allowed} onClick={() => void send()}>{sending ? 'Đang gửi đăng ký…' : 'Gửi đăng ký'}<Icon name="arrow" /></Button></div><p className="booking-subtle" role="status">{sending ? 'Đang xử lý đăng ký. Vui lòng chờ kết quả.' : 'Hệ thống sẽ kiểm tra lại tình trạng giường khi nhận đăng ký.'}</p></section><SelectionSummary room={room} bedNumber={bed?.number} /></div>}
      </div>
      {termsOpen && <Dialog title="Cam kết đăng ký phòng" onClose={() => setTermsOpen(false)} actions={<Button variant="outlined" onClick={() => setTermsOpen(false)}>Đóng</Button>}><div className="booking-commitment"><p>Tôi xác nhận ảnh hồ sơ là của mình và thông tin cung cấp là đúng.</p><p>Tôi đồng ý tuân thủ nội quy ký túc xá và cung cấp minh chứng bổ sung khi Ban quản lý yêu cầu.</p><p>Tôi hiểu rằng giường chỉ được giữ 48 giờ sau khi hệ thống tiếp nhận đăng ký; hồ sơ cần được Ban quản lý duyệt trước khi xác nhận lưu trú.</p></div></Dialog>}
    </>}
    {blocker.state === 'blocked' && <Dialog title="Rời trang đăng ký?" onClose={() => blocker.reset()} actions={<><Button variant="text" onClick={() => blocker.reset()}>Tiếp tục đăng ký</Button><Button variant="outlined" onClick={() => blocker.proceed()}>Rời trang</Button></>}><p>Giường và hồ sơ đang chọn sẽ bị bỏ. Bản nháp chưa tạo giữ chỗ hoặc gửi đăng ký.</p></Dialog>}
  </>
}
function SelectionSummary({ room, bedNumber }: { room: Room | null; bedNumber?: string }) {
  return <aside className="booking-selection booking-surface" aria-label="Chỗ ở đã chọn"><span className="booking-room-symbol"><Icon name="bed" /></span><h2>Chỗ ở đã chọn</h2><h3>Phòng {room?.name}</h3><p>{room?.buildingName}</p><dl className="booking-room-facts"><div><dt>Giường</dt><dd>{bedNumber}</dd></div><div><dt>Loại phòng</dt><dd>{room?.roomType}</dd></div><div><dt>Giường trống hiện tại</dt><dd>{room ? availableBeds(room) : 0}</dd></div></dl><div className="booking-info"><Icon name="info" /><p>Chỗ ở chưa được giữ. Tình trạng giường có thể thay đổi trước khi bạn gửi hồ sơ.</p></div></aside>
}
