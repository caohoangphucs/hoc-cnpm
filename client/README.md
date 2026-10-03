# Client — KTX Smart Hub

Thư mục giao diện chính của KTX Smart Hub, lựa chọn **React + TypeScript + Vite** và quản lý dependencies bằng **pnpm**.

## Trạng thái hiện tại

Đã khởi tạo React + TypeScript bằng template Vite `react-ts`, cấu hình React Router, ESLint, alias `@/`, biến môi trường, proxy dev và hàm gọi API dùng chung. Đã có khung ứng dụng theo 5 vai trò, điều hướng thích ứng và theme theo Material Design 3. Giao diện xác thực, quản lý tài khoản, đăng ký phòng UC-04, duyệt hồ sơ UC-12, phát hành hóa đơn UC-17 và thanh toán UC-08 đã có; các màn hình nghiệp vụ khác hiện là khung nội dung.

### Khung giao diện và điều hướng

- `/` đọc phiên và chuyển đến đăng nhập, 2FA hoặc không gian đúng vai trò. Các không gian: `/student`, `/admin`, `/accountant`, `/technician`, `/security`.
- Cấu hình menu tập trung tại `src/app/navigation.ts`, theo chức năng trong Phần II của `TODO.md`. Route và thẻ truy cập nhanh dùng cùng cấu hình này.
- `AppShell` giữ bố cục; các route con render qua `Outlet`. `FeatureFrame` là vùng chờ để thay bằng trang nghiệp vụ ở những nhánh tiếp theo.
- Trên 960px: drawer cố định rộng 280px. Từ 960px trở xuống: mở drawer modal bằng nút menu trên app bar. Mô hình này bám theo mẫu Admin/Sinh viên người dùng cung cấp.
- Có active state/`aria-current`, skip link, focus vào nội dung khi chuyển trang, đóng drawer bằng Escape và phục hồi focus. Theme khởi đầu theo hệ điều hành; nút sáng/tối trên thanh trên lưu lựa chọn vào trình duyệt; tôn trọng reduced motion.
- Các route theo vai trò được bảo vệ bằng session do backend trả về. ADMIN/KETOAN phải hoàn tất 2FA; truy cập không gian khác chuyển về không gian được cấp quyền. Guard phía client phục vụ điều hướng; backend vẫn phải kiểm quyền cho từng API.
- Theme nằm tại `src/theme/tokens.css`, `global.css`, `layout.css`, `theme.ts`; component dùng chung gồm `Icon`, `PageHeader`, `ThemeToggle`. Palette xanh dương `#00658f`, nền xám lạnh, menu pill và viền/bóng card dựa trên hai mẫu HTML/CSS Admin/Sinh viên do người dùng cung cấp. Google Sans/Roboto tải từ Google Fonts theo link trong mẫu Admin; có fallback hệ thống khi không tải được font. Các SVG filled dùng lại path từ mẫu cho những biểu tượng tương ứng. Chưa thêm thư viện UI hoặc dependencies mới.

Đã kiểm tra khung layout trên dev và production preview: route theo 5 vai trò, responsive từ 320 đến 1600px, active state, drawer/Tab/Escape/focus, trang 404, theme sáng/tối và reduced motion. Lint, typecheck, build đều đạt. Sau khi chỉnh theo mẫu, đối chiếu lại font, sidebar, app bar và card ở desktop/mobile. Đây là kiểm tra khung giao diện, chưa kiểm thử API hoặc xác thực.

Backend mới nằm trong server/, dùng Spring Boot/JAR và PostgreSQL. Đã có controller session/login/logout và POST /api/v1/bookings; các API tra cứu phòng/giường, đợt đăng ký và upload hồ sơ chưa hoàn chỉnh. Nginx proxy cùng origin và /api/health kiểm readiness thật.

### Giao diện xác thực

