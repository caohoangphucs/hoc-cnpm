import type { IconName } from '@/shared/components/Icon'

// danh sách mã định danh 5 vai trò / không gian làm việc
export type WorkspaceId = 'student' | 'admin' | 'accountant' | 'technician' | 'security'

// cấu trúc một mục điều hướng trên thanh menu
export type NavigationItem = {
  path: string
  label: string
  shortLabel: string
  description: string
  icon: IconName
}

// cấu trúc một không gian làm việc theo vai trò
export type Workspace = {
  id: WorkspaceId
  label: string
  description: string
  navigation: readonly NavigationItem[]
}

// mục menu tổng quan mặc định cho mọi không gian làm việc
const overview: NavigationItem = {
  path: '', label: 'Tổng quan', shortLabel: 'Tổng quan',
  description: 'Các khu vực công việc trong không gian của bạn.', icon: 'home',
}

// Nhóm chức năng theo Phần II của TODO.md. Đây là cấu hình điều hướng,
// không phải nguồn quyền truy cập; quyền thật cần session và backend.
// danh sách cấu hình menu chi tiết cho từng vai trò
export const workspaces: readonly Workspace[] = [
  {
    id: 'student', label: 'Sinh viên',
    description: 'Không gian dành cho cuộc sống và hoạt động của bạn tại ký túc xá.',
    navigation: [
      overview,
      { path: 'profile', label: 'Thông tin cá nhân', shortLabel: 'Hồ sơ', icon: 'people', description: 'Xem thông tin cá nhân đang lưu trong hồ sơ sinh viên của bạn.' },
      { path: 'stay', label: 'Phòng & lưu trú', shortLabel: 'Lưu trú', icon: 'building', description: 'Đăng ký phòng, hồ sơ minh chứng và yêu cầu đổi phòng.' },
      { path: 'invoices', label: 'Hóa đơn & thanh toán', shortLabel: 'Hóa đơn', icon: 'wallet', description: 'Xem hóa đơn, tải PDF và thanh toán tiền phòng, điện nước.' },
      { path: 'repairs', label: 'Yêu cầu sửa chữa', shortLabel: 'Sửa chữa', icon: 'tool', description: 'Gửi yêu cầu sửa chữa thiết bị trong phòng kèm ảnh.' },
      { path: 'visitors', label: 'Khách thăm', shortLabel: 'Khách thăm', icon: 'people', description: 'Khai báo thông tin khách đến thăm phòng.' },
      { path: 'community', label: 'Hoạt động công ích', shortLabel: 'Công ích', icon: 'community', description: 'Đăng ký hoạt động, quy đổi điểm và tải giấy xác nhận.' },
      { path: 'discipline', label: 'Quyết định & khiếu nại', shortLabel: 'Kỷ luật', icon: 'shield', description: 'Theo dõi quyết định xử lý kỷ luật và gửi khiếu nại.' },
      { path: 'entries', label: 'Lịch sử vào / ra', shortLabel: 'Vào / ra', icon: 'gate', description: 'Tra cứu nhật ký vào và ra ký túc xá của bạn.' },
    ],
  },
  {
    id: 'admin', label: 'Ban quản lý',
    description: 'Điều phối lưu trú, chính sách và công tác quản lý ký túc xá.',
    navigation: [
      overview,
      { path: 'rooms', label: 'Phòng & giường', shortLabel: 'Phòng', icon: 'building', description: 'Quản lý tòa nhà, phòng, cấu hình giường và trạng thái lưu trú.' },
      { path: 'bookings', label: 'Duyệt hồ sơ', shortLabel: 'Hồ sơ', icon: 'document', description: 'Duyệt hoặc từ chối hồ sơ đăng ký phòng và gán hợp đồng.' },
      { path: 'visitors', label: 'Duyệt khách thăm', shortLabel: 'Khách thăm', icon: 'people', description: 'Xét duyệt phiếu khai báo khách thăm.' },
      { path: 'repairs', label: 'Phân công sửa chữa', shortLabel: 'Sửa chữa', icon: 'tool', description: 'Giao phiếu sửa chữa cho kỹ thuật viên.' },
      { path: 'invoices', label: 'Tra cứu hóa đơn', shortLabel: 'Hóa đơn', icon: 'wallet', description: 'Kiểm tra hóa đơn và hạn thanh toán.' },
      { path: 'entries', label: 'Nhật ký vào / ra', shortLabel: 'Vào / ra', icon: 'gate', description: 'Tra cứu nhật ký kiểm soát cổng.' },
      { path: 'pricing', label: 'Đơn giá & chính sách', shortLabel: 'Đơn giá', icon: 'pricing', description: 'Phê duyệt đơn giá dịch vụ và cấu hình giảm tiền phòng.' },
      { path: 'discipline', label: 'Vi phạm & kỷ luật', shortLabel: 'Kỷ luật', icon: 'shield', description: 'Xử lý vi phạm, cảnh báo tái phạm và báo cáo về trễ.' },
      { path: 'community', label: 'Quản lý công ích', shortLabel: 'Công ích', icon: 'community', description: 'Mở hoạt động, điểm danh và xác nhận hoàn thành công ích.' },
      { path: 'accounts', label: 'Tài khoản & vai trò', shortLabel: 'Tài khoản', icon: 'people', description: 'Phân quyền truy cập và khóa hoặc mở tài khoản.' },
      { path: 'logs', label: 'Nhật ký hệ thống', shortLabel: 'Nhật ký', icon: 'logs', description: 'Tra cứu nhật ký lỗi và nhật ký thao tác hệ thống.' },
    ],
  },
  {
    id: 'accountant', label: 'Kế toán',
    description: 'Quản lý hóa đơn, thu tiền và theo dõi tài chính ký túc xá.',
    navigation: [
      overview,
      { path: 'invoices', label: 'Phát hành hóa đơn', shortLabel: 'Hóa đơn', icon: 'document', description: 'Tính tiền điện nước theo biểu giá và phát hành hóa đơn tháng.' },
      { path: 'payments', label: 'Tra cứu thanh toán', shortLabel: 'Thanh toán', icon: 'wallet', description: 'Kiểm tra trạng thái các phiên thanh toán và đối soát.' },
      { path: 'cashier', label: 'Thu tiền mặt', shortLabel: 'Thu tiền', icon: 'wallet', description: 'Ghi nhận thu tiền trực tiếp và in phiếu thu, biên lai.' },
      { path: 'pricing', label: 'Đơn giá dịch vụ', shortLabel: 'Đơn giá', icon: 'pricing', description: 'Đề xuất thay đổi đơn giá điện, nước, wifi và tiền phòng.' },
      { path: 'reports', label: 'Báo cáo tài chính', shortLabel: 'Báo cáo', icon: 'chart', description: 'Thống kê doanh thu và xuất báo cáo tài chính hàng tháng.' },
    ],
  },
  {
    id: 'technician', label: 'Kỹ thuật',
    description: 'Theo dõi sửa chữa, chỉ số điện nước và vật tư ký túc xá.',
    navigation: [
      overview,
      { path: 'repairs', label: 'Công việc sửa chữa', shortLabel: 'Sửa chữa', icon: 'tool', description: 'Tiếp nhận yêu cầu, cập nhật trạng thái và xuất vật tư sửa chữa.' },
      { path: 'meters', label: 'Chỉ số điện nước', shortLabel: 'Chỉ số', icon: 'meter', description: 'Nhập chỉ số điện nước hàng loạt bằng file Excel.' },
      { path: 'materials', label: 'Kho vật tư', shortLabel: 'Vật tư', icon: 'box', description: 'Quản lý danh mục vật tư và kiểm kê tồn kho thiết bị.' },
    ],
  },
  {
    id: 'security', label: 'Bảo vệ',
    description: 'Không gian kiểm soát vào cổng và khách thăm ký túc xá.',
    navigation: [
      overview,
      { path: 'gate', label: 'Kiểm soát cổng', shortLabel: 'Cổng', icon: 'gate', description: 'Quét mã vào cổng và ghi nhận vi phạm về trễ sau 23 giờ.' },
      { path: 'visitors', label: 'Kiểm soát khách thăm', shortLabel: 'Khách thăm', icon: 'people', description: 'Tra cứu, xác nhận và kiểm soát khách thăm tại cổng.' },
    ],
  },
]

// hàm tạo đường dẫn url hoàn chỉnh cho một mục trong không gian làm việc
export function workspacePath(workspace: Workspace, item?: NavigationItem) {
  return `/${workspace.id}${item?.path ? `/${item.path}` : ''}`
}
