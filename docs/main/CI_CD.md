# CI/CD trên server: backend và frontend

Workflow `.github/workflows/deploy.yml` chạy bằng runner `hoc-cnpm-deploy` với labels self-hosted/linux/x64/cnpm-deploy. Mọi push và PR vào develop chạy CI; chỉ push develop chạy bước deploy sau khi các bước trước thành công.

## Build và test

1. Tạo PostgreSQL16 test riêng, nạp schema/seed bằng ON_ERROR_STOP; kiểm 27 bảng.
2. Build stage `server/Dockerfile`: Maven/JDK17 chạy clean verify, JUnit unit test DTO/validation và xử lý status procedure.
3. JUnit integration test Spring Boot trên DB test: validate/read đủ27 entity, timezone, function đăng nhập/BCrypt5roles, cả9procedure, ID/QR OUT, yêu cầu transaction, lỗi status và rollback nguyên tử.
4. Build image backend executable JAR và frontend; frontend dùng pnpm lockfile, chạy ESLint, Node test, typecheck TypeScript và Vite production build. Nginx phục vụ SPA/proxy.
5. Khởi động cả hai trên network test với alias cnpm-server; kiểm readiness qua `/api/health`, trang chủ, JS asset, SPA fallback, nginx -t và log backend.
6. Develop deploy trực tiếp cặp image vừa test. Không xuất/upload/download image artifact qua GitHub; Maven/pnpm/Docker cache nằm trên server. GitHub vẫn checkout mã nguồn và lưu trạng thái/log.

Các test hiện là nền DB/DTO và frontend health; chưa có test HTTP login/dashboard hoặc E2E8UC cho nền mới vì các endpoint nghiệp vụ chưa được triển khai. Test59ca trong docs là thiết kế, không phải59ca đã chạy. CI xanh không đồng nghĩa nghiệm thu nghiệp vụ.

## Deploy và rollback

`scripts/deploy-server.sh SHA cnpm-server-ci:SHA cnpm-client-ci:SHA` kiểm SHA label của cả image, lock deploy và schema production trước khi đổi container.

- Backend `cnpm-server`: Spring Boot/JAR, chỉ8080 trong network cnpm-production.
- Frontend `cnpm-web`: Nginx, port8091→80; proxy `/api` đến backend cùng origin.
- DB `cnpm-postgres`: PostgreSQL16, volume cnpm_production_pg_data; không mở host port.
- Secrets: `/home/phuc/deployments/hoc-cnpm/runtime.env`, quyền0600, ngoài checkout.

Lưu container trước cho cả hai, chạy cặp mới, kiểm readiness DB qua Nginx và trang chủ. Nếu lỗi, khôi phục cả backend/frontend; hỗ trợ lần chuyển đầu từ container web đơn. Thành công ghi SHA tại deployed-sha và nhãn cnpm.commit trên cả hai container. Không replay DDL/seed/reset production; Hibernate chỉ validate schema.

Cập nhật DB cần backup/review SQL thủ công; schema sai khiến startup/deploy thất bại. Không dùng Flyway/Liquibase. Runner chạy từng job tuần tự, script có flock bổ sung. Mỗi push kiểm commit đầu nhánh, không tự kiểm từng commit trung gian trong một push nhiều commit.

GitHub hiện trả403 khi cấu hình branch protection cho repo private trên gói hiện tại. CD vẫn bị chặn khi CI thất bại; chưa có policy bắt buộc CI ở cấp merge. Không đổi visibility/gói tài khoản.
