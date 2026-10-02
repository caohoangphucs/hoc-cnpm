import React, { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { checkReadiness } from './api/health.mjs';
import './styles.css';

const modules = [
  ['04', 'Đăng ký phòng', 'Chọn giường và gửi hồ sơ đăng ký.'],
  ['12', 'Duyệt đăng ký', 'Xem xét hồ sơ và xác nhận chỗ ở.'],
  ['17', 'Hóa đơn', 'Quản lý điện nước và phát hành hóa đơn.'],
  ['08', 'Thanh toán', 'Theo dõi và thanh toán các khoản phí.'],
  ['22', 'Sửa chữa', 'Tiếp nhận yêu cầu và xử lý thiết bị.'],
  ['25', 'Ra vào', 'Ghi nhận hoạt động tại cổng ký túc xá.'],
  ['14', 'Kỷ luật', 'Theo dõi và xử lý vi phạm nội quy.'],
  ['30', 'Công ích', 'Đăng ký hoạt động và quản lý điểm.'],
];
function App() {
  const [health, setHealth] = useState<'loading' | 'ready' | 'unavailable'>('loading');
  useEffect(() => {
    let active = true;
    const refresh = async () => { const status = await checkReadiness(); if (active) setHealth(status); };
    void refresh(); const timer = window.setInterval(() => { void refresh(); }, 15000);
    return () => { active = false; window.clearInterval(timer); };
  }, []);
  return <div className="shell">
    <aside><a className="brand" href="/">KTX<span>SMART HUB</span></a><p className="aside-note">Quản lý ký túc xá</p><nav aria-label="Điều hướng chính"><a href="#modules" aria-current="page">Tổng quan chức năng</a></nav><p className="edition">Bản phát triển</p></aside>
    <main><header><span>Không gian quản lý</span><span role="status" className={'health ' + health}>{health === 'ready' ? 'Dịch vụ sẵn sàng' : health === 'loading' ? 'Đang kết nối' : 'Dịch vụ tạm gián đoạn'}</span></header>
      <section className="intro"><p className="eyebrow">KTX SMART HUB</p><h1>Một nơi cho mọi hoạt động ký túc xá.</h1><p>Quản lý chỗ ở, dịch vụ và đời sống sinh viên trong cùng một hệ thống.</p></section>
      <p className="notice">Các chức năng nghiệp vụ đang được triển khai. Trang này hiển thị phạm vi phát triển hiện tại.</p>
      <section id="modules" aria-labelledby="modules-title"><div className="section-heading"><h2 id="modules-title">Chức năng dự án</h2><span>8 nghiệp vụ trọng tâm</span></div><div className="grid">{modules.map(([id, title, description]) => <article key={id}><span className="uc">UC-{id}</span><h3>{title}</h3><p>{description}</p><span className="pending">Đang phát triển</span></article>)}</div></section>
      <footer>KTX Smart Hub · Hệ thống quản lý ký túc xá</footer>
    </main>
  </div>;
}
createRoot(document.getElementById('root')!).render(<React.StrictMode><App /></React.StrictMode>);
