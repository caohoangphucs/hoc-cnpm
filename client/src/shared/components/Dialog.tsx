import { useEffect, useId, useRef, type KeyboardEvent, type ReactNode } from 'react'
import { Icon } from './Icon'

export function Dialog({ title, children, actions, onClose, panel = false, busy = false }: {
  title: string; children: ReactNode; actions?: ReactNode; onClose: () => void; panel?: boolean; busy?: boolean
}) {
  const dialog = useRef<HTMLDialogElement>(null)
  const closeCallback = useRef(onClose)
  const titleId = useId()
  useEffect(() => { closeCallback.current = onClose }, [onClose])
  useEffect(() => {
    const element = dialog.current!
    const trigger = document.activeElement instanceof HTMLElement ? document.activeElement : null
    if (!element.open) element.showModal()
    return () => {
      element.close()
      if (trigger?.isConnected) trigger.focus({ preventScroll: true })
      else document.querySelector<HTMLElement>('#main-content')?.focus({ preventScroll: true })
    }
  }, [])
  function trapFocus(event: KeyboardEvent<HTMLDialogElement>) {
    if (event.key !== 'Tab') return
    const controls = Array.from(event.currentTarget.querySelectorAll<HTMLElement>('button:not(:disabled), a[href], input:not(:disabled), select:not(:disabled), textarea:not(:disabled)'))
      .filter((element) => element.getClientRects().length && !(element instanceof HTMLInputElement && element.type === 'radio' && !element.checked))
    const first = controls[0], last = controls.at(-1)
    if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last?.focus() }
    else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first?.focus() }
  }
  return <dialog ref={dialog} className={`m3-dialog ${panel ? 'm3-dialog--panel' : ''}`} aria-labelledby={titleId} onKeyDown={trapFocus}
    onCancel={(event) => { event.preventDefault(); if (!busy) closeCallback.current() }}
    onClick={(event) => { if (!busy && event.target === event.currentTarget) closeCallback.current() }}>
    <header className="dialog-header"><h2 id={titleId}>{title}</h2><button className="icon-button state-layer" type="button" aria-label="Đóng hộp thoại" onClick={onClose} disabled={busy} autoFocus><Icon name="close" /></button></header>
    <div className="dialog-body">{children}</div>
    {actions && <footer className="dialog-actions">{actions}</footer>}
  </dialog>
}
