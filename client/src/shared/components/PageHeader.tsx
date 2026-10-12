import type { ReactNode } from 'react'

// thuộc tính đầu vào của tiêu đề trang
type PageHeaderProps = {
  eyebrow?: string
  title: string
  description: string
  actions?: ReactNode
}

// component tiêu đề đầu trang gồm dòng phân loại, tên trang, mô tả và nút thao tác
export function PageHeader({ eyebrow, title, description, actions }: PageHeaderProps) {
  return (
    <header className="page-header">
      <div>
        {/* dòng chữ phụ phân loại nhỏ phía trên tiêu đề */}
        {eyebrow && <p className="eyebrow">{eyebrow}</p>}
        {/* tiêu đề chính của trang */}
        <h1>{title}</h1>
        {/* mô tả ngắn gọn nội dung và nhiệm vụ của trang */}
        <p className="page-description">{description}</p>
      </div>
      {/* khu vực các nút hành động góc phải */}
      {actions && <div className="page-actions">{actions}</div>}
    </header>
  )
}
