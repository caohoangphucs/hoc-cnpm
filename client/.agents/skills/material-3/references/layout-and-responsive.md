# M3 Layout — React/Web

Đối chiếu 2026-10-02. Nguồn thiết kế: [M3 layout](https://m3.material.io/foundations/layout/overview), [Android adaptive layouts](https://developer.android.com/develop/ui/compose/layouts/adaptive/canonical-layouts). Android source giúp giải thích mô hình; các đoạn CSS và lựa chọn bên dưới là cách áp dụng web, không phải API Compose.

## Chọn layout theo nội dung

| Nội dung | Hướng triển khai |
|---|---|
| Feed/cards | Grid responsive; tránh card quá hẹp hoặc text line quá dài |
| List-detail | Một pane trên compact; thêm detail pane khi đủ không gian |
| Nội dung + phụ trợ | Supporting pane khi thông tin phụ có ích và vẫn giữ được trọng tâm |
| Form | Giới hạn độ dài dòng/field, nhóm liên quan, lỗi nằm gần field |
| Bảng quản lý | Giữ semantics table, ưu tiên cột quan trọng; cuộn ngang có vùng rõ ràng khi cần |

Không ép mọi màn hình vào một canonical layout hoặc max-width 840–1040px. Page đọc, form và bảng có nhu cầu chiều rộng khác nhau. Không biến bảng thành cards nếu mất khả năng so sánh/sort cần thiết.

## Breakpoint và spacing

600, 840, 1200, 1600 CSS px có thể làm mốc thiết kế từ size classes; điều chỉnh theo nội dung/pane. dp Android không phải đơn vị CSS; các mốc số này là sự chuyển thể thiết kế, không quy tắc đổi pixel vật lý.

Dùng media queries cho app shell, container queries khi component nằm trong pane có chiều rộng riêng. Kiểm tra sát hai bên breakpoint và cả khoảng giữa, không chỉ ảnh chụp ở vài kích thước.

Ví dụ spacing cục bộ theo nhịp 4/8px; không phải bảng token hệ thống chính thức của Google:

```css
:root {
  --app-space-1: 4px;
  --app-space-2: 8px;
  --app-space-3: 16px;
  --app-space-4: 24px;
  --app-space-5: 32px;
}
.list-detail {
  display: grid;
  gap: var(--app-space-4);
  min-width: 0;
}
@media (min-width: 840px) {
  .list-detail {
    grid-template-columns: minmax(240px, 1fr) minmax(0, 2fr);
  }
}
```

Đoạn CSS không quyết định pane nào active trên compact; React/router phải xử lý selection, detail visibility và back behavior. Không chỉ display:none mà vô tình làm mất đường quay lại list.

## Web constraints

- Grid/flex children cần min-width:0 khi có text/table dài để tránh tràn ngoài viewport.
- Hỗ trợ zoom, text wrap và nhãn tiếng Việt; không dùng chiều cao cố định làm cắt nội dung field/error.
- Vùng scroll và headers cố định phải giữ focus/controls nhìn thấy được.
- Fixed bar cần content inset và `env(safe-area-inset-bottom)` khi phù hợp.
- Không khóa orientation hoặc thiết kế theo user-agent/device name.
- Foldable/Viewport Segments là nâng cấp theo nhu cầu và hỗ trợ browser, không điều kiện mặc định để hoàn thành web UI.
- Xem [navigation](navigation-patterns.md) và [accessibility](accessibility-and-validation.md).
