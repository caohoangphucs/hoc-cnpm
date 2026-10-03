import { useRef, useState } from 'react'
import { Button } from '@/shared/components/Button'
import { TextField } from '@/shared/components/TextField'
import { Icon } from '@/shared/components/Icon'
import { DataTable, Facts, Feedback, NotesField, SelectField, Status, Summary, WorkflowPage } from '@/shared/workflow/Workflow'
import { useClock, usePreviewAction } from '@/shared/workflow/hooks'
import { dateTime, searchText, type WorkflowOptions } from '@/shared/workflow/model'
import { barrierLabels, recordPreviewEntry, recordPreviewVisitor, studentIssue, updatePreviewBarrier, visitorIssue, type BarrierStatus, type EntryType, type GateData } from './model'
import './gate.css'

const emptyData = (): GateData => ({ students: [], visitors: [], entries: [], visitorEntries: [] })
export function GatePage({ initialData, serverNow, deviceResult = 'OPENED', initialSection = 'student', ...options }: WorkflowOptions & { initialData?: GateData; serverNow?: number; deviceResult?: BarrierStatus; initialSection?: 'student' | 'visitor' }) {
  const [data, setData] = useState(initialData ?? emptyData), [section, setSection] = useState<'student' | 'visitor'>(initialSection)
  const [method, setMethod] = useState('card'), [query, setQuery] = useState(''), [lookup, setLookup] = useState(''), [looked, setLooked] = useState(false)
  const [type, setType] = useState<EntryType>('VAO'), [note, setNote] = useState(''), [verified, setVerified] = useState(false)
  const [lastId, setLastId] = useState(''), [historyQuery, setHistoryQuery] = useState(''), [historyType, setHistoryType] = useState('')
  const request = useRef<{ signature: string; key: string } | null>(null), inputRef = useRef<HTMLInputElement>(null)
  const action = usePreviewAction(options), clock = useClock(), now = serverNow ?? clock
  const student = data.students.find(s => s.id === lookup), visitor = data.visitors.find(v => v.id === lookup), last = data.entries.find(e => e.eventId === lastId)
  const lastGuest = data.visitorEntries.find(e => e.eventId === lastId)
  const issue = section === 'student' ? studentIssue(student, now) : visitorIssue(visitor, type, now, verified)
  const history = section === 'student' ? data.entries : data.visitorEntries
  const shown = history.filter(e => (!historyType || e.type === historyType) && searchText(`${e.name} ${'studentId' in e ? e.studentId : e.visitorId}`).includes(searchText(historyQuery)))
  function resetLookup() { setLookup(''); setLooked(false); setVerified(false); setNote(''); action.setError('') }
  async function find() {
    if (!query.trim()) { action.setError('Nhập mã thẻ, MSSV hoặc mã khai báo khách để tra cứu.'); inputRef.current?.focus(); return }
    const value = query.trim().toUpperCase()
    await action.run(() => {
      const found = section === 'student' ? data.students.find(s => (method === 'card' ? s.card : s.id).toUpperCase() === value) : data.visitors.find(v => v.id.toUpperCase() === value || v.identity.toUpperCase() === value)
      setLookup(found?.id ?? ''); setLooked(true); setVerified(false)
    }, 'Đã tra cứu thông tin')
  }
  async function record() {
    if (issue || !lookup) { action.setError(issue); return }
    const signature = JSON.stringify([section, lookup, type, note.trim()])
    if (!request.current || request.current.signature !== signature) request.current = { signature, key: crypto.randomUUID() }
    let error: string | undefined
    const saved = await action.run(() => {
      if (section === 'student') {
        const result = recordPreviewEntry(data, { eventId: request.current!.key, studentId: lookup, type, note }, now)
        error = result.error; if (!error) { setData(result.data); setLastId(result.entry!.eventId); request.current = null }
      } else {
        const result = recordPreviewVisitor(data, lookup, type, verified, request.current!.key, now)
        error = result.error; if (!error) { setData(result.data); setLastId(request.current!.key); request.current = null; setVerified(false) }
      }
    }, section === 'student' ? 'Đã ghi lượt; cổng đang chờ thiết bị xử lý' : 'Đã ghi nhận lượt khách')
    if (saved && error) action.setError(error)
  }
  async function replay() {
    if (!last) return
    await action.run(() => { const result = recordPreviewEntry(data, { eventId: last.eventId, studentId: last.studentId, type: last.type, note: last.note }, now); setData(result.data) }, 'Đã kiểm tra lượt vừa ghi, không tạo thêm lượt hoặc vi phạm')
  }
  return <WorkflowPage title="Kiểm soát cổng" description="Tra cứu người vào/ra, ghi nhận lượt và theo dõi thiết bị tại cổng." view={options.view}>
    <Feedback notice={action.notice} error={action.error} />
    <Summary items={[{ label: 'Lượt sinh viên vào', value: data.entries.filter(e => e.type === 'VAO').length }, { label: 'Lượt sinh viên ra', value: data.entries.filter(e => e.type === 'RA').length }, { label: 'Lượt về trễ', value: data.entries.filter(e => e.late).length }]} />
    <div className="wl-segments" aria-label="Đối tượng kiểm soát"><Button variant="outlined" aria-pressed={section === 'student'} disabled={action.busy} onClick={() => { setSection('student'); setQuery(''); resetLookup() }}>Sinh viên</Button><Button variant="outlined" aria-pressed={section === 'visitor'} disabled={action.busy} onClick={() => { setSection('visitor'); setQuery(''); resetLookup() }}>Khách thăm</Button></div>
    <div className="wl-layout gate-layout"><section className="wl-card gate-station" aria-labelledby="gate-station-title">
      <div className="gate-station-heading"><Icon name="gate" /><div><h2 id="gate-station-title">{section === 'student' ? 'Tra cứu sinh viên' : 'Kiểm tra khai báo khách'}</h2><p className="gate-muted">Cổng chính · Giờ Việt Nam</p></div></div>
      <form className="wl-form gate-form" noValidate onSubmit={event => { event.preventDefault(); void find() }}><fieldset className="gate-fieldset" disabled={action.busy}>
        {section === 'student' && <SelectField label="Cách tra cứu" value={method} onChange={event => { setMethod(event.target.value); setQuery(''); resetLookup() }}><option value="card">Mã thẻ / mã từ máy quét</option><option value="student">Nhập MSSV thủ công</option></SelectField>}
        <TextField ref={inputRef} label={section === 'visitor' ? 'Mã khai báo / giấy tờ khách' : method === 'card' ? 'Mã thẻ' : 'MSSV'} value={query} onChange={event => { setQuery(event.target.value); resetLookup() }} maxLength={1000} autoComplete="off" spellCheck={false} supportingText={section === 'student' && method === 'card' ? 'Máy quét dạng bàn phím: quét mã rồi nhấn Enter để tra cứu.' : undefined} />
        <Button type="submit" pending={action.busy} disabled={!options.preview}><Icon name="search" />Tra cứu</Button>
      </fieldset></form>
      {looked && <div className="gate-profile" aria-live="polite">
        {(section === 'student' ? student : visitor) ? <><h3>{section === 'student' ? student!.name : visitor!.name}</h3><Facts items={section === 'student' ? [['MSSV', student!.id], ['Phòng', student!.room], ['Lưu trú', student!.resident ? 'Đang ở' : 'Không còn lưu trú'], ['Thẻ', student!.active ? 'Đang hoạt động' : 'Không còn hiệu lực'], ['Hạn thẻ', dateTime(new Date(student!.validTo).toISOString())]] : [['Mã khai báo', visitor!.id], ['Giấy tờ', `••••${visitor!.identity.slice(-4)}`], ['Sinh viên tiếp đón', visitor!.host], ['Phòng', visitor!.room], ['Thời gian thăm', `${dateTime(new Date(visitor!.from).toISOString())} – ${dateTime(new Date(visitor!.to).toISOString())}`], ['Duyệt', visitor!.approved ? 'Đã duyệt' : 'Chờ duyệt']]} /></> : <p className="wl-note">{issue}</p>}
        {(student && section === 'student' || visitor && section === 'visitor') && <div className="wl-form gate-form"><fieldset className="gate-fieldset" disabled={action.busy}>
          <SelectField label="Hướng di chuyển" value={type} onChange={event => { setType(event.target.value as EntryType); setVerified(false); action.setError('') }}><option value="VAO">Vào KTX</option><option value="RA">Ra khỏi KTX</option></SelectField>
          {section === 'student' ? <NotesField label="Ghi chú (tùy chọn)" value={note} onChange={event => setNote(event.target.value)} /> : <label className="wl-check"><input type="checkbox" checked={verified} onChange={event => { setVerified(event.target.checked); action.setError('') }} />Đã đối chiếu giấy tờ gốc với thông tin khai báo</label>}
          {issue ? <p className="wl-note">{issue}</p> : <Status tone="primary">Đủ điều kiện ghi nhận</Status>}
          <Button pending={action.busy} disabled={!!issue || !options.preview} onClick={() => void record()}>Ghi nhận {type === 'VAO' ? 'vào' : 'ra'} cổng</Button>
        </fieldset></div>}
      </div>}
    </section><aside className="wl-card wl-side gate-guidance">
      <h2>Thông tin ca trực</h2><p className="gate-time">{dateTime(new Date(now).toISOString())}</p><p className="wl-note">Sinh viên về sau 23:00 hoặc trước 05:00 được ghi nhận về trễ. Đúng 23:00 và 05:00 không tính vi phạm.</p>
      {last && section === 'student' && <section className="gate-last"><h3>Lượt sinh viên vừa ghi</h3><p><strong>{last.name}</strong><br />{last.studentId} · {last.type === 'VAO' ? 'Vào' : 'Ra'} · {dateTime(new Date(last.at).toISOString())}</p><Status tone={last.late ? 'danger' : 'primary'}>{last.late ? 'Về trễ · Đã ghi vi phạm' : 'Đã lưu lượt'}</Status><Status tone={last.barrier === 'FAILED' ? 'danger' : 'neutral'}>{barrierLabels[last.barrier]}</Status>{last.barrier === 'FAILED' && <p className="wl-note">Nhật ký vẫn được giữ. Kiểm tra thiết bị và xử lý tại cổng; không ghi thêm lượt để thử mở lại.</p>}<Button variant="outlined" pending={action.busy} disabled={last.barrier === 'OPENED'} onClick={() => void action.run(() => setData(updatePreviewBarrier(data, last.eventId, deviceResult)), 'Đã cập nhật phản hồi thiết bị')}>Kiểm tra thiết bị</Button><Button variant="text" pending={action.busy} onClick={() => void replay()}>Kiểm tra lượt vừa ghi</Button></section>}
      {lastGuest && section === 'visitor' && <section className="gate-last"><h3>Lượt khách vừa ghi</h3><p><strong>{lastGuest.name}</strong><br />{lastGuest.visitorId} · {lastGuest.type === 'VAO' ? 'Vào' : 'Ra'} · {dateTime(new Date(lastGuest.at).toISOString())}</p><Status tone="primary">Đã lưu lượt khách</Status></section>}{!last && !lastGuest && <p className="gate-muted">Sau khi ghi lượt, trạng thái cổng sẽ hiển thị tại đây. Chỉ mở cổng sau khi lượt được lưu thành công.</p>}
    </aside></div>
    <section className="wl-card gate-history" aria-labelledby="gate-history-title"><div className="wl-card-heading"><h2 id="gate-history-title">Nhật ký {section === 'student' ? 'sinh viên' : 'khách thăm'}</h2><p>{history.length} lượt</p></div><div className="wl-toolbar"><TextField label="Tìm trong nhật ký" type="search" value={historyQuery} onChange={event => setHistoryQuery(event.target.value)} placeholder="Tên hoặc mã người qua cổng" /><SelectField label="Lọc hướng" value={historyType} onChange={event => setHistoryType(event.target.value)}><option value="">Tất cả hướng</option><option value="VAO">Vào KTX</option><option value="RA">Ra khỏi KTX</option></SelectField></div><DataTable key={`${section}-${historyQuery}-${historyType}`} label="Nhật ký cổng" head={<tr><th scope="col">Người qua cổng</th><th scope="col">Thời điểm</th><th scope="col">Hướng</th><th scope="col">Kết quả</th></tr>} rows={shown.map(e => <tr key={e.eventId}><td><strong>{e.name}</strong><small>{'studentId' in e ? e.studentId : e.visitorId}</small></td><td>{dateTime(new Date(e.at).toISOString())}</td><td>{e.type === 'VAO' ? 'Vào KTX' : 'Ra khỏi KTX'}</td><td>{'late' in e ? <><Status tone={e.late ? 'danger' : 'primary'}>{e.late ? 'Về trễ' : 'Đúng giờ'}</Status><small>{barrierLabels[e.barrier]}</small></> : <Status>Đã ghi lượt khách</Status>}</td></tr>)} /></section>
  </WorkflowPage>
}

export function VisitorGatePage() { return <GatePage initialSection="visitor" /> }
