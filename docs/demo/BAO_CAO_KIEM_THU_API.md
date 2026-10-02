# Báo cáo kiểm thử API theo C06-D2

Ngày chạy: 02/10/2026. Fixture dùng PostgreSQL 16 riêng, nạp `database/01_schema.sql` và `database/02_seed.sql`; không dùng dữ liệu production.

## Kết quả lần chạy

| Nhóm | Số ca | PASS | FAIL |
|---|---:|---:|---:|
| Unit / Controller Tests | 10 | 10 | 0 |
| Integration & Black-box Tests | 28 | 28 | 0 |
| **Tổng cộng** | **38** | **38** | **0** |

Tỷ lệ đạt của lần chạy này là 100%. API suite chiếm 17 ca trong nhóm integration. Toàn bộ các ca kiểm thử đều vượt qua thành công, không phát sinh lỗi nghiêm trọng.

## Ca HTTP API UC-04 (Đăng ký phòng ký túc xá)

Các ca gọi server Spring Boot qua HTTP API, đăng nhập lấy JWT token/session cookie và gửi header xác thực. Test đối chiếu cả HTTP response status và trạng thái dữ liệu trong PostgreSQL.

| ID | Ca kiểm thử và kết quả thực tế | Kết quả |
|---|---|---|
| API-04-01 | Đơn được tạo `CHO_DUYET`; giường `GIU_CHO`; hồ sơ `VALID`; file gắn; hold 48 giờ; có audit/outbox. Thực tế khớp. | PASS |
| API-04-02 | Request không đăng nhập phải nhận `401 UNAUTHENTICATED`, không tạo đơn. Thực tế khớp. | PASS |
| API-04-03 | Bảo vệ gọi API đăng ký phòng phải nhận `403 FORBIDDEN`, không tạo đơn. Thực tế khớp. | PASS |
| API-04-04 | Request đã đăng nhập nhưng thiếu CSRF/Token phải nhận `403 FORBIDDEN`, không tạo đơn. Thực tế khớp. | PASS |
| API-AUTH-01 | Admin gửi OTP khi chưa enroll phải nhận `503 TWO_FACTOR_NOT_CONFIGURED`. Thực tế khớp. | PASS |
| API-04-05 | Không đồng ý điều khoản hoặc thiếu số file tối thiểu của DTO phải nhận `422`, không tạo đơn. Thực tế khớp. | PASS |
| API-04-06 | Hai file chạm biên DTO nhưng thiếu loại giấy tờ bắt buộc phải nhận `422`, không giữ giường. Thực tế khớp. | PASS |
| API-04-07 | UUID file không tồn tại phải nhận `404 NOT_FOUND`, không tạo đơn. Thực tế khớp. | PASS |
| API-04-08 | File đã hết hạn phải nhận `422`, không tạo đơn. Thực tế khớp. | PASS |
| API-04-09 | Năm file hợp lệ (biên trên) được chấp nhận và gắn với hồ sơ. Thực tế khớp. | PASS |
| API-04-10 | Sáu file vượt giới hạn phải nhận `422`; đơn không được tạo. Thực tế khớp. | PASS |
| API-04-11 | Đợt đăng ký đã đóng phải nhận `422`; giường vẫn `TRONG`. Thực tế khớp. | PASS |
| API-04-12 | Sinh viên nam chọn tòa nữ phải nhận `422`; giường vẫn `TRONG`. Thực tế khớp. | PASS |
| API-04-13 | Giường không khả dụng phải nhận `409`; không tạo đơn. Thực tế khớp. | PASS |
| API-04-14 | Sinh viên đã lưu trú phải nhận `409`; không tạo đơn, giường không đổi. Thực tế khớp. | PASS |
| API-04-15 | Cùng key/cùng body trả cùng đơn; cùng key/body khác nhận `409`; chỉ có một đơn. Thực tế khớp. | PASS |
| API-04-16 | Hai sinh viên tranh cùng giường: một request `200`, request kia `409`, chỉ một đơn. Thực tế khớp. | PASS |

> *Ghi chú kiểm thử tải file: Các ca kiểm tra dung lượng/MIME file được kiểm thử thông qua lớp DTO Validator và Service fixture trong môi trường test.*

## Phạm vi kiểm thử tích hợp

Đợt kiểm thử tập trung vào bộ Black-box API Endpoints cho phân hệ Đăng ký phòng UC-04 (`BookingApiBlackBoxIT`), kiểm thử xác thực 2FA (`TotpServiceTest`), kiểm thử phân quyền tầng Service (`ServiceAuthorizationIT`) và tích hợp kiểm thử các Controllers (`RemainingUseCasesApiBlackBoxIT`).

C06-D2 yêu cầu mỗi Use Case tối thiểu một happy path, hai ca negative, hai ca boundary và một ca cho mỗi alternative flow. Bộ API ở trên đã kiểm tra đầy đủ các kịch bản cho phân hệ UC-04 và các ràng buộc phân quyền cốt lõi.
