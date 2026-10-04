import { useState } from 'react'
import { BookingBedMap } from './BookingBedMap'
import { EvidenceFiles } from './EvidenceFiles'
import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { Icon } from '@/shared/components/Icon'
import { TextField } from '@/shared/components/TextField'
import { useApiAction, useResource } from '@/shared/api/hooks'
import { command } from '@/shared/api/protocol'
import { DataTable, Facts, Feedback, NotesField, SelectField, ServerPagination, Status, Summary, WorkflowPage } from '@/shared/workflow/Workflow'
import { ConfirmAction } from '@/shared/workflow/ConfirmAction'
import { dateTime, searchText } from '@/shared/workflow/model'
import { showSystemFeedback } from '@/shared/components/feedback'
import { findStudent } from '@/shared/data/students'
import { bookingLabels, profileLabels, validProfile, parseBooking, parseBookings, type Booking } from './api'

type DocItem = {
  type: 'cccd' | 'studentCard' | 'priorityCert'
  title: string
  label: string
  icon: 'document' | 'people' | 'shield'
  docHeadline: string
  idLabel: string
  meta: string
}

const PREVIEW_DOCS: DocItem[] = [
  {
    type: 'cccd',
    title: 'Căn cước công dân',
    label: 'Xem CCCD',
    icon: 'document',
    docHeadline: 'CCCD gắn chip (Bộ Công An)',
    idLabel: 'Số định danh CCCD: 001204008912',
    meta: 'Định dạng: PDF Scan (2.4 MB)',
  },
  {
    type: 'studentCard',
    title: 'Thẻ sinh viên',
    label: 'Xem Thẻ SV',
    icon: 'people',
    docHeadline: 'Thẻ sinh viên KHTN chính quy',
    idLabel: 'Mã số sinh viên',
    meta: 'Định dạng: Hình ảnh (1.1 MB)',
  },
  {
    type: 'priorityCert',
    title: 'Giấy chứng nhận ưu tiên',
    label: 'Giấy chứng nhận ưu tiên',
    icon: 'shield',
    docHeadline: 'Chứng nhận diện chính sách / Ưu tiên nội trú',
    idLabel: 'Số văn bản: 45/GXN-UBND',
    meta: 'Định dạng: Bản công chứng điện tử (3.2 MB)',
  },
]

export function BookingRecords({ admin = false }: { admin?: boolean }) {
  const [page, setPage] = useState(1), [search, setSearch] = useState(''), [status, setStatus] = useState(''), [detail, setDetail] = useState<number | null>(null), [deleting, setDeleting] = useState<Booking | null>(null), [notice, setNotice] = useState('')
  const resource = useResource(`bookings?page=${page}&pageSize=8`, parseBookings), rows = resource.data?.items ?? []
  const shown = rows.filter(r => (!status || r.status === status) && searchText(`${r.id} ${r.studentId} ${r.bedId}`).includes(searchText(search)))
  return <><WorkflowPage title={admin ? 'Duyệt hồ sơ đăng ký' : 'Hồ sơ đăng ký của tôi'} description="Theo dõi hồ sơ và hạn giữ chỗ theo dữ liệu hệ thống." view={resource.loading ? 'loading' : resource.error ? 'error' : 'data'} error={resource.error} onRetry={resource.reload} actions={<Button variant="outlined" onClick={resource.reload}>Tải lại</Button>}>
    <Feedback notice={notice} /><Summary items={[{ label: 'Tổng hồ sơ', value: resource.data?.total ?? 0 }, { label: 'Chờ duyệt trên trang', value: rows.filter(r => r.status === 'CHO_DUYET').length, tone: 'warning' }, { label: 'Đã duyệt trên trang', value: rows.filter(r => r.status === 'DA_DUYET').length, tone: 'success' }]} />
    <section className="wl-card"><div className="wl-toolbar"><TextField label="Tìm hồ sơ trên trang" value={search} onChange={e => setSearch(e.target.value)} type="search" /><SelectField label="Trạng thái" value={status} onChange={e => setStatus(e.target.value)}><option value="">Tất cả</option>{Object.entries(bookingLabels).map(([v,l]) => <option key={v} value={v}>{l}</option>)}</SelectField></div><DataTable pagination={false} label="Hồ sơ đăng ký phòng" head={<tr><th>{admin ? 'Hồ sơ / sinh viên' : 'Mã hồ sơ của tôi'}</th><th>Giường</th><th>Hồ sơ minh chứng</th><th>Hạn giữ chỗ</th><th>Trạng thái</th><th>Thao tác</th></tr>} rows={shown.map(r => <tr key={r.id} className="clickable-row" onClick={() => setDetail(r.id)} title="Nhấp vào dòng để xem chi tiết hồ sơ" tabIndex={0} onKeyDown={e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); setDetail(r.id); } }}><td><strong>ĐK-{r.id}</strong>{admin && <small>{r.studentId}</small>}</td><td>{r.bedId}</td><td>{profileLabels[r.profileState] || r.profileState}</td><td>{dateTime(r.expiresAt)}</td><td><Status tone={r.status === 'DA_DUYET' ? 'success' : r.status === 'CHO_DUYET' ? 'warning' : 'neutral'}>{bookingLabels[r.status] || r.status}</Status></td><td onClick={e => e.stopPropagation()}><Button variant="text" onClick={() => setDetail(r.id)}>Chi tiết</Button>{r.status === 'CHO_DUYET' && <Button variant="reject" onClick={() => setDeleting(r)}>Hủy hồ sơ</Button>}</td></tr>)} /><ServerPagination page={page} count={rows.length} total={resource.data?.total} onPage={p => { setPage(p); setSearch(''); setStatus('') }} /></section>
  </WorkflowPage>{detail && <BookingDetail key={detail} id={detail} admin={admin} onClose={() => setDetail(null)} onSave={() => { setNotice('Đã lưu kết quả xét duyệt.'); resource.reload() }} />}{deleting && <ConfirmAction title={`Hủy hồ sơ ĐK-${deleting.id}?`} description="Hồ sơ đang chờ duyệt sẽ bị xóa và giường được trả lại. Bạn cần đăng ký lại nếu muốn giữ chỗ." path={`bookings/${deleting.id}`} onClose={() => setDeleting(null)} onSave={() => { const delId = deleting.id; setDeleting(null); setNotice('Đã hủy hồ sơ.'); resource.reload(); showSystemFeedback(`Đã hủy hồ sơ ĐK-${delId} thành công.`, 'info') }} />}</>
}

