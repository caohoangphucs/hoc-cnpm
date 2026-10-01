# M3 Component Catalog — React/Web

Đối chiếu 2026-10-02 với [Material Web exports](https://github.com/material-components/material-web/blob/main/all.ts) và tài liệu component. API này là triển khai thư viện, không phải toàn bộ danh sách đặc tả M3. Kiểm tra package đã cài trước khi import; client hiện chưa cài Material Web.

## Chọn giải pháp

Dùng React/HTML có ngữ nghĩa + CSS tokens hoặc thư viện UI đã được dự án chọn. Nếu chủ ý dùng Material Web, import từng module cần dùng; không mặc định thêm toàn thư viện. Không coi tên “Material” của một thư viện khác là bảo đảm M3.

## Material Web đã đối chiếu

Prefix import là `@material/web/`; file ví dụ phải tồn tại trong phiên bản package sử dụng.

| Nhóm | Elements | Module ví dụ |
|---|---|---|
| Buttons | `md-filled-button`, `md-filled-tonal-button`, `md-elevated-button`, `md-outlined-button`, `md-text-button` | `button/filled-button.js` |
| FAB | `md-fab` | `fab/fab.js` |
| Icon buttons | `md-icon-button` và filled/tonal/outlined variants | `iconbutton/icon-button.js` |
| Text fields | `md-filled-text-field`, `md-outlined-text-field` | `textfield/outlined-text-field.js` |
| Checkbox/radio/switch | `md-checkbox`, `md-radio`, `md-switch` | `checkbox/checkbox.js` |
| Select | `md-filled-select`, `md-outlined-select`, `md-select-option` | `select/outlined-select.js` |
| Chips | `md-chip-set`, assist/filter/input/suggestion variants | `chips/filter-chip.js` |
| Dialog | `md-dialog` | `dialog/dialog.js` |
| Menu | `md-menu`, `md-menu-item`, `md-sub-menu` | `menu/menu.js` |
| Tabs | `md-tabs`, `md-primary-tab`, `md-secondary-tab` | `tabs/tabs.js` |
| Lists | `md-list`, `md-list-item` | `list/list.js` |
| Progress | `md-linear-progress`, `md-circular-progress` | `progress/linear-progress.js` |
| Other | `md-divider`, `md-slider`, `md-icon`, `md-elevation` | Theo [exports](https://github.com/material-components/material-web/blob/main/all.ts) |

### Sửa các ví dụ upstream

- Extended FAB dùng `md-fab` với `label`; không có module `fab/extended-fab.js` trong catalog đã đối chiếu.
- Button group dùng React grouping/HTML phù hợp và tokens; không import `button/button-group.js` chưa được xác minh.
- Navigation bar/rail/drawer: tự triển khai React hoặc dùng API đã xác minh của thư viện được chọn. Không dùng các import `navigation/navigation-bar.js`/`navigation-drawer.js` từ ví dụ cũ như API ổn định.
- Card, snackbar, tooltip, sheets, date/time picker: xác định giải pháp riêng khi có nhu cầu; không tự chế tên `md-*` và kỳ vọng trình duyệt render đúng.

```html
<!-- Material Web Extended FAB: cần import fab/fab.js và icon/icon.js. -->
<md-fab label="Tạo yêu cầu">
  <md-icon slot="icon">add</md-icon>
</md-fab>
```

Nguồn: [FAB](https://material-web.dev/components/fab/). Icon font cần được cung cấp, hoặc dùng SVG đã có trong dự án.

### Dialog có form liên kết đúng

```html
<!-- Các actions liên kết tới id của form, không phải id của dialog. -->
<md-dialog aria-label="Xác nhận thao tác">
  <form slot="content" id="confirm-form" method="dialog">
    Bạn muốn tiếp tục?
  </form>
  <div slot="actions">
    <md-text-button form="confirm-form" value="cancel">Hủy</md-text-button>
    <md-filled-button form="confirm-form" value="confirm">Tiếp tục</md-filled-button>
  </div>
</md-dialog>
```

Dùng `open` hoặc methods theo API và kiểm tra close/cancel behavior. Form `method="dialog"` chỉ đóng dialog, không thay thế xử lý submit API. Mỗi instance phải có id form riêng. Nguồn: [Dialog](https://material-web.dev/components/dialog/).

## Variant và state

- Filled cho hành động nổi bật; tonal/outlined/text tùy hierarchy và background. Không ép mọi trang phải có FAB.
- Button và link khác ngữ nghĩa: điều hướng dùng link/router, hành động dùng button. Đặt type của button trong form rõ ràng.
- Text field phải có label, mô tả/lỗi liên kết và giá trị controlled/uncontrolled nhất quán.
- Loading cần phản hồi trạng thái và tránh submit trùng; empty/error/data states phải giúp người dùng biết bước tiếp theo.
- Xem [interaction states](interaction-states.md), [React integration](react-integration.md) và [accessibility](accessibility-and-validation.md).

## Baseline và Expressive

Không đánh đồng thay đổi height/shape với API Expressive đầy đủ. Ghi rõ capability tùy nền tảng/version; layout hoặc motion custom trên web là triển khai riêng. Material Web hiện ở [maintenance mode](https://github.com/material-components/material-web).
