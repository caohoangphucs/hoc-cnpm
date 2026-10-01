# M3 Theming — React/Web

Đối chiếu 2026-10-02. Nguồn: [Material Color Utilities](https://github.com/material-foundation/material-color-utilities), [Material Web theming](https://material-web.dev/theming/color/).

## Chọn cách cung cấp token

- Nếu sản phẩm có scheme cố định: generate/export CSS light/dark trong giai đoạn phát triển. Không cần runtime dependency chỉ để áp một bộ màu đã xác định.
- Nếu người dùng thay seed/contrast trong lúc chạy: cân nhắc utilities ở runtime, kiểm tra API/package version và chi phí bundle.
- Nếu thư viện UI đã cung cấp theme: map role có chủ ý, kiểm tra contrast và component token thay vì tạo hệ theme cạnh tranh.
- Brand seed không đồng nghĩa mọi role giữ nguyên hex của brand. Nếu cố định một màu riêng, kiểm tra tất cả foreground/background/state liên quan.

## Sinh màu

Utilities hỗ trợ HCT và scheme builders. Chọn scheme phù hợp thiết kế; không mặc định `SchemeContent` là lựa chọn bắt buộc cho mọi app. Xác minh exports/signature theo phiên bản thực tế; không cài dependencies chỉ vì đoạn ví dụ dùng chúng.

Khi được yêu cầu thêm utilities, dùng package manager của client:

```powershell
pnpm add @material/material-color-utilities
```

Đây là lệnh có điều kiện, không phải bước tự chạy khi đọc skill. Theme đã sinh phải bao gồm role mà component thực sự dùng, gồm surface containers, error, inverse, outline và shadow/scrim nếu cần. Xem [color-system](color-system.md).

## Sáng/tối/hệ thống

Lựa chọn hợp lý cho web là light, dark và system. Scheme nào được dùng phải rõ ràng và có thể kiểm tra.

- CSS root theme cho app; scope subtree khi cần preview/theme riêng.
- Nếu có toggle, đồng bộ React state với `data-theme` hoặc class của root; xử lý thay đổi `matchMedia` ở chế độ system và cleanup listener.
- Persist lựa chọn khi sản phẩm yêu cầu. Đọc storage có thể thất bại; có fallback, không crash giao diện.
- Với SSR, tránh truy cập window/storage trong server render và tránh theme flash/hydration mismatch. Client này hiện là Vite SPA; không áp giải pháp SSR nếu không cần.

```css
/* Các role light/dark được generate/khai báo đầy đủ trong file theme. */
:root { color-scheme: light; }
:root[data-theme='dark'] { color-scheme: dark; }
@media (prefers-color-scheme: dark) {
  :root:not([data-theme='light']):not([data-theme='dark']) {
    color-scheme: dark;
    /* Khai báo bộ role dark tại đây hoặc dùng file generate tương ứng. */
  }
}
```

`color-scheme` giúp browser controls theo theme; nó không tự sinh token M3. Đừng coi đoạn CSS này là theme hoàn chỉnh.

## Kiểm tra

- Đo text/non-text contrast trên nền thực tế; thử state layers, disabled và overlay theo điều kiện tương ứng.
- Kiểm tra tất cả role tiêu thụ đã được định nghĩa ở ancestor.
- Kiểm tra lựa chọn theme sau reload, chuyển system theme và route navigation nếu có toggle.
- Không dùng mode/contrast dropdown chỉ để thỏa checklist nếu sản phẩm không cần; accessibility vẫn phải đạt ở scheme được cung cấp.
- Dynamic color lấy từ wallpaper Android không phải API web mặc định; dùng seed/content được người dùng cho phép khi sản phẩm có nhu cầu.
