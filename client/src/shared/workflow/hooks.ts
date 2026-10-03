import { useEffect, useRef, useState } from 'react'
import type { WorkflowOptions } from './model'

export function useClock(intervalMs = 15000) {
  const [now, setNow] = useState(Date.now)
  useEffect(() => { const timer=window.setInterval(()=>setNow(Date.now()),intervalMs);return()=>window.clearInterval(timer) },[intervalMs])
  return now
}
// This simulation runs only for explicit development previews.
export function usePreviewAction({ preview = false, outcome = 'success' }: WorkflowOptions) {
  const [busy, setBusy] = useState(false), [notice, setNotice] = useState(''), [error, setError] = useState('')
  const lock = useRef(false), generation = useRef(0)
  useEffect(() => () => { generation.current++ }, [])
  async function run(operation: () => void, message: string) {
    if (!preview || lock.current) return false
    lock.current = true; setBusy(true); setError(''); setNotice('')
    const runId = generation.current
    try {
      await new Promise(resolve => window.setTimeout(resolve, 300))
      if (runId !== generation.current) return false
      if (outcome !== 'success') { setError(outcome === 'conflict' ? 'Dữ liệu đã thay đổi (minh họa). Quyết định chưa được lưu; hãy kiểm tra lại và thử lại.' : 'Dịch vụ đang gián đoạn (minh họa). Thông tin đã nhập vẫn được giữ để thử lại.'); return false }
      operation(); setNotice(`${message} (chỉ trong bản xem trước).`); return true
    } finally { lock.current = false; if (runId === generation.current) setBusy(false) }
  }
  function showError(value: string) { setError(value); if (value) setNotice('') }
  return { busy, notice, error, run, setError: showError }
}
