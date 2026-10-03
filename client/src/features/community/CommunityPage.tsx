import { useState } from 'react'
import { Button } from '@/shared/components/Button'
import { TextField } from '@/shared/components/TextField'
import { Dialog } from '@/shared/components/Dialog'
import { Icon } from '@/shared/components/Icon'
import { DataTable, Facts, Feedback, ServerPagination, Status, WorkflowPage } from '@/shared/workflow/Workflow'
import { useClock } from '@/shared/workflow/hooks'
import { useApiAction, useResource } from '@/shared/api/hooks'
import { command, type CommandResult } from '@/shared/api/protocol'
import { dateTime, searchText } from '@/shared/workflow/model'
import { cancellationRequest, parseActivities, parseCommands, parseRegistration, type Activity, type Registration } from './model'
import { ActivityCreateDialog } from './ActivityCreateDialog'
import { CommunityPoints } from './CommunityPoints'
import './community.css'
type Ticket = { activity: Activity; receipt: Registration; cancelled: boolean }
type Confirmation = { kind: 'register'; activity: Activity } | { kind: 'cancel'; ticket: Ticket } | { kind: 'complete'; ids: number[] }
export function CommunityPage({ admin = false }: { admin?: boolean }) {
  const [page, setPage] = useState(1), [section, setSection] = useState('activities'), [search, setSearch] = useState(''), [semester, setSemester] = useState('')
  const [tickets, setTickets] = useState<Ticket[]>([]), [ticket, setTicket] = useState<Ticket | null>(null), [attendance, setAttendance] = useState<CommandResult[]>([]), [selected, setSelected] = useState<number[]>([])
  const [token, setToken] = useState(''), [creating, setCreating] = useState(false), [confirmation, setConfirmation] = useState<Confirmation | null>(null), [notice, setNotice] = useState('')
  const query = useResource(`activities?page=${page}&size=8`, parseActivities), action = useApiAction(), now = useClock()
  const activities = query.data ?? [], shown = activities.filter(a => searchText(a.title).includes(searchText(search)))
  async function submit() {
    const c = confirmation
    if (!c) return
    if (c.kind === 'register') {
      const result = await action.run(`activities/${c.activity.id}/registrations`, undefined, parseRegistration)
      if (result) { const saved = { activity: c.activity, receipt: result, cancelled: false }; setTickets(rows => [saved, ...rows.filter(t => t.receipt.id !== result.id)]); setTicket(saved); setConfirmation(null); setNotice('Đã đăng ký và giữ chỗ. Lưu lại mã điểm danh trên phiếu.'); query.reload() }
    } else if (c.kind === 'cancel') {
      const request = cancellationRequest(c.ticket.receipt), result = await action.run(request.path, request.body, command, request.method)
      if (result) { setTickets(rows => rows.map(t => t.receipt.id === c.ticket.receipt.id ? { ...t, cancelled: true } : t)); setConfirmation(null); setTicket(null); setNotice('Đã hủy đăng ký và trả chỗ.'); query.reload() }
    } else {
      const result = await action.run('activities/completion', { registrationIds: c.ids }, parseCommands)
      if (result) { setAttendance(rows => rows.map(r => result.find(x => x.id === r.id) ?? r)); setSelected([]); setConfirmation(null); setNotice(`Đã xác nhận hoàn thành ${result.length} lượt và ghi điểm.`) }
    }
  }
  async function attend() {
    if (!token.trim() || token.trim().length > 1000) { action.setError('Nhập mã điểm danh hợp lệ, tối đa 1.000 ký tự.'); return }
    const result = await action.run('activities/attendance', { qrToken: token.trim() }, command)
    if (result) { setAttendance(rows => [result, ...rows.filter(r => r.id !== result.id)]); setToken(''); setNotice(`Đã điểm danh lượt ${result.id}. Chưa cộng điểm đến khi xác nhận hoàn thành.`) }
  }
  function confirm(value: Confirmation) { action.setError(''); setConfirmation(value) }
  return <WorkflowPage title={admin ? 'Quản lý hoạt động công ích' : 'Hoạt động công ích'} description={admin ? 'Mở hoạt động, điểm danh và xác nhận hoàn thành.' : 'Đăng ký tham gia, theo dõi điểm và yêu cầu giấy xác nhận.'} view="data" actions={admin ? <Button onClick={() => setCreating(true)} disabled={action.busy}><Icon name="community" />Mở hoạt động</Button> : undefined}>
    <Feedback notice={notice} error={confirmation ? undefined : action.error} />
    <div className="community-navigation"><div className="wl-segments">{(admin ? [['activities', 'Hoạt động'], ['mine', 'Điểm danh & hoàn thành']] : [['activities', 'Khám phá hoạt động'], ['mine', 'Phiếu trong phiên này'], ['points', 'Điểm & xác nhận']]).map(([id, label]) => <Button key={id} variant="outlined" aria-pressed={section === id} disabled={action.busy} onClick={() => { setSection(id); action.setError('') }}>{label}</Button>)}</div></div>
    <div className="wl-layout community-layout"><section>
      {section === 'activities' && <><div className="wl-card community-filters"><TextField label="Tìm trong trang hoạt động" type="search" value={search} onChange={e => setSearch(e.target.value)} placeholder="Tên hoạt động" /><Button variant="outlined" onClick={query.reload} disabled={query.loading || action.busy}>Tải lại</Button></div>{query.loading ? <div className="wl-card wl-empty" role="status">Đang tải hoạt động…</div> : query.error ? <Feedback error={query.error} /> : <><div className="community-grid">{shown.map(a => { const open = a.status === 'MO_DANG_KY' && a.remaining > 0 && Date.parse(a.startAt) > now; return <article className="wl-card community-activity" key={a.id}><div className="community-activity-icon"><Icon name="community" /></div><Status tone={open ? 'primary' : 'neutral'}>{open ? 'Đang mở đăng ký' : a.remaining === 0 ? 'Hết chỗ' : 'Không còn nhận đăng ký'}</Status><h2>{a.title}</h2><p><Icon name="community" />{dateTime(a.startAt)}<br />Đến {dateTime(a.endAt)}</p><p>Học kỳ {a.semesterId}</p><div className="community-card-facts"><strong>+{a.points} điểm công ích</strong><span>{a.remaining}/{a.capacity} chỗ còn lại</span></div>{!admin && <Button disabled={!open || action.busy || tickets.some(t => t.receipt.activityId && t.activity.id === a.id && !t.cancelled)} onClick={() => confirm({ kind: 'register', activity: a })}>Đăng ký</Button>}</article> })}</div>{!shown.length && <section className="wl-card wl-empty"><h2>Chưa có hoạt động phù hợp</h2><p>Thử đổi từ khóa hoặc quay lại trang trước.</p></section>}<ServerPagination page={page} count={activities.length} onPage={setPage} busy={action.busy} /></>}</>}
      {section === 'mine' && !admin && <section className="wl-card"><div className="wl-card-heading"><h2>Phiếu đăng ký trong phiên này</h2><p>Lưu mã điểm danh trước khi đóng trang. Tra cứu toàn bộ lịch tham gia chưa được hỗ trợ.</p></div><DataTable label="Phiếu đăng ký vừa tạo" head={<tr><th>Hoạt động</th><th>Trạng thái</th><th>Thao tác</th></tr>} rows={tickets.map(t => <tr key={t.receipt.id}><td><strong>{t.activity.title}</strong><small>{dateTime(t.activity.startAt)}</small></td><td><Status tone={t.cancelled ? 'neutral' : 'primary'}>{t.cancelled ? 'Đã hủy' : 'Đã đăng ký'}</Status></td><td>{!t.cancelled && <><Button variant="text" onClick={() => setTicket(t)}>Phiếu tham gia</Button><Button variant="text" disabled={action.busy || Date.parse(t.activity.startAt) - now < 12 * 3600000} onClick={() => confirm({ kind: 'cancel', ticket: t })}>Hủy đăng ký</Button></>}</td></tr>)} /></section>}
      {section === 'mine' && admin && <section className="wl-card"><div className="wl-card-heading"><h2>Điểm danh & hoàn thành</h2><p>Các lượt ghi trong phiên này; chưa có danh sách toàn bộ người tham gia.</p></div><div className="community-attendance"><form noValidate onSubmit={e => { e.preventDefault(); void attend() }}><TextField label="Mã điểm danh từ phiếu" value={token} onChange={e => setToken(e.target.value)} disabled={action.busy} maxLength={1000} autoComplete="off" /><Button type="submit" pending={action.busy}>Điểm danh</Button></form><Button variant="outlined" disabled={action.busy || !selected.length || selected.length > 100} onClick={() => confirm({ kind: 'complete', ids: selected })}>Xác nhận hoàn thành ({selected.length})</Button></div><DataTable label="Lượt điểm danh trong phiên" head={<tr><th>Chọn</th><th>Mã đăng ký</th><th>Trạng thái</th></tr>} rows={attendance.map(r => <tr key={r.id}><td><input type="checkbox" aria-label={`Chọn lượt ${r.id}`} checked={selected.includes(Number(r.id))} disabled={action.busy || r.status !== 'ATTENDED'} onChange={e => setSelected(e.target.checked ? [...selected, Number(r.id)] : selected.filter(id => id !== Number(r.id)))} /></td><td>{r.id}</td><td><Status tone={r.status === 'COMPLETED' ? 'success' : 'tertiary'}>{r.status === 'COMPLETED' ? 'Đã hoàn thành' : 'Đã điểm danh'}</Status></td></tr>)} /></section>}
      {section === 'points' && !admin && <><div className="wl-card community-panel"><TextField label="Mã học kỳ muốn xem điểm" inputMode="numeric" value={semester} onChange={e => setSemester(e.target.value)} supportingText={activities.length ? `Học kỳ có hoạt động ở trang này: ${[...new Set(activities.map(a => a.semesterId))].join(', ')}.` : 'Nhập mã học kỳ do Ban quản lý cung cấp.'} /></div>{/^[1-9]\d*$/.test(semester) && Number.isSafeInteger(Number(semester)) ? <CommunityPoints key={semester} semesterId={Number(semester)} /> : <p className="wl-note">Chọn mã học kỳ hợp lệ để tải điểm của bạn.</p>}</>}
    </section><aside className="wl-card wl-side community-guidance"><Icon name="community" /><h2>Tham gia đúng lịch</h2><p>Không đăng ký trùng giờ. Hủy đăng ký khi còn ít nhất 12 giờ trước lúc bắt đầu.</p><p className="wl-note">Điểm danh chưa cộng điểm; Ban quản lý cần xác nhận hoàn thành.</p><h3>Kết quả từ hệ thống</h3><p>Số chỗ và điểm lấy từ máy chủ. Quy đổi điểm và giấy xác nhận có bước xử lý tiếp sau khi gửi yêu cầu.</p></aside></div>
    {creating && <ActivityCreateDialog onClose={() => setCreating(false)} onSaved={() => { query.reload(); setNotice('Đã mở hoạt động công ích.') }} />}
    {ticket && !confirmation && <Dialog title="Phiếu tham gia" onClose={() => setTicket(null)} actions={<Button variant="text" onClick={() => setTicket(null)}>Đóng</Button>}><div className="community-ticket"><h3>{ticket.activity.title}</h3><Facts items={[[ 'Mã đăng ký', ticket.receipt.id ], ['Bắt đầu', dateTime(ticket.activity.startAt)]]} /><TextField label="Mã điểm danh" value={ticket.receipt.qrToken} readOnly supportingText="Lưu mã này và xuất trình khi điểm danh. Không chia sẻ mã cho người khác." /><Status tone="primary">Đã đăng ký</Status></div></Dialog>}
    {confirmation && <Dialog title={confirmation.kind === 'register' ? 'Xác nhận đăng ký' : confirmation.kind === 'cancel' ? 'Hủy đăng ký hoạt động?' : 'Xác nhận hoàn thành'} busy={action.busy} onClose={() => setConfirmation(null)} actions={<><Button variant="text" disabled={action.busy} onClick={() => setConfirmation(null)}>Quay lại</Button><Button pending={action.busy} onClick={() => void submit()}>Xác nhận</Button></>}><Feedback error={action.error} /><p>{confirmation.kind === 'complete' ? `Xác nhận ${confirmation.ids.length} lượt đã tham gia và cộng điểm. Kiểm tra trước khi lưu.` : `${confirmation.kind === 'cancel' ? 'Hủy đăng ký và trả chỗ của' : 'Đăng ký tham gia'}: ${confirmation.kind === 'register' ? confirmation.activity.title : confirmation.ticket.activity.title}.`}</p></Dialog>}
  </WorkflowPage>
}
export function AdminCommunityPage() { return <CommunityPage admin /> }
