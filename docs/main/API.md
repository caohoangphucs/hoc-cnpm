# Tài liệu API backend

Tài liệu này mô tả các HTTP route đang được khai báo trong `ApiRoutes` và được xử lý bởi controller/service hiện có. Base path là `/api/v1`; dữ liệu trao đổi dùng JSON UTF-8. Các request ghi dữ liệu hiện trả HTTP `200` khi thành công. Quy tắc nghiệp vụ và quyền truy cập được kiểm tra tại service.

## Đăng nhập, session và CSRF

Admin hoặc kế toán đã thiết lập Google Authenticator phải xác thực 2FA sau mỗi lần đăng nhập bằng mật khẩu. Response login/session trả `twoFactorVerified: false` cho đến khi `POST /auth/2fa` với body `{"code":"123456"}` xác nhận mã đúng. Trong thời gian chờ, API nghiệp vụ trả `403 TWO_FACTOR_REQUIRED`; session, xác thực mã và đăng xuất vẫn dùng được. Admin/kế toán chưa thiết lập và các vai trò khác tiếp tục đăng nhập như trước. Khóa thiết lập/QR dùng để thêm ứng dụng; mã cần nhập khi đăng nhập là mã TOTP 6 chữ số trong ứng dụng.


Swagger UI được mở tại `/swagger-ui.html` (JSON OpenAPI tĩnh tại `/openapi.yaml`). Mở UI cùng host với API để trình duyệt tự gửi session cookie. Trong Swagger, gọi `GET /api/v1/auth/session`, copy `csrfToken` vào nút **Authorize** (`X-CSRF-TOKEN`), rồi thử `POST /api/v1/auth/login`. Sau khi đăng nhập, gọi lại session và cập nhật token trong **Authorize** vì login thay token CSRF. Các lệnh ghi cần token mới; callback thanh toán không dùng session/CSRF.

Ứng dụng dùng session cookie (`JSESSIONID` mặc định), không dùng bearer token. Trình tự gọi API từ client:

1. Gọi `GET /api/v1/auth/session`; giữ cookie và đọc `csrfToken` trong JSON.
2. Gọi `POST /api/v1/auth/login` với cookie đó, header `X-CSRF-TOKEN` và thông tin đăng nhập.
3. Giữ cookie mới nhất và dùng `X-CSRF-TOKEN` nhận từ kết quả đăng nhập cho các request `POST`, `PUT`, `DELETE` tiếp theo.

`X-Request-Id` là UUID tùy chọn. Nếu không gửi, server tự sinh request ID. `Idempotency-Key` là UUID tùy chọn cho các lệnh dùng chung handler; server tự sinh nếu bị bỏ trống. Riêng `POST /bookings`, cả hai header này đều được chấp nhận tùy chọn, nhưng `Idempotency-Key` bắt buộc và phải là UUID.

Ví dụ đăng nhập bằng `curl` (cần `jq` để lấy CSRF token):

```bash
BASE_URL=http://localhost:8080

CSRF=$(curl -sS -c cookies.txt "$BASE_URL/api/v1/auth/session" | jq -r .csrfToken)
curl -sS -b cookies.txt -c cookies.txt \
  -H 'Content-Type: application/json' \
  -H "X-CSRF-TOKEN: $CSRF" \
  -d '{"username":"sv001","password":"123456"}' \
  "$BASE_URL/api/v1/auth/login"
```

Admin/Kế toán đã thiết lập authenticator nhận `twoFactorVerified: false` sau bước mật khẩu. Xác minh TOTP thành công mới được đọc API nghiệp vụ, bao gồm minh chứng. Các role khác và tài khoản chưa thiết lập authenticator đăng nhập theo điều kiện hiện tại mà không có bước OTP.

## Endpoint xác thực

| Method | Path | Quyền | Mô tả |
|---|---|---|---|
| `GET` | `/auth/session` | Công khai | Đọc trạng thái session và lấy CSRF token. |
| `POST` | `/auth/login` | Công khai + CSRF | Đăng nhập bằng username/password, tạo session. |
| `POST` | `/auth/2fa` | Đã đăng nhập, role `ADMIN`/`KETOAN` | Xác minh mã TOTP nếu tài khoản đã cấu hình. Hiện không cần gọi để dùng API nghiệp vụ. |
| `POST` | `/auth/2fa/setup` | Đã đăng nhập, role `ADMIN`/`KETOAN` | Tạo secret và provisioning URI để thêm authenticator. |
| `POST` | `/auth/2fa/setup/confirm` | Đã đăng nhập, role `ADMIN`/`KETOAN` | Xác nhận cài đặt bằng mã 6 chữ số. |
| `POST` | `/auth/logout` | Đã đăng nhập | Hủy session và cấp CSRF token mới. |

Body đăng nhập: `{"username":"sv001","password":"123456"}`. Body xác minh/cài đặt 2FA: `{"code":"123456"}`. Session response có dạng `authenticated`, `role`, `twoFactorVerified`, `csrfToken`, `idleExpiresAt`.

## API nghiệp vụ

Trong bảng dưới, path ghi phần sau base path `/api/v1`; ví dụ `GET /rooms` gọi tại `/api/v1/rooms`. Phòng/giường, booking, invoice và payment dùng `page` (mặc định `1`) cùng `pageSize` (mặc định `20`, tối đa `100`). Repair, entries, visitors, violations và activities dùng `page` (mặc định `1`) cùng `size` (mặc định `20`).

