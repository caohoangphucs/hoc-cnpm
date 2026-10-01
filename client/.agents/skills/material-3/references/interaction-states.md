# M3 Interaction States — Web

Đối chiếu 2026-10-02. Baseline tham chiếu [Material Web state v0.192](https://github.com/material-components/material-web/blob/main/tokens/versions/v0_192/_md-sys-state.scss); không dùng các số này như bảng Expressive mới nhất cho mọi nền tảng.

## Ma trận theo component

| Trạng thái | Cần thể hiện/kiểm tra |
|---|---|
| Rest | Hierarchy, tên, affordance |
| Hover | Phản hồi pointer; không là cách duy nhất để thấy nội dung quan trọng |
| Focus-visible | Indicator rõ, không bị clip/che; keyboard luôn biết vị trí |
| Pressed | Phản hồi activation, không đồng nghĩa selected |
| Selected/checked | State bền vững + semantics thích hợp |
| Disabled | Không kích hoạt; có ngữ nghĩa disabled và treatment phù hợp |
| Dragged | Chỉ khi có drag; cung cấp phương án keyboard/không drag khi cần |
| Loading | Phản hồi tiến trình, tránh thao tác trùng, thông báo phù hợp |
| Error | Message rõ, liên kết field, không chỉ đổi màu |

State layer dùng màu theo component/foreground role và opacity. Theo baseline đang đối chiếu: hover 0.08, focus 0.12, pressed 0.12, dragged 0.16. Đây không phải opacity của toàn bộ component. Disabled dùng component-specific foreground/container opacity; không đặt một opacity chung cho mọi control rồi coi là spec.

Selected không dùng chung semantics ở mọi nơi: route dùng aria-current, toggle dùng aria-pressed, checkbox dùng checked, tabs dùng aria-selected.

## CSS recipe cục bộ

Ví dụ một filled native button; cần theme color/shape tokens tại ancestor. Chiều cao/padding dưới đây là lựa chọn mẫu, không bảng đầy đủ của mọi button variant.

```css
.m3-button--filled {
  position: relative;
  overflow: visible;
  border: 0;
  border-radius: var(--md-sys-shape-corner-full);
  min-height: 40px;
  padding: 0 24px;
  color: var(--md-sys-color-on-primary);
  background: var(--md-sys-color-primary);
}
.m3-button--filled::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  pointer-events: none;
  background: var(--md-sys-color-on-primary);
  opacity: 0;
}
@media (hover: hover) {
  .m3-button--filled:hover:not(:disabled)::before { opacity: 0.08; }
}
.m3-button--filled:focus-visible::before { opacity: 0.12; }
.m3-button--filled:active:not(:disabled)::before { opacity: 0.12; }
.m3-button--filled:focus-visible {
  outline: 2px solid var(--md-sys-color-primary);
  outline-offset: 3px;
}
```

State layer/focus CSS là ví dụ để điều chỉnh, không component hoàn chỉnh. Kiểm tra focus ring trên background thực tế; cùng primary có thể không đủ nhận diện ở mọi nơi. Recipe 40px visual height không tự đáp ứng target chạm 48px: tăng hit area/spacing khi thiết kế yêu cầu. Hover/focus/pressed chồng nhau không cộng opacity tùy tiện; dùng precedence có chủ ý.

## Hành vi

- Ripple/state layer không thay focus indicator. Không bỏ outline mà chưa có giải pháp tương đương.
- Không tắt keyboard focus chỉ vì lần tương tác trước dùng chuột.
- `aria-disabled` không tự chặn click/keyboard. Nếu giữ control focusable bằng thuộc tính này, phải xử lý activation rõ ràng.
- Loading không tự tạo disabled cho toàn trang. Giữ route/navigation và khả năng đọc thông tin khi hợp lý.
- Empty/error/offline là trạng thái của dữ liệu; không giả dữ liệu thành công để tránh render lỗi.
- Motion là bổ sung; đọc [visual tokens](typography-and-shape.md) về giảm chuyển động.
