export const MAX_MONEY = 999_999_999_999_999
export type Resident = { code: string; name: string; discount: number }
export type InvoiceRoom = {
  id: string; building: string; period: string; roomPrice: number
  electricity: { previous: number | null; current: number | null; rate: number }
  water: { previous: number | null; current: number | null; rate: number }
  residents: readonly Resident[]; issued?: boolean
}
export type ExtraCharge = { amount: string; note: string }
export type Calculation = {
  room: InvoiceRoom; issue: string; roomFee: number; electricity: number; water: number
  discount: number; extra: number; extraNote: string; total: number; shares: { resident: Resident; roomFee: number; discount: number }[]
}
export type Draft = { version: number; period: string; createdAt: number; rows: Calculation[]; skipped: Calculation[] }
export type IssuanceResult = { room: string; status: 'issued' | 'skipped' | 'error'; message: string; total: number; due?: string }
const amountValid = (value: number) => Number.isSafeInteger(value) && value >= 0 && value <= MAX_MONEY
function discountBasisPoints(value: number) {
  const parts = String(value).match(/^(\d{1,3})(?:\.(\d{1,2}))?$/)
  if (!parts || value > 100) return null
  return BigInt(parts[1]) * 100n + BigInt((parts[2] ?? '').padEnd(2, '0'))
}

export function extraIssue(extra: ExtraCharge) {
  if (!/^\d+$/.test(extra.amount) || !amountValid(Number(extra.amount))) return 'Nhập số tiền nguyên đồng từ 0 đến 999.999.999.999.999.'
  if (Number(extra.amount) > 0 && !extra.note.trim()) return 'Nhập căn cứ hoặc ghi chú cho khoản thu thêm.'
  return ''
}
export function calculateInvoice(room: InvoiceRoom, period: string, extra: ExtraCharge = { amount: '0', note: '' }): Calculation {
  const result: Calculation = { room, issue: '', roomFee: room.roomPrice, electricity: 0, water: 0, discount: 0, extra: 0, extraNote: extra.note.trim(), total: 0, shares: [] }
  const fail = (issue: string) => ({ ...result, issue })
  if (room.period !== period) return fail('Chưa có dữ liệu chỉ số, đơn giá và lưu trú cho kỳ này.')
  if (room.issued) return fail('Đã phát hành hóa đơn cho phòng trong kỳ này.')
  if (!room.residents.length) return fail('Chưa có sinh viên lưu trú tại ngày đầu kỳ.')
  if (new Set(room.residents.map(r => r.code)).size !== room.residents.length) return fail('Danh sách lưu trú trùng MSSV; cần kiểm tra lại.')
  if (!amountValid(room.roomPrice)) return fail('Phí phòng không hợp lệ.')
  const chargeIssue = extraIssue(extra)
  if (chargeIssue) return fail(chargeIssue)
  for (const [label, meter] of [['điện', room.electricity], ['nước', room.water]] as const) {
    if (meter.previous === null || meter.current === null) return fail(`Thiếu chỉ số ${label}.`)
    if (!amountValid(meter.previous) || !amountValid(meter.current) || !amountValid(meter.rate) || meter.current < meter.previous) return fail(`Chỉ số hoặc đơn giá ${label} không hợp lệ.`)
  }
  if (room.residents.some(r => discountBasisPoints(r.discount) === null)) return fail('Mức giảm tiền phòng không hợp lệ.')
  const count = BigInt(room.residents.length), base = BigInt(room.roomPrice) / count, remainder = BigInt(room.roomPrice) % count
  result.shares = [...room.residents].sort((a, b) => a.code < b.code ? -1 : a.code > b.code ? 1 : 0).map((resident, index) => {
    const share = base + (BigInt(index) < remainder ? 1n : 0n)
    return { resident, roomFee: Number(share), discount: Number(share * discountBasisPoints(resident.discount)! / 10000n) }
  })
  const electricity = BigInt(room.electricity.current! - room.electricity.previous!) * BigInt(room.electricity.rate)
  const water = BigInt(room.water.current! - room.water.previous!) * BigInt(room.water.rate)
  const discount = result.shares.reduce((sum, share) => sum + BigInt(share.discount), 0n)
  const total = BigInt(room.roomPrice) + electricity + water + BigInt(extra.amount) - discount
  if (electricity > BigInt(MAX_MONEY) || water > BigInt(MAX_MONEY) || total > BigInt(MAX_MONEY) || total < 0n) return fail('Số tiền vượt giới hạn hóa đơn; cần kiểm tra dữ liệu.')
  return { ...result, electricity: Number(electricity), water: Number(water), discount: Number(discount), extra: Number(extra.amount), total: Number(total) }
}

// Business dates use Vietnam calendar days, including month/year rollover.
export function dueDate(issuedAt: number) {
  const vietnam = new Date(issuedAt + 7 * 60 * 60 * 1000)
  vietnam.setUTCDate(vietnam.getUTCDate() + 15)
  return vietnam.toISOString().slice(0, 10)
}
export function formatDate(date: string) { return date.split('-').reverse().join('/') }
