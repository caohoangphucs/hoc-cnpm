# Kết quả kiểm tra dự án

Ngày 02/10/2026.

| Hạng mục | Kết quả |
|---|---|
| SRS nguồn | Snapshot SRS cũ nguyên byte từ 265ecc8; chọn 8 UC; giữ SRS hiện hành |
| Thiết kế | 17 BR; 59 test case thiết kế (51 nghiệp vụ + 8 Spring Boot), chưa chạy E2E |
| Diagram | 8 sequence, 2 class; ERD27 theo SQL và ERD19 lịch sử; 12 PNG/SVG đầy đủ khung |
| API | OpenAPI3.0.3, 44 operation; input/output/error và headers đã kiểm hợp lệ |
| DB | PostgreSQL16: 27 bảng, 5 view, 1 function, 9 procedure; schema/seed đã kiểm |
| Authentication | BCrypt Java tương thích, hash sai/mật khẩu sai thất bại; 5 roles đăng nhập/dashboard đã kiểm |
| Sequence | SERIAL được đồng bộ sau toàn bộ fixture; tránh trùng ID explicit |
| CI/CD | Mọi push chạy CI; develop deploy image đúng SHA sau CI, health check và rollback |
| Server | PostgreSQL đã deploy qua Actions thành công tại 4633afe; dữ liệu được bảo toàn |
| Runtime | Servlet/JSP WAR hiện hành; Spring Boot4.1.1/Java17 là kiến trúc tài liệu đích |

Sau đợt dọn, code/config/script/CI chỉ dùng PostgreSQL; không có thành phần DB khác trong quy trình vận hành. CI bổ sung kiểm OUT/JDBC cả 9 procedure; Compose/env/reset đồng bộ cổng và credentials. Nguồn DDL/seed duy nhất nằm trong database/, CD không chạy DDL/seed trên DB có dữ liệu.

Các test thiết kế 8 UC, IPN/sandbox, kiểm quyền/ownership đầy đủ và tranh chấp cần triển khai/kiểm riêng trước nghiệm thu. CI xanh không thay thế nghiệm thu E2E. File lock Word người dùng được giữ nguyên.
