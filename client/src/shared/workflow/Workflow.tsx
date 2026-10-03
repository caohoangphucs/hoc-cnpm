import { useId, useState, type ReactNode, type SelectHTMLAttributes, type TextareaHTMLAttributes } from 'react'
import { Button } from '@/shared/components/Button'
import { Icon } from '@/shared/components/Icon'
import { PageHeader } from '@/shared/components/PageHeader'
import type { DemoOutcome, ViewState, WorkflowOptions } from './model'

export function WorkflowPage({ title, description, children, view = 'error', actions }: { title: string; description: string; children: ReactNode; view?: ViewState; actions?: ReactNode }) {
  return <><PageHeader title={title} description={description} actions={view === 'data' ? actions : undefined} />{view === 'data' ? children : <section className="wl-card wl-empty" aria-busy={view === 'loading'} role={view === 'error' ? 'alert' : 'status'}><Icon name={view === 'loading' ? 'refresh' : 'info'} /><h2>{view === 'loading' ? 'Đang tải dữ liệu…' : view === 'empty' ? 'Chưa có dữ liệu' : 'Chưa thể tải dữ liệu'}</h2><p>{view === 'error' ? 'Dữ liệu hiện chưa sẵn sàng. Vui lòng thử lại sau.' : view === 'loading' ? 'Vui lòng chờ trong giây lát.' : 'Các bản ghi sẽ xuất hiện tại đây khi có dữ liệu.'}</p></section>}</>
}
export function PreviewWorkflow({ title, children }: { title: string; children: (options: WorkflowOptions) => ReactNode }) {
  const [view, setView] = useState<ViewState>('data'), [outcome, setOutcome] = useState<DemoOutcome>('success'), [revision, setRevision] = useState(0)
  return <><section className="wl-preview"><div><Icon name="info" /><div><strong>Xem trước {title}</strong><p>Dữ liệu minh họa, thao tác chỉ trong bộ nhớ. Chưa ghi vào hệ thống thật.</p></div></div><div className="wl-preview-controls"><label>Hiển thị<select aria-label="Hiển thị dữ liệu" value={view} onChange={e => setView(e.target.value as ViewState)}><option value="data">Có dữ liệu</option><option value="loading">Đang tải</option><option value="empty">Trống</option><option value="error">Lỗi tải</option></select></label><label>Kết quả thao tác<select aria-label="Kết quả thao tác" value={outcome} onChange={e => setOutcome(e.target.value as DemoOutcome)}><option value="success">Thành công</option><option value="conflict">Xung đột phiên bản</option><option value="unavailable">Dịch vụ gián đoạn</option></select></label><Button variant="text" onClick={() => { setView('data'); setOutcome('success'); setRevision(v => v + 1) }}><Icon name="refresh" />Đặt lại dữ liệu</Button></div></section><div key={revision}>{children({ preview: true, view, outcome })}</div></>
}
export function Feedback({ notice, error }: { notice?: string; error?: string }) { return <>{error && <div className="wl-feedback wl-feedback--error" role="alert"><Icon name="info" /><p>{error}</p></div>}<div className="wl-feedback wl-feedback--success" role="status">{notice && <><Icon name="check" /><p>{notice}</p></>}</div></> }
type Tone = 'neutral' | 'primary' | 'danger' | 'success' | 'warning' | 'tertiary'
export function Status({ children, tone = 'neutral' }: { children: ReactNode; tone?: Tone }) { return <span className={`wl-status wl-status--${tone}`}>{children}</span> }
export function Summary({ items }: { items: { label: string; value: ReactNode; tone?: Tone }[] }) { return <dl className="wl-summary">{items.map(({ label, value, tone = 'primary' }) => <div key={label} className={`wl-summary--${tone}`}><dt><span>{label}</span><span className="wl-summary-icon"><Icon name={tone === 'success' ? 'check' : tone === 'warning' || tone === 'danger' ? 'info' : 'chart'} /></span></dt><dd>{value}</dd></div>)}</dl> }
export function SelectField({ label, children, error, ...props }: SelectHTMLAttributes<HTMLSelectElement> & { label: string; error?: string }) {
  const id = useId()
  return <label className="wl-field"><span>{label}</span><select {...props} id={id} aria-label={label} aria-invalid={error ? true : undefined} aria-describedby={error ? `${id}-error` : undefined}>{children}</select>{error && <small className="wl-error" id={`${id}-error`}>{error}</small>}</label>
}
export function NotesField({ label, error, ...props }: TextareaHTMLAttributes<HTMLTextAreaElement> & { label: string; error?: string }) {
  const id = useId()
  return <label className="wl-field"><span>{label}</span><textarea {...props} id={id} aria-label={label} maxLength={1000} aria-invalid={error ? true : undefined} aria-describedby={error ? `${id}-error` : undefined} />{error && <small className="wl-error" id={`${id}-error`}>{error}</small>}</label>
}
export function Facts({ items }: { items: [string, ReactNode][] }) { return <dl className="wl-facts">{items.map(([label, value]) => <div key={label}><dt>{label}</dt><dd>{value}</dd></div>)}</dl> }
export function DataTable({ label, head, rows }: { label: string; head: ReactNode; rows: ReactNode[] }) {
  const [page, setPage] = useState(1)
  const pages = Math.max(1, Math.ceil(rows.length / 8)), active = Math.min(page, pages)
  return <><div className="wl-table-scroll" role="region" tabIndex={0} aria-label={`${label}, có thể cuộn ngang`}><table className="wl-table"><caption className="sr-only">{label}</caption><thead>{head}</thead><tbody>{rows.slice((active - 1) * 8, active * 8)}</tbody></table>{!rows.length && <div className="wl-empty"><Icon name="search" /><h3>Không có kết quả phù hợp</h3><p>Thử thay đổi bộ lọc hoặc từ khóa.</p></div>}</div><div className="wl-pagination"><span role="status">{rows.length ? `${(active - 1) * 8 + 1}–${Math.min(active * 8, rows.length)}` : '0'} / {rows.length} kết quả</span><button className="icon-button" disabled={active === 1} aria-label="Trang trước" onClick={() => setPage(active - 1)}><Icon name="back" /></button><span>Trang {active}/{pages}</span><button className="icon-button" disabled={active === pages} aria-label="Trang sau" onClick={() => setPage(active + 1)}><Icon name="arrow" /></button></div></>
}
