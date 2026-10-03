import { useRef, useState } from 'react'
import { Button } from '@/shared/components/Button'
import { TextField } from '@/shared/components/TextField'
import { Icon } from '@/shared/components/Icon'
import { DataTable, Facts, Feedback, NotesField, SelectField, Status, Summary, WorkflowPage } from '@/shared/workflow/Workflow'
import { useApiAction } from '@/shared/api/hooks'
import { command, requestIdentity, type CommandResult, type RequestIdentity } from '@/shared/api/protocol'
import { dateTime } from '@/shared/workflow/model'
import { barrierLabels, entryPayload, parseEntry, visitorPayload, type Entry, type EntryType } from './model'
import './gate.css'
import { EntryHistory } from './EntryHistory'
import { VisitorList } from './VisitorList'
const visitorStateLabels: Record<string, string> = { PENDING: 'Chờ xét duyệt', CHECKED_IN: 'Đã vào KTX', CHECKED_OUT: 'Đã ra khỏi KTX' }

export function GatePage({ initialSection = 'student' }: { initialSection?: 'student' | 'visitor' }) {
  const [section, setSection] = useState(initialSection), [card, setCard] = useState(''), [device, setDevice] = useState(''), [visitor, setVisitor] = useState('')
  const [type, setType] = useState<EntryType>('VAO'), [note, setNote] = useState(''), [verified, setVerified] = useState(false)
  const [entries, setEntries] = useState<Entry[]>([]), [guests, setGuests] = useState<CommandResult[]>([]), [receipt, setReceipt] = useState<CommandResult | null>(null)
  const event = useRef<RequestIdentity | null>(null), action = useApiAction()
  const [revision, setRevision] = useState(0)
  async function record() {
    let body: ReturnType<typeof entryPayload>
    try {
      event.current = requestIdentity(event.current, JSON.stringify([card.trim(), type, device.trim(), note.trim()]), () => crypto.randomUUID())
      body = entryPayload(event.current.key, card, type, device, note)
    } catch (error) { action.setError((error as Error).message); return }
    const saved = await action.run('entries', body, parseEntry)
    if (saved) { setEntries(rows => [saved, ...rows.filter(e => e.eventId !== saved.eventId)]); event.current = null; setCard(''); setNote(''); setRevision(v => v + 1) }
  }
  async function recordVisitor() {
    if (!/^[1-9]\d*$/.test(visitor) || !Number.isSafeInteger(Number(visitor))) { action.setError('Nhập mã khai báo khách hợp lệ.'); return }
    if (!verified) { action.setError('Đối chiếu giấy tờ gốc trước khi ghi nhận.'); return }
    const saved = await action.run(`visitors/${visitor}/${type === 'VAO' ? 'check-in' : 'check-out'}`, undefined, command)
    if (saved) { setReceipt(saved); setGuests(rows => [saved, ...rows.filter(r => r.requestId !== saved.requestId)]); setVerified(false); setRevision(v => v + 1) }
  }
  const last = entries[0]
  return <WorkflowPage title="Kiểm soát cổng" description="Ghi nhận vào/ra theo kết quả xác thực của hệ thống." view="data">
    <Feedback error={action.error} />
    <Summary items={[{ label: 'Lượt sinh viên vào trong phiên', tone: 'success', value: entries.filter(e => e.type === 'VAO').length }, { label: 'Lượt sinh viên ra trong phiên', value: entries.filter(e => e.type === 'RA').length }, { label: 'Lượt có vi phạm trong phiên', tone: 'warning', value: entries.filter(e => e.violationId).length }]} />
    <div className="wl-segments"><Button variant="outlined" aria-pressed={section === 'student'} disabled={action.busy} onClick={() => { setSection('student'); action.setError('') }}>Sinh viên</Button><Button variant="outlined" aria-pressed={section === 'visitor'} disabled={action.busy} onClick={() => { setSection('visitor'); action.setError('') }}>Khách thăm</Button></div>
    <div className="wl-layout gate-layout"><section className="wl-card gate-station"><div className="gate-station-heading"><Icon name="gate" /><h2>{section === 'student' ? 'Ghi lượt sinh viên' : 'Ghi lượt khách thăm'}</h2></div>
      <form className="wl-form gate-form" noValidate onSubmit={e => { e.preventDefault(); void (section === 'student' ? record() : recordVisitor()) }}><fieldset className="gate-fieldset" disabled={action.busy}>
        {section === 'student' ? <><TextField label="Mã thiết bị tại cổng" value={device} onChange={e => setDevice(e.target.value)} maxLength={1000} required /><TextField label="Mã thẻ / mã từ máy quét" value={card} onChange={e => setCard(e.target.value)} maxLength={1000} autoComplete="off" required supportingText="Quét thẻ dạng bàn phím, nhấn Enter để ghi nhận. Hệ thống kiểm tra hiệu lực thẻ và lưu trú." /></> : <><TextField label="Mã khai báo khách" value={visitor} onChange={e => { setVisitor(e.target.value); setVerified(false) }} inputMode="numeric" required /><p className="wl-note">Chọn phiếu đã duyệt từ danh sách bên dưới, hoặc nhập mã trên phiếu khai báo.</p><label className="wl-check"><input type="checkbox" checked={verified} onChange={e => setVerified(e.target.checked)} />Đã đối chiếu giấy tờ gốc và phiếu khai báo</label></>}
        <SelectField label="Hướng di chuyển" value={type} onChange={e => setType(e.target.value as EntryType)}><option value="VAO">Vào KTX</option><option value="RA">Ra khỏi KTX</option></SelectField>
        {section === 'student' && <NotesField label="Ghi chú (tùy chọn)" value={note} onChange={e => setNote(e.target.value)} />}
        <Button type="submit" pending={action.busy} disabled={section === 'visitor' && !verified}>Ghi nhận {type === 'VAO' ? 'vào' : 'ra'} cổng</Button>
      </fieldset></form></section>
      <aside className="wl-card wl-side gate-guidance"><h2>Kết quả vừa ghi</h2>{section === 'student' && last ? <section className="gate-last"><Facts items={[[ 'MSSV', last.studentId ], ['Thời điểm', dateTime(last.time)], ['Hướng', last.type === 'VAO' ? 'Vào' : 'Ra']]} /><Status tone={last.violationId ? 'warning' : 'success'}>{last.violationId ? `Đã ghi vi phạm ${last.violationId}` : 'Đã lưu lượt'}</Status><Status tone={last.barrierStatus === 'FAILED' ? 'danger' : 'neutral'}>{barrierLabels[last.barrierStatus]}</Status><p className="wl-note">Chờ thiết bị không đồng nghĩa cổng đã mở. Kiểm tra thiết bị tại chỗ; không ghi lại lượt để thử mở cổng.</p></section> : section === 'visitor' && receipt ? <><Status tone="success">Đã lưu lượt khách</Status><Facts items={[[ 'Mã khai báo', receipt.id ], ['Trạng thái', visitorStateLabels[receipt.status] || receipt.status]]} /></> : <p className="wl-note">Kết quả chỉ hiển thị sau khi hệ thống xác nhận lưu thành công.</p>}</aside></div>
    <section className="wl-card gate-history"><div className="wl-card-heading"><h2>Lượt ghi trong phiên này</h2><p>Đóng hoặc tải lại trang sẽ xóa danh sách tạm; lịch sử trên máy chủ vẫn được giữ.</p></div>{section === 'student' ? <DataTable label="Lượt sinh viên trong phiên" head={<tr><th>MSSV</th><th>Thời điểm</th><th>Hướng</th><th>Kết quả</th></tr>} rows={entries.map(e => <tr key={e.eventId}><td>{e.studentId}</td><td>{dateTime(e.time)}</td><td>{e.type === 'VAO' ? 'Vào' : 'Ra'}</td><td><Status tone={e.violationId ? 'warning' : 'success'}>{e.violationId ? `Vi phạm ${e.violationId}` : 'Đã lưu'}</Status><small>{barrierLabels[e.barrierStatus]}</small></td></tr>)} /> : <DataTable label="Lượt khách trong phiên" head={<tr><th>Mã khai báo</th><th>Trạng thái</th></tr>} rows={guests.map(r => <tr key={r.requestId}><td>{r.id}</td><td>{visitorStateLabels[r.status] || r.status}</td></tr>)} />}</section>
    {section === 'student' ? <EntryHistory revision={revision} /> : <VisitorList security revision={revision} />}
  </WorkflowPage>
}
export function VisitorGatePage() { return <GatePage initialSection="visitor" /> }

