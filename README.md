# KTX Smart Hub

Hệ thống quản lý ký túc xá, phạm vi demo **8 UC**: UC-04, UC-12, UC-17, UC-08, UC-22, UC-25, UC-14 và UC-30.

- Backend: **Spring Boot 4.1.1 / Java17**, Spring JDBC, JPA read model và PostgreSQL16.
- Frontend: **React / TypeScript / Vite**, Nginx phục vụ SPA và proxy API cùng origin.
- [Production](https://cnpm.caohoangphuc.id.vn) · [Thiết kế](docs/demo/README.md) · [CI/CD](docs/main/CI_CD.md).

## Trạng thái triển khai

Đã thay nền Servlet/JSP bằng Spring Boot executable JAR, cấu hình DataSource, 27 entity theo schema hiện hành, repository đọc, DTO theo OpenAPI và các test nền. Tầng service/repository cho 8 UC dùng Java và SQL trực tiếp; schema hiện hành không định nghĩa function hoặc procedure nghiệp vụ. Frontend hiện là khung tổng quan chức năng với trạng thái kết nối thật.

Tầng Java service/repository và controller cho 8 UC đã có; quy tắc nghiệp vụ nằm trong service Java, còn repository đọc/ghi SQL trực tiếp. Auth routes tập trung trong `ApiRoutes`; ADMIN/KETOAN đã thiết lập Google Authenticator phải nhập mã TOTP khi đăng nhập lại. API nghiệp vụ bị chặn cho đến khi xác thực mã thành công. Adapter thanh toán và ký chứng nhận cần cấu hình provider trước khi dùng. Mật khẩu seed `123456` chỉ phục vụ dữ liệu demo và test DB. Không coi CI xanh là nghiệm thu end-to-end 8 UC.

## Tài khoản demo theo tình huống kiểm thử

Mật khẩu mặc định trong seed của các tài khoản dưới đây là **`123456`**. Trạng thái production được đối chiếu ngày **04/10/2026**; có thể thay đổi sau khi thao tác nghiệp vụ.

### Đăng nhập và 2FA

| Tài khoản | Vai trò | Tình huống |
|---|---|---|
| `admin` | Ban quản lý | Duyệt đăng ký phòng, phân công sửa chữa, xử lý vi phạm và quản lý công ích. Production đã thiết lập 2FA: cần mã 6 số từ Google Authenticator sau bước mật khẩu. |
| `ketoan1` | Kế toán | Xem/phát hành hóa đơn, theo dõi thanh toán. Production đã thiết lập 2FA: cần mã 6 số từ ứng dụng. |
| `kythuat1` | Kỹ thuật | Theo dõi, xử lý yêu cầu sửa chữa và vật tư; không yêu cầu 2FA. |
| `baove1` | Bảo vệ | Khách thăm và ghi nhận ra/vào; không yêu cầu 2FA. |

Để thử thiết lập 2FA trên database local mới, đăng nhập `admin` hoặc `ketoan1`, chọn **Thiết lập 2FA → Bắt đầu thiết lập**, quét QR bằng Google Authenticator rồi nhập mã để xác nhận. Tài khoản chưa thiết lập chưa bị yêu cầu mã; sau khi thiết lập, đăng xuất rồi đăng nhập lại để thử luồng OTP. Không chia sẻ khóa/QR hoặc mã xác thực của tài khoản production.

### Sinh viên và 8 UC

| UC / tình huống | Tài khoản đề nghị | Điều kiện / cách sử dụng |
|---|---|---|
| UC-04: đăng ký phòng mới, sinh viên nam | `sv041` | `NAM`, `CHO_DUYET`, chưa có phòng hiện tại. Chọn phòng/giường phù hợp giới tính và còn trống. |
| UC-04: đăng ký phòng mới, sinh viên nữ | `sv042` | `NU`, `CHO_DUYET`, chưa có phòng hiện tại. Chọn phòng/giường phù hợp giới tính và còn trống. |
| UC-04: từ chối đăng ký khi đang lưu trú | `sv001` | `DANG_O`, phòng A-102; không dùng để thử happy path đăng ký mới. |
| UC-12: duyệt/từ chối đăng ký | `admin` + `demo_pending_m1` | Có sẵn 4 đơn chờ duyệt: `demo_pending_m1`, `demo_pending_m2`, `demo_pending_f1`, `demo_pending_f2`; admin có thể duyệt/từ chối ngay. Đơn đã duyệt làm thay đổi trạng thái lưu trú, không dùng lại như sinh viên mới. |
| UC-17: xem trước/phát hành hóa đơn | `ketoan1` | Cần chỉ số điện/nước, đơn giá và dữ liệu phòng hợp lệ cho tháng thử. |
| UC-08: xem hóa đơn phòng mình / thử thanh toán | `sv001` | Có ba hóa đơn A-102 tháng 08–10/2026 (ID 2, 22, 42). Không được đọc hóa đơn phòng khác. Tạo thanh toán cần adapter provider; chưa cấu hình trả `503 PAYMENT_GATEWAY_UNAVAILABLE`. |
| UC-22: yêu cầu sửa chữa / xử lý | `sv001` → `admin` → `kythuat1` | Sinh viên đang lưu trú gửi yêu cầu; admin phân công; kỹ thuật xử lý với quyền được cấp và vật tư đủ tồn kho. |
| UC-25: khai báo khách / ghi nhận ra-vào | `sv001` + `baove1` | Khách cần khai báo hợp lệ. Quét ra/vào cần thẻ/token được cấp và dữ liệu lưu trú phù hợp; đăng nhập bảo vệ không tự tạo các điều kiện này. |
| UC-14: vi phạm và quyết định | `admin` + sinh viên có vi phạm | Dùng danh sách vi phạm thực tế để chọn sinh viên, không giả định mọi tài khoản đều có vi phạm/đơn khiếu nại. |
| UC-30: xem và đăng ký công ích | `sv041` hoặc sinh viên khác + `admin` | Có ACT-01, ACT-02, ACT-03 trên production. Đăng ký cần hoạt động còn mở/chỗ; quy đổi/chứng nhận cần điểm và lịch sử hoàn thành hợp lệ. `demo_resident_m` đã đăng ký ACT-01; `demo_resident_f` đã hoàn thành ACT-DEMO-DONE và có 57 điểm demo. |

Các tài khoản sinh viên gốc trên production là **`sv001`–`sv055`**, cùng các tài khoản `demo_*` bên dưới. Tại lần đối chiếu, **`sv041`–`sv055`** đều `CHO_DUYET`, chưa có phòng hiện tại; kiểm tra đơn đang chờ/giường còn trống trước khi dùng lại tài khoản đã thử đăng ký. Có thể dùng `sv002` (nữ, B-103) hoặc `sv003` (nam, A-101) để kiểm tra quyền dữ liệu giữa các phòng.

### Dữ liệu demo có sẵn cho các trạng thái nghiệp vụ

Mật khẩu các tài khoản `demo_*` là **`123456`**. Sau lần nạp đầu tiên: **4 chờ duyệt, 2 đã duyệt, 1 từ chối, 1 đã hủy**. Các bộ đếm thay đổi khi bạn xử lý đơn; deploy tiếp theo giữ kết quả xử lý.

| Tài khoản | Case có sẵn |
|---|---|
| `demo_pending_m1`, `demo_pending_m2` | Hai sinh viên nam, đơn 900001/900002 chờ duyệt, hồ sơ VALID, giường đang giữ chỗ. |
| `demo_pending_f1`, `demo_pending_f2` | Hai sinh viên nữ, đơn 900003/900004 chờ duyệt; dùng để thử duyệt hoặc từ chối. |
| `demo_resident_m` | Đã duyệt đơn 900005, đang ở DEMO-A; hóa đơn 900001 chưa thanh toán, sửa chữa 900001, vi phạm 900001, đăng ký ACT-01 và ba trạng thái khách thăm. |
| `demo_resident_f` | Đã duyệt đơn 900006, đang ở DEMO-B; hóa đơn 900002 đã thanh toán, phiên thanh toán tổng hợp SUCCESS, sửa chữa 900002 đã phân công và 57 điểm công ích. |
| `demo_rejected` | Đơn 900007 bị từ chối có lý do; không có lưu trú, giường trống. |
| `demo_cancelled` | Đơn 900008 đã hủy; không có lưu trú, giường trống. |

Chi tiết dữ liệu, điều kiện từng UC và giới hạn adapter: [Demo seed cho 8 UC](docs/demo/DEMO_SEED.md). Migration bổ sung dữ liệu, không reset database hay khóa 2FA. Hồ sơ có ảnh minh chứng tổng hợp ghi rõ DỮ LIỆU DEMO; chủ hồ sơ và admin có thể xem/tải ảnh. Không chứa CCCD thật. Giao dịch seed không chuyển tiền thật.

### Tài khoản chỉ có trong seed local/test

| Tài khoản | Tình huống |
|---|---|
| `sv_new_nam` | Fixture sinh viên nam chưa lưu trú cho đăng ký phòng và boundary/negative test. |
| `sv_new_nu` | Fixture sinh viên nữ chưa lưu trú cho kiểm tra giới tính phòng. |

Hai tài khoản này **chưa có trên production** tại lần đối chiếu; không dùng chúng để đăng nhập website production. Không chạy lại schema/seed trên production để thêm tài khoản: reset sẽ xóa dữ liệu nghiệp vụ và cấu hình 2FA. CI dùng database test riêng; deploy bình thường giữ database và khóa 2FA. Giữ nguyên `TOTP_ENCRYPTION_KEY` để đọc được khóa đã mã hóa.

## Cấu trúc

| Thư mục | Nội dung |
|---|---|
| `server/src/main/java/` | Spring Boot, config, entity/read repository, service/repository cho 8 UC, DTO |
| `server/src/main/resources/` | Cấu hình ứng dụng và profile production |
| `server/src/test/java/` | JUnit unit test và integration test PostgreSQL |
| `server/pom.xml`, `server/Dockerfile` | Build/test và đóng gói backend JAR |
| `client/src/`, `client/tests/` | Frontend React và test trạng thái kết nối |
| `client/Dockerfile`, `client/nginx.conf` | Build SPA và reverse proxy |
| `database/` | Schema/seed PostgreSQL chuẩn; không chạy lại trên DB có dữ liệu |
| `scripts/` | Reset DB demo và deploy cặp backend/frontend |
| `docs/` | SRS, đặc tả, sơ đồ, truy vết và test case thiết kế |

## Chạy local

```bash
cp .env.example .env
docker compose up -d --build
# Frontend: http://localhost:8091
# Readiness backend qua Nginx: http://localhost:8091/api/health
```

PostgreSQL host port mặc định54321. Backend chỉ mở8080 trong network Compose; frontend mở8091. Với volume DB mới, entrypoint nạp01/02/04; volume hiện hữu không tự cập nhật schema. Reset demo xóa dữ liệu và yêu cầu flag:

```bash
bash scripts/reset-db.sh --reset-demo
```

Chạy ngoài Docker khi phát triển:

```bash
# Export DB_* hoặc SPRING_DATASOURCE_* phù hợp trước khi chạy backend.
mvn -f server/pom.xml spring-boot:run
cd client
pnpm install --frozen-lockfile
pnpm dev
```

Vite proxy `/api` đến localhost8080; Nginx triển khai dùng cùng origin nên không cần CORS khác domain.

## Cách viết backend đơn giản

Repository gọi SQL là class Java thông thường, được tạo bằng constructor trong
`DatabaseConfiguration`. Kiểm tra tham số dùng các hàm `requiredText`, `positive`,
`optionalText`; không cần `@Repository`, `@Validated` hay `@Transactional` trên các class này.
Tham số sai phát sinh `IllegalArgumentException`; DTO sai vẫn phát sinh
`ConstraintViolationException` từ Validator được gọi trực tiếp.

Các service điều phối quy tắc và transaction qua `ServiceContext`; repository dùng
`JdbcTemplate` cho SQL trực tiếp. HTTP routes được khai báo trong `ApiRoutes`,
còn hàm xử lý nằm riêng trong controller. Annotation JPA trên entity và
validation trên DTO được giữ vì phục vụ ánh xạ database và hợp đồng API.

## Kiểm thử

```bash
mvn -f server/pom.xml clean verify
# Chỉ dùng DB PostgreSQL16 test riêng, đã nạp schema/seed:
mvn -f server/pom.xml -Pdb-integration verify
cd client
pnpm install --frozen-lockfile
pnpm test
pnpm build
```

Integration test rollback thay đổi nghiệp vụ; SERIAL vẫn có thể tiến và test cần fixture demo. Không chạy lên DB production. CI tự tạo DB/container/network/cổng test riêng. Chi tiết giới hạn test và deploy: [CI/CD](docs/main/CI_CD.md).

## Quy trình phát triển

```bash
git switch -c feat/ten-chuc-nang
git push -u origin feat/ten-chuc-nang
```

Làm việc trên nhánh riêng, PR vào develop; main dành cho release. Xem [Git workflow](docs/main/GIT_WORKFLOW.md). Mọi push/PR chạy CI trên server; chỉ push develop deploy hai image đúng SHA đã qua kiểm thử, với rollback và bảo toàn DB.
