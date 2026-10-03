import type { Draft } from './model'

// Minimal OOXML workbook: stored ZIP entries, inline text (never Excel formulas).
// No uploaded files are parsed; this export contains only the reviewed draft.
const encoder = new TextEncoder()
const escapeXml = (value: string) => Array.from(value).filter(char => char.charCodeAt(0) >= 32 || ['\t', '\n', '\r'].includes(char)).join('').replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&apos;' })[char]!)
function columnName(index: number): string { return index < 26 ? String.fromCharCode(65 + index) : columnName(Math.floor(index / 26) - 1) + columnName(index % 26) }
function crc32(bytes: Uint8Array) {
  let crc = 0xffffffff
  for (const byte of bytes) { crc ^= byte; for (let bit = 0; bit < 8; bit++) crc = (crc >>> 1) ^ (crc & 1 ? 0xedb88320 : 0) }
  return (crc ^ 0xffffffff) >>> 0
}
function storedZip(files: [string, string][]) {
  const local: Uint8Array[] = [], central: Uint8Array[] = []
  let offset = 0, centralSize = 0
  for (const [path, text] of files) {
    const name = encoder.encode(path), data = encoder.encode(text), crc = crc32(data)
    const header = new Uint8Array(30 + name.length), view = new DataView(header.buffer)
    view.setUint32(0, 0x04034b50, true); view.setUint16(4, 20, true); view.setUint16(12, 33, true)
    view.setUint32(14, crc, true); view.setUint32(18, data.length, true); view.setUint32(22, data.length, true); view.setUint16(26, name.length, true); header.set(name, 30)
    local.push(header, data)
    const directory = new Uint8Array(46 + name.length), entry = new DataView(directory.buffer)
    entry.setUint32(0, 0x02014b50, true); entry.setUint16(4, 20, true); entry.setUint16(6, 20, true); entry.setUint16(14, 33, true)
    entry.setUint32(16, crc, true); entry.setUint32(20, data.length, true); entry.setUint32(24, data.length, true); entry.setUint16(28, name.length, true); entry.setUint32(42, offset, true); directory.set(name, 46)
    central.push(directory); offset += header.length + data.length; centralSize += directory.length
  }
  const end = new Uint8Array(22), view = new DataView(end.buffer)
  view.setUint32(0, 0x06054b50, true); view.setUint16(8, files.length, true); view.setUint16(10, files.length, true); view.setUint32(12, centralSize, true); view.setUint32(16, offset, true)
  return new Blob([...local, ...central, end].map(bytes => Uint8Array.from(bytes).buffer), { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
}
export function draftSpreadsheet(draft: Draft) {
  const rows: (string | number)[][] = [
    ['NHÁP MINH HỌA — CHƯA PHÁT HÀNH'], ['Kỳ', draft.period, 'Phiên bản', draft.version],
    ['Phòng', 'Tiền phòng', 'Tiền điện', 'Tiền nước', 'Giảm giá', 'Chi phí thêm', 'Tổng tiền', 'Ghi chú', 'Trạng thái'],
    ...draft.rows.map(r => [r.room.id, r.roomFee, r.electricity, r.water, r.discount, r.extra, r.total, r.extraNote, 'Sẵn sàng (minh họa)']),
    ...draft.skipped.map(r => [r.room.id, '', '', '', '', '', '', r.issue, 'Bỏ qua']),
  ]
  const sheet = `<?xml version="1.0" encoding="UTF-8"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetViews><sheetView workbookViewId="0"><pane ySplit="3" topLeftCell="A4" activePane="bottomLeft" state="frozen"/></sheetView></sheetViews><cols><col min="1" max="7" width="20" customWidth="1"/><col min="8" max="9" width="45" customWidth="1"/></cols><sheetData>${rows.map((row, i) => `<row r="${i + 1}">${row.map((value, j) => `<c r="${columnName(j)}${i + 1}"${typeof value === 'number' ? `><v>${value}</v>` : ` t="inlineStr"><is><t xml:space="preserve">${escapeXml(value)}</t></is>`}</c>`).join('')}</row>`).join('')}</sheetData></worksheet>`
  return storedZip([
    ['[Content_Types].xml', '<?xml version="1.0" encoding="UTF-8"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>'],
    ['_rels/.rels', '<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>'],
    ['xl/workbook.xml', '<?xml version="1.0" encoding="UTF-8"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="Nháp hóa đơn" sheetId="1" r:id="rId1"/></sheets></workbook>'],
    ['xl/_rels/workbook.xml.rels', '<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>'],
    ['xl/worksheets/sheet1.xml', sheet],
  ])
}
export function downloadDraft(draft: Draft) {
  const url = URL.createObjectURL(draftSpreadsheet(draft)), link = document.createElement('a')
  link.href = url; link.download = `NHAP-MINH-HOA-${draft.period}-v${draft.version}.xlsx`
  document.body.append(link); link.click(); link.remove()
  window.setTimeout(() => URL.revokeObjectURL(url), 1000)
}
