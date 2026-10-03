import type { ButtonHTMLAttributes } from 'react'

type Props = ButtonHTMLAttributes<HTMLButtonElement> & { variant?: 'filled' | 'outlined' | 'text'; pending?: boolean }
export function Button({ variant = 'filled', pending = false, disabled, className = '', type = 'button', children, ...props }: Props) {
  return <button {...props} type={type} disabled={disabled || pending} aria-busy={pending || undefined} className={`m3-button m3-button--${variant} state-layer ${className}`}>
    {pending && <span className="button-progress" aria-hidden="true" />}{children}
  </button>
}
