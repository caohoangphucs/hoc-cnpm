import { useState } from 'react'
import { Button } from '@/shared/components/Button'
import { TextField } from '@/shared/components/TextField'
import { Dialog } from '@/shared/components/Dialog'
import { Icon } from '@/shared/components/Icon'
import { DataTable, Facts, ServerPagination, Status, Summary, WorkflowPage } from '@/shared/workflow/Workflow'
import { useResource } from '@/shared/api/hooks'
import { dateTime, searchText } from '@/shared/workflow/model'
import { parseViolations, type Violation } from './model'
import './discipline.css'
const levelLabels: Record<string, string> = { LOW: 'Nhẹ', MEDIUM: 'Trung bình', HIGH: 'Nghiêm trọng' }
const stateLabels: Record<string, string> = { CHUA_XU_LY: 'Chờ xử lý', DA_XU_LY: 'Đã xử lý' }
export function DisciplinePage() {
  const [page, setPage] = useState(1), [search, setSearch] = useState(''), [detail, setDetail] = useState<Violation | null>(null)
  const query = useResource(`violations/pending?page=${page}&size=8`, parseViolations), rows = query.data ?? []
  const shown = rows.filter(v => searchText(`${v.id} ${v.studentId} ${v.content}`).includes(searchText(search)))
  return <><WorkflowPage title="Vi phạm & kỷ luật" description="Đối chiếu hồ sơ vi phạm đang chờ xử lý." view={query.loading ? 'loading' : query.error ? 'error' : 'data'} error={query.error} onRetry={query.reload} actions={<Button variant="outlined" onClick={query.reload}><Icon name="refresh" />Tải lại</Button>}>
    <Summary items={[{ label: 'Hồ sơ trên trang', tone: 'warning', value: rows.length }, { label: 'Sinh viên cần rà soát trên trang (≥ 3 lần)', tone: 'danger', value: new Set(rows.filter(v => v.semesterCount >= 3).map(v => `${v.studentId}:${v.semesterId}`)).size }]} />
    <div className="wl-layout discipline-layout"><section className="wl-card"><div className="wl-card-heading"><h2>Hồ sơ chờ xử lý</h2><p>Số lần đã xác nhận lấy từ hệ thống theo học kỳ của hồ sơ.</p></div><div className="wl-toolbar"><TextField label="Tìm trên trang hồ sơ" type="search" value={search} onChange={e => setSearch(e.target.value)} placeholder="MSSV, mã hoặc nội dung vi phạm" /></div><DataTable pagination={false} label="Vi phạm đang chờ" head={<tr><th>Hồ sơ / MSSV</th><th>Vi phạm</th><th>Học kỳ</th><th>Số lần đã xác nhận</th><th>Thao tác</th></tr>} rows={shown.map(v => <tr key={v.id}><td><strong>VP-{v.id}</strong><small>{v.studentId}</small></td><td>{v.content}<small>{levelLabels[v.level] || v.level}</small></td><td>{v.semesterId}</td><td><Status tone={v.semesterCount >= 3 ? 'danger' : 'neutral'}>{v.semesterCount} lần</Status></td><td><Button variant="text" onClick={() => setDetail(v)}>Chi tiết</Button></td></tr>)} /><ServerPagination page={page} count={rows.length} onPage={p => { setPage(p); setSearch('') }} /></section><aside className="wl-card wl-side discipline-guidance"><Icon name="shield" /><h2>Đối chiếu trước khi xử lý</h2><p>Cảnh báo từ 3 lần không tự khóa tài khoản hoặc buộc thôi lưu trú.</p><p className="wl-note">Ra quyết định cần minh chứng đã lưu. Tải minh chứng và tra cứu khiếu nại chưa được hỗ trợ, nên các thao tác này chưa khả dụng.</p></aside></div>
  </WorkflowPage>{detail && <Dialog panel title={`Hồ sơ VP-${detail.id}`} onClose={() => setDetail(null)} actions={<><Button disabled>Ra quyết định</Button><Button variant="text" onClick={() => setDetail(null)}>Đóng</Button></>}><Facts items={[[ 'MSSV', detail.studentId ], ['Học kỳ', detail.semesterId], ['Mức độ', levelLabels[detail.level] || detail.level], ['Trạng thái', stateLabels[detail.status] || detail.status], ['Số lần đã xác nhận', detail.semesterCount]]} /><p className="discipline-detail">{detail.content}</p><h3>Lịch sử quyết định</h3>{!detail.decisions.length && <p className="wl-note">Chưa có quyết định.</p>}<ol className="discipline-timeline">{detail.decisions.map(d => <li key={d.id}><h4>{d.decision}</h4><p>{d.reason}</p><small>Phiên bản {d.revision} · {dateTime(d.createdAt)}</small></li>)}</ol><p className="wl-note">Chưa thể ra quyết định vì chức năng tải minh chứng chưa sẵn sàng.</p></Dialog>}</>
}
export function StudentDisciplinePage() { return <WorkflowPage title="Quyết định & khiếu nại" description="Theo dõi quyết định liên quan đến bạn." view="error" error="Tra cứu quyết định và khiếu nại dành cho sinh viên chưa sẵn sàng. Vui lòng liên hệ Ban quản lý khi cần đối chiếu hồ sơ." /> }
