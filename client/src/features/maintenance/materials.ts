import { integer, list, object, text } from '@/shared/api/protocol'
export type Material = { id: string; name: string; unit: string; stock: number; price: number }
/** Đọc đúng contract danh mục kho từ API. */
export function parseMaterials(value: unknown): Material[] {
  return list(value, row => {
    const v = object(row)
    return { id: text(v.id), name: text(v.name), unit: text(v.unit), stock: integer(v.stock, 0), price: integer(v.price, 0) }
  })
}

