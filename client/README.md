# Client — KTX Smart Hub

Thư mục giao diện chính của KTX Smart Hub, lựa chọn **React + TypeScript + Vite** và quản lý dependencies bằng **pnpm**.

## Trạng thái hiện tại

Client dùng React + TypeScript + Vite, pnpm, React Router và theme Material Design 3 theo 5 vai trò. Các màn hình phòng/giường, hồ sơ đăng ký, hóa đơn/thanh toán, sửa chữa, cổng/khách thăm, vi phạm và công ích đã sử dụng API thật trong [API.md](../docs/main/API.md). Phần cần API hoặc dịch vụ chưa có được ghi rõ bên dưới. Các route thật giữ session guard; chỉ quản lý tài khoản còn preview do chưa có endpoint.

### Khung giao diện và điều hướng

- `/` đọc phiên và chuyển đến đăng nhập, 2FA hoặc không gian đúng vai trò. Các không gian: `/student`, `/admin`, `/accountant`, `/technician`, `/security`.
- Cấu hình menu tập trung tại `src/app/navigation.ts`, theo chức năng trong Phần II của `TODO.md`. Route và thẻ truy cập nhanh dùng cùng cấu hình này.
- `AppShell` giữ bố cục; các route con render qua `Outlet`. `FeatureFrame` là vùng chờ để thay bằng trang nghiệp vụ ở những nhánh tiếp theo.
- Trên 960px: drawer cố định rộng 280px. Từ 960px trở xuống: mở drawer modal bằng nút menu trên app bar. Mô hình này bám theo mẫu Admin/Sinh viên người dùng cung cấp.
- Có active state/`aria-current`, skip link, focus vào nội dung khi chuyển trang, đóng drawer bằng Escape và phục hồi focus. Theme khởi đầu theo hệ điều hành; nút sáng/tối trên thanh trên lưu lựa chọn vào trình duyệt; tôn trọng reduced motion.
- Các route theo vai trò được bảo vệ bằng session do backend trả về. ADMIN/KETOAN phải hoàn tất 2FA; truy cập không gian khác chuyển về không gian được cấp quyền. Guard phía client phục vụ điều hướng; backend vẫn phải kiểm quyền cho từng API.
- Theme nằm tại `src/theme/tokens.css`, `global.css`, `layout.css`, `theme.ts`; component dùng chung gồm `Icon`, `PageHeader`, `ThemeToggle`. Palette xanh dương `#00658f`, nền xám lạnh, menu pill và viền/bóng card dựa trên hai mẫu HTML/CSS Admin/Sinh viên do người dùng cung cấp. Google Sans/Roboto tải từ Google Fonts theo link trong mẫu Admin; có fallback hệ thống khi không tải được font. Các SVG filled dùng lại path từ mẫu cho những biểu tượng tương ứng. Chưa thêm thư viện UI hoặc dependencies mới.

Đã kiểm tra khung layout trên dev và production preview: route theo 5 vai trò, responsive từ 320 đến 1600px, active state, drawer/Tab/Escape/focus, trang 404, theme sáng/tối và reduced motion. Lint, typecheck, build đều đạt. Sau khi chỉnh theo mẫu, đối chiếu lại font, sidebar, app bar và card ở desktop/mobile. Đây là kiểm tra khung giao diện, chưa kiểm thử API hoặc xác thực.

Backend nằm trong `server/`, dùng Spring Boot/JAR và PostgreSQL. Danh sách endpoint hiện tại nằm trong [API.md](../docs/main/API.md). Nginx proxy cùng origin và `/api/health` kiểm readiness thật.

### Giao diện xác thực

