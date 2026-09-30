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

Connection đặt `options=-c TimeZone=Asia/Ho_Chi_Minh`; kiểm tra `SHOW timezone`. Transaction nhiều DAO phải dùng cùng Connection, autoCommit=false, commit một lần, rollback mọi lỗi; không tự COMMIT trong procedure. Đọc status OUT và SQLState, không trả stack trace/SQL ra trình duyệt.

## Sai lệch hiện có cần sửa khi bắt đầu code

| Vị trí | Hiện trạng | Đích |
|---|---|---|
| AGENTS.md | Đã thống nhất mô tả PostgreSQL 16 | Giữ nhất quán với tài liệu này |
| .env.example | MySQL/cổng 33061/DB_ROOT_PASS | PostgreSQL, host port 54321, bỏ biến root MySQL |
| docker-compose.override.yml | DB_PORT=3306 | 5432 trong mạng Compose |
| DBContext.java | Host mặc định 5432; chưa có escapeSyntaxCallMode | Host 54321, container 5432; chế độ CALL và timezone |
| AccountDAO.java | CALL function; fallback mật khẩu khi BCrypt lỗi | SELECT function; hash lỗi phải thất bại |
| database/02_seed.sql | Hash demo cần xác minh/sửa | Migration chỉ thay hash mẫu đã biết, BCrypt hợp lệ cho 123456 |
| scripts/reset-db.ps1 | cnpm-mysql/mysql CLI/root password | docker compose exec -T cnpm-db + psql, ON_ERROR_STOP=1; chờ healthy, nạp *.sql theo tên |
| docker-compose.yml | PostgreSQL đúng, thông tin hardcode, chưa healthcheck | Biến môi trường đồng bộ, pg_isready, app chờ service_healthy |
| .github/workflows/deploy.yml | Chỉ nạp 01/02 | Nạp migration có thứ tự; build và kiểm tra JDBC PostgreSQL |
| usp_DatGiuong | Giữ 3 ngày | 48 giờ, kiểm tra thời hạn/khóa theo BR-03/04 |
| usp_GhiNhanRaVao | >=23:00 | >23:00; giờ Việt Nam theo BR-12 |
| usp_XacNhanThanhToan | Callback lặp có thể cộng tiền lại | BR-09/10, khóa và idempotency |

**Các sửa runtime thử trong lượt rà soát đã được hoàn tác theo yêu cầu chỉ làm docs.** Bảng trên là công việc triển khai, không phải danh sách đã sửa code.

## Khởi tạo và migration

Database mới nạp 01_schema → 02_seed → migration mới theo thứ tự tên. Docker entrypoint chỉ chạy trên volume mới; restart không tự nạp lại. 01_schema có DROP TABLE, chỉ dùng khởi tạo/reset DB local có chủ đích; không dùng làm migration production.

DB có dữ liệu: backup, kiểm tra constraint/dữ liệu hiện hữu, áp dụng từng migration mới bằng `psql -v ON_ERROR_STOP=1 --single-transaction -f FILE.sql`, ghi version/checksum. Không chạy lại 01/02. Migration thay SP dùng CREATE OR REPLACE, giữ chữ ký để DAO tương thích; đổi chữ ký phải cập nhật contract/API và kiểm tra caller. Index concurrent cần tách khỏi transaction.

Khi script reset được sửa, cần cảnh báo xóa dữ liệu, yêu cầu xác nhận tại script, resolve đường dẫn từ thư mục repo và dừng ngay nếu docker/psql lỗi. PowerShell phải truyền UTF-8 để giữ tiếng Việt. Không dùng script reset hiện tại trước khi sửa.

## Kiểm tra đã thực hiện

02/10/2026: trên container PostgreSQL 16 tạm riêng, `01_schema.sql` và `02_seed.sql` nạp thành công với ON_ERROR_STOP; có 19 bảng, 5 view, 1 function đăng nhập và 7 procedure. Không reset DB của dự án/người dùng.

Một bản sửa JDBC tạm đã build thành công và kiểm tra function đăng nhập/BCrypt cùng 7 procedure có OUT; bản sửa đã hoàn tác. Kết quả chứng minh hướng sửa kỹ thuật, **không chứng minh code hiện tại chạy đúng**. Test chức năng 8 UC, IPN, phân quyền và cạnh tranh chưa chạy.
