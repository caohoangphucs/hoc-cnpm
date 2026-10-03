import { useState } from 'react'
import { Route, Routes } from 'react-router'
import { AppShell } from '@/app/layout/AppShell'
import { workspaces } from '@/app/navigation'
import { OverviewPage } from '@/app/pages/OverviewPage'
import { FeatureFrame } from '@/app/pages/FeatureFrame'
import { NotFoundPage } from '@/app/pages/NotFoundPage'
import { Button } from '@/shared/components/Button'
import { Icon } from '@/shared/components/Icon'
import { BookingPage } from './BookingPage'
import type { Bed, BookingView, Eligibility, Room, Student } from './model'

// Fictional room/student data is excluded from the production build.
function beds(roomId: string, statuses: Bed['status'][]): Bed[] { return statuses.map((status, index) => ({ id: `${roomId}-g${index + 1}`, number: String(index + 1).padStart(2, '0'), status, version: 1 })) }
const rooms: readonly Room[] = [
  { id: 'preview-a101', name: 'A101', buildingId: 'A', buildingName: 'Tòa A', roomType: 'Phòng 4 người', gender: 'NAM', capacity: 4, beds: beds('preview-a101', ['TRONG', 'TRONG', 'DANG_O', 'GIU_CHO']) },
  { id: 'preview-a102', name: 'A102', buildingId: 'A', buildingName: 'Tòa A', roomType: 'Phòng 4 người', gender: 'NAM', capacity: 4, beds: beds('preview-a102', ['DANG_O', 'DANG_O', 'GIU_CHO', 'KHOA']) },
  { id: 'preview-a201', name: 'A201', buildingId: 'A', buildingName: 'Tòa A', roomType: 'Phòng 6 người', gender: 'NAM', capacity: 6, beds: beds('preview-a201', ['TRONG', 'TRONG', 'TRONG', 'DANG_O', 'DANG_O', 'KHOA']) },
  { id: 'preview-b101', name: 'B101', buildingId: 'B', buildingName: 'Tòa B', roomType: 'Phòng 4 người', gender: 'NU', capacity: 4, beds: beds('preview-b101', ['TRONG', 'TRONG', 'DANG_O', 'GIU_CHO']) },
  { id: 'preview-b201', name: 'B201', buildingId: 'B', buildingName: 'Tòa B', roomType: 'Phòng 6 người', gender: 'NU', capacity: 6, beds: beds('preview-b201', ['TRONG', 'DANG_O', 'DANG_O', 'GIU_CHO', 'KHOA', 'DANG_O']) },
  { id: 'preview-c101', name: 'C101', buildingId: 'C', buildingName: 'Tòa C', roomType: 'Phòng 4 người', gender: 'TAT_CA', capacity: 4, beds: beds('preview-c101', ['TRONG', 'DANG_O', 'TRONG', 'DANG_O']) },
]
type Scenario = 'open' | 'closed' | 'pending' | 'resident' | 'loading' | 'empty' | 'error'
type Outcome = 'success' | 'conflict' | 'unavailable' | 'validation'
export function PreviewRegistration() {
  const [scenario, setScenario] = useState<Scenario>('open')
  const [gender, setGender] = useState<Student['gender']>('NAM')
  const [outcome, setOutcome] = useState<Outcome>('success')
  const [revision, setRevision] = useState(0)
  const view: BookingView = ['loading', 'empty', 'error'].includes(scenario) ? scenario as BookingView : 'data'
  const eligibility: Eligibility = scenario === 'pending' || scenario === 'resident' ? scenario : 'eligible'
  const registration = { id: 1, name: 'Đợt đăng ký học kỳ I', semester: 'Năm học 2026–2027', opensAt: '2026-10-01T08:00:00+07:00', closesAt: '2026-10-31T17:00:00+07:00', open: scenario !== 'closed' }
  return <>
    <section className="booking-preview-bar" aria-label="Công cụ xem trước"><div className="booking-preview-intro"><Icon name="info" /><div><strong>Xem trước giao diện UC-04</strong><p>Dữ liệu minh họa. Ảnh giữ trên máy, chưa tải lên; đăng ký không được lưu thật.</p></div></div>
      <div className="booking-preview-controls"><label>Tình huống<select aria-label="Tình huống" value={scenario} onChange={(event) => setScenario(event.target.value as Scenario)}><option value="open">Đợt đang mở</option><option value="closed">Đợt đã đóng</option><option value="pending">Có đơn chờ duyệt</option><option value="resident">Đang lưu trú</option><option value="loading">Đang tải</option><option value="empty">Không có phòng</option><option value="error">Lỗi tải dữ liệu</option></select></label>
        <label>Giới tính hồ sơ<select aria-label="Giới tính hồ sơ" value={gender} onChange={(event) => setGender(event.target.value as Student['gender'])}><option value="NAM">Nam</option><option value="NU">Nữ</option></select></label>
        <label>Kết quả gửi thử<select aria-label="Kết quả gửi thử" value={outcome} onChange={(event) => setOutcome(event.target.value as Outcome)}><option value="success">Thành công</option><option value="conflict">Giường vừa được giữ</option><option value="unavailable">Dịch vụ gián đoạn</option><option value="validation">Hồ sơ cần sửa</option></select></label>
        <Button variant="text" onClick={() => { setRevision((value) => value + 1); setScenario('open'); setOutcome('success') }}><Icon name="refresh" />Đặt lại</Button>
      </div>
    </section>
    <BookingPage key={`${scenario}-${gender}-${revision}`} preview initialRooms={rooms} student={{ name: 'Nguyễn Minh An', code: 'SV-DEMO-004', gender }} registration={registration} eligibility={eligibility} view={view} onSubmit={async (request, room) => {
      await new Promise((resolve) => window.setTimeout(resolve, 700))
      if (outcome === 'conflict') return { ok: false, kind: 'conflict', message: 'Giường vừa được sinh viên khác giữ chỗ. Hãy chọn một giường trống khác; hồ sơ đã chọn vẫn được giữ.' }
      if (outcome === 'unavailable') return { ok: false, kind: 'unavailable', message: 'Dịch vụ đang gián đoạn (minh họa). Hồ sơ vẫn được giữ trong trang. Bạn có thể thử gửi lại.' }
      if (outcome === 'validation') return { ok: false, kind: 'validation', message: 'Hồ sơ cần điều chỉnh (minh họa). Vui lòng kiểm tra ảnh CCCD.', fieldErrors: { identity: 'Ảnh CCCD chưa rõ. Vui lòng chọn ảnh khác.' } }
      return { ok: true, receipt: { id: 'DK-MINH-HOA-004', status: 'CHO_DUYET', expiresAt: new Date(Date.now() + 48 * 60 * 60 * 1000).toISOString(), roomName: room.name, buildingName: room.buildingName, bedNumber: room.beds.find((bed) => bed.id === request.bedId)!.number } }
    }} />
  </>
}
export default function BookingPreview() {
  const student = workspaces.find((workspace) => workspace.id === 'student')!
  return <Routes><Route element={<AppShell workspace={student} preview />}><Route index element={<OverviewPage workspace={student} prefix="/preview" />} /><Route path="stay" element={<PreviewRegistration />} />{student.navigation.filter((item) => item.path && item.path !== 'stay').map((item) => <Route key={item.path} path={item.path} element={<FeatureFrame workspace={student} item={item} prefix="/preview" />} />)}<Route path="*" element={<NotFoundPage home="/preview/student" />} /></Route></Routes>
}
