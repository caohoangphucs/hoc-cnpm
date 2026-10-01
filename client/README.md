# Client — KTX Smart Hub

Thư mục giao diện chính của KTX Smart Hub, lựa chọn **React + TypeScript + Vite** và quản lý dependencies bằng **pnpm**.

## Trạng thái hiện tại

Đã khởi tạo React + TypeScript bằng template Vite `react-ts`, cấu hình React Router, ESLint, alias `@/`, biến môi trường, proxy dev và hàm gọi API dùng chung. Đã có khung ứng dụng theo 5 vai trò, điều hướng thích ứng và theme theo Material Design 3. Các màn hình nghiệp vụ hiện là khung nội dung, chưa gọi API hay thực hiện thao tác nghiệp vụ.

### Khung giao diện và điều hướng

- `/` chuyển đến `/student`. Các không gian: `/student`, `/admin`, `/accountant`, `/technician`, `/security`.
- Cấu hình menu tập trung tại `src/app/navigation.ts`, theo chức năng trong Phần II của `TODO.md`. Route và thẻ truy cập nhanh dùng cùng cấu hình này.
- `AppShell` giữ bố cục; các route con render qua `Outlet`. `FeatureFrame` là vùng chờ để thay bằng trang nghiệp vụ ở những nhánh tiếp theo.
- Trên 960px: drawer cố định rộng 280px. Từ 960px trở xuống: mở drawer modal bằng nút menu trên app bar. Mô hình này bám theo mẫu Admin/Sinh viên người dùng cung cấp.
- Có active state/`aria-current`, skip link, focus vào nội dung khi chuyển trang, đóng drawer bằng Escape và phục hồi focus. Theme khởi đầu theo hệ điều hành; nút sáng/tối trên thanh trên lưu lựa chọn vào trình duyệt; tôn trọng reduced motion.
- Kiểm tra từng vai trò qua các đường dẫn `/student`, `/admin`, `/accountant`, `/technician`, `/security`. Các route hiện chưa bảo vệ bằng session; bước auth sẽ lấy vai trò từ backend và thêm route guard.
- Theme nằm tại `src/theme/tokens.css`, `global.css`, `layout.css`, `theme.ts`; component dùng chung gồm `Icon`, `PageHeader`, `ThemeToggle`. Palette xanh dương `#00658f`, nền xám lạnh, menu pill và viền/bóng card dựa trên hai mẫu HTML/CSS Admin/Sinh viên do người dùng cung cấp. Google Sans/Roboto tải từ Google Fonts theo link trong mẫu Admin; có fallback hệ thống khi không tải được font. Các SVG filled dùng lại path từ mẫu cho những biểu tượng tương ứng. Chưa thêm thư viện UI hoặc dependencies mới.

Đã kiểm tra khung layout trên dev và production preview: route theo 5 vai trò, responsive từ 320 đến 1600px, active state, drawer/Tab/Escape/focus, trang 404, theme sáng/tối và reduced motion. Lint, typecheck, build đều đạt. Sau khi chỉnh theo mẫu, đối chiếu lại font, sidebar, app bar và card ở desktop/mobile. Đây là kiểm tra khung giao diện, chưa kiểm thử API hoặc xác thực.

Backend hiện tại vẫn là Servlet/JSP WAR. Thiết kế đích là Spring Boot + PostgreSQL, API JSON dưới `/api/v1`. Một số tài liệu backend còn mô tả Thymeleaf; lựa chọn giao diện của client là React.

## 1. Công nghệ và môi trường

| Thành phần | Lựa chọn |
|---|---|
| Giao diện | React |
| Ngôn ngữ | TypeScript |
| Chạy dev và build | Vite, template `react-ts` |
| Quản lý dependencies | pnpm `11.19.0`, ghi trong `packageManager`; commit `pnpm-lock.yaml` |
| Điều hướng | React Router, `BrowserRouter` |
| Kiểm tra code | ESLint, TypeScript strict |
| Môi trường | Node.js `^22.12.0` hoặc `>=24.0.0`, theo `engines` |