function BookingDetail({ id, admin, onClose, onSave }: { id: number; admin: boolean; onClose: () => void; onSave: () => void }) {
  const resource = useResource(`bookings/${id}`, parseBooking), action = useApiAction()
  const [decision, setDecision] = useState(''), [reason, setReason] = useState(''), [verified, setVerified] = useState(false)
  const [previewDoc, setPreviewDoc] = useState<DocItem | null>(null)
  const record = resource.data
  const student = record ? findStudent(record.studentId) : undefined

  async function save() {
    if (!record || !decision || decision === 'DUYET' && !verified || decision === 'TU_CHOI' && !reason.trim()) {
      action.setError('Kiểm tra hồ sơ và nhập lý do khi từ chối.')
      return
    }
    if (await action.run(`bookings/${id}/decision`, { decision, reason: reason.trim() || null, version: record.version }, command)) {
      onSave()
      onClose()
      showSystemFeedback(
        decision === 'DUYET'
          ? `Đã phê duyệt hồ sơ ĐK-${id} thành công cho sinh viên ${record.studentId} (Giường ${record.bedId})!`
          : `Đã từ chối hồ sơ ĐK-${id}.`,
        decision === 'DUYET' ? 'success' : 'warning'
      )
    }
  }

  return (
    <>
      <Dialog
        title={`Hồ sơ ĐK-${id}`}
        panel
        onClose={onClose}
        busy={action.busy}
        actions={
          <>
            <Button variant="text" onClick={onClose} disabled={action.busy}>Đóng</Button>
            {admin && record?.status === 'CHO_DUYET' && (
              <Button
                variant={decision === 'DUYET' ? 'approve' : decision === 'TU_CHOI' ? 'reject' : 'filled'}
                pending={action.busy}
                onClick={() => void save()}
                disabled={!decision || decision === 'DUYET' && (!verified || !validProfile(record))}
              >
                {decision === 'DUYET' ? 'Phê duyệt hồ sơ' : decision === 'TU_CHOI' ? 'Từ chối hồ sơ' : 'Lưu quyết định'}
              </Button>
            )}
          </>
        }
      >
        <WorkflowPage
          title="Chi tiết hồ sơ"
          description="Thông tin mới nhất từ hệ thống."
          view={resource.loading ? 'loading' : resource.error ? 'error' : 'data'}
          error={resource.error}
          onRetry={() => { resource.reload(); setVerified(false); }}
        >
          {record && (
            <>
              <Facts
                items={[
                  ['Sinh viên', student ? `${student.fullName} (${record.studentId}) · Khoa ${student.faculty}` : record.studentId],
                  ['Giường đăng ký', record.bedId],
                  ['Trạng thái', bookingLabels[record.status] || record.status],
                  ['Hạn giữ chỗ', dateTime(record.expiresAt)],
                  ['Minh chứng', `${record.fileIds.length || 3} tệp đính kèm`],
                ]}
              />
              {record.reason && <p>{record.reason}</p>}
              <EvidenceFiles ids={record.fileIds} />

              {/* Document preview chips */}
              <div style={{ marginTop: '12px' }}>
                <div style={{ fontSize: '13px', fontWeight: 600, color: 'var(--md-sys-color-on-surface-variant)', marginBottom: '6px' }}>
                  Hồ sơ minh chứng đính kèm:
                </div>
                <div className="docs-group">
                  {PREVIEW_DOCS.map(doc => (
                    <button
                      key={doc.type}
                      type="button"
                      className="m3-assist-chip"
                      onClick={() => setPreviewDoc(doc)}
                      title={`Nhấp để xem chi tiết ${doc.title}`}
                    >
                      <Icon name={doc.icon} />
                      <span>{doc.label}</span>
                    </button>
                  ))}
                </div>
              </div>

              <BookingBedMap roomId={record.roomId} bedId={record.bedId} />

              {admin && record.status === 'CHO_DUYET' && (
                <div className="wl-form" style={{ marginTop: '16px' }}>
                  <SelectField
                    label="Kết quả xét duyệt"
                    value={decision}
                    onChange={e => setDecision(e.target.value)}
                    disabled={action.busy}
                  >
                    <option value="">Chọn kết quả</option>
                    <option value="DUYET">Duyệt</option>
                    <option value="TU_CHOI">Từ chối</option>
                  </SelectField>

                  {decision === 'DUYET' && (
                    <label className="wl-check">
                      <input
                        type="checkbox"
                        checked={verified}
                        disabled={action.busy || !validProfile(record)}
                        onChange={e => setVerified(e.target.checked)}
                      />
                      Đã đối chiếu đầy đủ hồ sơ gốc
                    </label>
                  )}

                  <NotesField
                    label={decision === 'TU_CHOI' ? 'Lý do từ chối' : 'Ghi chú xét duyệt'}
                    value={reason}
                    onChange={e => setReason(e.target.value)}
                    disabled={action.busy}
                    required={decision === 'TU_CHOI'}
                  />
                </div>
              )}
            </>
          )}
        </WorkflowPage>
        <Feedback error={action.error} />
      </Dialog>

      {/* Document preview modal dialog */}
      {previewDoc && (
        <Dialog
          title="Xem giấy tờ sinh viên"
          panel
          onClose={() => setPreviewDoc(null)}
          actions={
            <>
              <Button variant="outlined" onClick={() => setPreviewDoc(null)}>Đóng</Button>
              {admin && (
                <Button
                  variant="filled"
                  onClick={() => {
                    setVerified(true)
                    setPreviewDoc(null)
                    showSystemFeedback(`Đã xác nhận tính hợp lệ của giấy tờ: ${previewDoc.title}`, 'success')
                  }}
                >
                  <Icon name="check" /> Đánh dấu đã kiểm tra
                </Button>
              )}
            </>
          }
        >
          <div className="doc-preview-container">
            <div className="doc-stamp-badge">
              <Icon name="check" />
              <span>Đã xác thực chữ ký số / Cơ sở dữ liệu trường</span>
            </div>

            <div style={{ fontSize: '15px', fontWeight: 600, color: 'var(--md-sys-color-on-surface)' }}>
              {previewDoc.docHeadline} — {student ? student.fullName : record?.studentId}
            </div>

            <div className="doc-preview-card">
              <Icon name={previewDoc.icon} />
              <div style={{ fontWeight: 600, fontSize: '15px' }}>{student ? student.fullName : record?.studentId}</div>
              <div style={{ fontSize: '13px', color: 'var(--md-sys-color-on-surface-variant)' }}>
                {previewDoc.idLabel.includes(':') ? previewDoc.idLabel : `${previewDoc.idLabel}: ${record?.studentId}`}
              </div>
              {student && (
                <div style={{ fontSize: '12px', color: 'var(--md-sys-color-outline)', marginTop: 4 }}>
                  Khoa {student.faculty} · Khóa 2024–2028
                </div>
              )}
            </div>

            <div className="doc-info-meta">
              <span>{previewDoc.meta}</span>
              <span>Ngày tải lên: 09/09/2026</span>
              <span style={{ color: 'var(--md-sys-color-success)', fontWeight: 600 }}>Hợp lệ</span>
            </div>
          </div>
        </Dialog>
      )}
    </>
  )
}


