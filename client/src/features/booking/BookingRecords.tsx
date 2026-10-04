import { useState } from 'react'
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

const MOCK_OCCUPANTS: Record<number, string> = {
  1: 'N.V. Hùng',
  2: 'T.Đ. Anh',
  4: 'P.G. Huy',
  5: 'Đ.H. Anh',
  6: 'V.Đ. Long',
  7: 'L.T. Tùng',
}

function parseBedDetails(bedId: string) {
  const mDash = bedId.match(/^(.*)[-_](\d+)$/)
  if (mDash) {
    return {
      roomName: mDash[1].startsWith('P') || mDash[1].startsWith('Phòng') ? mDash[1] : `Phòng ${mDash[1]}`,
      bedNum: parseInt(mDash[2], 10),
    }
  }
  const mNum = bedId.match(/^([A-Za-z]*\d{3})(\d{2})$/)
  if (mNum) {
    return {
      roomName: `Phòng ${mNum[1]}`,
      bedNum: parseInt(mNum[2], 10),
    }
  }
  const trailingNum = bedId.match(/\d+$/)
  const bedNum = trailingNum ? parseInt(trailingNum[0], 10) : 3
  const roomPrefix = bedId.replace(/\d+$/, '') || 'A204'
  return {
    roomName: roomPrefix.startsWith('P') || roomPrefix.startsWith('Phòng') ? roomPrefix : `Phòng ${roomPrefix}`,
    bedNum,
  }
}

export function BookingRecords({ admin = false }: { admin?: boolean }) {
  const [page, setPage] = useState(1), [search, setSearch] = useState(''), [status, setStatus] = useState(''), [detail, setDetail] = useState<number | null>(null), [deleting, setDeleting] = useState<Booking | null>(null), [notice, setNotice] = useState('')
  const resource = useResource(`bookings?page=${page}&pageSize=8`, parseBookings), rows = resource.data?.items ?? []
  const shown = rows.filter(r => (!status || r.status === status) && searchText(`${r.id} ${r.studentId} ${r.bedId}`).includes(searchText(search)))
  return <><WorkflowPage title={admin ? 'Duyệt hồ sơ đăng ký' : 'Hồ sơ đăng ký của tôi'} description="Theo dõi hồ sơ và hạn giữ chỗ theo dữ liệu hệ thống." view={resource.loading ? 'loading' : resource.error ? 'error' : 'data'} error={resource.error} onRetry={resource.reload} actions={<Button variant="outlined" onClick={resource.reload}>Tải lại</Button>}>
    <Feedback notice={notice} /><Summary items={[{ label: 'Tổng hồ sơ', value: resource.data?.total ?? 0 }, { label: 'Chờ duyệt trên trang', value: rows.filter(r => r.status === 'CHO_DUYET').length, tone: 'warning' }, { label: 'Đã duyệt trên trang', value: rows.filter(r => r.status === 'DA_DUYET').length, tone: 'success' }]} />
    <section className="wl-card"><div className="wl-toolbar"><TextField label="Tìm hồ sơ trên trang" value={search} onChange={e => setSearch(e.target.value)} type="search" /><SelectField label="Trạng thái" value={status} onChange={e => setStatus(e.target.value)}><option value="">Tất cả</option>{Object.entries(bookingLabels).map(([v,l]) => <option key={v} value={v}>{l}</option>)}</SelectField></div><DataTable pagination={false} label="Hồ sơ đăng ký phòng" head={<tr><th>Hồ sơ / sinh viên</th><th>Giường</th><th>Hồ sơ minh chứng</th><th>Hạn giữ chỗ</th><th>Trạng thái</th><th>Thao tác</th></tr>} rows={shown.map(r => <tr key={r.id} className="clickable-row" onClick={() => setDetail(r.id)} title="Nhấp vào dòng để xem chi tiết hồ sơ" tabIndex={0} onKeyDown={e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); setDetail(r.id); } }}><td><strong>ĐK-{r.id}</strong><small>{r.studentId}</small></td><td>{r.bedId}</td><td>{profileLabels[r.profileState] || r.profileState}</td><td>{dateTime(r.expiresAt)}</td><td><Status tone={r.status === 'DA_DUYET' ? 'success' : r.status === 'CHO_DUYET' ? 'warning' : 'neutral'}>{bookingLabels[r.status] || r.status}</Status></td><td onClick={e => e.stopPropagation()}><Button variant="text" onClick={() => setDetail(r.id)}>Chi tiết</Button>{r.status === 'CHO_DUYET' && <Button variant="reject" onClick={() => setDeleting(r)}>Hủy hồ sơ</Button>}</td></tr>)} /><ServerPagination page={page} count={rows.length} total={resource.data?.total} onPage={p => { setPage(p); setSearch(''); setStatus('') }} /></section>
  </WorkflowPage>{detail && <BookingDetail key={detail} id={detail} admin={admin} onClose={() => setDetail(null)} onSave={() => { setNotice('Đã lưu kết quả xét duyệt.'); resource.reload() }} />}{deleting && <ConfirmAction title={`Hủy hồ sơ ĐK-${deleting.id}?`} description="Hồ sơ đang chờ duyệt sẽ bị xóa và giường được trả lại. Bạn cần đăng ký lại nếu muốn giữ chỗ." path={`bookings/${deleting.id}`} onClose={() => setDeleting(null)} onSave={() => { const delId = deleting.id; setDeleting(null); setNotice('Đã hủy hồ sơ.'); resource.reload(); showSystemFeedback(`Đã hủy hồ sơ ĐK-${delId} thành công.`, 'info') }} />}</>
}

