import { integer, object, paged, text } from '../../shared/api/protocol.ts'
export type Room = { id: string; buildingId: string; roomType: string; gender: string; capacity: number; availableBeds: number }
export type Bed = { id: string; number: string; status: string; version: number }
export function parseRoom(value: unknown): Room { const v = object(value); return { id: text(v.id), buildingId: text(v.buildingId), roomType: text(v.roomType), gender: text(v.gender), capacity: integer(v.capacity, 1), availableBeds: integer(v.availableBeds) } }
export const parseRooms = (v: unknown) => paged(v, parseRoom)
export function parseBed(value: unknown): Bed { const v = object(value); return { id: text(v.id), number: text(v.number), status: text(v.status), version: integer(v.version, 1) } }
export const parseBeds = (v: unknown) => paged(v, parseBed)
export const bedLabels: Record<string, string> = { TRONG: 'Trống', GIU_CHO: 'Giữ chỗ', DANG_O: 'Đang ở', KHOA: 'Khóa' }
export const roomLabels: Record<string, string> = { TRONG: 'Trống', CON_CHO: 'Còn chỗ', DAY: 'Đầy', DANG_SUA: 'Đang sửa', KHOA: 'Khóa' }
