import { integer, nullableText, object, paged, text } from '../../shared/api/protocol.ts'
export type Room = { id: string; buildingId: string; roomType: string; gender: string; capacity: number; availableBeds: number; configuration?: { number: string; floor: number; basePrice: number; status: string; roomTypeId: string | null } }
export type Bed = { id: string; number: string; status: string; version: number; position?: string | null }
export function parseRoom(value: unknown): Room {
  const v = object(value)
  const configuration = ['number', 'floor', 'basePrice', 'status', 'roomTypeId'].every(key => Object.hasOwn(v, key))
    ? { number: text(v.number), floor: integer(v.floor, 1), basePrice: integer(v.basePrice), status: text(v.status), roomTypeId: nullableText(v.roomTypeId) } : undefined
  return { id: text(v.id), buildingId: text(v.buildingId), roomType: text(v.roomType), gender: text(v.gender), capacity: integer(v.capacity, 1), availableBeds: integer(v.availableBeds), configuration }
}
export const parseRooms = (v: unknown) => paged(v, parseRoom)
export function parseBed(value: unknown): Bed { const v = object(value); return { id: text(v.id), number: text(v.number), status: text(v.status), version: integer(v.version, 1), ...(Object.hasOwn(v, 'position') ? { position: nullableText(v.position) } : {}) } }
export const parseBeds = (v: unknown) => paged(v, parseBed)
export const bedLabels: Record<string, string> = { TRONG: 'Trống', GIU_CHO: 'Giữ chỗ', DANG_O: 'Đang ở', KHOA: 'Khóa' }
export const roomLabels: Record<string, string> = { TRONG: 'Trống', CON_CHO: 'Còn chỗ', DAY: 'Đầy', DANG_SUA: 'Đang sửa', KHOA: 'Khóa' }

export const maxRoomPrice = 999999999999999
type CatalogFields = { id: string; number: string; building: string; type: string; floor: string; capacity: string; price: string; position: string; status: string }
export function catalogPayload(kind: 'room' | 'bed', fields: CatalogFields, roomId: string) {
  const code = (value: string, label: string, optional = false) => {
    const result = value.trim()
    if ((!optional && !result) || result.length > 20) throw new Error(`${label} phải dài từ 1 đến 20 ký tự.`)
    return result
  }
  const id = code(fields.id, 'Mã'), number = code(fields.number, 'Số / tên hiển thị')
  if (kind === 'bed') {
    if (!Object.hasOwn(bedLabels, fields.status)) throw new Error('Chọn trạng thái giường hợp lệ.')
    if (fields.position.trim().length > 50) throw new Error('Vị trí giường tối đa 50 ký tự.')
    return { id, number, roomId: code(roomId, 'Mã phòng'), position: fields.position.trim() || null, status: fields.status }
  }
  const wholeNumber = (value: string, label: string, min: number, max: number) => {
    const n = Number(value)
    if (!/^\d+$/.test(value) || !Number.isSafeInteger(n) || n < min || n > max) throw new Error(`${label} phải là số nguyên từ ${min} đến ${max}.`)
    return n
  }
  if (!Object.hasOwn(roomLabels, fields.status)) throw new Error('Chọn trạng thái phòng hợp lệ.')
  return { id, number, buildingId: code(fields.building, 'Mã tòa'), roomTypeId: code(fields.type, 'Mã loại phòng', true) || null,
    floor: wholeNumber(fields.floor, 'Tầng', 1, 2147483647), capacity: wholeNumber(fields.capacity, 'Sức chứa', 1, 2147483647),
    basePrice: wholeNumber(fields.price, 'Đơn giá phòng', 0, maxRoomPrice), status: fields.status }
}