- `/login`: tên đăng nhập/mật khẩu, hiện/ẩn mật khẩu, lỗi tại trường, trạng thái đang gửi và lỗi dịch vụ.
- `/login/2fa`: một trường mã 6 chữ số hỗ trợ dán mã/autofill, xác minh và dùng tài khoản khác qua logout. Không tạo nút gửi lại mã vì contract chưa có endpoint đó.
- `/login/help`: hướng dẫn liên hệ Ban quản lý khi quên mật khẩu hoặc mất mã. Chưa triển khai OTP email/reset token của UC-16 vì chưa có contract API khôi phục.
- `AuthProvider` khôi phục session khi mở trang, đọc lại khi quay lại tab, đến hạn phiên hoặc API nghiệp vụ trả 401. Khi chưa xác định được phiên, nội dung được bảo vệ không render; người dùng có thể thử kết nối lại.
- Gọi `GET /api/v1/auth/session` để lấy CSRF mới trước `POST /auth/login`, `/auth/2fa`, `/auth/logout`. Gửi cookie qua `credentials: include`; token CSRF/role chỉ giữ trong bộ nhớ, mật khẩu/mã không lưu vào browser storage. Theme sáng/tối là lựa chọn duy nhất lưu ở localStorage.
- Giữ đường dẫn đích trong router state để trở lại sau đăng nhập/2FA, chỉ chấp nhận đường dẫn trong không gian của vai trò server trả về.
- Style dùng chung Google Sans/Roboto, palette xanh dương, token surface/shape của layout; bổ sung token error cho light/dark. Nginx CSP cho phép đúng hai origin Google Fonts cần cho font của mẫu.

Backend expose session/login/logout và các route TOTP. Client dùng trạng thái `twoFactorVerified` thực tế từ session; có hộp thoại thiết lập TOTP cho ADMIN/KETOAN qua nút trên app bar: bắt đầu, nhập khóa vào ứng dụng xác thực rồi xác nhận mã. Secret/URI chỉ giữ trong bộ nhớ; đóng hộp thoại sẽ xóa khỏi giao diện. Đã kiểm tra đăng nhập và điều hướng theo vai trò với backend local. Không có tài khoản giả hoặc tùy chọn bỏ qua guard trong sản phẩm.

Đã kiểm tra login/2FA/logout cho 5 vai trò, CSRF, guard, lỗi HTTP/kết nối, session sai định dạng, phiên hết hạn, chống submit trùng, responsive light/dark từ 320 đến 1440px. `pnpm test` kiểm tra role/session/đường dẫn đích và health; dùng native TypeScript stripping của Node với cờ `--experimental-strip-types`, không thêm test runner hoặc browser dependency vào client.

### Giao diện tài khoản và vai trò

- `/admin/accounts`: danh sách tài khoản của Ban quản lý theo REQ-014 / UC-01, vẫn được bảo vệ bằng session và 2FA. Chưa có endpoint quản lý tài khoản nên trang hiển thị trạng thái chưa thể tải dữ liệu.
- Đã có tìm kiếm tên/tên đăng nhập/email (hỗ trợ tìm không dấu), lọc vai trò/trạng thái, sắp xếp và phân trang; panel chi tiết, dialog chọn một trong 5 vai trò và xác nhận khóa/mở tài khoản.
- Chạy `pnpm dev`, mở `/preview/admin/accounts` để thử giao diện bằng dữ liệu minh họa. Thay đổi chỉ giữ trong bộ nhớ; tải lại trang hoặc khôi phục dữ liệu mẫu sẽ đặt lại. Có lựa chọn xem trạng thái đang tải, danh sách trống và lỗi.
- Route xem trước và dữ liệu minh họa chỉ có trong Vite dev, không nằm trong bản build production. Không tạo session giả hoặc bỏ guard của `/admin/accounts`; chưa gửi request thay đổi tài khoản.
- Dùng chung theme, component và style xanh dương của layout. Dialog hỗ trợ Tab/Escape và phục hồi focus; bảng cuộn ngang trong vùng riêng trên màn hình nhỏ.

### Các màn hình dùng API thật

