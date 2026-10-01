# M3 Navigation — React/Web

Đối chiếu 2026-10-02. Nguồn thiết kế: [navigation bar](https://m3.material.io/components/navigation-bar/overview), [navigation rail](https://m3.material.io/components/navigation-rail/overview), [drawer](https://m3.material.io/components/navigation-drawer/overview). Nguồn triển khai: [React Router navigation](https://reactrouter.com/start/declarative/navigating), [WAI tabs](https://www.w3.org/WAI/ARIA/apg/patterns/tabs/).

## Chọn mô hình

- Nhóm điểm đến chính theo mục tiêu người dùng; không tạo một trang riêng cho từng action.
- Compact: bottom bar phù hợp với khoảng 3–5 điểm đến chính. Nhiều nhóm hoặc hierarchy sâu có thể phù hợp drawer/menu.
- Medium/expanded: cân nhắc rail, drawer hoặc navigation hiện có theo độ rộng hữu dụng, số mục, label và pane. Không tự động thay navigation ở một breakpoint dù nội dung không phù hợp.
- Tabs biểu diễn các view liên quan trong một ngữ cảnh; không thay global navigation bằng tabs chỉ vì có hai điểm đến.
- Hai view hoặc hai route không tự động là lý do bắt buộc dùng tabs.
- Phân quyền có thể thay danh sách mục; ẩn mục trên frontend không thay thế kiểm tra quyền backend.

## React Router

Dùng `Link`/`NavLink` cho điều hướng; tránh reload toàn trang bằng button + location khi router đã có. Active state cần tên, vị trí và `aria-current`, không chỉ đổi màu.

```tsx
import { NavLink } from 'react-router'

export function PrimaryNavigation() {
  return (
    <nav aria-label="Điều hướng chính">
      <NavLink to="/" end>Trang chủ</NavLink>
      <NavLink to="/requests">Yêu cầu</NavLink>
    </nav>
  )
}
```

Đây là ví dụ integration, không yêu cầu tạo route `/requests` trong sản phẩm. Với route nesting, thiết lập matching có chủ ý. React Router tự thêm `aria-current="page"` cho NavLink đang active.

## Adaptive navigation

Có thể dùng cùng dữ liệu điểm đến cho bar/rail/drawer. Nếu render nhiều phiên bản, bản ẩn phải không xuất hiện trong focus order/accessibility tree. Tránh duplicate ids và active state lệch nhau.

- CSS media/container queries là công cụ; breakpoint theo viewport không thay cho độ rộng pane thực tế.
- Compact fixed bottom bar cần content padding/safe area để không che nội dung và focus.
- Drawer modal cần focus management, Escape, backdrop/inert theo giải pháp được chọn; kiểm tra focus trở về trigger.
- Điểm đến dùng link. Nút mở drawer là button có accessible name và trạng thái expanded phù hợp.
- Không dùng `md-navigation-*` hoặc đường import từ ví dụ cũ nếu chưa xác minh package/API. Xem [catalog](component-catalog.md).

## Tabs

Nếu tabs đổi panel trên cùng trang, dùng tablist/tab/tabpanel với selection, quan hệ id và keyboard behavior theo WAI APG. Arrow keys điều hướng tabs; activation tự động chỉ phù hợp khi panel hiển thị nhanh.

Nếu các mục đơn thuần là links tới routes, dùng navigation links và current-page semantics; không gán role tab chỉ vì chúng trông giống tabs.

## Layout và dữ liệu

List-detail có thể cùng một route hoặc route detail lồng, tùy URL/back behavior. Trên compact, hiển thị list hoặc detail; trên rộng, có thể hiển thị cả hai. Giữ selection và lịch sử điều hướng hợp lý. Xem [layout](layout-and-responsive.md).
