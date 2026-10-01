# CI và CD của hoc-cnpm

Workflow: `.github/workflows/deploy.yml`.

## Khi chạy

- Mọi push trên mọi nhánh chạy CI; PR vào develop cũng chạy CI.
- Hai job phải đạt: `database-schema-seed-and-build` và `legacy-mysql-login-regression`.
- Push vào develop chỉ chạy job `deploy` sau khi cả hai job CI thành công. Nhánh khác và PR không deploy.
- Merge PR tạo commit trên develop và kích hoạt cùng quy trình. Mỗi push kiểm tra commit đầu nhánh; nếu push nhiều commit một lần, GitHub Actions không tự chạy riêng từng commit trung gian.

## CI kiểm tra

PostgreSQL 16: schema/seed với ON_ERROR_STOP, 27 bảng/9 procedure, fixture có dữ liệu, Maven verify và WAR không chứa migration. Build Docker; kiểm trang chủ, truy vấn đăng nhập thất bại có kết nối DB, đăng nhập/dashboard cả 5 tài khoản demo và log runtime. Kiểm tài khoản thật với mật khẩu sai/admin123 và một fixture hash hỏng: tất cả phải bị từ chối, không fallback.

MySQL 8: tái hiện lỗi collation của bản server cũ, áp dụng SQL sửa thủ công, kiểm username thường/Unicode/không tồn tại, bảo toàn số bản ghi và áp dụng lại an toàn. Đây là kiểm tra hotfix legacy; database đích vẫn là PostgreSQL.

Chỉ image đã qua kiểm tra được export/upload làm artifact của đúng commit. Tài khoản demo có BCrypt hợp lệ cho 123456; cuối seed đồng bộ mọi SERIAL sequence theo ID lớn nhất. Các ca kiểm thử SRS chưa tự động hóa không được coi là đã chạy bởi CI.

## CD trên server

Runner `hoc-cnpm-deploy`, nhãn `cnpm-deploy`, chạy bằng service systemd dưới user phuc. Chỉ job deploy của push develop dùng runner; CI chạy trên GitHub-hosted Ubuntu. Không upload SSH key cá nhân lên GitHub.

CD download image của đúng SHA, lấy lock chống deploy đồng thời, kiểm PostgreSQL, lưu container trước, chạy image mới ở cổng 8091, kiểm HTTP/kết nối DB/log. Lỗi deploy khôi phục container trước và báo job thất bại. Thành công ghi SHA trong `/home/phuc/deployments/hoc-cnpm/deployed-sha` và nhãn container `cnpm.commit`.

Cấu hình DB nằm trong `/home/phuc/deployments/hoc-cnpm/runtime.env` với quyền 0600; không commit/upload file đó. PostgreSQL có volume riêng `cnpm_production_pg_data`, network `cnpm-production`. Không replay schema/seed khi DB đã có dữ liệu.

Lần đầu PostgreSQL còn trống, `scripts/import-legacy-mysql.py` chạy sau khi dừng app cũ: đọc snapshot nhất quán của MySQL, tạo schema và copy dữ liệu trong một transaction PostgreSQL. Kiểm khóa/constraint và số dòng từng bảng; giữ ID, sửa SERIAL sequence. Bảng ngoài schema dự án được lưu JSONB ở `legacy_archive`, không bỏ bản ghi. MySQL và dump gốc được giữ nguyên để khôi phục. Import thất bại rollback PostgreSQL và khởi động app cũ.

Schema mới ở các lần sau phải được review/cập nhật SQL thủ công trước khi deploy; không dùng Flyway hay V1/V2. CD kiểm ứng dụng chạy, không tự sửa schema có dữ liệu.

## Hotfix MySQL thủ công

Trước sửa đã backup toàn DB, routines, events, triggers. Các script không thuộc thư mục init database:

- `scripts/sql/repair-legacy-mysql-login.sql`: tham số usp_DangNhap dùng cùng utf8mb4_unicode_ci với cột username; giữ nguyên contract/definer.
- `scripts/sql/repair-legacy-mysql-demo-passwords.sql`: chỉ thay hash seed sai đã biết cho 5 username demo; giữ mật khẩu đã được người dùng thay đổi.

Checkout `/home/phuc/hoc-cnpm` và hai commit JPA riêng không bị overwrite; runner dùng checkout riêng. Các commit riêng đó cần PR/CI riêng trước khi tích hợp.

GitHub hiện từ chối branch protection cho repo private trên gói tài khoản này (403, cần Pro hoặc public); không đổi visibility/gói. CI vẫn chạy mọi push và CD bị chặn nếu CI thất bại. Không thể cam kết merge bị GitHub chặn ở cấp branch policy.
