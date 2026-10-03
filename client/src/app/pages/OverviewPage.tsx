import { Link } from 'react-router'
import { type Workspace, workspacePath } from '../navigation'
import { Icon } from '@/shared/components/Icon'
import { PageHeader } from '@/shared/components/PageHeader'

export function OverviewPage({ workspace, prefix = '' }: { workspace: Workspace; prefix?: string }) {
  return (
    <>
      <PageHeader title="Tổng quan" description={workspace.description} />
      <section className="workspace-section" aria-labelledby="workspace-section-title">
        <div className="section-heading"><h2 id="workspace-section-title">Truy cập nhanh</h2><p>Chọn một khu vực để tiếp tục.</p></div>
        <div className="destination-grid">
          {workspace.navigation.filter((item) => item.path).map((item) => (
            <Link key={item.path} to={`${prefix}${workspacePath(workspace, item)}`} className="destination-card state-layer" data-icon={item.icon}>
              <span className="destination-icon"><Icon name={item.icon} /></span>
              <div className="destination-copy"><h3>{item.label}</h3><p>{item.description}</p></div>
              <Icon name="arrow" className="destination-arrow" />
            </Link>
          ))}
        </div>
      </section>
    </>
  )
}
