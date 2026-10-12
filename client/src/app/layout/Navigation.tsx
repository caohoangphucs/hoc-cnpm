import { NavLink } from 'react-router'
import { workspacePath, type NavigationItem, type Workspace } from '../navigation'
import { Icon } from '@/shared/components/Icon'

// thuộc tính đầu vào của thanh điều hướng
type NavigationProps = {
  workspace: Workspace
  items?: readonly NavigationItem[]
  className?: string
  label?: string
  onNavigate?: () => void
  prefix?: string
}

// component hiển thị danh sách các mục menu điều hướng
export function Navigation({
  workspace,
  items = workspace.navigation,
  className = '',
  label = 'Điều hướng chính',
  onNavigate,
  prefix = '',
}: NavigationProps) {
  return (
    <nav className={`navigation ${className}`} aria-label={label}>
      {/* duyệt qua từng mục menu và render liên kết nav link */}
      {items.map((item) => (
        <NavLink
          key={item.path}
          to={`${prefix}${workspacePath(workspace, item)}`}
          end
          className="nav-link state-layer"
          aria-label={item.label}
          title={item.label}
          onClick={onNavigate}
        >
          {/* biểu tượng icon của mục menu */}
          <span className="nav-icon"><Icon name={item.icon} /></span>
          {/* tên đầy đủ hiển thị trên desktop */}
          <span className="nav-label">{item.label}</span>
          {/* tên rút gọn hỗ trợ màn hình nhỏ */}
          <span className="nav-caption" aria-hidden="true">{item.shortLabel}</span>
        </NavLink>
      ))}
    </nav>
  )
}
