import { useEffect, useRef, useState, type KeyboardEvent } from 'react'
import { Link, Outlet, useLocation, useNavigate } from 'react-router'
import { type Workspace, workspacePath } from '../navigation'
import { Icon } from '@/shared/components/Icon'
import { BrandLogo } from '@/shared/components/BrandLogo'
import { ThemeToggle } from '@/shared/components/ThemeToggle'
import { Navigation } from './Navigation'
import { useAuth } from '@/features/auth/context'
import { asAuthError, type AuthError } from '@/features/auth/api'
import { AuthFeedback } from '@/features/auth/AuthFeedback'
import { Button } from '@/shared/components/Button'
import { TwoFactorSetupDialog } from '@/features/auth/TwoFactorSetupDialog'
import { SystemFeedback } from '@/shared/components/SystemFeedback'
import { showSystemFeedback } from '@/shared/components/feedback'

// khung giao diện chính cho từng không gian làm việc
export function AppShell({ workspace, preview = false }: { workspace: Workspace; preview?: boolean }) {
  // tiền tố khi chạy chế độ xem trước
  const prefix = preview ? '/preview' : ''
  const navigate = useNavigate()
  const { logout } = useAuth()
  const [signingOut, setSigningOut] = useState(false)
  const [logoutError, setLogoutError] = useState<AuthError | null>(null)
  const [settingUp, setSettingUp] = useState(false)
  const { pathname } = useLocation()
  const mainRef = useRef<HTMLElement>(null)
  const drawerRef = useRef<HTMLDialogElement>(null)
  const lastPath = useRef(pathname)
  const [drawerOpen, setDrawerOpen] = useState(false)

  // chuẩn hóa đường dẫn để so khớp menu
  const normalize = (path: string) => path.replace(/\/+$/, '') || '/'
  // tìm mục menu hiện tại để lấy tiêu đề trang
  const current = workspace.navigation.find((item) => normalize(`${prefix}${workspacePath(workspace, item)}`) === normalize(pathname))
  const title = current?.label ?? 'Không tìm thấy trang'

  // cập nhật tiêu đề tab trình duyệt và cuộn lên đầu khi chuyển trang
  useEffect(() => {
    document.title = `${title} · ${workspace.label} | KTX Smart Hub`
    if (lastPath.current !== pathname) {
      window.scrollTo({ top: 0, behavior: 'instant' })
      mainRef.current?.focus({ preventScroll: true })
      lastPath.current = pathname
    }
  }, [pathname, title, workspace.label])

  // tự động đóng drawer mobile khi chuyển sang màn hình desktop
  useEffect(() => {
    const desktop = window.matchMedia('(min-width: 961px)')
    function closeOnDesktop(event: MediaQueryListEvent) {
      if (event.matches) drawerRef.current?.close()
    }
    desktop.addEventListener('change', closeOnDesktop)
    return () => desktop.removeEventListener('change', closeOnDesktop)
  }, [])

  // mở drawer menu trên mobile
  function openDrawer() {
    drawerRef.current?.showModal()
    setDrawerOpen(true)
  }

  // đóng drawer menu trên mobile
  function closeDrawer() {
    drawerRef.current?.close()
  }

  // xử lý đăng xuất tài khoản và chuyển hướng về trang đăng nhập
  async function signOut() {
    if (signingOut) return
    setSigningOut(true)
    setLogoutError(null)
    try {
      await logout()
      showSystemFeedback('Đã đăng xuất khỏi tài khoản thành công.', 'info')
      navigate('/login', { replace: true })
    } catch (error) {
      setLogoutError(asAuthError(error))
      showSystemFeedback('Không thể hoàn tất đăng xuất. Vui lòng thử lại.', 'error')
    } finally {
      setSigningOut(false)
    }
  }

  // giữ phím tab xoay vòng bên trong drawer (focus trap cho a11y)
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
      {/* liên kết nhảy nhanh đến nội dung chính cho người dùng bàn phím */}
      <a className="skip-link" href="#main-content">Đến nội dung chính</a>

      {/* thanh bên sidebar cho màn hình desktop */}
      <aside className="sidebar" aria-label={`Không gian ${workspace.label}`}>
        <Link className="brand" to={`${prefix}${workspacePath(workspace)}`} aria-label="KTX Smart Hub — Tổng quan">
          <BrandLogo />
        </Link>
        <Navigation workspace={workspace} prefix={prefix} />
        <div className="sidebar-footer">
          <span className="workspace-avatar"><Icon name="people" /></span>
          <div><strong>{workspace.label}</strong><p>Không gian làm việc</p></div>
        </div>
      </aside>

      {/* khu vực thân ứng dụng */}
      <div className="app-body">
        {/* thanh tiêu đề trên cùng (top bar) */}
        <header className="top-bar">
          <div className="top-bar-start">
            {/* nút hamburger mở drawer trên mobile */}
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
            <Link className="header-brand" to={`${prefix}${workspacePath(workspace)}`} aria-label="KTX Smart Hub — Tổng quan">
              <BrandLogo symbol />
            </Link>
            {/* thanh dẫn đường (breadcrumbs) */}
            <nav className="top-breadcrumbs" aria-label="Đường dẫn trang">
              <Link to={`${prefix}${workspacePath(workspace)}`}>{workspace.label}</Link>
              <span aria-hidden="true">/</span>
              <span aria-current="page">{title}</span>
            </nav>
          </div>
          <div className="top-bar-end">
            <ThemeToggle />
            {/* thiết lập 2fa cho admin và kế toán */}
            {!preview && (workspace.id === 'admin' || workspace.id === 'accountant') && <Button variant="text" onClick={() => setSettingUp(true)} disabled={signingOut}>Thiết lập 2FA</Button>}
            {/* nút đăng xuất */}
            {!preview && <Button variant="text" pending={signingOut} onClick={signOut} aria-label="Đăng xuất" className="shell-logout"><Icon name="logout" /><span>{signingOut ? 'Đang đăng xuất…' : 'Đăng xuất'}</span></Button>}
          </div>
        </header>

        {/* vùng hiển thị nội dung chính của từng trang */}
        <main ref={mainRef} id="main-content" className="workspace-content" tabIndex={-1}>
          <AuthFeedback error={logoutError} />
          <p className="form-status" role="status">{signingOut ? 'Đang đăng xuất khỏi tài khoản.' : ''}</p>
          <Outlet />
          <footer className="content-footer">KTX Smart Hub · Hệ thống quản lý ký túc xá</footer>
        </main>
      </div>

      {/* menu trượt dạng dialog cho màn hình di động */}
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
          <div className="drawer-brand">
            <Link to={`${prefix}${workspacePath(workspace)}`} onClick={closeDrawer} aria-label="KTX Smart Hub — Tổng quan"><BrandLogo /></Link>
            <h2 id="drawer-title">{workspace.label}</h2>
          </div>
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

      {/* hộp thoại cài đặt 2fa khi được bật */}
      {settingUp && <TwoFactorSetupDialog onClose={() => setSettingUp(false)} />}
      <SystemFeedback />
    </div>
  )
}