| Màn hình | Phạm vi đã nối |
|---|---|
| `/admin/rooms`, `/student/stay` | Danh sách/chi tiết phòng, danh sách giường và phân trang. ADMIN thêm/sửa/xóa phòng và giường. Trường cấu hình API đọc chưa trả về cần được nhập và đối chiếu khi sửa; không suy đoán từ tên hiển thị. |
| `/student/stay`, `/admin/bookings` — UC-04/12 | Tra cứu hồ sơ/danh sách, trạng thái minh chứng và hạn giữ chỗ; hủy hồ sơ đang chờ. ADMIN duyệt/từ chối có lý do và version, yêu cầu đối chiếu hồ sơ gốc khi duyệt. |
| `/accountant/invoices`, `/admin/invoices` — UC-17 | Tra cứu/chi tiết hóa đơn, đổi hạn và xóa hóa đơn theo điều kiện backend. Kế toán tạo bản nháp từ tháng/mã phòng, kiểm tra số liệu/cảnh báo và phát hành theo draft ID/version. Tiền do server tính. |
| `/student/invoices`, `/accountant/payments` — UC-08 | Hóa đơn của sinh viên, tạo phiên VNPAY/MoMo, lịch sử/chi tiết phiên, kiểm tra trạng thái và hủy phiên chưa xử lý. Chỉ mở URL HTTPS do server trả về; quay về từ cổng không tự ghi nhận đã trả tiền. Kế toán tra cứu phiên theo quyền. |
| `/student/repairs`, `/technician/repairs`, `/admin/repairs` — UC-22 | Báo sự cố, danh sách/chi tiết, hủy phiếu chưa phân công; ADMIN giao việc bằng mã tài khoản kỹ thuật viên thật. Kỹ thuật viên đổi lịch/hoàn thành với version và vật tư thực tế. Thiếu tồn kho giữ trạng thái đang xử lý. |
| `/security/gate`, `/student/entries`, `/admin/entries` — UC-25 | Ghi lượt bằng mã thẻ/thiết bị và UUID, dùng timestamp/vi phạm/barrier của server; tra cứu nhật ký/chi tiết và phân trang. PENDING không có nghĩa cổng đã mở; nhật ký không có thao tác sửa/xóa. |
| `/student/visitors`, `/security/visitors`, `/admin/visitors` | Khai báo, tra cứu/chi tiết/hủy khai báo chờ duyệt, ADMIN xét duyệt bằng version. Bảo vệ xem phiếu đã duyệt và ghi vào/ra sau khi đối chiếu giấy tờ gốc. API không trả họ tên/giấy tờ đã giải mã. |
| `/admin/discipline` — UC-14 | Danh sách tất cả/chờ xử lý, chi tiết và lịch sử quyết định, thêm/sửa/xóa hồ sơ thủ công theo version/điều kiện backend. |
| `/student/community`, `/admin/community` — UC-30 | Danh sách hoạt động; ADMIN mở/sửa/xóa trước khi có lịch sử đăng ký, điểm danh và xác nhận hoàn thành. Sinh viên đăng ký/hủy, đọc điểm, quy đổi và yêu cầu giấy xác nhận. |

Danh sách có `total` (phòng, giường, hồ sơ, hóa đơn, nhật ký cổng) dùng tổng do server trả về. Các endpoint trả mảng (sửa chữa, khách thăm, vi phạm, hoạt động, phiên thanh toán) phân trang theo kích thước 8; có thể cần qua một trang trống để biết đã hết. Tìm/lọc tại client chỉ áp dụng trên trang đang tải và được ghi nhãn rõ.

### Các điều kiện còn thiếu

- Gửi đăng ký phòng cần API tra cứu đợt đăng ký/hồ sơ sinh viên và upload tệp để có window ID/file UUID thật. Chức năng gửi đang bị khóa; tra cứu phòng và hồ sơ vẫn dùng được.
- Duyệt hồ sơ chưa xem được nội dung tệp trực tuyến: chưa có API tải/tra cứu tệp. Đối chiếu hồ sơ gốc trước khi duyệt, không dựng ảnh minh chứng.
- Ra quyết định kỷ luật cần minh chứng đã lưu, nhưng chưa có upload/tra cứu tệp. Khiếu nại/trả lời đã có endpoint ghi, chưa có endpoint đọc quyết định của sinh viên hoặc danh sách/chi tiết khiếu nại nên chưa mở luồng này.
- VNPAY/MoMo cần adapter/cấu hình backend. Lỗi 503 được báo và giữ hóa đơn; callback do nhà cung cấp gửi về backend, client không gọi hoặc mô phỏng callback.
- Giấy xác nhận công ích cần dịch vụ ký và API tải/theo dõi PDF; hiện chỉ hiển thị tiếp nhận yêu cầu nếu server xác nhận.
- Không có API đọc danh mục tòa/loại phòng, danh sách kỹ thuật viên, kho, lịch hẹn/mô tả/lịch sử sửa chữa đầy đủ. Form quản lý dùng mã thực do đơn vị quản lý cung cấp; không thêm danh mục giả.
- Công ích chưa có GET toàn bộ đăng ký/danh sách người tham gia; các phiếu QR và kết quả điểm danh vừa nhận chỉ giữ trong phiên, được ghi nhãn rõ. Hủy đăng ký dùng `DELETE /activities/{registrationId}/registrations` với version: ID là **mã đăng ký**, theo service thực tế.
- Quản lý tài khoản, khôi phục mật khẩu, đơn giá, nhập chỉ số, thu tiền mặt và báo cáo chưa có endpoint tương ứng trong tài liệu hiện tại.

