# PostgreSQL: chuẩn triển khai và kết quả rà soát

## Chuẩn dùng cho demo

PostgreSQL **16**; schema triển khai tham chiếu `database/01_schema.sql`, seed `02_seed.sql`. Schema cũ trong docs/other chỉ tham khảo, không nạp cùng schema đang chạy. Không cần MySQL hoặc SQL Server để chạy ứng dụng KTX. Nội dung bài tập/đề thi nói đến DB khác không phải cấu hình ứng dụng.

| Môi trường | Host | Port | Database/user mặc định |
|---|---|---|---|
| App trong Compose | cnpm-db | 5432 | cnpm_db / cnpm_user |
| Java/psql trên máy host qua Compose | localhost | 54321 | cnpm_db / cnpm_user |
| CI PostgreSQL service | 127.0.0.1 | 5432 | cnpm_db / cnpm_user |

Password lấy từ biến môi trường/secret; không ghi secret production trong docs hoặc Git. Java chạy host không tự đọc .env. Cấu hình kỳ vọng: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS`; Compose truyền cho cả DB/app. `PORT_DB` là cổng host, không dùng làm cổng container.

JDBC URL: `jdbc:postgresql://HOST:PORT/DB`. HikariCP đặt `escapeSyntaxCallMode=callIfNoReturn` cho `{call procedure(...)}`. `usp_DangNhap` là function trả bảng, phải dùng PreparedStatement với `SELECT * FROM usp_DangNhap(?)`; các procedure khác dùng CallableStatement, đăng ký toàn bộ OUT theo đúng thứ tự. Thiết kế này theo [tài liệu pgJDBC](https://jdbc.postgresql.org/documentation/callproc/).

Connection đặt `options=-c TimeZone=Asia/Ho_Chi_Minh`; kiểm tra `SHOW timezone`. Đích Spring Boot: @Transactional tại Service và JdbcTemplate tại @Repository dùng Connection do Spring quản lý; không commit/rollback thủ công ở repository hoặc trong procedure. Xem [Spring Boot](SPRING_BOOT.md). Đọc status OUT và SQLState, không trả stack trace/SQL ra trình duyệt.

## Sai lệch hiện có cần sửa khi bắt đầu code

| Vị trí | Hiện trạng | Đích |
|---|---|---|
| AGENTS.md | Đã thống nhất mô tả PostgreSQL 16 | Giữ nhất quán với tài liệu này |
| .env.example | MySQL/cổng 33061/DB_ROOT_PASS | PostgreSQL, host port 54321, bỏ biến root MySQL |
| docker-compose.override.yml | DB_PORT=3306 | 5432 trong mạng Compose |
| DBContext.java | Host mặc định 5432; chưa có escapeSyntaxCallMode | Host 54321, container 5432; chế độ CALL và timezone |
| AccountDAO.java | CALL function; fallback mật khẩu khi BCrypt lỗi | SELECT function; hash lỗi phải thất bại |
| database/02_seed.sql | Hash demo đã sửa thành BCrypt hợp lệ | SQL thủ công chỉ thay hash mẫu đã biết, BCrypt hợp lệ cho 123456 |
| scripts/reset-db.ps1 | cnpm-mysql/mysql CLI/root password | docker compose exec -T cnpm-db + psql, ON_ERROR_STOP=1; chờ healthy, nạp *.sql theo tên |
| docker-compose.yml | PostgreSQL đúng, thông tin hardcode, chưa healthcheck | Biến môi trường đồng bộ, pg_isready, app chờ service_healthy |
| .github/workflows/deploy.yml | Chỉ nạp 01/02 | Nạp01/02 trên DB test sạch; build và kiểm tra JDBC PostgreSQL |
| usp_DatGiuong | Giữ 3 ngày | 48 giờ, kiểm tra thời hạn/khóa theo BR-03/04 |
| usp_GhiNhanRaVao | >=23:00 | >23:00; giờ Việt Nam theo BR-12 |
| usp_XacNhanThanhToan | Callback lặp có thể cộng tiền lại | BR-09/10, khóa và idempotency |

**Các sửa runtime thử trong lượt rà soát đã được hoàn tác theo yêu cầu chỉ làm docs.** Bảng trên là công việc triển khai, không phải danh sách đã sửa code.

## Khởi tạo và cập nhật schema thủ công

Nguồn duy nhất: database/01_schema.sql và02_seed.sql. Không dùng Flyway, plugin migrate hay migration ở resources. Database demo mới nạp01 rồi02; Docker entrypoint chỉ chạy trên volume mới, restart không nạp lại.

01_schema có DROP TABLE: chỉ khởi tạo/reset DB demo có chủ đích. DB cần giữ dữ liệu phải backup/preflight, chạy SQL ALTER/CREATE OR REPLACE đã review theo chênh lệch rồi kiểm hậu điều kiện; không chạy lại01/02. Dùng psql -v ON_ERROR_STOP=1 --single-transaction cho script phù hợp; index concurrent phải chạy riêng. Ghi script/checksum và người thực hiện trong biên bản triển khai.

Script reset hiện tại vẫn còn lệnh MySQL; phải sửa trước khi dùng PostgreSQL. App Spring Boot đích đặt spring.sql.init.mode=never, không tự DDL/seed. Chi tiết [Spring Boot](SPRING_BOOT.md).

## Kiểm tra đã thực hiện

02/10/2026: trên container PostgreSQL 16 tạm riêng, `01_schema.sql` và `02_seed.sql` nạp thành công với ON_ERROR_STOP; có 19 bảng, 5 view, 1 function đăng nhập và 7 procedure. Không reset DB của dự án/người dùng.

Một bản sửa JDBC tạm đã build thành công và kiểm tra function đăng nhập/BCrypt cùng 7 procedure có OUT; bản sửa đã hoàn tác. Kết quả chứng minh hướng sửa kỹ thuật, **không chứng minh code hiện tại chạy đúng**. Test chức năng 8 UC, IPN, phân quyền và cạnh tranh chưa chạy.

Đích thay DBContext bằng DataSource auto-configured, HikariCP và JdbcTemplate; cấu hình SPRING_DATASOURCE_* và profiles, giữ PostgreSQL16. Baseline kiểm tra JDBC trước đây chỉ là kiểm tra legacy, chưa kiểm Spring Boot.

Inventory tại a273bb0 đã cập nhật:27bảng/5view/1function/9procedure. Kiểm19bảng/7procedure ở phần trên là kết quả baseline cũ, không phải số lượng hiện tại.
