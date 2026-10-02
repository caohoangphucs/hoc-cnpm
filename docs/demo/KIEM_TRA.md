# Kiểm tra bộ tài liệu demo

Ngày: 02/10/2026. Baseline develop `0c08ae7`, nhánh docs/codex-sequence-diagrams.

| Hạng mục | Kết quả |
|---|---|
| Nguồn | Snapshot SRS cũ nguyên byte từ `265ecc8`, 8 UC chi tiết; giữ SRS hiện hành toàn hệ thống |
| Quy tắc | 17 BR, PostgreSQL16,48h,giờ Việt Nam,tiền VND và chống lặp |
| Thiết kế xử lý | 8 UC, mỗi UC 5 bước; liên kết Class/sequence/quy tắc |
| Diagram | 8 sequence + 2 Class Diagram qua PlantUML check/render; SVG XML hợp lệ; xem render Class và sequence |
| API | OpenAPI3.0.3 được kiểm bằng openapi-spec-validator; schema input/output/error và headers |
| Kiểm thử thiết kế | 51 test case có fixture/action/expected; tất cả NOT_RUN, chưa chạy chức năng |
| PostgreSQL baseline | Schema/seed nạp thành công trên container PG16 riêng;19bảng/5view/1function/7procedure |
| Thử hướng sửa JDBC | Build và kiểm OUT/function trên bản sửa tạm thành công; bản sửa runtime đã hoàn tác |
| Liên kết | File local trong Markdown được kiểm tra tồn tại |
| Phạm vi thay đổi | Chỉ tài liệu/diagram/JSON đặc tả và AGENTS; không giữ thay đổi Java/JSP/SQL/Compose/script/CI |
| Nghiệm thu app | Chưa chạy test8UC, chưa tích hợp sandbox/provider và chưa nghiệm thu |

Các lỗi code/cấu hình phát hiện được ghi trong POSTGRESQL.md để xử lý ở giai đoạn triển khai. Không dùng kết quả thử bản sửa tạm để tuyên bố code hiện tại đã kết nối/call đúng. Container kiểm tra riêng đã được dọn; không thay dữ liệu DB thật. File lock Word của người dùng giữ nguyên.