### Preview và kiểm tra

Preview/fixture đăng ký, duyệt hồ sơ, hóa đơn, thanh toán, sửa chữa, cổng, khách thăm, vi phạm và công ích đã bỏ. Các URL preview cũ chuyển tới route thật và yêu cầu đăng nhập. Chỉ `/preview/admin/accounts` còn dữ liệu minh họa.

- `src/shared/api/client.ts`: base URL, cookie, CSRF. `hooks.ts`: tải/hủy request, loading/error/retry, khóa submit và đọc CSRF mới trước mutation. `protocol.ts`: giải mã DTO/phân trang, lỗi an toàn và request identity.
- Mutation gửi `Idempotency-Key` và `X-Request-Id` ổn định theo method/path/payload để retry. Chỉ báo thành công khi server xác nhận; version/ownership/điều kiện nghiệp vụ vẫn do backend kiểm tra.
- Test engine demo đã được thay bằng test DTO/payload, phân trang, version, timezone, URL thanh toán và lỗi API. Giữ test xác thực/health và các test cần thiết; không xóa test backend.
- Kiểm tra bằng `pnpm run lint`, `pnpm run test`, `pnpm run typecheck`, `pnpm run build`. Test contract không thay thế kiểm thử tích hợp các mutation trên môi trường QA.
- Kiểm tra đọc API local ngày 2026-10-03: 29 kiểm tra danh sách/chi tiết/DTO/quyền đọc đạt. Sau khi đồng bộ bản sửa backend `37208ce` từ develop và build lại server, cả 5 GET từng trả 500 đều trả 200 qua proxy Vite: ADMIN đọc bookings/visitors/entries; BAOVE đọc visitors/entries. Dữ liệu local hiện trống; đã kiểm tra cấu trúc danh sách và tổng phân trang khi API cung cấp, chưa kiểm tra lại với danh sách có dữ liệu. Bản sửa backend ép kiểu bộ lọc NULL trong BookingRepository/GateRepository; client không cần đổi cách gọi API.
- Trạng thái hồ sơ hợp lệ để duyệt là `VALID` theo RegistrationService thực tế; ví dụ `COMPLETE` trong docs không đủ để mở nút duyệt.

## 1. Công nghệ và môi trường

| Thành phần | Lựa chọn |
|---|---|
| Giao diện | React |
| Ngôn ngữ | TypeScript |
| Chạy dev và build | Vite, template `react-ts` |
| Quản lý dependencies | pnpm `11.19.0`, ghi trong `packageManager`; commit `pnpm-lock.yaml` |
| Điều hướng | React Router, `createBrowserRouter` + `RouterProvider` |
| Kiểm tra code | ESLint, TypeScript strict |
| Môi trường | Node.js `^22.12.0` hoặc `>=24.0.0`, theo `engines` |

