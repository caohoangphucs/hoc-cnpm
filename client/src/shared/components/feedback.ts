export type FeedbackTone = 'info' | 'success' | 'warning' | 'error'

export type FeedbackDetail = {
  message: string
  tone?: FeedbackTone
  duration?: number
}

export function showSystemFeedback(
  messageOrDetail: string | FeedbackDetail,
  tone: FeedbackTone = 'success',
  duration = 3500
) {
  if (typeof window !== 'undefined') {
    const detail: FeedbackDetail = typeof messageOrDetail === 'string'
      ? { message: messageOrDetail, tone, duration }
      : { tone: 'success', duration: 3500, ...messageOrDetail }
    window.dispatchEvent(new CustomEvent<FeedbackDetail>('ktx:feedback', { detail }))
  }
}
