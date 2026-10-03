export type ViewState = 'data' | 'loading' | 'empty' | 'error'
export type DemoOutcome = 'success' | 'conflict' | 'unavailable'
export type WorkflowOptions = { preview?: boolean; view?: ViewState; outcome?: DemoOutcome }
export function searchText(value: string) { return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[đĐ]/g, 'd').toLowerCase().trim() }
export function money(value: number | bigint) { return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value) }
export function dateTime(value: string) { return new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short', timeStyle: 'short', timeZone: 'Asia/Ho_Chi_Minh' }).format(new Date(value)) }
