import { useState, useRef, useEffect, useId } from 'react'
import { useResource } from '../api/hooks'
import { list, object, text } from '../api/protocol'
import { Icon } from './Icon'
import type { StudentInfo } from '../data/students'

type Props = { label: string; value: string; onChange: (id: string, student?: StudentInfo) => void; disabled?: boolean; readOnly?: boolean; required?: boolean; errorText?: string; supportingText?: string; maxLength?: number; name?: string; autoFocus?: boolean }
function parseStudents(value: unknown): StudentInfo[] {
  return list(value, row => { const v = object(row); return { id: text(v.id), name: text(v.name), room: typeof v.room === 'string' ? v.room : undefined } })
}

/** Danh sách lấy từ API theo quyền, mở khi focus; mũi tên/Enter/Escape để chọn bằng bàn phím. */
export function StudentPicker({ label, value, onChange, disabled, readOnly, required, errorText, supportingText, maxLength = 50, name, autoFocus }: Props) {
  const [open, setOpen] = useState(false), [active, setActive] = useState(-1)
  const container = useRef<HTMLDivElement>(null), input = useRef<HTMLInputElement>(null), id = useId(), options = useId()
  const query = useResource(open && !disabled && !readOnly ? `students?search=${encodeURIComponent(value)}` : null, parseStudents)
  const rows = query.data ?? []
  useEffect(() => {
    const close = (e: MouseEvent) => { if (container.current && !container.current.contains(e.target as Node)) setOpen(false) }
    document.addEventListener('mousedown', close)
    return () => document.removeEventListener('mousedown', close)
  }, [])
  function choose(student: StudentInfo) { onChange(student.id, student); input.current?.focus(); setOpen(false); setActive(-1) }
  return <div className="student-picker-container" ref={container}>
    <div className={`m3-field ${errorText ? 'm3-field--error' : ''}`}><div className="field-control">
      <label htmlFor={id}>{label}{required && ' *'}</label>
      <input ref={input} id={id} name={name} value={value} disabled={disabled} readOnly={readOnly} required={required} maxLength={maxLength} autoFocus={autoFocus} role="combobox" aria-autocomplete="list" aria-expanded={open} aria-controls={options} aria-activedescendant={open && rows[active] ? `${options}-${active}` : undefined}
        onFocus={() => { if (!readOnly && !disabled) setOpen(true) }}
        onChange={e => { onChange(e.target.value); setOpen(true); setActive(-1) }}
        onKeyDown={e => {
          if (e.key === 'Escape') { setOpen(false); setActive(-1) }
          if (e.key === 'ArrowDown' || e.key === 'ArrowUp') { e.preventDefault(); setOpen(true); setActive(n => rows.length ? (n + (e.key === 'ArrowDown' ? 1 : -1) + rows.length) % rows.length : -1) }
          if (e.key === 'Enter' && open && rows[active]) { e.preventDefault(); choose(rows[active]) }
        }} />
      {value && !readOnly && !disabled && <button className="icon-button" type="button" aria-label="Xóa chọn sinh viên" onClick={() => { onChange(''); setOpen(true); setActive(-1); input.current?.focus() }}><Icon name="close" /></button>}
    </div>{(errorText || supportingText) && <p className={errorText ? 'field-error' : 'field-supporting'}>{errorText || supportingText}</p>}</div>
    {open && !readOnly && !disabled && <div className="student-dropdown-panel" id={options} role="listbox" aria-label="Danh sách sinh viên">
      {query.loading ? <p role="status">Đang tìm sinh viên…</p> : query.error ? <p role="alert">{query.error}</p> : rows.length ? rows.map((student, index) => <div key={student.id} id={`${options}-${index}`} role="option" aria-selected={active === index || value === student.id} className={`student-item ${active === index ? 'active' : ''}`} onMouseDown={e => e.preventDefault()} onClick={() => choose(student)}><strong>{student.id}</strong> · {student.name}{student.room && <small> · Phòng {student.room}</small>}</div>) : <p>Không tìm thấy sinh viên phù hợp</p>}
    </div>}
  </div>
}
