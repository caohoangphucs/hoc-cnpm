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

Chỉ push `develop` sau khi toàn bộ test thành công mới deploy. Runner build image ngay trên server, không upload/download image qua GitHub.

Production dùng `docker-compose.prod.yml` cho hai service Spring Boot và Nginx/React. PostgreSQL hiện hữu nằm ngoài Compose này; không nạp lại schema/seed hoặc thay volume DB. `docker-compose.yml` dùng cho môi trường local.

`scripts/deploy-server.sh SHA cnpm-server-ci:SHA cnpm-client-ci:SHA` kiểm image đúng commit, lưu cặp image trước rồi chạy `docker compose up -d --no-build --pull never`. Kiểm `/api/health` và trang chủ; nếu lỗi, Compose chạy lại cặp image trước. Không còn đổi tên và giữ container sau mỗi lần deploy.

Cấu hình riêng trên server tại `/home/phuc/deployments/hoc-cnpm`: `runtime.env` chứa kết nối DB, `release.env` chứa hai image đang chạy, `previous-release.env` chứa bản trước. Các file này không commit. Network có sẵn `cnpm-production`; frontend mở cổng8091, backend8080 chỉ trong network.

Chuyển container cũ sang Compose chỉ thực hiện một lần, giữ bản cũ đến khi kiểm health thành công. Những lần deploy tiếp theo đều dùng cùng một luồng Compose. TODO chưa đánh dấu hoàn thành; CI xanh chỉ xác nhận phạm vi test hiện có.