- `/login`: tên đăng nhập/mật khẩu, hiện/ẩn mật khẩu, lỗi tại trường, trạng thái đang gửi và lỗi dịch vụ.
- `/login/2fa`: một trường mã 6 chữ số hỗ trợ dán mã/autofill, xác minh và dùng tài khoản khác qua logout. Không tạo nút gửi lại mã vì contract chưa có endpoint đó.
- `/login/help`: hướng dẫn liên hệ Ban quản lý khi quên mật khẩu hoặc mất mã. Chưa triển khai OTP email/reset token của UC-16 vì chưa có contract API khôi phục.
- `AuthProvider` khôi phục session khi mở trang, đọc lại khi quay lại tab, đến hạn phiên hoặc API nghiệp vụ trả 401. Khi chưa xác định được phiên, nội dung được bảo vệ không render; người dùng có thể thử kết nối lại.
- Gọi `GET /api/v1/auth/session` để lấy CSRF mới trước `POST /auth/login`, `/auth/2fa`, `/auth/logout`. Gửi cookie qua `credentials: include`; token CSRF/role chỉ giữ trong bộ nhớ, mật khẩu/mã không lưu vào browser storage. Theme sáng/tối là lựa chọn duy nhất lưu ở localStorage.
- Giữ đường dẫn đích trong router state để trở lại sau đăng nhập/2FA, chỉ chấp nhận đường dẫn trong không gian của vai trò server trả về.
- Style dùng chung Google Sans/Roboto, palette xanh dương, token surface/shape của layout; bổ sung token error cho light/dark. Nginx CSP cho phép đúng hai origin Google Fonts cần cho font của mẫu.

Backend đã expose session/login/logout theo contract trong `docs/demo/api/`. Xác minh 2FA thật chưa hoàn chỉnh vì chưa có TOTP secret được đăng ký. Client chưa kiểm thử tích hợp với backend mới trong nhánh giao diện này. Không có tài khoản giả hay tùy chọn bỏ qua guard trong sản phẩm. Kiểm thử luồng giao diện dùng response mô phỏng trong trình duyệt; không thay cho kiểm thử tích hợp backend.

Đã kiểm tra login/2FA/logout cho 5 vai trò, CSRF, guard, lỗi HTTP/kết nối, session sai định dạng, phiên hết hạn, chống submit trùng, responsive light/dark từ 320 đến 1440px. `pnpm test` kiểm tra role/session/đường dẫn đích và health; dùng native TypeScript stripping của Node với cờ `--experimental-strip-types`, không thêm test runner hoặc browser dependency vào client.

### Giao diện tài khoản và vai trò

- `/admin/accounts`: danh sách tài khoản của Ban quản lý theo REQ-014 / UC-01, vẫn được bảo vệ bằng session và 2FA. Chưa có endpoint quản lý tài khoản nên trang hiển thị trạng thái chưa thể tải dữ liệu.
- Đã có tìm kiếm tên/tên đăng nhập/email (hỗ trợ tìm không dấu), lọc vai trò/trạng thái, sắp xếp và phân trang; panel chi tiết, dialog chọn một trong 5 vai trò và xác nhận khóa/mở tài khoản.
- Chạy `pnpm dev`, mở `/preview/admin/accounts` để thử giao diện bằng dữ liệu minh họa. Thay đổi chỉ giữ trong bộ nhớ; tải lại trang hoặc khôi phục dữ liệu mẫu sẽ đặt lại. Có lựa chọn xem trạng thái đang tải, danh sách trống và lỗi.
- Route xem trước và dữ liệu minh họa chỉ có trong Vite dev, không nằm trong bản build production. Không tạo session giả hoặc bỏ guard của `/admin/accounts`; chưa gửi request thay đổi tài khoản.
- Dùng chung theme, component và style xanh dương của layout. Dialog hỗ trợ Tab/Escape và phục hồi focus; bảng cuộn ngang trong vùng riêng trên màn hình nhỏ.

### Giao diện đăng ký phòng — UC-04

