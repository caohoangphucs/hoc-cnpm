import { useEffect, useRef, useState, type KeyboardEvent } from 'react'
import { Link, Outlet, useLocation } from 'react-router'
import { type Workspace, workspacePath } from '../navigation'
import { Icon } from '@/shared/components/Icon'
import { ThemeToggle } from '@/shared/components/ThemeToggle'
import { Navigation } from './Navigation'
import { useAuth } from '@/features/auth/context'
import { asAuthError, type AuthError } from '@/features/auth/api'
import { AuthFeedback } from '@/features/auth/AuthFeedback'
import { Button } from '@/shared/components/Button'
import { TwoFactorSetupDialog } from '@/features/auth/TwoFactorSetupDialog'

export function AppShell({ workspace, preview = false }: { workspace: Workspace; preview?: boolean }) {
  const prefix = preview ? '/preview' : ''
  const { logout } = useAuth()
  const [signingOut, setSigningOut] = useState(false)
  const [logoutError, setLogoutError] = useState<AuthError | null>(null)
  const [settingUp, setSettingUp] = useState(false)
  const { pathname } = useLocation()
  const mainRef = useRef<HTMLElement>(null)
  const drawerRef = useRef<HTMLDialogElement>(null)
  const lastPath = useRef(pathname)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const current = workspace.navigation.find((item) => `${prefix}${workspacePath(workspace, item)}` === pathname)
  const title = current?.label ?? 'Không tìm thấy trang'

  useEffect(() => {
    document.title = `${title} · ${workspace.label} | KTX Smart Hub`
    if (lastPath.current !== pathname) {
      window.scrollTo({ top: 0, behavior: 'instant' })
      mainRef.current?.focus({ preventScroll: true })
      lastPath.current = pathname
    }
  }, [pathname, title, workspace.label])

  useEffect(() => {
    const desktop = window.matchMedia('(min-width: 961px)')
    function closeOnDesktop(event: MediaQueryListEvent) {
      if (event.matches) drawerRef.current?.close()
    }
    desktop.addEventListener('change', closeOnDesktop)
    return () => desktop.removeEventListener('change', closeOnDesktop)
  }, [])

  function openDrawer() {
    drawerRef.current?.showModal()
    setDrawerOpen(true)
  }

  function closeDrawer() {
    drawerRef.current?.close()
  }

  async function signOut() {
    if (signingOut) return
    setSigningOut(true)
    setLogoutError(null)
    try { await logout() } catch (error) { setLogoutError(asAuthError(error)) }
    finally { setSigningOut(false) }
  }

  function keepDrawerFocus(event: KeyboardEvent<HTMLDialogElement>) {
    if (event.key !== 'Tab') return
    const controls = Array.from(
      event.currentTarget.querySelectorAll<HTMLElement>('a[href], button:not(:disabled)'),
    )
    const first = controls[0]
    const last = controls.at(-1)
    if (event.shiftKey && document.activeElement === first) {
      event.preventDefault()
      last?.focus()
    } else if (!event.shiftKey && document.activeElement === last) {
      event.preventDefault()
      first?.focus()
    }
  }

  return (
    <div className="app-shell">
      <a className="skip-link" href="#main-content">Đến nội dung chính</a>
      <aside className="sidebar" aria-label={`Không gian ${workspace.label}`}>
        <Link className="brand" to={`${prefix}${workspacePath(workspace)}`} aria-label="KTX Smart Hub — Tổng quan">
          <span className="brand-mark"><Icon name="building" /></span>
          <span className="brand-name">KTX Smart Hub<strong>Hệ thống ký túc xá</strong></span>
        </Link>
        <Navigation workspace={workspace} prefix={prefix} />
        <div className="sidebar-footer">
          <span className="workspace-avatar"><Icon name="people" /></span>
          <div><strong>{workspace.label}</strong><p>Không gian làm việc</p></div>
        </div>
      </aside>
      <div className="app-body">
        <header className="top-bar">
          <div className="top-bar-start">
            <button
              className="icon-button mobile-menu state-layer"
              type="button"
              onClick={openDrawer}
              aria-label="Mở điều hướng"
              aria-controls="mobile-drawer"
              aria-expanded={drawerOpen}
              aria-haspopup="dialog"
            >
              <Icon name="menu" />
            </button>
            <nav className="top-breadcrumbs" aria-label="Đường dẫn trang">
              <Link to={`${prefix}${workspacePath(workspace)}`}>{workspace.label}</Link>
              <span aria-hidden="true">/</span>
              <span aria-current="page">{title}</span>
            </nav>
          </div>
          <div className="top-bar-end">
            <ThemeToggle />
            {!preview && (workspace.id === 'admin' || workspace.id === 'accountant') && <Button variant="text" onClick={() => setSettingUp(true)} disabled={signingOut}>Thiết lập 2FA</Button>}
            {!preview && <Button variant="text" pending={signingOut} onClick={signOut} aria-label="Đăng xuất" className="shell-logout"><Icon name="logout" /><span>{signingOut ? 'Đang đăng xuất…' : 'Đăng xuất'}</span></Button>}
          </div>
        </header>
        <main ref={mainRef} id="main-content" className="workspace-content" tabIndex={-1}>
          <AuthFeedback error={logoutError} />
          <p className="form-status" role="status">{signingOut ? 'Đang đăng xuất khỏi tài khoản.' : ''}</p>
          <Outlet />
          <footer className="content-footer">KTX Smart Hub · Hệ thống quản lý ký túc xá</footer>
        </main>
      </div>
      <dialog
        ref={drawerRef}
        id="mobile-drawer"
        className="mobile-drawer"
        aria-labelledby="drawer-title"
        onKeyDown={keepDrawerFocus}
        onClose={() => setDrawerOpen(false)}
        onClick={(event) => {
          if (event.target === event.currentTarget) closeDrawer()
        }}
      >
        <div className="drawer-heading">
          <div><p className="eyebrow">KTX Smart Hub</p><h2 id="drawer-title">{workspace.label}</h2></div>
          <button
            className="icon-button state-layer"
            type="button"
            onClick={closeDrawer}
            autoFocus
            aria-label="Đóng điều hướng"
          >
            <Icon name="close" />
          </button>
        </div>
        <Navigation workspace={workspace} prefix={prefix} label="Tất cả mục điều hướng" onNavigate={closeDrawer} />
      </dialog>
      {settingUp && <TwoFactorSetupDialog onClose={() => setSettingUp(false)} />}
    </div>
  )
}
