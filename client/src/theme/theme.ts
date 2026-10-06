export type Theme = 'light' | 'dark'
const storageKey = 'ktx-theme'
type ThemeReveal = {
  theme: Theme
  target: Theme
  commit: () => void
  transition?: ViewTransition
  animation?: Animation
  finished: Promise<void>
}
let activeReveal: ThemeReveal | null = null

export function readStoredTheme(): Theme | null {
  try {
    const value = localStorage.getItem(storageKey)
    return value === 'light' || value === 'dark' ? value : null
  } catch {
    return null
  }
}

export function getPreferredTheme(): Theme {
  return readStoredTheme() ?? (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light')
}

export function applyTheme(theme: Theme) {
  document.documentElement.dataset.theme = theme
}

export function saveTheme(theme: Theme) {
  try {
    localStorage.setItem(storageKey, theme)
  } catch {
    // Vẫn đổi giao diện cho phiên hiện tại nếu trình duyệt không cho lưu.
  }
}

export async function revealTheme(theme: Theme, button: HTMLButtonElement, update: () => void) {
  let applied = false
  const commit = () => {
    if (applied) return
    applied = true
    update()
    applyTheme(theme)
    saveTheme(theme)
  }
  const root = document.documentElement
  const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)')
  if (activeReveal) {
    const current = activeReveal
    current.target = theme
    current.commit = commit
    if (current.animation) {
      commit()
      if (reducedMotion.matches || current.animation.playState === 'finished') {
        current.transition?.skipTransition()
      } else {
        // Reuse the snapshots and reverse at the current radius without a jump.
        try {
          current.animation.updatePlaybackRate(theme === current.theme ? 1 : -1)
        } catch {
          current.transition?.skipTransition()
        }
      }
    }
    return current.finished
  }
  if (!document.startViewTransition || !root.animate || reducedMotion.matches) {
    commit()
    return
  }

  const bounds = button.getBoundingClientRect()
  const x = bounds.left + bounds.width / 2
  const y = bounds.top + bounds.height / 2
  const reveal: ThemeReveal = { theme, target: theme, commit, finished: Promise.resolve() }
  activeReveal = reveal
  root.classList.add('theme-reveal')
  reveal.finished = (async () => {
    try {
      const transition = document.startViewTransition(() => {
        if (activeReveal === reveal) commit()
      })
      reveal.transition = transition
      // A skipped snapshot can reject ready before finished is awaited.
      void transition.finished.catch(() => {})
      await transition.ready
      if (reducedMotion.matches || reveal.target !== reveal.theme) {
        reveal.commit()
        transition.skipTransition()
        await transition.finished
        return
      }
      // A queued request for the same theme must update its own toggle too.
      reveal.commit()
      const radius = Math.ceil(Math.hypot(
        Math.max(x, window.innerWidth - x),
        Math.max(y, window.innerHeight - y),
      )) + 1
      const animation = root.animate(
        { clipPath: [`circle(0px at ${x}px ${y}px)`, `circle(${radius}px at ${x}px ${y}px)`] },
        {
          duration: 520,
          easing: 'linear',
          fill: 'both',
          pseudoElement: '::view-transition-new(root)',
        },
      )
      reveal.animation = animation
      await animation.finished
      await transition.finished
    } catch {
      reveal.transition?.skipTransition()
      reveal.commit()
    } finally {
      if (activeReveal === reveal) {
        activeReveal = null
        root.classList.remove('theme-reveal')
      }
    }
  })()
  return reveal.finished
}
