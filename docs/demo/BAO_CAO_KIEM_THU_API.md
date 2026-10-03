# Báo cáo kiểm thử API theo C6-D2

Ngày chạy: 03/10/2026. Fixture dùng PostgreSQL 16 riêng, nạp `database/01_schema.sql` và `database/02_seed.sql`; không dùng dữ liệu production.

## Kết quả lần chạy

| Nhóm | Số ca | PASS | FAIL |
|---|---:|---:|---:|
| Unit/controller | 10 | 10 | 0 |
| Integration PostgreSQL | 28 | 28 | 0 |
| Tổng | 38 | 38 | 0 |

Tỷ lệ đạt của lần chạy này là 100%. API suite chiếm 17 ca trong nhóm integration. Không có test thất bại nên lần chạy này không phát sinh bug report.

## Ca HTTP API UC-04

Các ca gọi server Spring Boot qua HTTP, đăng nhập bằng API, giữ session cookie và gửi CSRF token. Test đối chiếu cả HTTP response và trạng thái trong PostgreSQL.

| ID | Ca và kết quả thực tế | Kết quả |
|---|---|---|
| API-04-01 | Đơn được tạo `CHO_DUYET`; giường `GIU_CHO`; hồ sơ `VALID`; file gắn; hold 48 giờ; có audit/outbox. Thực tế khớp. | PASS |
| API-04-02 | Request không đăng nhập phải nhận `401 UNAUTHENTICATED`, không tạo đơn. Thực tế khớp. | PASS |
| API-04-03 | Bảo vệ gọi API đăng ký phòng phải nhận `403 FORBIDDEN`, không tạo đơn. Thực tế khớp. | PASS |
| API-04-04 | Request đã đăng nhập nhưng thiếu CSRF phải nhận `403 FORBIDDEN`, không tạo đơn. Thực tế khớp. | PASS |
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

Các ca file upload dung lượng/MIME không được kiểm qua HTTP vì chưa có endpoint `/files`; fixture đưa file metadata trực tiếp vào DB test. Đây không phải bằng chứng đã kiểm thử endpoint upload.

## Giới hạn phạm vi API

`ApiRoutes` hiện chỉ khai báo auth và `POST /api/v1/bookings`. Các UC-08, UC-12, UC-14, UC-17, UC-22, UC-25 và UC-30 chưa có route/controller, vì vậy chưa thể chạy test chức năng HTTP cho bảy UC này. `ServiceAuthorizationIT` kiểm tra service trực tiếp, không được tính là API black-box test.

C6-D2 yêu cầu mỗi UC tối thiểu một happy path, hai ca negative, hai ca boundary và một ca cho mỗi alternative flow. Bộ API ở trên đã kiểm tra các nhóm này cho UC-04; mục tiêu cho tám UC chưa hoàn tất cho tới khi các API còn thiếu được triển khai và có test HTTP tương ứng.

Tài liệu cũng đặt branch coverage làm mức tối thiểu cho unit test. Repo hiện chưa cấu hình công cụ đo branch coverage, vì vậy báo cáo này không gán phần trăm branch coverage.
