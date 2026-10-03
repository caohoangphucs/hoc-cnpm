import { useEffect, useId, useRef, useState } from 'react'
import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { Icon } from '@/shared/components/Icon'
import { fileMetadataError, type Documents } from './model'

async function validateImage(file: File) {
  const metadataError = fileMetadataError(file)
  if (metadataError) return metadataError
  const bytes = new Uint8Array(await file.slice(0, 8).arrayBuffer())
  const validHeader = file.type === 'image/png'
    ? [137, 80, 78, 71, 13, 10, 26, 10].every((byte, index) => bytes[index] === byte)
    : bytes[0] === 255 && bytes[1] === 216 && bytes[2] === 255
  if (!validHeader) return 'Nội dung tệp không phải ảnh JPG/PNG hợp lệ.'
  try { const image = await createImageBitmap(file); image.close() }
  catch { return 'Không đọc được ảnh này. Vui lòng chọn ảnh khác.' }
}
function ImagePreview({ file, onClose }: { file: File; onClose: () => void }) {
  const image = useRef<HTMLImageElement>(null)
  useEffect(() => { const objectUrl = URL.createObjectURL(file); const element = image.current!; element.src = objectUrl; return () => { element.removeAttribute('src'); URL.revokeObjectURL(objectUrl) } }, [file])
  return <Dialog title="Xem ảnh đã chọn" onClose={onClose} actions={<Button variant="outlined" onClick={onClose}>Đóng</Button>}>
    <p className="booking-file-name">{file.name}</p><img ref={image} className="booking-image-preview" alt="Ảnh hồ sơ bạn vừa chọn" />
  </Dialog>
}
export function DocumentField({ title, files, onChange, error, max = 1, field, onChecking }: {
  title: string; files: readonly File[]; onChange: (files: readonly File[]) => void; error?: string; max?: number; field: keyof Documents; onChecking: (field: keyof Documents, checking: boolean) => void
}) {
  const id = useId()
  const input = useRef<HTMLInputElement>(null)
  const generation = useRef(0)
  const [checking, setChecking] = useState(false)
  const [localError, setLocalError] = useState('')
  const [selected, setSelected] = useState<File | null>(null)
  useEffect(() => () => { generation.current++; onChecking(field, false) }, [field, onChecking])
  async function choose(next: File[]) {
    const run = ++generation.current
    setLocalError('')
    if (next.length > max || (max > 1 && files.length + next.length > max)) { setLocalError(`Chọn tối đa ${max} ảnh cho mục này.`); return }
    setChecking(true)
    onChecking(field, true)
    try {
      for (const file of next) {
        const issue = await validateImage(file)
        if (run !== generation.current) return
        if (issue) { setLocalError(`${file.name}: ${issue}`); return }
      }
      if (run === generation.current) onChange(max === 1 ? next : [...files, ...next])
    } catch { if (run === generation.current) setLocalError('Không đọc được tệp. Vui lòng chọn lại.') }
    finally { if (run === generation.current) { setChecking(false); onChecking(field, false) } }
  }
  const message = localError || error
  return <div className={`booking-upload ${message ? 'booking-upload--error' : ''}`}>
    <div className="booking-upload-heading"><span className="booking-upload-symbol"><Icon name="document" /></span><div><h3 id={`${id}-title`}>{title}{max === 1 && <span className="booking-required"> *</span>}</h3><p>JPG, PNG · tối đa 5 MB/ảnh{max > 1 ? ' · tối đa 3 ảnh' : ''}</p></div></div>
    <input id={id} ref={input} type="file" accept="image/jpeg,image/png,.jpg,.jpeg,.png" multiple={max > 1} aria-labelledby={`${id}-title`} aria-describedby={`${id}-notice`} aria-invalid={message ? true : undefined} disabled={checking}
      onChange={(event) => { const next = Array.from(event.target.files ?? []); event.target.value = ''; if (next.length) void choose(next) }} />
    <Button variant="outlined" aria-describedby={`${id}-notice`} aria-invalid={message ? true : undefined} pending={checking} disabled={max > 1 && files.length >= max} onClick={() => input.current?.click()}>{checking ? 'Đang kiểm tra ảnh…' : files.length && max === 1 ? `Thay ảnh ${title}` : `Chọn ảnh ${title}`}</Button>
    {files.length > 0 && <ul className="booking-file-list">{files.map((file, index) => <li key={`${file.name}-${index}`}><button className="booking-file-link" onClick={() => setSelected(file)} aria-label={`Xem ảnh ${file.name}`}><Icon name="eye" /><span><strong>{file.name}</strong><small>{Math.max(1, Math.ceil(file.size / 1024))} KB</small></span></button><button className="icon-button state-layer" aria-label={`Bỏ ảnh ${file.name}`} disabled={checking} onClick={() => { setLocalError(''); onChange(files.filter((_, i) => i !== index)) }}><Icon name="close" /></button></li>)}</ul>}
    <p id={`${id}-notice`} className={`booking-file-message ${message ? 'booking-error-text' : ''}`} role="status">{message || (checking ? 'Đang kiểm tra tệp ảnh trên máy của bạn.' : 'Ảnh chỉ được giữ tạm trong trang, chưa tải lên hệ thống.')}</p>
    {selected && <ImagePreview file={selected} onClose={() => setSelected(null)} />}
  </div>
}
