export type RepairStatus = 'MOI' | 'DANG_XU_LY' | 'HOAN_THANH' | 'HUY'
export type Priority = 'THAP' | 'TRUNG_BINH' | 'CAO' | 'KHAN_CAP'
export type Supply = { id: string; name: string; unit: string; stock: number }
export type SupplyLine = { supplyId: string; quantity: number }
export type Repair = {
  id: number; code: string; room: string; student: string; title: string; description: string
  category: 'Điện' | 'Nước' | 'Thiết bị'; priority: Priority; status: RepairStatus; version: number
  createdAt: string; assignedTo?: number; technician?: string; schedule?: string
  warranty?: { provider: string; expectedAt: string }; warrantyUntil?: string
  completedAt?: string; note?: string; usedSupplies: SupplyLine[]
  history: { at: string; actor: string; note: string }[]
}
export type Purchase = { key: string; repairId: number; supplyId: string; quantity: number; at: string }
export type RepairData = { repairs: Repair[]; supplies: Supply[]; purchases: Purchase[] }
export type RepairMode = 'accept' | 'schedule' | 'warranty' | 'complete' | 'cancel'
export type RepairDraft = { schedule: string; note: string; provider: string; warrantyUntil: string; received: boolean; supplies: SupplyLine[] }
export type RepairErrors = Partial<Record<'schedule' | 'note' | 'provider' | 'warrantyUntil' | 'received' | 'supplies', string>>
export const statusLabels: Record<RepairStatus, string> = { MOI: 'Chưa tiếp nhận', DANG_XU_LY: 'Đang xử lý', HOAN_THANH: 'Hoàn thành', HUY: 'Đã hủy' }
export const priorityLabels: Record<Priority, string> = { THAP: 'Thấp', TRUNG_BINH: 'Bình thường', CAO: 'Cao', KHAN_CAP: 'Khẩn cấp' }
export const priorityOrder: Record<Priority, number> = { KHAN_CAP: 0, CAO: 1, TRUNG_BINH: 2, THAP: 3 }
export const emptyDraft = (): RepairDraft => ({ schedule: '', note: '', provider: '', warrantyUntil: '', received: false, supplies: [] })

// datetime-local is always interpreted in the dormitory's timezone, independent of the device timezone.
export function vietnamInstant(value: string) {
  if (!/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(value)) return NaN
  const parsed = Date.parse(`${value}:00+07:00`)
  return Number.isFinite(parsed) && new Date(parsed + 7 * 3600000).toISOString().slice(0, 16) === value ? parsed : NaN
}
export function localInput(instant?: string) { return instant ? new Date(Date.parse(instant) + 7 * 3600000).toISOString().slice(0, 16) : '' }
export function vietnamToday(now: number) { return new Date(now + 7 * 3600000).toISOString().slice(0, 10) }
export function validDate(value: string) { return /^\d{4}-\d{2}-\d{2}$/.test(value) && Number.isFinite(Date.parse(value)) && new Date(value).toISOString().slice(0, 10) === value }
export function editableIssue(repair: Repair, actor: number, mode: RepairMode) {
  if (mode === 'accept') return repair.status === 'MOI' && !repair.assignedTo ? '' : 'Phiếu đã được tiếp nhận hoặc không còn ở trạng thái mới.'
  if (repair.status !== 'DANG_XU_LY') return 'Phiếu không còn ở trạng thái đang xử lý.'
  return repair.assignedTo === actor ? '' : 'Chỉ kỹ thuật viên phụ trách được cập nhật phiếu này.'
}
export function draftErrors(repair: Repair, mode: RepairMode, draft: RepairDraft, supplies: readonly Supply[], now: number): RepairErrors {
  const errors: RepairErrors = {}
  if (mode !== 'accept' && (!draft.note.trim() || draft.note.trim().length > 1000)) errors.note = 'Nhập nội dung từ 1 đến 1.000 ký tự.'
  if (['accept', 'schedule', 'warranty'].includes(mode) && (!Number.isFinite(vietnamInstant(draft.schedule)) || vietnamInstant(draft.schedule) <= now)) errors.schedule = 'Chọn thời gian trong tương lai theo giờ Việt Nam.'
  if (mode === 'warranty' && (!draft.provider.trim() || draft.provider.trim().length > 200)) errors.provider = 'Nhập tên đơn vị bảo hành (tối đa 200 ký tự).'
  if (mode === 'complete') {
    if (repair.warranty && !draft.received) errors.received = 'Xác nhận thiết bị đã được bàn giao trước khi hoàn thành.'
    if (draft.warrantyUntil && (!validDate(draft.warrantyUntil) || draft.warrantyUntil < vietnamToday(now))) errors.warrantyUntil = 'Hạn bảo hành phải là ngày hợp lệ, từ hôm nay trở đi.'
    if (draft.supplies.length > 100 || new Set(draft.supplies.map(line => line.supplyId)).size !== draft.supplies.length) errors.supplies = 'Tối đa 100 vật tư; mỗi vật tư chỉ có một dòng.'
    else if (draft.supplies.some(line => !supplies.some(s => s.id === line.supplyId) || !Number.isInteger(line.quantity) || line.quantity < 1 || line.quantity > 2147483647)) errors.supplies = 'Chọn vật tư và nhập số lượng nguyên dương hợp lệ cho từng dòng.'
  }
  return errors
}
export function shortages(lines: readonly SupplyLine[], supplies: readonly Supply[]) {
  return lines.flatMap(line => { const supply = supplies.find(s => s.id === line.supplyId); return supply && line.quantity > supply.stock ? [{ ...supply, needed: line.quantity, missing: line.quantity - supply.stock }] : [] })
}

