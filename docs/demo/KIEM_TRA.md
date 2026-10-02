# Kiểm tra bộ tài liệu demo

Ngày: 02/10/2026. Tài liệu trước trên develop `82b52a1`; cập nhật Spring Boot trên nhánh docs/codex-spring-boot.

| Hạng mục | Kết quả |
|---|---|
| Nguồn | Snapshot SRS cũ nguyên byte từ `265ecc8`, 8 UC chi tiết; giữ SRS hiện hành toàn hệ thống |
| Quy tắc | 17 BR, Spring Boot4.1.1/Java17 + PostgreSQL16,48h,giờ Việt Nam,tiền VND và chống lặp |
| Thiết kế xử lý | 8 UC, mỗi UC 5 bước; liên kết Class/sequence/quy tắc |
| Diagram | 8 sequence + 2 Class Diagram qua PlantUML check/render; SVG XML hợp lệ; xem render Class và sequence |
| API | OpenAPI3.0.3 được kiểm bằng openapi-spec-validator; schema input/output/error và headers |
| Kiểm thử thiết kế | 59 test case (51 nghiệp vụ + 8 chuyển Spring Boot) có fixture/action/expected; tất cả NOT_RUN, chưa chạy chức năng |
| PostgreSQL baseline | Schema/seed nạp thành công trên container PG16 riêng;19bảng/5view/1function/7procedure |
| Thử hướng sửa JDBC | Build và kiểm OUT/function trên bản sửa tạm thành công; bản sửa runtime đã hoàn tác |
| Liên kết | File local trong Markdown được kiểm tra tồn tại |
| Phạm vi thay đổi | Chỉ tài liệu/diagram/JSON đặc tả và AGENTS; không giữ thay đổi Java/JSP/SQL/Compose/script/CI |
| Nghiệm thu app | Chưa chạy test8UC, chưa tích hợp sandbox/provider và chưa nghiệm thu |

Các lỗi code/cấu hình phát hiện được ghi trong POSTGRESQL.md để xử lý ở giai đoạn triển khai. Không dùng kết quả thử bản sửa tạm để tuyên bố code hiện tại đã kết nối/call đúng. Container kiểm tra riêng đã được dọn; không thay dữ liệu DB thật. File lock Word của người dùng giữ nguyên.

Spring Boot: xác minh phiên bản/dependency/transaction/security theo tài liệu chính thức được dẫn trong SPRING_BOOT.md. 10 diagram render lại theo class Spring Boot; OpenAPI1.1.0 kiểm hợp lệ; toàn bộ test vẫn NOT_RUN, chưa có Boot runtime.

## Sau khi bỏ migration và cập nhật ảnh

- Gỡ dependency/plugin Flyway và2file V1/V2; Maven clean verify đạt, dependency tree/ROOT.war không còn Flyway/migration.
- Schema/seed SQL giữ nguyên; quản lý bằng SQL thủ công, không chạy DDL/seed ở app startup.
-8sequence+2class render PNG/SVG trực tiếp từ nguồn Spring Boot; không có scrollbar/cắt viewport.
- ERD hiện hành27bảng dựng lại từ SQL, kiểm tên trường/kiểu/PK/FK/UQ; ERD19bảng là lịch sử. Render cả2nguồn, tổng12nguồn diagram.
- Ảnh alias main/ClassDiagram,main/ERD,D10/SequenceDiagram được cập nhật từ nguồn chuẩn và ghi mapping tại docs/main/README.md.
- Kiểm liên kết Markdown, SVG XML và PlantUML đạt; OpenAPI không đổi contract. Các lỗi sequence/fixture/UC30 trong review trước chưa sửa ở đợt này.

## Hotfix DB và CI/CD (2026-10-02)

Đã sao lưu và sửa collation tham số usp_DangNhap trên server MySQL cũ. Hash của 5 tài khoản demo khớp hash seed sai đã biết; sửa có điều kiện, không ghi đè mật khẩu đã đổi. Cả 5 tài khoản đăng nhập 302/dashboard 200, không có lỗi SQL mới. Seed PostgreSQL sửa hash mẫu/fixture và đồng bộ SERIAL cuối file.

Preflight riêng đã copy 20 bảng nguồn/650 dòng (19 bảng dự án, 1 bảng ngoài schema lưu legacy_archive) vào PostgreSQL trong transaction; MySQL giữ nguyên. Đăng nhập/dashboard 5 roles và MySQL collation regression được kiểm tra trước push. CI chạy mọi push; CD develop cần cả hai job xanh, dùng image cùng SHA, health check/rollback và không replay schema/seed có dữ liệu. Xem [CI/CD](../main/CI_CD.md).
