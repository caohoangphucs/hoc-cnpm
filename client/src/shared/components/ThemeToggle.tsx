import { useEffect, useState } from 'react'
import { applyTheme, getPreferredTheme, readStoredTheme, saveTheme } from '@/theme/theme'
import { Icon } from './Icon'
import { showSystemFeedback } from './feedback'

export function ThemeToggle() {
  const [theme, setTheme] = useState(getPreferredTheme)

  useEffect(() => {
    const preference = window.matchMedia('(prefers-color-scheme: dark)')
    function followSystem() {
      if (readStoredTheme()) return
      const next = preference.matches ? 'dark' : 'light'
      setTheme(next)
      applyTheme(next)
    }
    preference.addEventListener('change', followSystem)
    return () => preference.removeEventListener('change', followSystem)
  }, [])

  const label = theme === 'light' ? 'Dùng giao diện tối' : 'Dùng giao diện sáng'
  return (
    <button
      className="icon-button state-layer"
      type="button"
      aria-label={label}
      title={label}
      onClick={() => {
        const next = theme === 'light' ? 'dark' : 'light'
        setTheme(next)
        applyTheme(next)
        saveTheme(next)
        showSystemFeedback(`Đã chuyển sang giao diện ${next === 'dark' ? 'Tối (Dark)' : 'Sáng (Light)'}`, 'info')
      }}
    >
      <Icon name={theme === 'light' ? 'moon' : 'sun'} />
    </button>
  )
}
