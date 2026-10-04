import type { ButtonHTMLAttributes } from 'react'

export type ButtonVariant = 'filled' | 'outlined' | 'text' | 'tonal' | 'approve' | 'reject' | 'danger'

type Props = ButtonHTMLAttributes<HTMLButtonElement> & { variant?: ButtonVariant; pending?: boolean }
export function Button({ variant = 'filled', pending = false, disabled, className = '', type = 'button', children, ...props }: Props) {
  return <button {...props} type={type} disabled={disabled || pending} aria-busy={pending || undefined} className={`m3-button m3-button--${variant} state-layer ${className}`}>
    {pending && <span className="button-progress" aria-hidden="true" />}{children}
  </button>
}
