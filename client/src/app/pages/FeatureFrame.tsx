import { Link } from 'react-router'
import { type NavigationItem, type Workspace, workspacePath } from '../navigation'
import { Icon } from '@/shared/components/Icon'
import { PageHeader } from '@/shared/components/PageHeader'

// Thay vùng nội dung bên dưới bằng feature page khi triển khai nhánh nghiệp vụ.
export function FeatureFrame({ workspace, item, prefix = '' }: { workspace: Workspace; item: NavigationItem; prefix?: string }) {
  return (
    <>
      <PageHeader title={item.label} description={item.description} />
      <section className="feature-frame" aria-labelledby="feature-frame-title">
        <span className="empty-symbol"><Icon name={item.icon} /></span>
        <h2 id="feature-frame-title">Nội dung đang được chuẩn bị</h2>
        <p>Khu vực này sẽ hiển thị các thông tin và công việc liên quan.</p>
        <Link className="text-link" to={`${prefix}${workspacePath(workspace)}`}><Icon name="back" />Về tổng quan</Link>
      </section>
    </>
  )
}
