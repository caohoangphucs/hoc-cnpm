import { useEffect, useRef, type ReactNode } from 'react'
import { Link, useLocation } from 'react-router'
import { Icon } from '@/shared/components/Icon'
import { BrandLogo } from '@/shared/components/BrandLogo'
import { ThemeToggle } from '@/shared/components/ThemeToggle'
import { SystemFeedback } from '@/shared/components/SystemFeedback'
import { DraggableSticker } from '@/shared/components/DraggableSticker'
import loginSticker from '@/assets/login-sticker.png'

export function AuthLayout({ title, description, children }: { title: string; description: string; children: ReactNode }) {
  const heading = useRef<HTMLHeadingElement>(null)
  const { pathname } = useLocation()
  useEffect(() => {
    document.title = pathname === '/login' ? 'KTX Smart Hub' : `${title} | KTX Smart Hub`
    heading.current?.focus({ preventScroll: true })
    window.scrollTo({ top: 0, behavior: 'instant' })
  }, [title, pathname])
  return <div className="auth-layout">
    <a className="skip-link" href="#auth-content">Đến nội dung chính</a>
    <header className="auth-top-bar">
      <Link className="brand auth-brand" to="/" aria-label="KTX Smart Hub — Trang chủ">
        <BrandLogo />
      </Link>
      <ThemeToggle />
    </header>
    <main id="auth-content" className="auth-content">
      <aside className="auth-intro" aria-label="Giới thiệu KTX Smart Hub">
        {pathname === '/login' ? <DraggableSticker image={loginSticker} alt="Hình minh họa KTX Smart Hub" /> : <div className="auth-illustration" aria-hidden="true">
          <div className="auth-building"><Icon name="building" /></div>
          <span className="auth-orbit auth-orbit--one"><Icon name="home" /></span>
          <span className="auth-orbit auth-orbit--two"><Icon name="community" /></span>
          <span className="auth-orbit auth-orbit--three"><Icon name="shield" /></span>
        </div>}
        <h2>Một nơi cho mọi việc<br />ở ký túc xá.</h2>
        <p>Theo dõi lưu trú, hóa đơn và các hoạt động trong không gian dành riêng cho bạn.</p>
        <div className="auth-intro-note"><Icon name="shield" /><span>Truy cập bằng tài khoản được cấp.</span></div>
      </aside>
      <section className="auth-card" aria-labelledby="auth-heading">
        <div className="auth-card-heading"><span className="auth-symbol"><Icon name={pathname === '/login/2fa' ? 'shield' : 'lock'} /></span>
          <h1 id="auth-heading" ref={heading} tabIndex={-1}>{title}</h1><p>{description}</p>
        </div>
        {children}
      </section>
    </main>
    <SystemFeedback />
  </div>
}