// Development-only simulation of a server decision; never an authorization or stock write to the backend.
export function applyPreviewRepair(data: RepairData, id: number, version: number, mode: RepairMode, draft: RepairDraft, actor: { id: number; name: string }, key: string, now: number): { data: RepairData; error?: string; shortage?: ReturnType<typeof shortages> } {
  const repair = data.repairs.find(r => r.id === id)
  if (!repair || repair.version !== version) return { data, error: 'Phiếu đã thay đổi. Kiểm tra lại trạng thái trước khi gửi.' }
  const issue = editableIssue(repair, actor.id, mode)
  if (issue) return { data, error: issue }
  if (Object.keys(draftErrors(repair, mode, draft, data.supplies, now)).length) return { data, error: 'Thông tin nhập chưa hợp lệ.' }
  const at = new Date(now).toISOString()
  if (mode === 'complete') {
    const missing = shortages(draft.supplies, data.supplies)
    if (missing.length) return { data: { ...data, purchases: [...data.purchases, ...missing.filter(s => !data.purchases.some(p => p.key === key && p.supplyId === s.id)).map(s => ({ key, repairId: id, supplyId: s.id, quantity: s.missing, at }))] }, shortage: missing }
  }
  const updated: Repair = { ...repair, version: repair.version + 1 }
  let history = draft.note.trim()
  const scheduleText = `${draft.schedule.slice(0, 10).split('-').reverse().join('/')} ${draft.schedule.slice(11)} (giờ Việt Nam)`
  if (mode === 'accept') { updated.status = 'DANG_XU_LY'; updated.assignedTo = actor.id; updated.technician = actor.name; updated.schedule = `${draft.schedule}:00+07:00`; history = `Tiếp nhận và lên lịch kiểm tra: ${scheduleText}.` }
  if (mode === 'schedule' || mode === 'warranty') { updated.schedule = `${draft.schedule}:00+07:00`; updated.note = draft.note.trim(); history = `${mode === 'warranty' ? `Bảo hành ngoài · ${draft.provider.trim()}` : 'Hẹn lại'} · ${scheduleText}: ${history}` }
  if (mode === 'warranty') updated.warranty = { provider: draft.provider.trim(), expectedAt: `${draft.schedule}:00+07:00` }
  if (mode === 'complete') { updated.status = 'HOAN_THANH'; updated.completedAt = at; updated.note = draft.note.trim(); updated.usedSupplies = draft.supplies.map(line => ({ ...line })); updated.warrantyUntil = draft.warrantyUntil || undefined; updated.warranty = undefined; history = `Hoàn thành: ${history}` }
  if (mode === 'cancel') { updated.status = 'HUY'; updated.note = draft.note.trim(); history = `Hủy phiếu: ${history}` }
  updated.history = [...repair.history, { at, actor: actor.name, note: history }]
  return { data: { ...data, repairs: data.repairs.map(r => r.id === id ? updated : r), supplies: mode === 'complete' ? data.supplies.map(s => ({ ...s, stock: s.stock - (draft.supplies.find(line => line.supplyId === s.id)?.quantity ?? 0) })) : data.supplies } }
}
