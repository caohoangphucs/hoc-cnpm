import { NavLink } from 'react-router'
import { workspacePath, type NavigationItem, type Workspace } from '../navigation'
import { Icon } from '@/shared/components/Icon'

type NavigationProps = {
  workspace: Workspace
  items?: readonly NavigationItem[]
  className?: string
  label?: string
  onNavigate?: () => void
}

export function Navigation({
  workspace,
  items = workspace.navigation,
  className = '',
  label = 'Điều hướng chính',
  onNavigate,
}: NavigationProps) {
  return (
    <nav className={`navigation ${className}`} aria-label={label}>
      {items.map((item) => (
        <NavLink
          key={item.path}
          to={workspacePath(workspace, item)}
          end
          className="nav-link state-layer"
          aria-label={item.label}
          title={item.label}
          onClick={onNavigate}
        >
          <span className="nav-icon"><Icon name={item.icon} /></span>
          <span className="nav-label">{item.label}</span>
          <span className="nav-caption" aria-hidden="true">{item.shortLabel}</span>
        </NavLink>
      ))}
    </nav>
  )
}
