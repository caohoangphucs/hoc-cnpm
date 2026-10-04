import { useState, useRef, useEffect, useId } from 'react'
import { searchStudents, sampleStudents, type StudentInfo } from '../data/students'
import { Icon } from './Icon'

type Props = {
  label: string
  value: string
  onChange: (studentId: string, student?: StudentInfo) => void
  disabled?: boolean
  readOnly?: boolean
  required?: boolean
  errorText?: string
  supportingText?: string
  maxLength?: number
  name?: string
  autoFocus?: boolean
}

export function StudentPicker({
  label,
  value,
  onChange,
  disabled = false,
  readOnly = false,
  required = false,
  errorText,
  supportingText,
  maxLength = 50,
  name,
  autoFocus = false,
}: Props) {
  const [open, setOpen] = useState(false)
  const [filter, setFilter] = useState('')
  const containerRef = useRef<HTMLDivElement>(null)
  const inputId = useId()
  const listboxId = useId()

  const currentStudent = sampleStudents.find((s) => s.id === value)
  const suggestions = searchStudents(filter || value)

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  function handleSelect(student: StudentInfo) {
    onChange(student.id, student)
    setFilter('')
    setOpen(false)
  }

  const quickPicks = sampleStudents.slice(0, 4)

  return (
    <div className="student-picker-container" ref={containerRef}>
      <div className={`m3-field ${errorText ? 'm3-field--error' : ''}`}>
        <div className="field-control">
          <input
            id={inputId}
            name={name}
            type="text"
            role="combobox"
            aria-autocomplete="list"
            aria-expanded={open}
            aria-controls={listboxId}
            value={value}
            disabled={disabled}
            readOnly={readOnly}
            required={required}
            maxLength={maxLength}
            autoFocus={autoFocus}
            placeholder=" "
            onFocus={() => {
              if (!readOnly && !disabled) setOpen(true)
            }}
            onChange={(e) => {
              onChange(e.target.value)
              setFilter(e.target.value)
              if (!open) setOpen(true)
            }}
          />
          <label htmlFor={inputId}>
            {label}
            {required && ' *'}
          </label>
          <div className="field-trailing">
            {value && !readOnly && !disabled && (
              <button
                type="button"
                className="icon-button state-layer"
                aria-label="Xóa chọn sinh viên"
                onClick={() => {
                  onChange('')
                  setFilter('')
                }}
              >
                <Icon name="close" />
              </button>
            )}
            <button
              type="button"
              className="icon-button state-layer"
              aria-label={open ? 'Đóng danh sách sinh viên' : 'Mở danh sách sinh viên'}
              disabled={disabled || readOnly}
              onClick={() => setOpen((prev) => !prev)}
            >
              <Icon name="arrow" />
            </button>
          </div>
        </div>

        {supportingText && !errorText && <p className="field-supporting">{supportingText}</p>}
        {errorText && <p className="field-error" role="alert">{errorText}</p>}
      </div>

      {currentStudent && (
        <div className="student-selected-badge">
          <span className="badge-name"><strong>{currentStudent.name}</strong> ({currentStudent.id})</span>
          {currentStudent.room && <span className="badge-room">Phòng {currentStudent.room}</span>}
          {currentStudent.faculty && <span className="badge-faculty">{currentStudent.faculty}</span>}
        </div>
      )}

      {/* Quick click suggestions */}
      {!readOnly && !disabled && (
        <div className="student-quick-chips" aria-label="Gợi ý chọn nhanh sinh viên">
          <span className="quick-chips-label">Gợi ý nhanh:</span>
          {quickPicks.map((student) => (
            <button
              key={student.id}
              type="button"
              className={`m3-assist-chip ${value === student.id ? 'selected' : ''}`}
              onClick={() => handleSelect(student)}
            >
              <strong>{student.id}</strong> · {student.name} {student.room ? `(${student.room})` : ''}
            </button>
          ))}
        </div>
      )}

      {/* Dropdown list of students */}
      {open && !readOnly && !disabled && (
        <div className="student-dropdown-panel" id={listboxId} role="listbox">
          <div className="dropdown-header">
            <span>Danh sách sinh viên (bấm để chọn)</span>
            <span className="student-count">{suggestions.length} gợi ý</span>
          </div>
          <ul className="student-list" role="presentation">
            {suggestions.map((student) => (
              <li
                key={student.id}
                role="option"
                aria-selected={value === student.id}
                className={`student-item ${value === student.id ? 'active' : ''}`}
                onClick={() => handleSelect(student)}
              >
                <div className="student-item-main">
                  <span className="student-id-pill">{student.id}</span>
                  <span className="student-name">{student.name}</span>
                </div>
                <div className="student-item-sub">
                  {student.room && <span className="student-room">Phòng: {student.room}</span>}
                  {student.classId && <span className="student-class">{student.classId}</span>}
                  {student.faculty && <span className="student-faculty">{student.faculty}</span>}
                </div>
              </li>
            ))}
            {suggestions.length === 0 && (
              <li className="student-item-empty">Không tìm thấy sinh viên phù hợp</li>
            )}
          </ul>
        </div>
      )}
    </div>
  )
}
