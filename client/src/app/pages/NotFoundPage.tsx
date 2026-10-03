import { useEffect } from 'react'
import { Link } from 'react-router'
import { Icon } from '@/shared/components/Icon'

export function NotFoundPage({ home = '/', standalone = false }: { home?: string; standalone?: boolean }) {
  useEffect(() => {
    if (standalone) document.title = 'Không tìm thấy trang | KTX Smart Hub'
  }, [standalone])

  const content = (
    <div className="not-found">
      <p className="eyebrow">KTX Smart Hub · 404</p><h1>Không tìm thấy trang</h1>
      <p>Đường dẫn có thể đã thay đổi hoặc không tồn tại.</p>
      <Link className="filled-link state-layer" to={home}><Icon name="back" />Về tổng quan</Link>
    </div>
  )
  return standalone ? <main className="standalone-page">{content}</main> : content
}
