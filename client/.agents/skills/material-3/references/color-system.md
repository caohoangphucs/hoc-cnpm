# M3 Color System — Web

Đối chiếu 2026-10-02. Nguồn: [Material Web color tokens](https://github.com/material-components/material-web/blob/main/tokens/_md-sys-color.scss), [Material Color Utilities](https://github.com/material-foundation/material-color-utilities).

## Vai trò màu

| Nhóm | Token CSS | Dùng cho |
|---|---|---|
| Accent | `--md-sys-color-primary`, `secondary`, `tertiary` | Nhấn mạnh, lựa chọn, hành động |
| Nội dung trên accent | `--md-sys-color-on-primary`, `on-secondary`, `on-tertiary` | Nội dung trên đúng background tương ứng |
| Accent container | `--md-sys-color-primary-container` và các nhóm tương ứng | Nền có mức nhấn mạnh khác |
| Nội dung trên container | `--md-sys-color-on-primary-container` và các nhóm tương ứng | Chữ/icon trên accent container |
| Surface | `--md-sys-color-surface`, `surface-container-lowest/low/high/highest`, `surface-container` | Nền và vùng nội dung |
| Nội dung surface | `--md-sys-color-on-surface`, `on-surface-variant` | Chữ/icon mức nhấn mạnh khác nhau |
| Error | `--md-sys-color-error`, `on-error`, `error-container`, `on-error-container` | Trạng thái lỗi |
| Outline | `--md-sys-color-outline`, `outline-variant` | Ranh giới quan trọng và ranh giới trang trí |
| Inverse | `--md-sys-color-inverse-surface`, `inverse-on-surface`, `inverse-primary` | Vùng tương phản như snackbar |
| Overlay/elevation | `--md-sys-color-scrim`, `shadow` | Modal scrim, shadow |

Tên không có prefix trong bảng vẫn thuộc `--md-sys-color-*`. Không dùng các token này trước khi chúng được khai báo trong theme.

- Ghép màu theo vai trò rồi đo contrast trên nền thực tế, kể cả state layers/ảnh/opacity. Có tên token đúng không đảm bảo contrast sau tùy biến.
- `outline-variant` thường dành cho divider/border trang trí. Ranh giới cần nhận diện control phải đáp ứng contrast phù hợp; không gán mọi border cùng một token.
- Có thể thêm semantic token của sản phẩm như success/warning/info. Định nghĩa cặp foreground/background, nhãn/icon và theme; không gọi chúng là role chính thức M3 nếu nguồn không có.
- Error roles vẫn thay đổi tone giữa light/dark và contrast settings; không mô tả chúng như mã hex bất biến.
- Fixed accent roles có background ổn định giữa theme nhưng vẫn có foreground roles tương ứng. Không suy luận contrast chỉ từ chữ “fixed”.

## Cách khai báo

`src/theme/` là nơi hợp lý để tập trung token trong client này. Scope ở root hoặc subtree có nhu cầu theme riêng. Component tiêu thụ token, không tự sinh một scheme riêng ở mỗi render.

```css
/* Ví dụ cách tiêu thụ; cần theme đầy đủ ở ancestor. */
.surface {
  color: var(--md-sys-color-on-surface);
  background: var(--md-sys-color-surface-container);
  border: 1px solid var(--md-sys-color-outline-variant);
}
```

Không bắt buộc dùng màu tím baseline. Chọn seed/brand theo yêu cầu người dùng; sinh scheme hoặc xác định bộ role đầy đủ rồi kiểm tra light/dark. Xem [theming](theming-and-dynamic-color.md).

## Tránh suy luận sai

- Không ánh xạ một elevation level thành một surface role cho mọi component.
- Không coi dynamic color từ wallpaper Android là chức năng web sẵn có.
- Không hardcode tỷ lệ pha primary lên surface như yêu cầu chung của M3 hiện tại.
- Không thể hiện trạng thái lỗi/thành công chỉ bằng màu.
