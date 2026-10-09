import { Link } from 'react-router'
import { type Workspace, workspacePath } from '../navigation'
import { Icon } from '@/shared/components/Icon'
import { PageHeader } from '@/shared/components/PageHeader'

// trang tổng quan (dashboard) hiển thị lối tắt cho từng không gian làm việc
export function OverviewPage({ workspace, prefix = '' }: { workspace: Workspace; prefix?: string }) {
  return (
    <>
      {/* tiêu đề trang và phần mô tả của không gian làm việc */}
      <PageHeader title="Tổng quan" description={workspace.description} />

      {/* khu vực hiển thị các lối tắt truy cập nhanh */}
      <section className="workspace-section" aria-labelledby="workspace-section-title">
        <div className="section-heading">
          <h2 id="workspace-section-title">Truy cập nhanh</h2>
          <p>Chọn một khu vực để tiếp tục.</p>
        </div>

        {/* danh sách lưới thẻ điều hướng đến từng chức năng con */}
        <div className="destination-grid">
          {/* chỉ lấy các mục có đường dẫn cụ thể (bỏ qua trang tổng quan) */}
          {workspace.navigation.filter((item) => item.path).map((item) => (
            <Link
              key={item.path}
              to={`${prefix}${workspacePath(workspace, item)}`}
              className="destination-card state-layer"
              data-icon={item.icon}
            >
              {/* biểu tượng của chức năng */}
              <span className="destination-icon"><Icon name={item.icon} /></span>

              {/* tên và nội dung tóm tắt của chức năng */}
              <div className="destination-copy">
                <h3>{item.label}</h3>
                <p>{item.description}</p>
              </div>

              {/* biểu tượng mũi tên chuyển tiếp */}
              <Icon name="arrow" className="destination-arrow" />
            </Link>
          ))}
        </div>
      </section>
    </>
  )
}
