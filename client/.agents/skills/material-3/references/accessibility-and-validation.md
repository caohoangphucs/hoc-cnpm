# Accessibility, Validation and M3 Audit — Web

Đối chiếu 2026-10-02. M3 design guidance và WCAG là hai hệ tiêu chí khác nhau. Theo WCAG 2.2 AA cho kiểm tra web khi được yêu cầu; không gọi một vài checklist pass là chứng nhận toàn bộ sản phẩm.

## Checklist theo phạm vi

### Contrast và thông tin

- Chữ thường ít nhất 4.5:1; chữ lớn theo định nghĩa WCAG ít nhất 3:1, xét các ngoại lệ của tiêu chí. Không gọi text 18 CSS px bất kỳ là chữ lớn.
- Non-text contrast thường cần 3:1 cho phần nhận diện control/state và đồ họa mang thông tin; không ép mọi border trang trí hoặc disabled control đạt cùng ngưỡng.
- Đo trên nền thực tế sau opacity/state layer/theme, không chỉ so mã màu gốc.
- Màu phải đi kèm label, icon, pattern hoặc thông tin khác để diễn đạt trạng thái.

Nguồn: [text contrast](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html), [non-text contrast](https://www.w3.org/WAI/WCAG22/Understanding/non-text-contrast.html).

### Keyboard, focus và semantics

- Link điều hướng, button hành động, label đúng control, semantic headings/landmarks.
- Tab order hợp lý; Enter/Space/Arrow/Escape theo loại component. Không gán ARIA role nếu chưa có behavior tương ứng.
- Focus nhìn thấy và không bị sticky bar/modal overlay che hoàn toàn. Không dùng tabindex dương để chữa cấu trúc DOM sai.
- Dialog modal giữ focus bên trong, xử lý Escape phù hợp, có tên và trả focus khi đóng. Ưu tiên control an toàn nếu dialog có thao tác khó đảo ngược; không coi mọi confirm là alertdialog.
- Panel/tab có id và association đúng; khi ẩn pane/navigation, loại khỏi focus order/accessibility tree.

Nguồn: [focus not obscured](https://www.w3.org/WAI/WCAG22/Understanding/focus-not-obscured-minimum.html), [dialog pattern](https://www.w3.org/WAI/ARIA/apg/patterns/dialog-modal/), [tabs pattern](https://www.w3.org/WAI/ARIA/apg/patterns/tabs/).

### Target, zoom và motion

- M3 thường hướng tới vùng chạm 48dp; dùng 48 CSS px như mục tiêu thiết kế cục bộ khi phù hợp, không gọi đó là tiêu chí WCAG AA bắt buộc.
- WCAG 2.2 AA 2.5.8 dùng 24x24 CSS px hoặc đáp ứng điều kiện/ngoại lệ như spacing; 44x44 thuộc tiêu chí enhanced AAA. Đánh giá đúng target area, không chỉ kích thước icon.
- Kiểm tra reflow ở 320 CSS px và zoom/text scaling theo tiêu chí áp dụng; bảng dữ liệu có thể có ngoại lệ two-dimensional layout, nhưng không dùng ngoại lệ đó cho toàn page.
- Tôn trọng prefers-reduced-motion cho motion không thiết yếu như khuyến nghị bổ sung. WCAG 2.3.3 animation from interactions là AAA, không gắn sai nhãn AA.
- Form cần message liên kết với field; lỗi submit và status async cần được thông báo thích hợp, không dùng aria-live assertive cho mọi thay đổi.

Nguồn: [target minimum](https://www.w3.org/WAI/WCAG22/Understanding/target-size-minimum.html), [target enhanced](https://www.w3.org/WAI/WCAG22/Understanding/target-size-enhanced.html), [reflow](https://www.w3.org/WAI/WCAG22/Understanding/reflow.html), [animation](https://www.w3.org/WAI/WCAG22/Understanding/animation-from-interactions.html), [status messages](https://www.w3.org/WAI/WCAG22/Understanding/status-messages.html).

## Kiểm tra implementation

Chọn kiểm tra theo thay đổi, không chạy mọi công cụ cho mỗi sửa text.

- TSX/config: lint, typecheck và build của package thực tế. Trong client hiện có `pnpm run lint`, `pnpm run typecheck`, `pnpm run build`.
- Component tương tác: kiểm tra keyboard, state transitions, accessible name, form submit/value và focus sau close/navigation.
- Theme/layout: light/dark, contrast của states, compact/expanded, nhãn dài, zoom và pane width thực tế.
- Nếu có browser tool: quan sát DOM/AX/computed CSS và screenshot khi hữu ích. Nếu chỉ đọc source, ghi rõ giới hạn xác minh runtime.
- Automated accessibility tools hỗ trợ tìm lỗi; không thay kiểm tra keyboard/screen-reader/semantics thủ công.
- Không cài test runner/browser dependency vào sản phẩm chỉ để chạy audit tài liệu nếu công cụ hiện có đủ.

## Rubric audit nội bộ

Dùng khi người dùng yêu cầu audit, không bắt buộc tạo report cho mọi thay đổi.

| Nhóm | Bằng chứng cần tìm |
|---|---|
| Color/theme | Defined roles, pairing, light/dark được cung cấp, contrast |
| Typography | Roles, readable size/line-height, tiếng Việt, zoom |
| Shape/elevation | Component variants, token/customization rõ ràng |
| Components/states | Variant đúng mục đích, interaction/async states thực dùng |
| Layout/navigation | Responsive, history/back, active/selection semantics |
| Accessibility | Keyboard, focus, labels, dialogs, status, targets |
| Motion | Motion thực có; reduced-motion nếu liên quan |

Phân loại từng phát hiện: lỗi cần sửa, khuyến nghị, đã xác minh, chưa kiểm tra hoặc không áp dụng. Không coi thiếu FAB, dark-mode toggle, dynamic wallpaper hoặc Expressive motion là lỗi nếu sản phẩm không yêu cầu.

Nếu người dùng cần score:

- Chấm 0–10 cho nhóm áp dụng, giải thích bằng chứng; group chưa kiểm tra không được coi là pass.
- N/A loại khỏi mẫu số. Công thức nội bộ: `100 * sum(scores) / (10 * count(scored applicable groups))`.
- Nếu thiếu runtime/coverage, công bố điểm chỉ cho phần đã kiểm tra và liệt kê phần chưa kiểm tra; không trình bày như overall product score.
- Nếu không có nhóm được chấm, báo không đủ dữ liệu; không chia cho 0.
- Accessibility blockers quan trọng phải hiển thị riêng dù tổng điểm cao.

Report ngắn: target/phạm vi → phát hiện theo mức ảnh hưởng → bằng chứng file/line hoặc UI → đề xuất sửa → kiểm tra đã chạy/giới hạn. Điểm này không phải chứng nhận Google M3 hay WCAG conformance.
