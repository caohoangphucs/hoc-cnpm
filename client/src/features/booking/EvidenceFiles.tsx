import { useEffect, useState } from 'react'
import { apiFetch } from '@/shared/api/client'
import { useResource } from '@/shared/api/hooks'
import { errorMessage, readJson } from '@/shared/api/protocol'
import { evidenceLabels, parseEvidence } from './evidence'

// Mỗi ảnh được lấy bằng phiên hiện tại; không tạo URL công khai cho giấy tờ cá nhân.
function EvidencePreview({ id }: { id: string }) {
  const metadata = useResource(`files/${id}`, parseEvidence)
  const [content, setContent] = useState<{ id: string; url?: string; error?: string }>()
  useEffect(() => {
    const abort = new AbortController()
    let objectUrl: string | undefined
    void apiFetch(`files/${id}/content`, { signal: abort.signal }).then(async response => {
      if (!response.ok) await readJson(response, parseEvidence)
      const blob = await response.blob()
      if (abort.signal.aborted) return
      objectUrl = URL.createObjectURL(blob)
      setContent({ id, url: objectUrl })
    }).catch((error: unknown) => { if (!abort.signal.aborted) setContent({ id, error: errorMessage(error) }) })
    return () => { abort.abort(); if (objectUrl) URL.revokeObjectURL(objectUrl) }
  }, [id])
  const current = content?.id === id ? content : undefined
  const label = evidenceLabels[metadata.data?.kind ?? ''] ?? 'Minh chứng'
  return <figure className="evidence-preview">
    <figcaption><strong>{label}</strong></figcaption>
    {metadata.error || current?.error ? <p role="alert">{metadata.error || current?.error}</p>
      : current?.url ? <><a href={current.url} target="_blank" rel="noopener noreferrer" aria-label={`Mở ảnh ${label}`}><img src={current.url} alt={label} /></a><a href={current.url} download={`${metadata.data?.kind ?? 'minh-chung'}.${metadata.data?.mime === 'image/jpeg' ? 'jpg' : 'png'}`}>Tải ảnh</a></>
      : <p role="status">Đang tải minh chứng…</p>}
  </figure>
}

// Chỉ hiển thị các ID mà API hồ sơ đã cho phép người dùng đọc.
export function EvidenceFiles({ ids }: { ids: string[] }) {
  return <div className="evidence-grid">{ids.length ? ids.map(id => <EvidencePreview key={id} id={id} />) : <p>Hồ sơ chưa có minh chứng.</p>}</div>
}