Theo [hướng dẫn Vite](https://vite.dev/guide/), yêu cầu hiện tại là Node.js 20.19+ hoặc 22.12+. Chọn bản LTS còn được hỗ trợ tại [Node.js](https://nodejs.org/en/download); kiểm tra thêm yêu cầu của template khi setup.

Kiểm tra môi trường trong PowerShell:

```powershell
node --version
pnpm --version
```

## 2. Cài đặt dependencies

Chạy từ thư mục gốc repository:

```powershell
cd client
pnpm install --frozen-lockfile
```

Ở lần khởi tạo đầu tiên, dùng `pnpm install` để cài dependencies và tạo lockfile. Các lần lấy code về sau dùng `pnpm install --frozen-lockfile` để cài theo lockfile, không cập nhật file này. Lệnh sẽ báo lỗi nếu lockfile thiếu hoặc không khớp `package.json`.

Chạy các lệnh pnpm của client trong `client/`. Dùng pnpm đã có trên máy; không cần cài lại. Tham khảo [cài dependencies bằng pnpm](https://pnpm.io/cli/install).

## 3. Cấu hình môi trường

`.env.example` chứa cấu hình mẫu công khai. Mỗi máy tạo `.env.local` từ file mẫu, chạy trong `client/`:

```powershell
Copy-Item .env.example .env.local
```

Các biến đã được sử dụng trong code/cấu hình Vite:

| Biến | Giá trị mẫu | Mục đích |
|---|---|---|
| `VITE_API_BASE_URL` | `/api/v1` | Prefix được `src/shared/api/client.ts` sử dụng |
| `API_PROXY_TARGET` | `http://localhost:8091` | Backend local cho proxy dev, được `vite.config.ts` đọc |

`8091` là cổng ứng dụng legacy trong Docker Compose hiện tại. Khi backend Spring Boot được triển khai, dùng đúng địa chỉ backend đang chạy; không mặc định API `/api/v1` đã tồn tại trên ứng dụng legacy.

Biến có tiền tố `VITE_` có thể được đưa vào mã chạy trên trình duyệt. Không đặt mật khẩu database, khóa VNPAY hoặc secret backend trong cấu hình client. Đổi file môi trường cần khởi động lại dev server.

Tham khảo [biến môi trường của Vite](https://vite.dev/guide/env-and-mode.html).

## 4. Chạy local và kết nối backend

Chạy trong `client/`:

```powershell
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

Luồng lấy/lưu token CSRF, login, 2FA và bảo vệ route chưa triển khai. Trang khởi đầu chưa gọi backend. Khi deploy với `BrowserRouter`, máy chủ phục vụ frontend cần fallback các route giao diện về `index.html`, đồng thời giữ `/api/v1` đi tới backend.

## 5. Lint, kiểm tra kiểu, build và preview

Các lệnh dưới đây đã có trong `package.json`, chạy trong `client/`:

| Lệnh | Mục đích |
|---|---|
| `pnpm run dev` | Chạy Vite dev server |
| `pnpm run lint` | ESLint; warning cũng làm lệnh thất bại |
| `pnpm run typecheck` | `tsc -b --noEmit`, kiểm tra TypeScript |
| `pnpm run build` | `tsc -b && vite build` |
| `pnpm run preview` | Xem bản build local |

Sau setup, kiểm tra trước khi gửi PR:

```powershell
pnpm run lint
pnpm run typecheck
pnpm run build
```

Sau khi build thành công:

```powershell
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
│   ├── features/     # Chỗ dành cho các nhóm nghiệp vụ
│   ├── shared/       # api/client.ts; components, hooks, utils, types còn trống
│   ├── theme/        # tokens.css, global.css và layout.css
│   ├── assets/       # Chỗ dành cho tài nguyên import vào code
│   ├── main.tsx      # React root và BrowserRouter
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
| PowerShell chặn `pnpm.ps1` | Dùng `pnpm.cmd` thay cho `pnpm`; không cần đổi execution policy toàn máy |
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
