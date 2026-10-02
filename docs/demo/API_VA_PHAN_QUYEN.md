# API, phân quyền và bảo vệ dữ liệu

API JSON dùng prefix `/api/v1` và được mô tả bằng [OpenAPI](api/openapi.json). Route được khai báo tập trung trong `ApiRoutes`; handler không đặt annotation route trên từng phương thức. `AuthController` xử lý login/session/logout, và `BookingController` bắt đầu nối `POST /bookings` với service. Các UC còn lại chưa có controller. Kiến trúc tại [Spring Boot](SPRING_BOOT.md).

## Giao thức chung

Session JSESSIONID cookie HttpOnly, Secure trong HTTPS, SameSite=Lax; idle timeout 15 phút, rotate ID sau login. ADMIN/KETOAN phải hoàn tất 2FA trước truy cập API bảo vệ. GET chỉ đọc. POST/PATCH/DELETE yêu cầu X-CSRF-TOKEN và Origin hợp lệ, ngoại trừ đúng hai IPN được xác thực bằng chữ ký provider. Login cũng kiểm CSRF; GET /auth/session public cung cấp token ban đầu và trạng thái chưa login. Login giới hạn 5 lần sai/15 phút/account + IP; thông báo không tiết lộ account tồn tại.

POST nghiệp vụ dùng Idempotency-Key UUID. Giữ kết quả 24 giờ; cùng key/cùng payload trả lại response, khác payload trả 409. Idempotency tài chính (provider_ref/event) lưu suốt vòng đời chứng từ, không hết hạn cùng cache API. Request ID UUID trong header X-Request-Id và response lỗi. Thời gian RFC3339 có offset; month YYYY-MM; ID số nguyên dương, UUID dùng format uuid. List mặc định page=1/pageSize=20, tối đa 100, sort có whitelist; server không nhận raw SQL sort.

Lỗi `{code,message,requestId,fieldErrors:{field:reason}}`: 400 cú pháp JSON/query, 401 chưa đăng nhập/2FA, 403 quyền/CSRF, 404 không có hoặc không sở hữu tài nguyên, 409 trạng thái/xung đột/key khác payload, 413 quá dung lượng, 415 MIME không hợp lệ, 422 nghiệp vụ đầu vào, 429 giới hạn, 503 DB/tích hợp tạm lỗi. Không trả hash/CCCD/stacktrace. Kiểm sở hữu dựa session, không dựa studentId/actorId từ body.

## Ma trận quyền

| Nhóm endpoint | SV | ADMIN | KETOAN | KYTHUAT | BAOVE |
|---|---|---|---|---|---|
| Phòng/giường trống | đọc | đọc | — | — | — |
| Hồ sơ/đăng ký phòng | tạo/đọc của mình | đọc/duyệt/từ chối | — | — | — |
| Hóa đơn | đọc hóa đơn phòng mình | đọc | chỉ số/preview/phát hành/đọc | — | — |
| Phiên thanh toán | tạo/đọc của mình | đọc đối soát | đọc/đối soát | — | — |
| Sửa chữa | tạo/đọc của mình | đọc/gán | — | tiếp nhận/hoàn thành phiếu được gán | — |
| Ra/vào/khách thăm | đọc log mình | đọc/báo cáo/duyệt khách | — | — | ghi/quét/đọc log trực ca |
| Kỷ luật | đọc mình/khiếu nại | xem/xử lý/trả lời | — | — | tạo minh chứng vi phạm |
| Công ích | đọc/đăng ký/hủy/điểm/quy đổi/giấy của mình | quản lý/xác nhận | — | — | — |
| IPN | không dùng session: chỉ chữ ký provider + amount/ref/partner hợp lệ |
| Verify giấy | công khai bằng token không đoán được; chỉ trạng thái/định danh tối thiểu, không CCCD/điểm chi tiết |

ADMIN không mặc nhiên được thanh toán thay SV hoặc hoàn thành phiếu kỹ thuật. Service kiểm quyền lần nữa ở thao tác ghi; DAO/SP kiểm các ràng buộc dữ liệu. Quyền hiện tại khác role hợp lệ trả 403, tài nguyên thuộc SV khác trả 404.

## Endpoint hỗ trợ và tích hợp

- Upload `POST /files` multipart: file JPG/PNG <=5MB, loại hồ sơ/minh chứng, tối đa 5 ảnh/phiếu. Kiểm magic bytes, decode ảnh, quét malware, strip EXIF, tên object UUID; trả fileId. Dữ liệu AES-256-GCM bằng khóa ngoài DB, mỗi tệp nonce riêng; TTL DRAFT 24 giờ nếu chưa gắn hồ sơ. Tải tệp qua `/files/{id}` kiểm owner hoặc quyền nghiệp vụ; không có public bucket.
- `POST /auth/login`, `/auth/2fa`, `/auth/2fa/setup`, `/auth/2fa/setup/confirm`, `/auth/logout`; `GET /auth/session` public trả authenticated; khi chưa login chỉ csrfToken, khi đã login thêm role, trạng thái 2FA và idleExpiresAt. ADMIN/KETOAN chưa đăng ký dùng `/auth/2fa/setup` để lấy khóa và provisioning URI; xác nhận mã bằng `/auth/2fa/setup/confirm`. Secret lưu mã hóa AES-GCM với `TOTP_ENCRYPTION_KEY`; `/auth/2fa` kiểm mã các lần đăng nhập sau.
- `GET /notifications` và PATCH trạng thái đọc thuộc người dùng; worker mail/SMS không có endpoint công khai để gọi gửi tùy ý.
- VNPay IPN GET `/payments/vnpay/ipn`, MoMo IPN POST `/payments/momo/ipn`; return URL GET chỉ hiển thị kết quả lấy từ server.
- `POST /activities/{id}/attendance` chỉ ADMIN quét QR hợp lệ của hoạt động; không cộng điểm tại endpoint này.
- `POST /violations/{id}/appeals`, `POST /appeals/{id}/response`, `POST /visitors`, `POST /visitors/{id}/approval` xử lý nhánh theo SRS với audit/outbox.

## UI và xử lý lỗi

Giữ 5 bước thiết kế trong THIET_KE_XU_LY. Dùng loading, chống double click, empty state, validation tại trường và requestId để tra lỗi. 409 giữ dữ liệu nhập, refresh trạng thái và đề nghị thao tác lại; không tự retry thanh toán bằng phiên mới. 503 thử lại cùng key; 401 chuyển login và chỉ khôi phục draft không nhạy cảm. CSV/Excel import chỉ tạo bản nháp, preview dòng lỗi; giới hạn 1000 dòng, không chạy macro/formula và không phát hành ngầm.

Spring SecurityFilterChain xử lý session/CSRF/2FA; ownership được kiểm lại trong service. Route dùng `RouterFunction` tập trung tại `ApiRoutes`; middleware bảo mật chuẩn hóa lỗi 401/403. Chi tiết [Spring Boot](SPRING_BOOT.md).