Theo [hướng dẫn Vite](https://vite.dev/guide/), yêu cầu hiện tại là Node.js 20.19+ hoặc 22.12+. Chọn bản LTS còn được hỗ trợ tại [Node.js](https://nodejs.org/en/download); kiểm tra thêm yêu cầu của template khi setup.

Kiểm tra môi trường trong Bash:

```bash
node --version
pnpm --version
```

## 2. Cài đặt dependencies

Chạy từ thư mục gốc repository:

```bash
cd client
pnpm install --frozen-lockfile
```

Ở lần khởi tạo đầu tiên, dùng `pnpm install` để cài dependencies và tạo lockfile. Các lần lấy code về sau dùng `pnpm install --frozen-lockfile` để cài theo lockfile, không cập nhật file này. Lệnh sẽ báo lỗi nếu lockfile thiếu hoặc không khớp `package.json`.

Chạy các lệnh pnpm của client trong `client/`. Dùng pnpm đã có trên máy; không cần cài lại. Tham khảo [cài dependencies bằng pnpm](https://pnpm.io/cli/install).

## 3. Cấu hình môi trường

`.env.example` chứa cấu hình mẫu công khai. Mỗi máy tạo `.env.local` từ file mẫu, chạy trong `client/`:

```bash
cp .env.example .env.local
```

Các biến đã được sử dụng trong code/cấu hình Vite:

| Biến | Giá trị mẫu | Mục đích |
|---|---|---|
| `VITE_API_BASE_URL` | `/api/v1` | Prefix được `src/shared/api/client.ts` sử dụng |
| `API_PROXY_TARGET` | `http://localhost:8080` | Backend local cho proxy dev, được `vite.config.ts` đọc |

`8091` là cổng ứng dụng legacy trong Docker Compose hiện tại. Khi backend Spring Boot được triển khai, dùng đúng địa chỉ backend đang chạy; không mặc định API `/api/v1` đã tồn tại trên ứng dụng legacy.

Biến có tiền tố `VITE_` có thể được đưa vào mã chạy trên trình duyệt. Không đặt mật khẩu database, khóa VNPAY hoặc secret backend trong cấu hình client. Đổi file môi trường cần khởi động lại dev server.

Tham khảo [biến môi trường của Vite](https://vite.dev/guide/env-and-mode.html).

## 4. Chạy local và kết nối backend

Chạy trong `client/`:

```bash
pnpm run dev
```

Mở địa chỉ được Vite in trong terminal. Cổng mặc định thường là `5173`; dùng địa chỉ thực tế nếu cổng đã bận hoặc cấu hình được thay đổi.

Frontend có thể chạy riêng để phát triển giao diện. Các thao tác lấy/gửi dữ liệu thực cần backend đang chạy và có endpoint tương ứng.

Proxy dev đã được cấu hình:

- Chuyển request `/api/v1` đến backend local, giữ nguyên đường dẫn nếu backend dùng cùng prefix.
- Gọi API bằng đường dẫn tương đối trên cùng origin của frontend.
- `apiFetch(path, options)` tại `src/shared/api/client.ts` gửi cookie session và nhận `csrfToken` qua options để đặt header. Hàm trả `Response`; nơi gọi kiểm tra `response.ok` và xử lý body/lỗi theo endpoint.
- Theo contract hiện tại, lấy token ban đầu qua `GET /api/v1/auth/session`; request ghi dữ liệu và login gửi `X-CSRF-TOKEN`.
- Proxy dev chỉ áp dụng khi chạy Vite dev server; môi trường triển khai cần cấu hình phục vụ frontend và chuyển tiếp API riêng.

Chi tiết session, 2FA, CSRF và quyền truy cập nằm trong [API và phân quyền](../docs/demo/API_VA_PHAN_QUYEN.md); endpoint được liệt kê tại [danh mục API](../docs/demo/api/README.md).

Luồng session, CSRF, login, 2FA, logout và bảo vệ route đã có ở frontend; chờ endpoint backend để kiểm thử tích hợp thật. Khi deploy SPA, máy chủ phục vụ frontend cần fallback các route giao diện về `index.html`, đồng thời giữ `/api/v1` đi tới backend.

## 5. Lint, kiểm tra kiểu, build và preview

Các lệnh dưới đây đã có trong `package.json`, chạy trong `client/`:

| Lệnh | Mục đích |
|---|---|
| `pnpm run dev` | Chạy Vite dev server |
| `pnpm run lint` | ESLint; warning cũng làm lệnh thất bại |
| `pnpm run typecheck` | `tsc -b --noEmit`, kiểm tra TypeScript |
| `pnpm test` | Kiểm thử session, vai trò, đường dẫn đích, health và đầu vào UC-04 bằng Node test runner |
| `pnpm run build` | `tsc -b && vite build` |
| `pnpm run preview` | Xem bản build local |

Sau setup, kiểm tra trước khi gửi PR:

```bash
pnpm run lint
pnpm run typecheck
pnpm run build
```

Sau khi build thành công:

```bash
pnpm run preview
```

Vite mặc định tạo bản build trong `dist/`. Preview dùng để kiểm tra bản build local; không phải bước deploy và không tự kế thừa proxy dev.

Đã kiểm tra setup trên Node.js `22.18.0`, pnpm `11.19.0`: lint, typecheck và build đều thành công. Vite dev server chạy được; đã mở trang `/`, trang 404 và kiểm tra liên kết về trang chủ trong trình duyệt. Chưa kiểm tra tích hợp API vì backend JSON `/api/v1` chưa được triển khai.

## 6. Cấu trúc thư mục

```text
client/
├── public/           # Tài nguyên được phục vụ trực tiếp
├── src/
│   ├── app/          # App, routes, navigation; layout/ và pages/
│   ├── features/     # auth/, accounts/, booking/ và các nhóm nghiệp vụ
│   ├── shared/       # api/client.ts; components, hooks, utils, types còn trống
│   ├── theme/        # tokens.css, global.css và layout.css
│   ├── assets/       # Chỗ dành cho tài nguyên import vào code
│   ├── main.tsx      # React root và RouterProvider
│   └── vite-env.d.ts # Kiểu biến môi trường
├── .env.example      # Cấu hình mẫu công khai
├── .gitignore
├── eslint.config.js
├── index.html
├── package.json
├── pnpm-lock.yaml
├── tsconfig.json
├── tsconfig.app.json
├── tsconfig.node.json
└── vite.config.ts
```

Alias `@/` trỏ tới `src/` trong cả Vite và TypeScript. Các thư mục chưa có code được giữ bằng `.gitkeep`. Cập nhật sơ đồ khi cấu trúc thực tế thay đổi.

## 7. Lỗi setup thường gặp

| Hiện tượng | Cách kiểm tra |
|---|---|
| Không tìm thấy `node` hoặc `pnpm` | Kiểm tra PATH của Node.js và pnpm đã có trên máy; mở lại terminal sau khi sửa PATH |
| Cảnh báo Node không tương thích | Đối chiếu phiên bản Node với yêu cầu Vite/template |
| pnpm báo thiếu `package.json` | Kiểm tra đã ở `client/` và project đã được khởi tạo |
| `pnpm install --frozen-lockfile` lỗi lockfile | Kiểm tra lockfile đã được commit và khớp `package.json`; sau khi đổi dependencies, cập nhật bằng `pnpm install` |
| `Missing script` | Kiểm tra tên script thực tế trong `package.json` |
| Cổng dev đã được sử dụng | Mở đúng URL Vite in ra; nếu cần chỉ định cổng bằng `pnpm run dev --port 5174` |
| API bị connection refused/404 | Kiểm tra backend, proxy target và sự tồn tại của endpoint `/api/v1` |
| Login/API trả 401 hoặc 403 | Kiểm tra cookie, token CSRF, phiên đăng nhập/2FA và quyền theo contract backend |
| Biến môi trường chưa đổi | Kiểm tra tên biến, cách đọc trong config/code và khởi động lại Vite |

## 8. Git và các file không commit

Theo [Git workflow của dự án](../docs/main/GIT_WORKFLOW.md): làm trên nhánh riêng, đặt tên `<loại>/<tên-bạn>-<chức-năng>` và tạo PR vào `develop`. Nhánh cho đợt setup này là `feat/dat-client-setup`.

Commit source, cấu hình dùng chung, `.env.example` và `pnpm-lock.yaml`. `client/.gitignore` đã bỏ qua các mục sau:

```gitignore
node_modules/
dist/
.env
.env.*
!.env.example
*.local
*.log
```

Không commit dependencies đã cài, bản build, cấu hình môi trường riêng hoặc secret.

## CI/CD và deploy

client/Dockerfile dùng pnpm theo version/lockfile của dự án, chạy lint/test/typecheck/build và đóng gói SPA bằng Nginx. Root Compose phục vụ frontend port8091 và backend8080 nội bộ; không đổi API_BASE_URL sang một domain khác khi deploy. Các phần nối API và phần còn chờ contract được liệt kê ở đầu tài liệu. Xem [CI/CD](../docs/main/CI_CD.md).