export function StudentVisitorDeclarationPage() {
  const [host, setHost] = useState(''), [name, setName] = useState(''), [identity, setIdentity] = useState(''), [from, setFrom] = useState(''), [to, setTo] = useState('')
  const [receipt, setReceipt] = useState<CommandResult | null>(null), action = useApiAction()
  const [revision, setRevision] = useState(0)
  async function submit() {
    let body: ReturnType<typeof visitorPayload>
    try { body = visitorPayload(host, name, identity, from, to) } catch (error) { action.setError((error as Error).message); return }
    const saved = await action.run('visitors', body, command)
    if (saved) { setReceipt(saved); setName(''); setIdentity(''); setFrom(''); setTo(''); setRevision(v => v + 1) }
  }
  return <WorkflowPage title="Khai báo khách thăm" description="Gửi thông tin khách và khoảng thời gian thăm để Ban quản lý xét duyệt." view="data"><Feedback error={action.error} /><div className="wl-layout gate-layout"><section className="wl-card gate-station"><form className="wl-form gate-form" noValidate onSubmit={e => { e.preventDefault(); void submit() }}><fieldset className="gate-fieldset" disabled={action.busy}><TextField label="MSSV của bạn" value={host} onChange={e => setHost(e.target.value)} maxLength={1000} required supportingText="MSSV phải thuộc tài khoản đang đăng nhập." /><TextField label="Họ tên khách" value={name} onChange={e => setName(e.target.value)} maxLength={1000} required /><TextField label="Số giấy tờ khách" value={identity} onChange={e => setIdentity(e.target.value)} maxLength={1000} required autoComplete="off" /><TextField label="Bắt đầu thăm (giờ Việt Nam)" type="datetime-local" value={from} onChange={e => setFrom(e.target.value)} required /><TextField label="Kết thúc thăm (giờ Việt Nam)" type="datetime-local" value={to} onChange={e => setTo(e.target.value)} required /><Button type="submit" pending={action.busy}>Gửi khai báo</Button></fieldset></form></section><aside className="wl-card wl-side gate-guidance"><h2>Phiếu vừa gửi</h2>{receipt ? <><Status tone="warning">Chờ xét duyệt</Status><Facts items={[[ 'Mã khai báo', receipt.id ], ['Trạng thái', visitorStateLabels[receipt.status] || receipt.status]]} /><p className="wl-note">Theo dõi trạng thái xét duyệt trong danh sách bên dưới.</p></> : <p>Thông tin phiếu sẽ xuất hiện sau khi gửi thành công.</p>}</aside></div><VisitorList revision={revision} /></WorkflowPage>
}
