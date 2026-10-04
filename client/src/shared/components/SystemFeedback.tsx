import { useEffect, useState } from 'react'
import { Icon } from './Icon'
import type { FeedbackDetail } from './feedback'

export function SystemFeedback() {
  const [feedback, setFeedback] = useState<FeedbackDetail | null>(null)
  const [visible, setVisible] = useState(false)

  useEffect(() => {
    let hideTimer: number | undefined
    let removeTimer: number | undefined

    function handleFeedback(event: Event) {
      const customEvent = event as CustomEvent<FeedbackDetail>
      if (!customEvent.detail || !customEvent.detail.message) return

      window.clearTimeout(hideTimer)
      window.clearTimeout(removeTimer)

      setFeedback(customEvent.detail)
      setVisible(true)

      const duration = customEvent.detail.duration ?? 3500
      hideTimer = window.setTimeout(() => {
        setVisible(false)
        removeTimer = window.setTimeout(() => {
          setFeedback(null)
        }, 300)
      }, duration)
    }

    window.addEventListener('ktx:feedback', handleFeedback)
    return () => {
      window.removeEventListener('ktx:feedback', handleFeedback)
      window.clearTimeout(hideTimer)
      window.clearTimeout(removeTimer)
    }
  }, [])

  if (!feedback) return null

  const tone = feedback.tone ?? 'info'
  const iconName = tone === 'success' ? 'check' : tone === 'error' ? 'info' : tone === 'warning' ? 'shield' : 'info'

  return (
    <div
      className={`m3-snackbar ${visible ? 'show' : ''} m3-snackbar--${tone}`}
      role="status"
      aria-live="polite"
    >
      <span className="m3-snackbar-icon">
        <Icon name={iconName} />
      </span>
      <span className="m3-snackbar-text">{feedback.message}</span>
      <button
        type="button"
        className="snackbar-btn"
        onClick={() => setVisible(false)}
        aria-label="Đóng thông báo"
      >
        Đóng
      </button>
    </div>
  )
}
