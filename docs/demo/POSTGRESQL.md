# PostgreSQL: cấu hình và vận hành

## Cấu hình hiện hành

Ứng dụng chỉ dùng PostgreSQL **16**. Nguồn schema/seed: `database/01_schema.sql` và `database/02_seed.sql`. Schema có 27 bảng, 5 view, 1 function đăng nhập và 9 procedure.

| Môi trường | Host | Port | Database/user mặc định |
|---|---|---|---|
| App trong Compose | cnpm-db | 5432 | cnpm_db / cnpm_user |
| Java/psql trên host qua Compose | localhost | 54321 | cnpm_db / cnpm_user |
| CI PostgreSQL service trên host | 127.0.0.1 | cấp động cho từng run | cnpm_db / cnpm_user |
| CD trên server | cnpm-postgres | 5432 | runtime.env riêng |

Compose đọc `.env` với PORT_WEB, PORT_DB, DB_NAME, DB_USER, DB_PASS; truyền cấu hình đồng bộ cho DB/app và chờ PostgreSQL healthy. Override chỉ mount JSP phục vụ hot reload, không đổi cổng DB. PORT_DB là cổng host, không phải cổng container.

Java chạy host không tự đọc `.env`; cần export DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASS. DBContext mặc định host localhost:54321, trong container cnpm-db:5432; CI/CD truyền giá trị tường minh. JDBC URL: `jdbc:postgresql://HOST:PORT/DB`. HikariCP đặt `escapeSyntaxCallMode=callIfNoReturn` và timezone Asia/Ho_Chi_Minh.

Function đăng nhập được gọi qua PreparedStatement `SELECT * FROM usp_DangNhap(?)`. Procedures dùng CallableStatement/CALL, khai báo đầy đủ OUT đúng thứ tự. CI kiểm JDBC binding cả 9 procedure trong transaction rồi rollback. Tài khoản demo dùng BCrypt tương thích thư viện Java cho 123456; hash hỏng/mật khẩu sai bị từ chối. SERIAL được đồng bộ sau toàn bộ seed.

## Khởi tạo và cập nhật SQL

DB demo mới nạp 01 rồi 02 qua psql với ON_ERROR_STOP. Docker entrypoint chỉ nạp trên volume mới; restart không nạp lại. Không có framework quản lý phiên bản schema tự động và app không tự DDL/seed.

01_schema có DROP TABLE: chỉ dùng khi khởi tạo hoặc reset demo có chủ đích. Reset local/demo:

```powershell
.\scripts\reset-db.ps1 -ResetDemo
```

```bash
bash scripts/reset-db.sh --reset-demo
```

DB cần giữ dữ liệu phải backup, review SQL ALTER/CREATE OR REPLACE theo chênh lệch, chạy thủ công và kiểm hậu điều kiện. CD chỉ deploy image và kiểm schema sẵn có; không reset hay nạp lại seed. Xem [CI/CD](../main/CI_CD.md).

## Giới hạn và kiến trúc đích

Runtime hiện tại là Servlet/JSP WAR. Đích Spring Boot dùng DataSource auto-configured, HikariCP, JdbcTemplate và @Transactional, giữ PostgreSQL16; xem [Spring Boot](SPRING_BOOT.md).

CI đã có kiểm schema/seed, JDBC OUT, build Docker và smoke test 5 roles. Các test thiết kế cho 8 UC, sandbox/IPN, phân quyền đầy đủ và cạnh tranh chưa được coi là đã nghiệm thu E2E. Các khác biệt nghiệp vụ còn lại được quản lý theo QUY_TAC_NGHIEP_VU.md và KIEM_THU.md.
