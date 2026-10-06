import { useCallback, useEffect, useRef, useState } from 'react'
import { flushSync } from 'react-dom'
import { applyTheme, getPreferredTheme, readStoredTheme, revealTheme } from '@/theme/theme'
import { Icon } from './Icon'

export function ThemeToggle() {
  const [theme, setTheme] = useState(getPreferredTheme)
  const requestedTheme = useRef(theme)
  const buttonRef = useRef<HTMLButtonElement>(null)
  const toggleTheme = useCallback((button: HTMLButtonElement) => {
    const next = requestedTheme.current === 'light' ? 'dark' : 'light'
    requestedTheme.current = next
    void revealTheme(next, button, () => {
      // Include the updated icon and accessible name in the new snapshot.
      flushSync(() => setTheme(next))
    })
  }, [])

  useEffect(() => {
    // Captured root contents can lose pointer hit-testing during a transition.
    // Receive only clicks inside this button when its real DOM target is hidden.
    function receiveSnapshotClick(event: MouseEvent) {
      const button = buttonRef.current
      if (!button || event.button !== 0 || event.detail === 0 ||
          !document.documentElement.classList.contains('theme-reveal') ||
          (event.target instanceof Node && button.contains(event.target))) return
      const bounds = button.getBoundingClientRect()
      if (event.clientX < bounds.left || event.clientX > bounds.right ||
          event.clientY < bounds.top || event.clientY > bounds.bottom) return
      event.preventDefault()
      toggleTheme(button)
    }
    document.addEventListener('click', receiveSnapshotClick, true)
    return () => document.removeEventListener('click', receiveSnapshotClick, true)
  }, [toggleTheme])

  useEffect(() => {
    const preference = window.matchMedia('(prefers-color-scheme: dark)')
    function followSystem() {
      if (readStoredTheme()) return
      const next = preference.matches ? 'dark' : 'light'
      requestedTheme.current = next
      setTheme(next)
      applyTheme(next)
    }
    preference.addEventListener('change', followSystem)
    return () => preference.removeEventListener('change', followSystem)
  }, [])

  const label = theme === 'light' ? 'Dùng giao diện tối' : 'Dùng giao diện sáng'
  return (
    <button
      ref={buttonRef}
      className="icon-button state-layer"
      type="button"
      aria-label={label}
      title={label}
      onClick={(event) => toggleTheme(event.currentTarget)}
    >
      <Icon name={theme === 'light' ? 'moon' : 'sun'} />
    </button>
  )
}
