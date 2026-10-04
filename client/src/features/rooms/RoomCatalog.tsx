import { useState } from 'react'
import { useResource } from '@/shared/api/hooks'
import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { Icon } from '@/shared/components/Icon'
import { TextField } from '@/shared/components/TextField'
import { DataTable, Facts, ServerPagination, Status, Summary, WorkflowPage } from '@/shared/workflow/Workflow'
import { searchText } from '@/shared/workflow/model'
import { ConfirmAction } from '@/shared/workflow/ConfirmAction'
import { parseBeds, parseRoom, parseRooms, bedLabels, type Room } from './model'
import { CatalogEditDialog } from './RoomEditDialog'

export function RoomCatalog({ admin = false }: { admin?: boolean }) {
  const [page, setPage] = useState(1), [search, setSearch] = useState(''), [selected, setSelected] = useState<string | null>(null), [editing, setEditing] = useState<Room | 'new' | null>(null), [deleting, setDeleting] = useState<Room | null>(null)
  const resource = useResource(`rooms?page=${page}&pageSize=8`, parseRooms)
  const shown = (resource.data?.items ?? []).filter(r => searchText(`${r.id} ${r.buildingId} ${r.roomType}`).includes(searchText(search)))
  const rows = resource.data?.items ?? []
  return <><WorkflowPage title={admin ? 'Phòng & giường' : 'Tra cứu phòng'} description="Tra cứu phòng và số giường còn trống theo dữ liệu hiện tại." view={resource.loading ? 'loading' : resource.error ? 'error' : 'data'} error={resource.error} onRetry={resource.reload} actions={<div className="wl-actions">{admin && <Button onClick={() => setEditing('new')}>Thêm phòng</Button>}<Button variant="outlined" onClick={resource.reload}>Tải lại</Button></div>}>
    <Summary items={[{ label: 'Tổng số phòng', value: resource.data?.total ?? 0 }, { label: 'Giường trống trên trang', value: rows.reduce((sum, r) => sum + r.availableBeds, 0), tone: 'success' }, { label: 'Phòng hết chỗ trên trang', value: rows.filter(r => !r.availableBeds).length, tone: 'warning' }]} />
    <section className="wl-card"><div className="wl-toolbar"><TextField label="Tìm phòng trên trang" type="search" value={search} onChange={e => setSearch(e.target.value)} /></div><DataTable pagination={false} label="Danh sách phòng" head={<tr><th>Phòng / tòa</th><th>Loại phòng</th><th>Giới tính</th><th>Giường trống</th><th>Thao tác</th></tr>} rows={shown.map(r => <tr key={r.id} className="clickable-row" onClick={() => setSelected(r.id)} tabIndex={0} onKeyDown={e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); setSelected(r.id) } }} title="Nhấp vào dòng để xem danh sách giường trong phòng"><td><strong>{r.id}</strong><small>{r.buildingId}</small></td><td>{r.roomType}</td><td>{r.gender === 'NAM' ? 'Nam' : r.gender === 'NU' ? 'Nữ' : r.gender === 'TAT_CA' ? 'Tất cả' : r.gender}</td><td><Status tone={r.availableBeds ? 'success' : 'neutral'}>{r.availableBeds}/{r.capacity}</Status></td><td onClick={e => e.stopPropagation()}><Button variant="text" onClick={() => setSelected(r.id)}>Xem giường</Button>{admin && <><Button variant="text" onClick={() => setEditing(r)}>Sửa</Button><Button variant="text" onClick={() => setDeleting(r)}>Xóa</Button></>}</td></tr>)} /><ServerPagination page={page} count={resource.data?.items.length ?? 0} total={resource.data?.total} onPage={p => { setPage(p); setSearch('') }} /></section>
  </WorkflowPage>{selected && <RoomBeds key={selected} id={selected} admin={admin} onClose={() => setSelected(null)} onSave={resource.reload} />}{editing && <CatalogEditDialog kind="room" room={editing === 'new' ? undefined : editing} onClose={() => setEditing(null)} onSave={() => { setEditing(null); resource.reload() }} />}{deleting && <ConfirmAction title={`Xóa phòng ${deleting.id}?`} description="Phòng có lịch sử lưu trú hoặc nghiệp vụ liên quan sẽ được hệ thống giữ lại. Kiểm tra trước khi xác nhận." path={`rooms/${encodeURIComponent(deleting.id)}`} onClose={() => setDeleting(null)} onSave={() => { setDeleting(null); resource.reload() }} />}</>
}
function RoomBeds({ id, admin, onClose, onSave }: { id: string; admin: boolean; onClose: () => void; onSave: () => void }) {
  const [page, setPage] = useState(1), [editing, setEditing] = useState<string | null>(null), [deleting, setDeleting] = useState<string | null>(null), [viewMode, setViewMode] = useState<'grid' | 'table'>('grid')
  const room = useResource(`rooms/${encodeURIComponent(id)}`, parseRoom), beds = useResource(`rooms/${encodeURIComponent(id)}/beds?page=${page}&pageSize=8`, parseBeds)
  const reload = () => { beds.reload(); room.reload(); onSave() }
  const selected = beds.data?.items.find(b => b.id === editing)
  const bedList = beds.data?.items ?? []
  const emptyCount = bedList.filter(b => b.status === 'TRONG').length
  const occupiedCount = bedList.filter(b => b.status === 'DANG_O').length

  return <><Dialog title={`Phòng ${id}`} panel onClose={onClose} actions={<>{admin && <Button onClick={() => setEditing('new')}>Thêm giường</Button>}<Button variant="text" onClick={onClose}>Đóng</Button></>}>
    {room.data && <Facts items={[[ 'Loại phòng', room.data.roomType ], ['Tòa', room.data.buildingId ], ['Sức chứa', `${room.data.capacity} giường` ], ['Giường còn trống', `${room.data.availableBeds} chỗ` ]]} />}
    <WorkflowPage title="Sơ đồ & Danh sách giường" description="Trạng thái phân bổ giường hiện tại trên hệ thống." view={room.loading || beds.loading ? 'loading' : room.error || beds.error ? 'error' : 'data'} error={room.error || beds.error} onRetry={reload}>
      <div className="bed-selection-section">
        <div className="bed-section-header">
          <div className="bed-section-title">
            <Icon name="bed" />
            <span>Sơ đồ bố trí giường ({id})</span>
          </div>
          <div className="wl-segments" style={{ margin: 0 }}>
            <Button variant="outlined" aria-pressed={viewMode === 'grid'} onClick={() => setViewMode('grid')}>Sơ đồ trực quan</Button>
            <Button variant="outlined" aria-pressed={viewMode === 'table'} onClick={() => setViewMode('table')}>Dạng bảng</Button>
          </div>
        </div>

        <div className="bed-legend-bar">
          <span className="legend-item"><span className="legend-color-dot available"></span><span>Còn trống ({emptyCount})</span></span>
          <span className="legend-item"><span className="legend-color-dot roommate"></span><span>Đang ở ({occupiedCount})</span></span>
          <span className="legend-item"><span className="legend-color-dot" style={{ backgroundColor: 'var(--app-color-warning-container, #ffdea0)' }}></span><span>Giữ chỗ</span></span>
          <span className="legend-item"><span className="legend-color-dot locked"></span><span>Khóa / Sửa</span></span>
        </div>

        {viewMode === 'grid' ? (
          <div className="bed-grid-container">
            {bedList.map(b => (
              <div
                key={b.id}
                className={`bed-slot-card ${b.status === 'TRONG' ? 'available' : b.status === 'DANG_O' ? 'roommate' : b.status === 'KHOA' ? 'locked' : ''}`}
                onClick={admin ? () => setEditing(b.id) : undefined}
                role={admin ? 'button' : undefined}
                tabIndex={admin ? 0 : undefined}
                onKeyDown={admin ? (e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); setEditing(b.id) } }) : undefined}
                title={admin ? `Nhấp để quản lý ${b.number}` : `${b.number} - ${bedLabels[b.status] || b.status}`}
              >
                {b.status === 'TRONG' && <span className="bed-swap-tag">Còn trống</span>}
                <div className="bed-icon-wrapper">
                  <Icon name="bed" />
                </div>
                <div className="bed-num-label">{b.number}</div>
                <div className="bed-occupant-name">{bedLabels[b.status] || b.status}</div>
                <div className="bed-occupant-sub">{b.id}</div>
                {admin && (
                  <div className="wl-actions" style={{ marginTop: 4 }} onClick={e => e.stopPropagation()}>
                    <Button variant="text" onClick={() => setEditing(b.id)}>Sửa</Button>
                    <Button variant="text" onClick={() => setDeleting(b.id)}>Xóa</Button>
                  </div>
                )}
              </div>
            ))}
            {!bedList.length && <p className="wl-note">Chưa có giường nào trong phòng này.</p>}
          </div>
        ) : (
          <DataTable pagination={false} label="Giường trong phòng" head={<tr><th>Giường</th><th>Trạng thái</th>{admin && <th>Thao tác</th>}</tr>} rows={bedList.map(b => <tr key={b.id} className={admin ? 'clickable-row' : undefined} onClick={admin ? () => setEditing(b.id) : undefined} tabIndex={admin ? 0 : undefined} onKeyDown={admin ? (e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); setEditing(b.id) } }) : undefined} title={admin ? 'Nhấp vào dòng để sửa thông tin giường' : undefined}><td><strong>{b.number}</strong><small>{b.id}</small></td><td><Status tone={b.status === 'TRONG' ? 'success' : 'neutral'}>{bedLabels[b.status] || b.status}</Status></td>{admin && <td onClick={e => e.stopPropagation()}><Button variant="text" onClick={() => setEditing(b.id)}>Sửa</Button><Button variant="text" onClick={() => setDeleting(b.id)}>Xóa</Button></td>}</tr>)} />
        )}
      </div>
      <ServerPagination page={page} count={beds.data?.items.length ?? 0} total={beds.data?.total} onPage={setPage} />
    </WorkflowPage>
  </Dialog>{editing && <CatalogEditDialog kind="bed" roomId={id} bed={selected} onClose={() => setEditing(null)} onSave={() => { setEditing(null); reload() }} />}{deleting && <ConfirmAction title="Xóa giường?" description="Giường có lịch sử đăng ký hoặc lưu trú sẽ không thể xóa." path={`beds/${encodeURIComponent(deleting)}`} onClose={() => setDeleting(null)} onSave={() => { setDeleting(null); reload() }} />}</>
}
