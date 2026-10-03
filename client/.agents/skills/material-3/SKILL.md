---
name: material-3
description: Design, implement, or review Material Design 3 interfaces for React and TypeScript web apps, including themes, components, adaptive navigation, interaction states, and accessibility. Use for M3, MD3, Material You, or Material UI design-system requests; do not use for unrelated backend work.
license: MIT
metadata:
  version: "1.1.1-react.1"
  upstream: "hamen/material-3-skill@1.1.1"
---

# Material Design 3 — React Web

Bản điều chỉnh cục bộ cho React Web từ skill của Ivan Morgillo/CWTI Ltd. Giữ giấy phép MIT trong [LICENSE](LICENSE). Các chỉnh sửa này không phải bản phát hành upstream hay tài liệu chứng nhận của Google.

## Phạm vi

- Mục tiêu chính: React + TypeScript, CSS custom properties, responsive web.
- Với repository này, đọc `client/package.json` và README thực tế: dùng Vite, pnpm, React Router, alias `@/`; đặt theme trong `src/theme/` và component dùng chung trong `src/shared/components/`.
- Đây là hướng dẫn thiết kế/triển khai M3. Không tự cài thư viện, đổi stack, triển khai nghiệp vụ hoặc sửa backend chỉ vì skill có ví dụ về chúng.
- Nếu làm Android/Flutter, chuyển sang tài liệu chính thức của nền tảng; không dịch nguyên CSS/DOM sang Compose hoặc ngược lại.

## Chọn tài liệu theo công việc

| Công việc | Tài liệu cần đọc |
|---|---|
| Màu và token | [Color system](references/color-system.md) |
| Sinh theme, sáng/tối | [Theming](references/theming-and-dynamic-color.md) |
| Typography, shape, elevation, motion | [Visual tokens](references/typography-and-shape.md) |
| Chọn component, kiểm tra Material Web API | [Component catalog](references/component-catalog.md) |
| Component TSX, form, refs, custom elements | [React integration](references/react-integration.md) |
| Hover, focus, press, disabled, loading | [Interaction states](references/interaction-states.md) |
| Navigation, tabs, router | [Navigation](references/navigation-patterns.md) |
| List-detail, bảng, responsive | [Layout](references/layout-and-responsive.md) |
| Accessibility, kiểm tra, audit | [Accessibility and validation](references/accessibility-and-validation.md) |

Chỉ đọc các tài liệu liên quan đến yêu cầu hiện tại.

## Quyết định triển khai

1. Xác định yêu cầu người dùng, stack, component/theme hiện có và thư viện đang cài. Tận dụng chúng trước khi thêm giải pháp mới.
2. Chọn component theo mục đích và mức nhấn mạnh; thiết kế cả trạng thái dữ liệu/tương tác liên quan.
3. Dùng token có vai trò rõ ràng cho màu, kiểu chữ, shape, state và motion. Màu hex hợp lệ ở nơi khai báo/generate theme; hạn chế rải màu trực tiếp trong component.
4. Ưu tiên HTML có ngữ nghĩa và React components với CSS tokens khi dự án chưa chọn thư viện UI. Đây là mặc định cục bộ, không phải yêu cầu của Google. Nếu thư viện đã được chọn, dùng API của nó và kiểm tra mức phù hợp M3.
5. Kiểm tra màn hình nhỏ/lớn, bàn phím, focus, contrast, theme và các trạng thái thực sự dùng. Chọn mức kiểm tra phù hợp thay đổi.

## Ranh giới đặc tả và thư viện

- M3 là hệ thống thiết kế; tên component trong đặc tả không chứng minh có element/import tương ứng trong một thư viện.
- Material Web là Web Components và hiện ở maintenance mode. Không mặc định thêm `@material/web` hoặc coi thư viện này có đầy đủ M3 Expressive.
- React 19 hỗ trợ custom elements. Wrapper là lựa chọn để chuẩn hóa props/events và typing, không phải điều kiện bắt buộc để render chúng.
- Phân biệt baseline, Expressive, API thư viện và tùy biến của dự án. CSS height/radius tùy chỉnh không đủ để tuyên bố component có đầy đủ Expressive behavior.
- Surface color và shadow đều có thể biểu đạt elevation. Không áp bảng ánh xạ surface/elevation tuyệt đối cho mọi component.
- Không coi mọi trang đều cần dynamic color, animation, bottom navigation, FAB hoặc layout nhiều pane.

## Nguồn và độ mới

Đối chiếu ngày **2026-10-02** với tài liệu React, W3C và Material Web chính thức. Các bảng baseline web dùng token **Material Web v0.192** khi được ghi rõ; đó không phải tuyên bố về toàn bộ đặc tả M3 mới nhất.

Khi cần giá trị/API chính xác, kiểm tra phiên bản đã cài và nguồn được dẫn ở từng reference. Nguồn trên `main` có thể thay đổi; ghi ngày, phiên bản hoặc commit khi phát hành bộ token/component. Đánh dấu thông tin chưa xác minh; không suy ra tính năng từ tiêu đề release/blog.

- [Material Design 3](https://m3.material.io/)
- [Material Web và trạng thái bảo trì](https://github.com/material-components/material-web)
- [React custom elements](https://react.dev/reference/react-dom/components#custom-html-elements)
- [WCAG 2.2](https://www.w3.org/TR/WCAG22/)

## Khi được yêu cầu audit

Đọc reference accessibility và kiểm tra target được chỉ định. Báo phát hiện kèm file/line hoặc bằng chứng trình duyệt; phân biệt lỗi, khuyến nghị và phần chưa kiểm tra. Nếu có điểm số, dùng rubric nội bộ, loại N/A khỏi mẫu số và không coi điểm số là chứng nhận M3/WCAG. Audit không tự cấp quyền sửa target.
