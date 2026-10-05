import { useState } from 'react'
import { useApiAction, useResource } from '@/shared/api/hooks'
import { command } from '@/shared/api/protocol'
import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { TextField } from '@/shared/components/TextField'
import { Feedback, SelectField } from '@/shared/workflow/Workflow'
import { bedLabels, roomLabels, parseRoom, catalogPayload, maxRoomPrice, type Room, type Bed } from './model'

function CatalogForm({ kind, room, bed, roomId = '', onClose, onSave }: { kind: 'room' | 'bed'; room?: Room; bed?: Bed; roomId?: string; onClose: () => void; onSave: () => void }) {
  const existing = room || bed, [id, setId] = useState(existing?.id ?? ''), [number, setNumber] = useState(room?.configuration?.number ?? bed?.number ?? '')
  const [building, setBuilding] = useState(room?.buildingId ?? ''), [type, setType] = useState(room?.configuration?.roomTypeId ?? ''), [floor, setFloor] = useState(room?.configuration?.floor.toString() ?? ''), [capacity, setCapacity] = useState(room?.capacity.toString() ?? ''), [price, setPrice] = useState(room?.configuration?.basePrice.toString() ?? ''), [position, setPosition] = useState(bed?.position ?? '')
  const [status, setStatus] = useState(room?.configuration?.status ?? bed?.status ?? (kind === 'room' ? 'CON_CHO' : 'TRONG')), action = useApiAction()
  async function submit() {
    let body: ReturnType<typeof catalogPayload>
    try { body = catalogPayload(kind, { id, number, building, type, floor, capacity, price, position, status }, roomId) }
    catch (error) { action.setError((error as Error).message); return }
    if (await action.run(existing ? `${kind === 'room' ? 'rooms' : 'beds'}/${encodeURIComponent(id)}` : kind === 'room' ? 'rooms' : 'beds', body, command, existing ? 'PUT' : 'POST')) onSave()
  }
  return <Dialog title={`${existing ? 'Sửa' : 'Thêm'} ${kind === 'room' ? 'phòng' : 'giường'}`} onClose={onClose} busy={action.busy} actions={<><Button variant="text" disabled={action.busy} onClick={onClose}>Hủy</Button><Button type="submit" form="catalog-edit" pending={action.busy}>Lưu</Button></>}><Feedback error={action.error} /><form id="catalog-edit" className="wl-form" onSubmit={e => { e.preventDefault(); void submit() }}><fieldset disabled={action.busy} className="gate-fieldset"><TextField label={`Mã ${kind === 'room' ? 'phòng' : 'giường'}`} value={id} onChange={e => setId(e.target.value)} readOnly={!!existing} required maxLength={20} /><TextField label="Số / tên hiển thị" value={number} onChange={e => setNumber(e.target.value)} required maxLength={20} />{kind === 'room' ? <><TextField label="Mã tòa" value={building} onChange={e => setBuilding(e.target.value)} required maxLength={20} /><TextField label="Mã loại phòng" value={type} onChange={e => setType(e.target.value)} maxLength={20} supportingText="Dùng mã loại phòng đã có trong danh mục." /><TextField label="Tầng" value={floor} onChange={e => setFloor(e.target.value)} type="number" min="1" max={2147483647} step="1" required /><TextField label="Sức chứa" value={capacity} onChange={e => setCapacity(e.target.value)} type="number" min="1" max={2147483647} step="1" required /><TextField label="Đơn giá phòng (đồng)" value={price} onChange={e => setPrice(e.target.value)} type="number" min="0" max={maxRoomPrice} step="1" required /></> : <TextField label="Vị trí giường" value={position} onChange={e => setPosition(e.target.value)} maxLength={50} supportingText="Tối đa 50 ký tự." />}<SelectField label="Trạng thái" value={status} onChange={e => setStatus(e.target.value)} required><option value="">Chọn trạng thái</option>{Object.entries(kind === 'room' ? roomLabels : bedLabels).map(([v,l]) => <option key={v} value={v}>{l}</option>)}</SelectField></fieldset></form></Dialog>
}


// PUT replaces the entire configuration. Missing fields must never become guessed defaults.
export function CatalogEditDialog(props: Parameters<typeof CatalogForm>[0]) {
  if (props.room) return <ExistingRoomForm {...props} room={props.room} />
  if (props.bed && !Object.hasOwn(props.bed, 'position')) return <Dialog title="Chưa thể sửa giường" onClose={props.onClose} actions={<Button variant="text" onClick={props.onClose}>Đóng</Button>}><p>Hệ thống chưa cung cấp vị trí giường đang lưu. Cần bổ sung dữ liệu cấu hình trước khi sửa để tránh ghi đè thông tin hiện tại.</p></Dialog>
  return <CatalogForm {...props} />
}
function ExistingRoomForm(props: Parameters<typeof CatalogForm>[0] & { room: Room }) {
  const resource = useResource(`rooms/${encodeURIComponent(props.room.id)}`, parseRoom)
  if (resource.data?.configuration) return <CatalogForm {...props} room={resource.data} />
  return <Dialog title="Cấu hình phòng" onClose={props.onClose} actions={<><Button variant="outlined" disabled={resource.loading} onClick={resource.reload}>Tải lại</Button><Button variant="text" onClick={props.onClose}>Đóng</Button></>}>
    <Feedback error={resource.error} />
    {resource.loading ? <p role="status">Đang tải cấu hình phòng…</p> : !resource.error && <p>Hệ thống chưa cung cấp đầy đủ số phòng, tầng, loại phòng, đơn giá và trạng thái đang lưu. Cần bổ sung dữ liệu cấu hình trước khi sửa để tránh ghi đè thông tin hiện tại.</p>}
  </Dialog>
}
