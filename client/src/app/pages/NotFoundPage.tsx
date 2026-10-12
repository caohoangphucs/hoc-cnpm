import { useEffect } from 'react'
import { Link } from 'react-router'
import { Icon } from '@/shared/components/Icon'

// màn hình hiển thị thông báo lỗi 404 không tìm thấy trang
export function NotFoundPage({ home = '/', standalone = false }: { home?: string; standalone?: boolean }) {
  // cập nhật tiêu đề tab trình duyệt nếu hiển thị ở chế độ trang độc lập
  useEffect(() => {
    if (standalone) document.title = 'Không tìm thấy trang | KTX Smart Hub'
  }, [standalone])

  // nội dung thông báo 404
  const content = (
    <div className="not-found">
      <p className="eyebrow">KTX Smart Hub · 404</p>
      <h1>Không tìm thấy trang</h1>
      <p>Đường dẫn có thể đã thay đổi hoặc không tồn tại.</p>
      {/* nút bấm quay về trang chủ hoặc trang tổng quan */}
      <Link className="filled-link state-layer" to={home}><Icon name="back" />Về tổng quan</Link>
    </div>
  )
  // bọc trong thẻ main nếu là trang độc lập ngoài layout
  return standalone ? <main className="standalone-page">{content}</main> : content
}