| UC | Method và path | Role | Body/query và tác vụ |
|---|---|---|---|
| UC-04 | `GET /rooms?page=1&pageSize=20` | `SINHVIEN`, `ADMIN` | Danh sách phòng, thông tin tòa/loại phòng/giới tính áp dụng, sức chứa và số giường có thể đặt. |
| UC-04 | `GET /rooms/{id}` | `SINHVIEN`, `ADMIN` | Chi tiết một phòng theo mã phòng; trả `404` nếu không tồn tại. |
| UC-04 | `POST /rooms` | `ADMIN` | Tạo phòng từ `RoomWriteDto`. |
| UC-04 | `PUT /rooms/{id}` | `ADMIN` | Cập nhật thông tin phòng; `id` trong body phải khớp URL. |
| UC-04 | `DELETE /rooms/{id}` | `ADMIN` | Xóa phòng nếu chưa có sinh viên, hóa đơn, lưu trú, đăng ký, chỉ số hoặc yêu cầu sửa chữa liên kết; nếu còn dữ liệu trả `409`. |
| UC-04 | `GET /rooms/{id}/beds?page=1&pageSize=20` | `SINHVIEN`, `ADMIN` | Danh sách giường thuộc phòng, gồm mã, số giường, trạng thái và version. Trả `404` nếu phòng không tồn tại. |
| UC-04 | `POST /beds` | `ADMIN` | Tạo giường trong phòng đã có. |
| UC-04 | `PUT /beds/{id}` | `ADMIN` | Cập nhật giường; `id` trong body phải khớp URL. |
| UC-04 | `DELETE /beds/{id}` | `ADMIN` | Xóa giường nếu chưa có đăng ký hoặc lưu trú liên kết; nếu còn dữ liệu trả `409`. |
| UC-04 | `POST /bookings` | `SINHVIEN` | Tạo đăng ký/giữ chỗ. Body xem [Đăng ký phòng](#uc-04-đăng-ký-phòng). Bắt buộc `Idempotency-Key`. |
| UC-04 | `GET /bookings?page=1&pageSize=20` | `SINHVIEN`, `ADMIN` | Sinh viên chỉ xem đăng ký của mình; Admin xem tất cả. |
| UC-04 | `GET /bookings/{id}` | `SINHVIEN`, `ADMIN` | Chi tiết đăng ký; sinh viên khác chủ sở hữu nhận `404`. |
| UC-04 | `DELETE /bookings/{id}` | `SINHVIEN`, `ADMIN` | Xóa đăng ký đang chờ duyệt và trả giường về trạng thái trống; trạng thái khác trả `409`. |
| UC-12 | `POST /bookings/{id}/decision` | `ADMIN` | Duyệt hoặc từ chối đăng ký; body gồm `decision`, `reason`, `version`. |
| UC-17 | `POST /invoice-drafts` | `KETOAN` | Tạo bản xem trước hóa đơn theo tháng/phòng. |
| UC-17 | `GET /invoices?page=1&pageSize=20` | `SINHVIEN`, `KETOAN`, `ADMIN` | Danh sách hóa đơn; sinh viên được lọc theo quyền hóa đơn bên dưới. |
| UC-17 | `POST /invoices/issue` | `KETOAN` | Phát hành từ bản xem trước; body gồm `draftId`, `version`. |
| UC-17 | `GET /invoices/{id}` | `SINHVIEN`, `KETOAN`, `ADMIN` | Chi tiết hóa đơn và các dòng phí; quyền sinh viên áp dụng giống danh sách và thanh toán. |
| UC-17 | `PUT /invoices/{id}` | `KETOAN`, `ADMIN` | Đổi hạn thanh toán khi hóa đơn chưa thanh toán. Body `{"dueDate":"YYYY-MM-DD"}`. |
| UC-17 | `DELETE /invoices/{id}` | `KETOAN`, `ADMIN` | Xóa hóa đơn chưa thanh toán và chưa có giao dịch thanh toán. |
| UC-08 | `POST /payment-sessions` | `SINHVIEN` | Tạo phiên thanh toán cho hóa đơn của sinh viên. Hiện trả `503 PAYMENT_GATEWAY_UNAVAILABLE` nếu chưa cấu hình adapter tương ứng. |
| UC-08 | `GET /payment-sessions?page=1&pageSize=20` | `SINHVIEN`, `KETOAN`, `ADMIN` | Danh sách phiên của sinh viên hiện tại hoặc tất cả phiên với Kế toán/Admin. |
| UC-08 | `GET /payment-sessions/{id}` | `SINHVIEN` là chủ hóa đơn, `KETOAN`, `ADMIN` | Đọc trạng thái phiên; `id` là UUID. |
| UC-08 | `DELETE /payment-sessions/{id}` | `SINHVIEN`, `KETOAN`, `ADMIN` | Xóa phiên còn chờ trước khi bắt đầu xử lý ở cổng thanh toán. |
| UC-08 | `POST /payment-callbacks/{gateway}` | Callback nhà cung cấp, không yêu cầu session/CSRF | Nhận tham số callback và chỉ xử lý sau khi adapter kiểm chữ ký. Chỉ hoạt động khi gateway adapter được cấu hình. |
| UC-22 | `POST /repairs` | `SINHVIEN` | Gửi yêu cầu sửa chữa. |
| UC-22 | `GET /repairs?page=1&size=20` | `SINHVIEN`, `KYTHUAT`, `ADMIN` | Danh sách phiếu mà người gọi được phép xem. |
| UC-22 | `GET /repairs/{id}` | `SINHVIEN`, `KYTHUAT`, `ADMIN` | Chi tiết phiếu theo quyền sở hữu/phân công. |
| UC-22 | `DELETE /repairs/{id}` | `SINHVIEN`, `ADMIN` | Xóa phiếu mới chưa phân công hoặc xuất vật tư; nếu đã có lịch sử xử lý trả `409`. |
| UC-22 | `POST /repairs/{id}/assignment` | `KYTHUAT`, `ADMIN` | Gán kỹ thuật viên, lịch dự kiến và `version`. |
| UC-22 | `PUT /repairs/{id}/schedule` | `KYTHUAT` | Đổi lịch/bảo hành; body gồm `version`, `scheduledAt`, `warrantyUntil`, `note`. |
| UC-22 | `POST /repairs/{id}/complete` | `KYTHUAT` | Hoàn tất phiếu; body gồm `version`, `note`, `supplies`, `warrantyUntil`. |
| UC-25 | `POST /entries` | `BAOVE` | Ghi nhận sự kiện thẻ ra/vào. |
| UC-25 | `GET /entries?page=1&size=20` | `SINHVIEN`, `BAOVE`, `ADMIN` | Danh sách lượt ra/vào; sinh viên chỉ xem sự kiện của mình. |
| UC-25 | `GET /entries/{eventId}` | `SINHVIEN`, `BAOVE`, `ADMIN` | Chi tiết lượt ra/vào theo UUID sự kiện. |
| UC-25 | — | — | Lượt ra/vào là audit log; không thể sửa hoặc xóa trực tiếp. Ghi nhận sai cần quy trình nghiệp vụ điều chỉnh riêng. |
| UC-25 | `POST /visitors` | `SINHVIEN` | Khai báo khách thăm. |
| UC-25 | `GET /visitors?page=1&size=20` | `SINHVIEN`, `BAOVE`, `ADMIN` | Danh sách khách; sinh viên chỉ xem yêu cầu của mình, bảo vệ chỉ đọc thông tin đã duyệt. |
| UC-25 | `GET /visitors/{id}` | `SINHVIEN`, `BAOVE`, `ADMIN` | Chi tiết yêu cầu theo quyền truy cập. |
| UC-25 | `DELETE /visitors/{id}` | `SINHVIEN`, `ADMIN` | Xóa yêu cầu còn chờ duyệt. |
| UC-25 | `POST /visitors/{id}/approval` | `ADMIN` | Duyệt/từ chối khai báo khách, body cùng dạng `decision`, `reason`, `version`. |
| UC-25 | `POST /visitors/{id}/check-in` | `BAOVE` | Ghi nhận khách đến; không có body. |
| UC-25 | `POST /visitors/{id}/check-out` | `BAOVE` | Ghi nhận khách rời đi; không có body. |
| UC-14 | `GET /violations?page=1&size=20` | `ADMIN` | Danh sách vi phạm, gồm cả trạng thái đã xử lý. |
| UC-14 | `GET /violations/pending?page=1&size=20` | `ADMIN` | Hàng chờ xử lý vi phạm. |
| UC-14 | `GET /violations/{id}` | `ADMIN` | Chi tiết vi phạm và lịch sử quyết định. |
| UC-14 | `POST /violations` | `ADMIN` | Tạo vi phạm: `studentId`, `content`, `level`, `occurredAt` (tùy chọn, phải thuộc học kỳ đã cấu hình). |
| UC-14 | `PUT /violations/{id}` | `ADMIN` | Sửa nội dung và mức độ của vi phạm chưa xử lý; body cần `version` để tránh ghi đè cập nhật đồng thời. |
| UC-14 | `DELETE /violations/{id}` | `ADMIN` | Chỉ xóa vi phạm đang chờ, tạo thủ công và chưa có lượt cổng hoặc quyết định; không xóa lịch sử. |
| UC-14 | `POST /violations/decisions` | `ADMIN` | Ra quyết định theo lô; body gồm `violationIds`, `decision`, `reason`, `evidenceFileId`, `versions`. |
| UC-14 | `POST /decisions/{id}/appeals` | `SINHVIEN` | Gửi khiếu nại, body `{"content":"..."}`. |
| UC-14 | `POST /appeals/{id}/response` | `ADMIN` | Phản hồi khiếu nại, body gồm `accepted`, `reason`, `version`. |
| UC-30 | `GET /activities?page=1&size=20` | `SINHVIEN`, `ADMIN` | Danh sách hoạt động. |
| UC-30 | `POST /activities` | `ADMIN` | Tạo hoạt động mới. |
| UC-30 | `PUT /activities/{id}` | `ADMIN` | Cập nhật hoạt động trước khi có đăng ký. |
| UC-30 | `DELETE /activities/{id}` | `ADMIN` | Xóa hoạt động trước khi có đăng ký; nếu có lịch sử đăng ký trả `409`. |
| UC-30 | `POST /activities/{id}/registrations` | `SINHVIEN` | Đăng ký hoạt động; không có body. |
| UC-30 | `DELETE /activities/{id}/registrations` | `SINHVIEN` | Hủy đăng ký, body `{"version":1}`. |
| UC-30 | `POST /activities/attendance` | `ADMIN` | Điểm danh bằng QR, body `{"qrToken":"..."}`. |
| UC-30 | `POST /activities/completion` | `ADMIN` | Xác nhận hoàn thành theo lô, body `{"registrationIds":[1,2]}`. |
| UC-30 | `GET /activities/points?semesterId={id}` | `SINHVIEN` | Xem điểm hoạt động của học kỳ. |
| UC-30 | `POST /activities/conversions` | `SINHVIEN` | Gửi yêu cầu quy đổi, body `{"semesterId":1,"drl":1}`. |
| UC-30 | `POST /activities/certificates` | `SINHVIEN` | Yêu cầu chứng nhận, body `{"semesterId":1}`. |

## Cấu trúc body theo UC

### UC-04 đăng ký phòng

Tra cứu danh sách phòng, chi tiết một phòng và danh sách giường bằng `GET /api/v1/rooms?page=1&pageSize=20`, `GET /api/v1/rooms/{id}` và `GET /api/v1/rooms/{id}/beds?page=1&pageSize=20`. Các API chỉ cho sinh viên và Admin; danh sách được phân trang và giường có trạng thái/version để giao diện biết tình trạng mới nhất. Số giường có thể đặt chỉ đếm giường `TRONG` ở phòng không bị khóa, sửa chữa hoặc đầy. Trạng thái có thể đổi sau khi tải danh sách, nên service vẫn kiểm tra lại khi gửi đăng ký.

Ví dụ response danh sách phòng:

```json
{
  "items":[
    {"id":"A-101","buildingId":"TOA-A","roomType":"Phòng Tiêu Chuẩn 4 Giường","gender":"NAM","capacity":4,"availableBeds":1}
  ],
  "page":1,
  "pageSize":20,
  "total":20
}
```

Ví dụ response giường của phòng:

```json
{
  "items":[
    {"id":"G-A-101-4","number":"G-04","status":"TRONG","version":1}
  ],
  "page":1,
  "pageSize":20,
  "total":4
}
```

```json
{
  "bedId": "A101-01",
  "windowId": 1,
  "fileIds": ["09f89963-f7d9-4cf2-a36d-2b6b582297e1", "e63baa22-7204-47d3-8817-4fe85d7e0a8b"],
  "acceptedTerms": true
}
```

`fileIds` yêu cầu từ 2 đến 5 UUID. Dịch vụ kiểm tra giường, đợt đăng ký, quyền sở hữu tệp và điều kiện lưu trú.

### UC-12 duyệt đăng ký

```json
{"decision":"DUYET","reason":null,"version":1}
```

`decision` là `DUYET` hoặc `TU_CHOI`; từ chối cần có `reason`. `version` phải khớp phiên bản đăng ký đang xử lý.

Quyền hóa đơn của sinh viên dùng chung cho danh sách, chi tiết và thanh toán:

- Nếu hóa đơn có snapshot lúc phát hành, chỉ sinh viên trong `occupants` được truy cập.
- Nếu chưa có snapshot, đối chiếu khoảng ngày lưu trú với tháng hóa đơn (bao gồm ngày đầu/cuối tháng).
- Với dữ liệu cũ chưa có bất kỳ lịch sử lưu trú nào, chỉ sinh viên `DANG_O` có `MaPhongHienTai` trùng phòng hóa đơn được truy cập. Khi đã có lịch sử hoặc snapshot, không dùng quy tắc tương thích này.

Migration `V20261007` bổ sung ba hoạt động demo và học kỳ còn thiếu cho database cũ. Migration chỉ thêm bản ghi chưa tồn tại, không đổi hoạt động đã chỉnh sửa và không tạo đăng ký/điểm công ích giả; số đăng ký của hoạt động mới là 0.

### UC-17 hóa đơn

```json
{"month":"2026-10","roomIds":["A-101","A-102"]}
```

Body phát hành dùng `{"draftId":"<uuid>","version":1}` lấy từ kết quả preview. Preview trả snapshot/cảnh báo để kế toán rà trước khi phát hành.

### UC-08 thanh toán

```json
{"invoiceIds":[1,2],"gateway":"VNPAY"}
```

`gateway` nhận `VNPAY` hoặc `MOMO`. Status nhận UUID phiên ở path. Callback nhận query/form parameters theo adapter gateway; không tự gửi callback giả lập vì service yêu cầu chữ ký hợp lệ.

### UC-22 sửa chữa

Tạo phiếu:

```json
{"title":"Đèn phòng hỏng","description":"Đèn không sáng","priority":"TRUNG_BINH","fileIds":[]}
```

`priority` là `THAP`, `TRUNG_BINH`, `CAO`, `KHAN_CAP`. Gán kỹ thuật viên: `{"technicianAccountId":3,"scheduledAt":"2026-10-05T09:00:00Z","version":1}`. Đổi lịch: `{"version":1,"scheduledAt":"2026-10-06T09:00:00Z","warrantyUntil":"2026-11-06","note":"Khách đổi lịch"}`. Hoàn tất: `{"version":2,"note":"Đã thay bóng","supplies":[{"supplyId":"BONG-LED","quantity":1}],"warrantyUntil":"2026-11-06"}`.

### UC-25 ra/vào và khách thăm

Sự kiện thẻ: `{"eventId":"<uuid>","cardToken":"<token>","type":"VAO","deviceId":"GATE-01","note":null}`. `type` là `VAO` hoặc `RA`; token không được ghi lại trong `toString` log của DTO. Khai báo khách: `{"hostStudentId":"21110001","name":"Nguyen Van A","identity":"<so-giay-to>","validFrom":"2026-10-03T12:00:00Z","validTo":"2026-10-03T16:00:00Z"}`. Body duyệt giống UC-12. Check-in/check-out nhận id khách trong path và không có body.

### UC-14 kỷ luật

```json
{
  "violationIds":[1,2],
  "decision":"NHAC_NHO",
  "reason":"Vi phạm nội quy",
  "evidenceFileId":"09f89963-f7d9-4cf2-a36d-2b6b582297e1",
  "versions":{"1":1,"2":1}
}
```

Mỗi ID phải có version tương ứng trong `versions`. Khiếu nại body `{"content":"Đề nghị xem xét lại..."}`; phản hồi body `{"accepted":false,"reason":"Không đủ căn cứ","version":1}`.

### UC-30 công ích

Tạo hoạt động: `{"semesterId":1,"title":"Ngày Chủ nhật xanh","startAt":"2026-10-10T07:00:00Z","endAt":"2026-10-10T11:00:00Z","capacity":30,"points":2}`. `endAt` phải sau `startAt`. Đăng ký không có body; kết quả đăng ký cung cấp QR token để điểm danh. Điểm danh QR do `ADMIN` thực hiện với body `{"qrToken":"..."}`. Hủy dùng version đăng ký; xác nhận nhận danh sách ID đăng ký; điểm cần `semesterId`; quy đổi DRL từ 1 đến 5; yêu cầu chứng nhận chỉ cần `semesterId`.

## Hợp đồng request/response cho các API CRUD

Các ví dụ dưới đây là JSON thực tế theo DTO/controller hiện tại. Với các endpoint ghi, cần gửi `Content-Type: application/json`, session cookie và `X-CSRF-TOKEN`; `Idempotency-Key` được yêu cầu ở `POST /bookings` và tùy chọn ở các command khác. `X-Request-Id` tùy chọn. Dấu `—` nghĩa là endpoint không đọc body/query tương ứng.

### UC-04 phòng, giường và đăng ký

| Method và path | Path/query params | Request body | Response thành công |
|---|---|---|---|
| `GET /rooms` | `page` (mặc định 1), `pageSize` (mặc định 20, tối đa 100) | — | `{"items":[{"id":"A-101","buildingId":"TOA-A","roomType":"Phòng tiêu chuẩn","gender":"NAM","capacity":4,"availableBeds":2}],"page":1,"pageSize":20,"total":1}` |
| `GET /rooms/{id}` | `id`: mã phòng | — | `{"id":"A-101","buildingId":"TOA-A","roomType":"Phòng tiêu chuẩn","gender":"NAM","capacity":4,"availableBeds":2}` |
| `POST /rooms`, `PUT /rooms/{id}` | `PUT`: `id` phải trùng `body.id` | `{"id":"A-101","buildingId":"TOA-A","roomTypeId":"RT-01","number":"101","floor":1,"capacity":4,"basePrice":1500000,"status":"CON_CHO"}` | `{"id":"A-101","status":"COMPLETED","version":1,"requestId":"<uuid>"}` |
| `DELETE /rooms/{id}` | `id`: mã phòng | — | `{"id":"A-101","status":"COMPLETED","version":1,"requestId":"<uuid>"}`; `409` nếu phòng còn dữ liệu vận hành liên kết. |
| `GET /rooms/{id}/beds` | `id`: mã phòng; `page`, `pageSize` như danh sách phòng | — | `{"items":[{"id":"G-A-101-1","number":"G-01","status":"TRONG","version":1}],"page":1,"pageSize":20,"total":1}` |
| `POST /beds`, `PUT /beds/{id}` | `PUT`: `id` phải trùng `body.id` | `{"id":"G-A-101-1","roomId":"A-101","number":"G-01","position":"Cửa sổ","status":"TRONG"}` | `{"id":"G-A-101-1","status":"COMPLETED","version":1,"requestId":"<uuid>"}` |
| `DELETE /beds/{id}` | `id`: mã giường | — | `{"id":"G-A-101-1","status":"COMPLETED","version":1,"requestId":"<uuid>"}`; `409` nếu đã có đăng ký/lưu trú. |
| `POST /bookings` | — | `{"bedId":"G-A-101-1","windowId":1,"fileIds":["<uuid-1>","<uuid-2>"],"acceptedTerms":true}` | `{"id":"12","status":"CHO_DUYET","version":1,"requestId":"<uuid>"}` |
| `GET /bookings` | `page` (mặc định 1), `pageSize` (mặc định 20) | — | `{"items":[{"id":12,"bedId":"G-A-101-1","studentId":"21110001","status":"CHO_DUYET","expiresAt":"2026-10-04T12:00:00+07:00","profileState":"COMPLETE","version":1,"fileIds":["<uuid>"],"reason":null}],"page":1,"pageSize":20,"total":1}` |
| `GET /bookings/{id}` | `id`: mã đăng ký | — | Một `BookingResourceDto` như phần tử `items` ở response danh sách. |
| `DELETE /bookings/{id}` | `id`: mã đăng ký đang chờ duyệt | — | `{"id":"12","status":"DELETED","version":2,"requestId":"<uuid>"}`; `409` nếu không còn trạng thái chờ. |

### UC-17 hóa đơn

| Method và path | Path/query params | Request body | Response thành công |
|---|---|---|---|
| `POST /invoice-drafts` | — | `{"month":"2026-10","roomIds":["A-101"]}` | `{"id":"<uuid>","version":1,"month":"2026-10","rooms":[{"roomId":"A-101","oldElectricity":100,"newElectricity":110,"oldWater":20,"newWater":22,"electricityPrice":3000,"waterPrice":10000,"roomPrice":1000000,"discount":0,"extras":0,"total":1050000,"warnings":[]}],"inputHash":"<hash>"}`. |
| `POST /invoices/issue` | — | `{"draftId":"<uuid>","version":1}` | Danh sách kết quả phát hành theo phòng, mỗi phần tử gồm `roomId`, `status`, `invoiceId`, `message`. |
| `GET /invoices` | `page` (mặc định 1), `pageSize` (mặc định 20) | — | `{"items":[{"id":1,"roomId":"A-101","month":"2026-10","lines":[{"name":"Điện","quantity":10,"unitPrice":3000,"total":30000}],"discount":0,"total":30000,"paid":0,"balance":30000,"dueDate":"2026-10-15","status":"CHUA_TT"}],"page":1,"pageSize":20,"total":1}` |
| `GET /invoices/{id}` | `id`: mã hóa đơn | — | Một `InvoiceResourceDto` như phần tử `items` ở response danh sách. |
| `PUT /invoices/{id}` | `id`: mã hóa đơn | `{"dueDate":"2026-10-20"}` | `{"id":"1","status":"CHUA_TT","version":1,"requestId":"<uuid>"}` |
| `DELETE /invoices/{id}` | `id`: mã hóa đơn chưa thanh toán và chưa có payment | — | `{"id":"1","status":"DELETED","version":1,"requestId":"<uuid>"}`; nếu đã thanh toán/có giao dịch thì `409`. |

### UC-08 phiên thanh toán

| Method và path | Path/query params | Request body | Response thành công |
|---|---|---|---|
| `POST /payment-sessions` | — | `{"invoiceIds":[1],"gateway":"VNPAY"}` (`gateway`: `VNPAY` hoặc `MOMO`) | `{"id":"<uuid>","gateway":"VNPAY","providerRef":"<uuid>","amount":30000,"invoiceIds":[1],"status":"PENDING","payUrl":"https://...","expiresAt":"2026-10-03T13:00:00Z"}`. Gateway chưa cấu hình trả `503`. |
| `GET /payment-sessions` | `page` (mặc định 1), `pageSize` (mặc định 20) | — | Mảng `PaymentResourceDto`; sinh viên chỉ thấy phiên của mình. |
| `GET /payment-sessions/{id}` | `id`: UUID phiên | — | Một `PaymentResourceDto` như phần tử danh sách. |
| `DELETE /payment-sessions/{id}` | `id`: UUID phiên đang chờ | — | `{"id":"<uuid>","status":"DELETED","version":1,"requestId":"<uuid>"}`; chỉ xóa được trước khi provider xử lý/callback. |

### UC-22 yêu cầu sửa chữa

| Method và path | Path/query params | Request body | Response thành công |
|---|---|---|---|
| `POST /repairs` | — | `{"title":"Đèn hỏng","description":"Đèn không sáng","priority":"TRUNG_BINH","fileIds":[]}` | `{"id":"8","status":"MOI","version":1,"requestId":"<uuid>"}` |
| `GET /repairs` | `page` (mặc định 1), `size` (mặc định 20) | — | Mảng record sửa chữa: `id`, `roomId`, `studentId`, `title`, `priority`, `status`, `version`. |
| `GET /repairs/{id}` | `id`: mã phiếu | — | Một record sửa chữa với các trường như trên. |
| `DELETE /repairs/{id}` | `id`: phiếu mới, chưa phân công/chưa có lịch sử | — | `{"id":"8","status":"DELETED","version":2,"requestId":"<uuid>"}`; nếu đã xử lý trả `409`. |

### UC-25 lượt ra/vào và khách thăm

| Method và path | Path/query params | Request body | Response thành công |
|---|---|---|---|
| `POST /entries` | — | `{"eventId":"<uuid>","cardToken":"<token>","type":"VAO","deviceId":"GATE-01","note":null}` | `{"eventId":"<uuid>","studentId":"21110001","type":"VAO","time":"2026-10-03T12:00:00+07:00","barrierStatus":"PENDING","violationId":null}`; `cardToken` không được trả về. |
| `GET /entries` | `page` (mặc định 1), `size` (mặc định 20) | — | `{"items":[{"eventId":"<uuid>","studentId":"21110001","type":"VAO","time":"2026-10-03T12:00:00+07:00","barrierStatus":"OPENED","violationId":null}],"page":1,"pageSize":20,"total":1}` |
| `GET /entries/{eventId}` | `eventId`: UUID sự kiện | — | Một `EntryResourceDto` như phần tử `items`. Đây là audit log, không hỗ trợ sửa/xóa trực tiếp. |
| `POST /visitors` | — | `{"hostStudentId":"21110001","name":"Nguyen Van A","identity":"<so-giay-to>","validFrom":"2026-10-03T12:00:00Z","validTo":"2026-10-03T16:00:00Z"}` | `{"id":"3","status":"PENDING","version":1,"requestId":"<uuid>"}`; dữ liệu định danh lưu ở dạng mã hóa. |
| `GET /visitors` | `page` (mặc định 1), `size` (mặc định 20) | — | Mảng `{ "id":3, "hostStudentId":"21110001", "state":"PENDING", "validFrom":"...", "validTo":"...", "version":1 }`; không trả tên/giấy tờ đã mã hóa. |
| `GET /visitors/{id}` | `id`: mã khai báo | — | Một record visitor với các trường như trên. |
| `DELETE /visitors/{id}` | `id`: visitor còn `PENDING` | — | `{"id":"3","status":"DELETED","version":2,"requestId":"<uuid>"}`; sau khi duyệt không được xóa. |

### UC-14 vi phạm

| Method và path | Path/query params | Request body | Response thành công |
|---|---|---|---|
| `GET /violations`, `GET /violations/pending` | `page` (mặc định 1), `size` (mặc định 20) | — | Mảng `ViolationResourceDto`: `id`, `studentId`, `semesterId`, `content`, `level`, `status`, `semesterCount`, `decisions`, `version`. |
| `GET /violations/{id}` | `id`: mã vi phạm | — | Một `ViolationResourceDto` cùng các revision quyết định trong `decisions`. |
| `POST /violations` | — | `{"studentId":"21110001","content":"Vi phạm nội quy","level":"LOW","occurredAt":"2026-10-03T12:00:00+07:00"}`; `occurredAt` có thể bỏ qua để dùng giờ hiện tại, nhưng ngày phải thuộc học kỳ cấu hình. | Một `ViolationResourceDto` có `status` là `CHUA_XU_LY`, `version`: 1 và `decisions`: `[]`. |
| `PUT /violations/{id}` | `id`: mã vi phạm chưa xử lý | `{"content":"Nội dung đã hiệu chỉnh","level":"MEDIUM","version":1}` | Một `ViolationResourceDto` đã cập nhật, `version` tăng lên 2. |
| `DELETE /violations/{id}` | `id`: vi phạm chờ, tạo thủ công, chưa liên kết lượt cổng/decision | — | `{"id":"3","status":"DELETED","version":1,"requestId":"<uuid>"}`; dữ liệu có lịch sử trả `409` và được giữ lại. |

### UC-30 hoạt động

| Method và path | Path/query params | Request body | Response thành công |
|---|---|---|---|
| `GET /activities` | `page` (mặc định 1), `size` (mặc định 20) | — | Mảng `ActivityResourceDto`: `id`, `semesterId`, `title`, `startAt`, `endAt`, `capacity`, `remaining`, `points`, `status`, `version`. |
| `POST /activities`, `PUT /activities/{id}` | `PUT`: `id` là mã hoạt động | `{"semesterId":1,"title":"Ngày Chủ nhật xanh","startAt":"2026-10-10T07:00:00Z","endAt":"2026-10-10T11:00:00Z","capacity":30,"points":2}` | `ActivityResourceDto` như trên; sửa chỉ được trước khi có lịch sử đăng ký. |
| `DELETE /activities/{id}` | `id`: mã hoạt động chưa có đăng ký | — | `{"id":"2","status":"COMPLETED","version":2,"requestId":"<uuid>"}`; có lịch sử đăng ký trả `409`. |

## Request/response cho các thao tác nghiệp vụ khác

| Method và path | Params | Request body | Response thành công |
|---|---|---|---|
| `POST /bookings/{id}/decision` | `id`: mã booking | `{"decision":"DUYET","reason":null,"version":1}` | `CommandResultDto`: `{"id":"12","status":"DA_DUYET","version":2,"requestId":"<uuid>"}`. `decision` nhận `DUYET`/`TU_CHOI`; từ chối phải có `reason`. |
| `POST /repairs/{id}/assignment` | `id`: mã phiếu | `{"technicianAccountId":3,"scheduledAt":"2026-10-05T09:00:00Z","version":1}` | `CommandResultDto` gồm `id`, `status`, `version`, `requestId`. |
| `PUT /repairs/{id}/schedule` | `id`: mã phiếu | `{"version":2,"scheduledAt":"2026-10-06T09:00:00Z","warrantyUntil":"2026-11-06","note":"Khách đổi lịch"}` | `CommandResultDto` gồm `id`, `status`, `version`, `requestId`. |
| `POST /repairs/{id}/complete` | `id`: mã phiếu | `{"version":3,"note":"Đã thay bóng","supplies":[{"supplyId":"BONG-LED","quantity":1}],"warrantyUntil":"2026-11-06"}` | `{"status":"HOAN_THANH","result":{"id":"8","status":"HOAN_THANH","version":4,"requestId":"<uuid>"},"shortages":[]}`; thiếu vật tư trả trạng thái/`shortages` để xử lý bổ sung. |
| `POST /visitors/{id}/approval` | `id`: mã khai báo | `{"decision":"DUYET","reason":null,"version":1}` | `CommandResultDto` gồm `id`, trạng thái `APPROVED` hoặc `REJECTED`, version và requestId. |
| `POST /visitors/{id}/check-in`, `POST /visitors/{id}/check-out` | `id`: mã khách đang được duyệt | — | `CommandResultDto` gồm `id`, trạng thái lượt khách, version và requestId. |
| `POST /violations/decisions` | — | `{"violationIds":[1],"decision":"NHAC_NHO","reason":"Vi phạm nội quy","evidenceFileId":"<uuid>","versions":{"1":1}}` | Mảng `ViolationResourceDto` sau quyết định; mỗi phần tử có lịch sử `decisions` và `version` mới. |
| `POST /decisions/{id}/appeals` | `id`: mã decision | `{"content":"Đề nghị xem xét lại"}` | `CommandResultDto` với trạng thái `OPEN`. |
| `POST /appeals/{id}/response` | `id`: mã appeal | `{"accepted":false,"reason":"Không đủ căn cứ","version":1}` | `CommandResultDto` với trạng thái `REJECTED` hoặc `ACCEPTED`. |
| `POST /activities/{id}/registrations` | `id`: mã hoạt động | — | `{"id":4,"activityId":"2","status":"REGISTERED","qrToken":"<token>…","version":1}`. QR token dùng cho điểm danh; không ghi/chia sẻ trong log công khai. |
| `DELETE /activities/{id}/registrations` | `id`: mã hoạt động; version nằm trong body | `{"version":1}` | `CommandResultDto` với `status` là `CANCELLED` và version mới. |
| `POST /activities/attendance` | — | `{"qrToken":"<token>"}` | `CommandResultDto` cho đăng ký được điểm danh. |
| `POST /activities/completion` | — | `{"registrationIds":[4,5]}` | Mảng `CommandResultDto`, một phần tử cho mỗi đăng ký xác nhận. |
| `GET /activities/points` | Query bắt buộc `semesterId` | — | `{"semesterId":1,"earned":10,"available":10,"convertedDrl":0,"ledger":[]}`. |
| `POST /activities/conversions` | — | `{"semesterId":1,"drl":1}` | `{"id":"<uuid>","status":"PENDING","pointsSpent":10,"drl":1,"externalState":"PENDING"}`. |
| `POST /activities/certificates` | — | `{"semesterId":1}` | `{"id":"<uuid>","status":"PENDING","downloadUrl":null,"sha256":null,"signedAt":null}`. |
| `POST /payment-callbacks/{gateway}` | `gateway`: `vnpay` hoặc `momo`; tham số callback do provider gửi dạng query/form | — | `PaymentResourceDto` của phiên được cập nhật. Callback không dùng cookie/CSRF; adapter phải xác minh chữ ký trước khi thay đổi trạng thái. |

## Hợp đồng endpoint xác thực

| Method và path | Params | Request body | Response thành công |
|---|---|---|---|
| `GET /auth/session` | Cookie session nếu có | — | `{"authenticated":false,"role":null,"twoFactorVerified":false,"csrfToken":"<token>","idleExpiresAt":null}`; khi đăng nhập, `authenticated`/`role`/`idleExpiresAt` phản ánh session hiện tại. |
| `POST /auth/login` | Header `X-CSRF-TOKEN` lấy từ session trước | `{"username":"sv001","password":"123456"}` | `SessionDto`; đồng thời server đặt session cookie. Sai thông tin trả `401 INVALID_CREDENTIALS`. |
| `POST /auth/2fa`, `POST /auth/2fa/setup/confirm` | Header `X-CSRF-TOKEN` | `{"code":"123456"}` | `SessionDto` với `twoFactorVerified` được cập nhật; setup confirm kích hoạt secret đã cấp trước. |
| `POST /auth/2fa/setup` | Header `X-CSRF-TOKEN` | — | `{"secret":"<base32>","provisioningUri":"otpauth://totp/..."}`; response đặt `Cache-Control: no-store`. |
| `POST /auth/logout` | Header `X-CSRF-TOKEN` | — | Session kết thúc và trả `SessionDto` chưa xác thực cùng CSRF token mới. |

### Response lệnh và lỗi

Các endpoint lệnh xóa/cập nhật đơn giản trả `CommandResultDto`:

```json
{"id":"12","status":"DELETED","version":2,"requestId":"<uuid>"}
```

`requestId` là UUID do client gửi qua `X-Request-Id` hoặc server tự sinh. Status và version cụ thể phụ thuộc lệnh; ví dụ tạo booking trả `CHO_DUYET`, còn xóa trả `DELETED`. Nếu request không hợp lệ, không đủ quyền, không thấy tài nguyên hoặc sai trạng thái, API trả error JSON như phần dưới; bảng trên ghi các trường hợp `409` quan trọng theo nghiệp vụ.

## Response và lỗi

Các endpoint nghiệp vụ trả JSON theo DTO của use case: lệnh thường có `id`, `status`, `version`, `requestId`; danh sách trả mảng hoặc DTO phân trang tương ứng; riêng quy trình thanh toán, hoàn tất sửa chữa và công ích có DTO kết quả riêng. Nên đọc trực tiếp response thay vì giả định mọi endpoint trả cùng một wrapper.

Lỗi chuẩn từ service/controller có dạng:

```json
{
  "code":"CONFLICT",
  "message":"Request conflicts with current data",
  "requestId":"09f89963-f7d9-4cf2-a36d-2b6b582297e1",
  "fieldErrors":{}
}
```

Các status thường gặp: `400` JSON/header/ID không hợp lệ; `401` chưa đăng nhập hoặc thông tin đăng nhập sai; `403` sai role/CSRF; `404` không tìm thấy hoặc không được phép xem tài nguyên; `409` xung đột trạng thái; `422` dữ liệu vi phạm ràng buộc; `503` gateway chưa cấu hình. Code lỗi cụ thể (`code`) là căn cứ để client xử lý; `message` dành cho hiển thị/chẩn đoán.

## Ghi chú triển khai

- Role hợp lệ trong luồng hiện tại gồm `SINHVIEN`, `ADMIN`, `KETOAN`, `BAOVE`, `KYTHUAT`.
- Callback thanh toán được miễn session/CSRF vì gọi từ gateway, nhưng vẫn cần adapter xác minh chữ ký và dữ liệu callback.
- Các thao tác ghi dữ liệu khác cần session đăng nhập và CSRF token.
- Khai báo khách cần cấu hình `KTX_PERSONAL_DATA_KEY` để mã hóa dữ liệu cá nhân; yêu cầu chứng nhận cần `KTX_CERTIFICATE_TOKEN_KEY` để ký token. Nếu thiếu cấu hình, service trả lỗi `503` tương ứng.
- Một số tích hợp ngoài như cổng thanh toán, barrier, worker thông báo và tạo/chứng thực PDF chưa cấu hình đầy đủ; endpoint có trong bảng không khẳng định tích hợp ngoài đã hoạt động.
- Route nguồn chuẩn: `server/src/main/java/vn/id/caohoangphuc/cnpm/api/ApiRoutes.java`; DTO request nằm trong `server/src/main/java/vn/id/caohoangphuc/cnpm/api/dto/`.

## Tải lên và xem minh chứng UC-04/UC-12

Các URL dưới đây có tiền tố `/api/v1`. Dùng session cookie như các API khác; upload bắt buộc có `X-CSRF-TOKEN`. Admin đã bật 2FA phải xác thực OTP trước khi xem tệp.

| Endpoint | Param/body | Response |
|---|---|---|
| `GET /booking-windows` | SINHVIEN hoặc ADMIN, không có body | Mảng `{id,name,semesterName,opensAt,closesAt}`; chỉ đợt đang mở và học kỳ ACTIVE/chưa kết thúc. |
| `POST /files` | `multipart/form-data`: `file` ảnh PNG/JPEG, `kind` = CCCD_FRONT/CCCD_BACK/STUDENT_CARD/REPAIR/EVIDENCE/POLICY. CCCD/thẻ chỉ SINHVIEN tải lên. | `{id,kind,mime,sizeBytes,sha256,contentUrl}`. Tệp DRAFT dùng để gửi hồ sơ trong 24 giờ. |
| `GET /files/{id}` | `id` UUID | Metadata cùng cấu trúc upload. Owner được đọc; ADMIN được đọc tệp ATTACHED gắn vào hồ sơ đăng ký. |
| `GET /files/{id}/content` | `id` UUID; `download=true` để tải thay vì xem inline | Byte ảnh, MIME PNG/JPEG, `Content-Disposition`, `Cache-Control: no-store, private`, `X-Content-Type-Options: nosniff`. |

Ví dụ upload (để curl tự tạo multipart boundary):

```sh
curl -b cookies.txt -H 'X-CSRF-TOKEN: TOKEN_SAU_DANG_NHAP'   -F 'kind=CCCD_FRONT' -F 'file=@front.png;type=image/png'   https://cnpm.caohoangphuc.id.vn/api/v1/files
```

Ảnh tối đa 5 MiB (5242880 byte), mỗi chiều tối đa 6000 pixel, tổng tối đa 20 megapixel. Backend đối chiếu MIME với nội dung, kiểm tra decoder và ghi lại ảnh bỏ metadata; không tin tên file. Quá kích thước trả 413; rỗng/sai loại/ảnh giả/kind sai trả 422; UUID sai trả 400; tệp không tồn tại hoặc không có quyền trả 404; chưa đăng nhập GET trả 401, thiếu CSRF upload trả 403. Nội dung được mã hóa AES-GCM trong database, không có URL ảnh công khai.

Sau khi tải đủ ba ảnh, gửi `POST /bookings` với `bedId`, `windowId` lấy từ `/booking-windows`, ba `fileIds` nhận từ upload và `acceptedTerms: true`; kèm `Idempotency-Key` UUID. Backend kiểm tra owner/kind/DRAFT/expiry và chuyển tệp sang ATTACHED trong cùng transaction tạo đơn. Tệp seed giữ nguyên ID nhưng được bổ sung PNG tổng hợp khi app khởi động. Quyền admin xem tệp dựa trên hồ sơ đã gắn; tệp nháp của người khác vẫn trả 404.
