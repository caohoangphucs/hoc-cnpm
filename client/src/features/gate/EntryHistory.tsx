import { useState } from 'react'
import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { useResource } from '@/shared/api/hooks'
import { DataTable, Facts, ServerPagination, Status, WorkflowPage } from '@/shared/workflow/Workflow'
import { dateTime } from '@/shared/workflow/model'
import { barrierLabels, parseEntries, parseEntry } from './model'
export function EntryHistory({ revision = 0 }: { revision?: number }) {
  const [page, setPage] = useState(1), [detail, setDetail] = useState<string | null>(null)
  const resource = useResource(`entries?page=${page}&size=8`, parseEntries, revision), rows = resource.data?.items ?? []
  return <><WorkflowPage title="Lịch sử vào / ra" description="Nhật ký được lưu trên máy chủ theo phạm vi tài khoản." view={resource.loading ? 'loading' : resource.error ? 'error' : 'data'} error={resource.error} onRetry={resource.reload} actions={<Button variant="outlined" onClick={resource.reload}>Tải lại</Button>}><section className="wl-card"><DataTable pagination={false} label="Nhật ký cổng" head={<tr><th>MSSV</th><th>Thời điểm</th><th>Hướng</th><th>Kết quả</th><th>Chi tiết</th></tr>} rows={rows.map(e => <tr key={e.eventId} className="clickable-row" onClick={() => setDetail(e.eventId)} tabIndex={0} onKeyDown={evt => { if (evt.key === 'Enter' || evt.key === ' ') { evt.preventDefault(); setDetail(e.eventId) } }} title="Nhấp vào dòng để xem chi tiết lượt cổng"><td><strong>{e.studentId}</strong></td><td>{dateTime(e.time)}</td><td>{e.type === 'VAO' ? 'Vào' : 'Ra'}</td><td><Status tone={e.violationId ? 'warning' : 'success'}>{e.violationId ? `Vi phạm ${e.violationId}` : 'Đã lưu'}</Status><small>{barrierLabels[e.barrierStatus]}</small></td><td onClick={evt => evt.stopPropagation()}><Button variant="text" onClick={() => setDetail(e.eventId)}>Chi tiết</Button></td></tr>)} /><ServerPagination page={page} count={rows.length} total={resource.data?.total} onPage={setPage} /></section></WorkflowPage>{detail && <EntryDetail key={detail} id={detail} onClose={() => setDetail(null)} />}</>
}
function EntryDetail({ id, onClose }: { id: string; onClose: () => void }) {
  const resource = useResource(`entries/${encodeURIComponent(id)}`, parseEntry), e = resource.data
  return <Dialog title="Chi tiết lượt cổng" onClose={onClose} actions={<Button variant="text" onClick={onClose}>Đóng</Button>}><WorkflowPage title="Thông tin lượt" description="Kết quả từ máy chủ." view={resource.loading ? 'loading' : resource.error ? 'error' : 'data'} error={resource.error} onRetry={resource.reload}>{e && <Facts items={[[ 'MSSV', e.studentId ], ['Thời điểm', dateTime(e.time) ], ['Hướng', e.type === 'VAO' ? 'Vào' : 'Ra' ], ['Thiết bị', barrierLabels[e.barrierStatus] ], ['Vi phạm', e.violationId || 'Không ghi nhận' ]]} />}</WorkflowPage></Dialog>
}
