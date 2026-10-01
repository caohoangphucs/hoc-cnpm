# M3 Typography, Shape, Elevation and Motion — Web

Đối chiếu 2026-10-02. Các giá trị baseline dưới đây tham chiếu **Material Web v0.192**; kiểm tra lại variant/component và phiên bản trước khi dùng như giá trị chính xác.

## Typography

M3 phân vai Display, Headline, Title, Body và Label; mỗi nhóm có L/M/S. Dùng semantic HTML headings theo cấu trúc nội dung, không chọn h1/h2 chỉ để đạt kích thước chữ.

- Khai báo font, weight, size, line-height, tracking của style dùng trong UI.
- Dùng rem cho text để hỗ trợ đổi font size; không khóa root font size nhằm vô hiệu hóa tùy chọn người dùng.
- Roboto/Roboto Flex là lựa chọn Material quen thuộc; font khác vẫn hợp lệ khi chủ ý tùy biến. Kiểm tra glyph tiếng Việt, độ dài nhãn, fallback và line-height.
- Chỉ dùng `font: var(--md-sys-typescale-body-large)` khi đã tự định nghĩa shorthand đó. Tên shorthand không tự được tạo từ các axis tokens.

```css
:root {
  /* Ví dụ hoàn chỉnh cho một style; đây là tùy chọn font cục bộ. */
  --md-sys-typescale-body-large-font: system-ui, sans-serif;
  --md-sys-typescale-body-large-weight: 400;
  --md-sys-typescale-body-large-size: 1rem;
  --md-sys-typescale-body-large-line-height: 1.5rem;
  --md-sys-typescale-body-large-tracking: 0.03125rem;
}
.body-large {
  font-family: var(--md-sys-typescale-body-large-font);
  font-weight: var(--md-sys-typescale-body-large-weight);
  font-size: var(--md-sys-typescale-body-large-size);
  line-height: var(--md-sys-typescale-body-large-line-height);
  letter-spacing: var(--md-sys-typescale-body-large-tracking);
}
```

Nguồn: [typescale tokens](https://github.com/material-components/material-web/blob/main/tokens/versions/v0_192/_md-sys-typescale.scss).

## Shape

| Baseline token | CSS value |
|---|---|
| none | 0px |
| extra-small | 4px |
| small | 8px |
| medium | 12px |
| large | 16px |
| extra-large | 28px |
| full | 9999px |

Đây là scale, không phải quy tắc rằng mọi component cùng loại dùng cùng radius.

- Filled text field: extra-small ở góc trên; outlined text field: extra-small các góc, theo token web baseline đang đối chiếu.
- FAB: small dùng medium, default dùng large, large dùng extra-large. Extended FAB dùng `md-fab` có label.
- Component variant và brand theme có thể override shape. Ghi rõ override; không coi mọi nút, menu, chip hoặc Expressive component đều có một radius chung.
- Shape Expressive mới cần source và version riêng; không tự nhập toàn bộ scale Android làm mặc định web.

Nguồn: [shape scale](https://github.com/material-components/material-web/blob/main/tokens/versions/v0_192/_md-sys-shape.scss), [filled field](https://github.com/material-components/material-web/blob/main/tokens/versions/v0_192/_md-comp-filled-text-field.scss), [outlined field](https://github.com/material-components/material-web/blob/main/tokens/versions/v0_192/_md-comp-outlined-text-field.scss), [FAB](https://material-web.dev/components/fab/).

## Elevation

Surface roles và shadow đều góp phần biểu đạt phân lớp. Material Web có `md-elevation`; không cấm shadow mặc định trên mọi component, và không ánh xạ surface role/elevation level theo công thức toàn cục.

Chọn surface và elevation từ component/variant cụ thể; kiểm tra resting/hover/pressed, light/dark và ngữ cảnh overlay. Nếu tự dùng CSS shadow, tập trung vào token dùng chung thay vì rải số trong component.

Nguồn: [elevation API/tokens](https://material-web.dev/components/elevation/).

## Motion

Dùng motion để giải thích thay đổi trạng thái hoặc quan hệ giữa vùng nội dung. Tránh áp duration dài lên mọi thao tác; không bắt buộc spring/morphing để đạt M3 baseline.

- Ghi rõ easing/duration là theo source, library hay lựa chọn cục bộ.
- Không trình bày một cubic-bezier đơn như tương đương mọi đường emphasized/spring.
- Chỉ thêm thư viện motion khi yêu cầu và giải pháp hiện có cần nó.
- Tôn trọng `prefers-reduced-motion`; giảm/loại animation không thiết yếu nhưng giữ dấu hiệu phản hồi dễ hiểu.
- Tùy biến CSS để trông expressive không chứng minh đã có đầy đủ Expressive behavior.

Ví dụ fallback accessibility, không phải bảng motion token chính thức:

```css
@media (prefers-reduced-motion: reduce) {
  .animated-panel {
    animation: none;
    transition: none;
  }
}
```

Nguồn: [M3 motion](https://m3.material.io/styles/motion/overview), [animation from interactions](https://www.w3.org/WAI/WCAG22/Understanding/animation-from-interactions.html). Nội dung M3 render bằng JavaScript: nếu chưa đọc được, ghi chưa xác minh thay vì đoán giá trị.