- Ưu tiên phạm vi demo trong `TODO.md`, theo [thiết kế UC-04](../docs/demo/THIET_KE_XU_LY.md) và BR-03/04. `/student/stay` dùng guard sinh viên hiện có; khi chưa có API tra cứu/đợt đăng ký, trang hiển thị trạng thái chưa thể tải dữ liệu.
- Một trang, ba bước: tra cứu/chọn giường → ảnh hồ sơ/cam kết → kiểm tra và gửi. Có tìm kiếm phòng/tòa, lọc tòa/loại phòng/phòng còn giường trống; sơ đồ giường thể hiện trống, giữ chỗ, đang ở và khóa. Chặn chọn giường không phù hợp giới tính, đợt đóng, đang có đơn chờ duyệt hoặc lưu trú.
- Desktop hiển thị danh sách và panel giường cạnh nhau. Dưới 1200px, chọn phòng chuyển sang panel giường với nút quay lại danh sách; các bước form giữ phần tóm tắt chỗ ở đã chọn.
- Hồ sơ gồm một ảnh CCCD, một ảnh thẻ sinh viên và tối đa ba ảnh minh chứng tùy chọn. Mỗi ảnh JPG/PNG tối đa 5 MB; kiểm loại, dung lượng, chữ ký tệp và khả năng đọc ảnh trên client. Có xem/bỏ/thay ảnh và lỗi tại trường. Kiểm tra client không thay việc kiểm nội dung, ownership, quét và bảo vệ tệp ở backend.
- Link và Back trong ứng dụng hỏi trước khi bỏ bản nháp; tải lại/đóng tab dùng cảnh báo của trình duyệt. Dùng `createBrowserRouter`/`RouterProvider` để hỗ trợ `useBlocker`; URL, session và guard giữ cùng cấu trúc. Hồ sơ chỉ giữ trong bộ nhớ, không lưu browser storage.
- Chạy `pnpm dev`, mở `/preview/student/stay` để thử bằng dữ liệu minh họa. Có đợt mở/đóng, giới tính hồ sơ, đơn đang chờ/lưu trú, loading/empty/error và kết quả gửi thành công/xung đột/lỗi dịch vụ/lỗi hồ sơ. Khi xung đột, chọn lại giường và giữ hồ sơ; thành công minh họa hiển thị mã đơn, chờ duyệt và hạn 48 giờ theo giờ Việt Nam.
- Preview và fixture chỉ có ở dev. Không upload ảnh hoặc gửi đăng ký thật; chưa nối API phòng/giường, upload, đợt đăng ký, hồ sơ sinh viên và đơn đăng ký. Các trường tên phòng/tòa/thông tin sinh viên cần được ánh xạ khi API tra cứu hoàn chỉnh. Chưa triển khai đổi phòng UC-07 trong nhánh này.

### Giao diện phát hành hóa đơn — UC-17

- `/accountant/invoices` giữ guard session/2FA của Kế toán. Chưa nối API hóa đơn, chỉ số, đơn giá và lưu trú nên trang thật hiển thị trạng thái chưa thể tải dữ liệu.
- Chạy `pnpm dev`, mở `/preview/accountant/invoices`: chọn kỳ/phòng, tìm/lọc tòa, kiểm tra chỉ số và chi phí; thêm khoản thu nguyên đồng với căn cứ; tạo nháp có phiên bản, tải Excel `.xlsx` và xác nhận phát hành. Dữ liệu minh họa có ở kỳ `2026-10`; đổi kỳ không dùng lại chỉ số hoặc đơn giá cũ.
- Theo BR-06/07/08 của phạm vi demo: điện nước bằng mức sử dụng × đơn giá, không lũy tiến; chia tiền phòng theo lưu trú đầu kỳ, số dư theo MSSV tăng dần; giảm cao nhất đã xác định cho từng sinh viên, chỉ giảm tiền phòng và làm tròn xuống. Tính bằng số nguyên/BigInt, kiểm giới hạn tiền.
- Phòng thiếu/sai dữ liệu hoặc đã phát hành bị chặn; có thể chọn bỏ qua phòng lỗi. Thay đổi kỳ/phòng/chi phí làm mất hiệu lực nháp; xung đột khi phát hành yêu cầu tạo lại nháp, lỗi dịch vụ giữ nháp để thử lại. Kết quả hiển thị riêng từng phòng, hạn trả bằng ngày phát hành tại Việt Nam + 15 ngày; có mô phỏng lỗi một phòng để thử lại và chống phát hành trùng.
- Preview/fixture chỉ có ở dev; thao tác trong bộ nhớ, tải lại sẽ đặt lại. Excel đánh dấu nháp minh họa, chưa phát hành. Chưa lưu hóa đơn, gửi thông báo, tạo QR thanh toán hoặc thay đổi backend; nhập chỉ số/đơn giá thuộc UC khác.

