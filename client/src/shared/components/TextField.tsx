import { useId, type InputHTMLAttributes, type ReactNode, type Ref } from 'react'

type Props = InputHTMLAttributes<HTMLInputElement> & { label: string; errorText?: string; supportingText?: string; trailing?: ReactNode; ref?: Ref<HTMLInputElement> }
export function TextField({ label, id, errorText, supportingText, trailing, className = '', ref, 'aria-describedby': describedBy, ...props }: Props) {
  const generatedId = useId()
  const inputId = id ?? generatedId
  const message = errorText || supportingText
  return <div className={`m3-field ${errorText ? 'm3-field--error' : ''} ${className}`}>
    <div className="field-control">
      <label htmlFor={inputId}>{label}</label>
      <input {...props} id={inputId} ref={ref} aria-invalid={errorText ? true : undefined} aria-describedby={[describedBy, message ? `${inputId}-message` : undefined].filter(Boolean).join(' ') || undefined} />
      {trailing && <span className="field-trailing">{trailing}</span>}
    </div>
    {message && <p className="field-message" id={`${inputId}-message`}>{message}</p>}
  </div>
}
