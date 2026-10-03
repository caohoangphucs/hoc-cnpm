# Tài liệu API backend

Tài liệu này mô tả các HTTP route đang được khai báo trong `ApiRoutes` và được xử lý bởi controller/service hiện có. Base path là `/api/v1`; dữ liệu trao đổi dùng JSON UTF-8. Các request ghi dữ liệu hiện trả HTTP `200` khi thành công. Quy tắc nghiệp vụ và quyền truy cập được kiểm tra tại service.

## Đăng nhập, session và CSRF

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

Trong cấu hình hiện tại, đăng nhập không yêu cầu 2FA cho bất kỳ role nào. `twoFactorVerified` trong session response hiện trả `true` sau khi đăng nhập thành công; điều này phản ánh chế độ tạm tắt kiểm tra 2FA, không khẳng định authenticator đã được đăng ký. Các endpoint cài đặt/xác minh authenticator vẫn tồn tại cho Admin/Kế toán.

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

Trong bảng dưới, đường dẫn đã bao gồm `/api/v1`. `page` mặc định là `1`, `size` mặc định là `20` ở các endpoint danh sách.

| UC | Method và path | Role | Body/query và tác vụ |
|---|---|---|---|
| UC-04 | `POST /bookings` | `SINHVIEN` | Tạo đăng ký/giữ chỗ. Body xem [Đăng ký phòng](#uc-04-đăng-ký-phòng). Bắt buộc `Idempotency-Key`. |
| UC-12 | `POST /bookings/{id}/decision` | `ADMIN` | Duyệt hoặc từ chối đăng ký; body gồm `decision`, `reason`, `version`. |
| UC-17 | `POST /invoice-drafts` | `KETOAN` | Tạo bản xem trước hóa đơn theo tháng/phòng. |
| UC-17 | `POST /invoices/issue` | `KETOAN` | Phát hành từ bản xem trước; body gồm `draftId`, `version`. |
| UC-08 | `POST /payment-sessions` | `SINHVIEN` | Tạo phiên thanh toán cho hóa đơn của sinh viên. Hiện trả `503 PAYMENT_GATEWAY_UNAVAILABLE` nếu chưa cấu hình adapter tương ứng. |
| UC-08 | `GET /payment-sessions/{id}` | `SINHVIEN` là chủ hóa đơn, `KETOAN`, `ADMIN` | Đọc trạng thái phiên; `id` là UUID. |
| UC-08 | `POST /payment-callbacks/{gateway}` | Callback nhà cung cấp, không yêu cầu session/CSRF | Nhận tham số callback và chỉ xử lý sau khi adapter kiểm chữ ký. Chỉ hoạt động khi gateway adapter được cấu hình. |
| UC-22 | `POST /repairs` | `SINHVIEN` | Gửi yêu cầu sửa chữa. |
| UC-22 | `GET /repairs?page=1&size=20` | `SINHVIEN`, `KYTHUAT`, `ADMIN` | Danh sách phiếu mà người gọi được phép xem. |
| UC-22 | `POST /repairs/{id}/assignment` | `KYTHUAT`, `ADMIN` | Gán kỹ thuật viên, lịch dự kiến và `version`. |
| UC-22 | `PUT /repairs/{id}/schedule` | `KYTHUAT` | Đổi lịch/bảo hành; body gồm `version`, `scheduledAt`, `warrantyUntil`, `note`. |
| UC-22 | `POST /repairs/{id}/complete` | `KYTHUAT` | Hoàn tất phiếu; body gồm `version`, `note`, `supplies`, `warrantyUntil`. |
| UC-25 | `POST /entries` | `BAOVE` | Ghi nhận sự kiện thẻ ra/vào. |
| UC-25 | `POST /visitors` | `SINHVIEN` | Khai báo khách thăm. |
| UC-25 | `POST /visitors/{id}/approval` | `ADMIN` | Duyệt/từ chối khai báo khách, body cùng dạng `decision`, `reason`, `version`. |
| UC-25 | `POST /visitors/{id}/check-in` | `BAOVE` | Ghi nhận khách đến; không có body. |
| UC-25 | `POST /visitors/{id}/check-out` | `BAOVE` | Ghi nhận khách rời đi; không có body. |
| UC-14 | `GET /violations?page=1&size=20` | `ADMIN` | Danh sách vi phạm chờ xử lý. |
| UC-14 | `POST /violations/decisions` | `ADMIN` | Ra quyết định theo lô; body gồm `violationIds`, `decision`, `reason`, `evidenceFileId`, `versions`. |
| UC-14 | `POST /decisions/{id}/appeals` | `SINHVIEN` | Gửi khiếu nại, body `{"content":"..."}`. |
| UC-14 | `POST /appeals/{id}/response` | `ADMIN` | Phản hồi khiếu nại, body gồm `accepted`, `reason`, `version`. |
| UC-30 | `GET /activities?page=1&size=20` | `SINHVIEN`, `ADMIN` | Danh sách hoạt động. |
| UC-30 | `POST /activities` | `ADMIN` | Tạo hoạt động mới. |
| UC-30 | `POST /activities/{id}/registrations` | `SINHVIEN` | Đăng ký hoạt động; không có body. |
| UC-30 | `DELETE /activities/{id}/registrations` | `SINHVIEN` | Hủy đăng ký, body `{"version":1}`. |
| UC-30 | `POST /activities/attendance` | `ADMIN` | Điểm danh bằng QR, body `{"qrToken":"..."}`. |
| UC-30 | `POST /activities/completion` | `ADMIN` | Xác nhận hoàn thành theo lô, body `{"registrationIds":[1,2]}`. |
| UC-30 | `GET /activities/points?semesterId={id}` | `SINHVIEN` | Xem điểm hoạt động của học kỳ. |
| UC-30 | `POST /activities/conversions` | `SINHVIEN` | Gửi yêu cầu quy đổi, body `{"semesterId":1,"drl":1}`. |
| UC-30 | `POST /activities/certificates` | `SINHVIEN` | Yêu cầu chứng nhận, body `{"semesterId":1}`. |

## Cấu trúc body theo UC

### UC-04 đăng ký phòng

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