### Giao diện thanh toán hóa đơn — UC-08

- `/student/invoices` giữ guard Sinh viên. API hóa đơn/phiên thanh toán/biên lai chưa được nối; trang thật hiển thị trạng thái chưa thể tải dữ liệu. `/preview/student/invoices` chỉ có ở Vite dev, với hóa đơn minh họa của phòng lưu trú và thao tác trong bộ nhớ.
- Một trang gồm danh sách hóa đơn, tìm/lọc kỳ/trạng thái, chi tiết khoản thu, chọn một hoặc nhiều hóa đơn và khung thanh toán. Có VNPAY/MoMo theo `PaymentDto`, không nhập số tiền tùy ý: lấy toàn bộ dư nợ, lưu phân bổ cố định, kiểm VND nguyên đồng/giới hạn tổng tiền. Desktop có khung thanh toán cạnh danh sách; màn hình nhỏ chuyển thành một cột với liên kết đến phần thanh toán.
- Xác nhận tạo phiên có hạn 15 phút, đếm ngược từng giây và chặn tạo thêm khi đang chờ hoặc cần đối soát. Retry lỗi tạo phiên giữ cùng request key; tạo phiên mới sau kết quả cuối dùng key mới. Quay về từ cổng hoặc đóng cửa sổ không gạch nợ; chỉ phản hồi thành công đã xác nhận mới cập nhật, kiểm tra lặp không cộng tiền lần hai.
- Xem trước hỗ trợ pending/success/failed/canceled, sai tiền/kết quả không hợp lệ, hết hạn và thành công đến muộn. Thất bại/hủy/hết hạn giữ dư nợ; tiền đến muộn hoặc dữ liệu không khớp chuyển sang đối soát, chưa cho thanh toán lại. Có lịch sử phiên và biên lai minh họa sau thành công; nút in dùng chế độ in của trình duyệt, chỉ in nội dung biên lai có dấu minh họa.
- Chưa redirect đến cổng thật, tạo QR, xác minh IPN/chữ ký ở browser, tự polling API, thu tiền hoặc cấp biên lai chính thức. Quyền sở hữu, số tiền, chữ ký và kết quả thanh toán thật phải do backend xác minh theo BR-09/10; fixture xem trước không nằm trong build production. Chưa triển khai tải PDF hóa đơn UC-02 trong nhánh UC-08.

## 1. Công nghệ và môi trường

Giao diện UC-12 đã có tại `/admin/bookings`: tìm/lọc/sắp ưu tiên, xem hồ sơ/minh chứng, kiểm tra hồ sơ/hạn giữ chỗ/giường, duyệt/từ chối có lý do và duyệt hàng loạt. `/preview/admin/bookings` dùng fixture, trạng thái lỗi/loading/empty và kết quả mô phỏng; chưa gọi API hoặc tạo lưu trú/hợp đồng thật. Preview của các vai trò dùng chung `/preview/:workspace/*`; các route thật giữ session/2FA guard.

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

client/Dockerfile dùng pnpm theo version/lockfile của dự án, chạy lint/test/typecheck/build và đóng gói SPA bằng Nginx. Root Compose phục vụ frontend port8091 và backend8080 nội bộ; không đổi API_BASE_URL sang một domain khác khi deploy. Trang tổng quan8UC là scaffold, chưa có màn hình nghiệp vụ. Xem [CI/CD](../docs/main/CI_CD.md).