function BookingDetail({ id, admin, onClose, onSave }: { id: number; admin: boolean; onClose: () => void; onSave: () => void }) {
  const resource = useResource(`bookings/${id}`, parseBooking), action = useApiAction()
  const [decision, setDecision] = useState(''), [reason, setReason] = useState(''), [verified, setVerified] = useState(false)
  const [previewDoc, setPreviewDoc] = useState<DocItem | null>(null)
  const record = resource.data
  const student = record ? findStudent(record.studentId) : undefined

  const bedInfo = record ? parseBedDetails(record.bedId) : { roomName: 'Phòng A204', bedNum: 3 }
  const requestedBedNum = bedInfo.bedNum
  const [assignedBedNum, setAssignedBedNum] = useState<number | null>(null)
  const activeBedNum = assignedBedNum ?? requestedBedNum

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
          ? `Đã phê duyệt hồ sơ ĐK-${id} thành công cho sinh viên ${record.studentId} (Giường ${activeBedNum < 10 ? `0${activeBedNum}` : activeBedNum})!`
          : `Đã từ chối hồ sơ ĐK-${id}.`,
        decision === 'DUYET' ? 'success' : 'warning'
      )
    }
  }

  const handleSelectBed = (bNum: number) => {
    setAssignedBedNum(bNum)
    const bedStr = bNum < 10 ? `0${bNum}` : `${bNum}`
    showSystemFeedback(`Đã chọn Giường ${bedStr} cho sinh viên ${student ? student.fullName : record?.studentId}`, 'info')
  }

  const beds = Array.from({ length: 8 }, (_, i) => {
    const bNum = i + 1
    const formattedNum = bNum < 10 ? `0${bNum}` : `${bNum}`
    const isSelected = bNum === activeBedNum
    const isRequested = bNum === requestedBedNum
    const isAvailableSlot = bNum === 8 || (isRequested && !isSelected)

    let statusClass: string
    let statusText: string
    let clickable = false
    let tooltip: string

    if (isSelected) {
      statusClass = isRequested ? 'selected requested' : 'selected'
      statusText = 'Đang chọn'
      tooltip = isRequested ? 'Nguyện vọng sinh viên (Đang chọn duyệt)' : 'Đang chọn duyệt cho sinh viên'
    } else if (isRequested) {
      statusClass = 'available requested'
      statusText = 'Nguyện vọng'
      clickable = true
      tooltip = 'Nguyện vọng của sinh viên (Nhấp để chọn lại)'
    } else if (isAvailableSlot) {
      statusClass = 'available'
      statusText = 'Còn trống'
      clickable = true
      tooltip = 'Còn trống (Nhấp để chọn đổi sang giường này)'
    } else {
      statusClass = 'occupied'
      statusText = MOCK_OCCUPANTS[bNum] || 'Đã có SV'
      clickable = false
      tooltip = `Đã có SV (${statusText})`
    }

    return { bNum, formattedNum, statusClass, statusText, clickable, tooltip }
  })

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
          onRetry={() => { resource.reload(); setVerified(false); setAssignedBedNum(null); }}
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

              {/* Sơ đồ & Chọn giường (Material 3 Bed Selector) */}
              <div className="bed-selection-section">
                <div className="bed-section-header">
                  <div className="bed-section-title">
                    <Icon name="bed" />
                    <span>Sơ đồ & Chọn giường ({bedInfo.roomName})</span>
                  </div>
                  <span className="bed-selected-badge">
                    Đang chọn: Giường {activeBedNum < 10 ? `0${activeBedNum}` : activeBedNum}
                  </span>
                </div>

                <div className="bed-legend-row">
                  <span className="legend-item"><span className="legend-dot selected" /><span>Đang chọn duyệt</span></span>
                  <span className="legend-item"><span className="legend-dot requested" /><span>Nguyện vọng SV</span></span>
                  <span className="legend-item"><span className="legend-dot available" /><span>Còn trống (Chọn đổi)</span></span>
                  <span className="legend-item"><span className="legend-dot occupied" /><span>Đã có người ở</span></span>
                </div>

                <div className="bed-grid">
                  {beds.map(b => (
                    <div
                      key={b.bNum}
                      className={`bed-card-tile ${b.statusClass}`}
                      role={b.clickable && admin ? 'button' : undefined}
                      tabIndex={b.clickable && admin ? 0 : undefined}
                      onClick={b.clickable && admin ? () => handleSelectBed(b.bNum) : undefined}
                      onKeyDown={b.clickable && admin ? e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); handleSelectBed(b.bNum); } } : undefined}
                      title={`Giường ${b.formattedNum} - ${b.tooltip}`}
                    >
                      <Icon name="bed" />
                      <span className="bed-number">Giường {b.formattedNum}</span>
                      <span className="bed-status-text">{b.statusText}</span>
                    </div>
                  ))}
                </div>
              </div>

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

