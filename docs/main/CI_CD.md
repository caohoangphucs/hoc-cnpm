# CI/CD: PostgreSQL và deploy develop

Workflow: `.github/workflows/deploy.yml`.

## Trigger và điều kiện

Mọi push trên mọi nhánh chạy CI; PR vào develop cũng chạy CI. Job `database-schema-seed-and-build` phải thành công trước CD. Chỉ push vào develop chạy bước deploy trong cùng job; nhánh khác/PR không deploy. Merge PR tạo commit develop và kích hoạt quy trình này.

Mỗi push kiểm tra commit đầu nhánh; nếu push nhiều commit một lần, GitHub Actions không tự chạy riêng từng commit trung gian. CI thất bại sẽ chặn CD.

## Kiểm tra trước deploy

- PostgreSQL16 schema/seed với ON_ERROR_STOP, 27 bảng, 9 procedure và fixture.
- Maven clean verify; WAR không chứa framework/file quản lý schema tự động.
- JDBC gọi function đăng nhập bằng SELECT và CALL/OUT của cả 9 procedure, rollback dữ liệu test.
- Build Docker, trang chủ, đăng nhập/dashboard cả 5 roles, sai mật khẩu/admin123 và hash hỏng bị từ chối; kiểm log runtime.

Toàn bộ CI và CD chạy trên server của dự án. Maven/JDK17 chạy trong Docker build stage, dùng cache dependency và layer tại server. Image runtime có tag và label SHA bất biến; bước deploy dùng trực tiếp image vừa kiểm tra trên cùng máy. Không export, upload hoặc download image/artifact qua GitHub. GitHub chỉ điều phối workflow, checkout mã nguồn và nhận log/trạng thái. Các ca kiểm thử SRS chưa tự động hóa không được coi là đã chạy bởi CI.

## Deploy và rollback

Runner `hoc-cnpm-deploy`, nhãn `cnpm-deploy`, chạy bằng service systemd dưới user phuc. Mọi CI và CD dùng runner này; không dùng máy build GitHub-hosted. Container PostgreSQL test, network và cổng host được cấp riêng cho từng run, không dùng DB production. Runner xử lý từng job tuần tự; script deploy có lock bổ sung. Không dùng SSH key cá nhân trong GitHub Actions.

Server dùng PostgreSQL16, volume `cnpm_production_pg_data`, network `cnpm-production`. Cấu hình DB nằm trong `/home/phuc/deployments/hoc-cnpm/runtime.env` quyền 0600, ngoài checkout Git.

`scripts/deploy-server.sh` kiểm schema 27 bảng sẵn có, lấy lock, lưu container trước, chạy image mới cổng8091, kiểm HTTP/kết nối DB/log. Nếu lỗi, khôi phục container trước và báo job thất bại. Thành công ghi SHA ở `/home/phuc/deployments/hoc-cnpm/deployed-sha` và nhãn container `cnpm.commit`.

CD không khởi tạo, reset, nạp seed hay chuyển đổi DB. Schema có dữ liệu cập nhật bằng SQL thủ công đã backup/review trước deploy. Reset chỉ dành cho local/demo với flag tường minh.

GitHub hiện từ chối branch protection cho repo private trên gói tài khoản này (403, cần Pro hoặc public); không đổi visibility/gói. CI chạy mọi push và CD vẫn bị chặn khi CI thất bại; chưa thể chặn merge ở cấp branch policy.
