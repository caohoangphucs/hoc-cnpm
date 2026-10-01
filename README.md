# KTX Smart Hub

Hệ thống quản lý ký túc xá, phạm vi demo **8 UC**: UC-04, UC-12, UC-17, UC-08, UC-22, UC-25, UC-14 và UC-30.

- Backend: **Spring Boot 4.1.1 / Java17**, Spring JDBC, JPA read model và PostgreSQL16.
- Frontend: **React / TypeScript / Vite**, Nginx phục vụ SPA và proxy API cùng origin.
- [Production](https://cnpm.caohoangphuc.id.vn) · [Thiết kế](docs/demo/README.md) · [CI/CD](docs/main/CI_CD.md).

## Trạng thái triển khai

Đã thay nền Servlet/JSP bằng Spring Boot executable JAR, cấu hình DataSource, 27 entity theo schema hiện hành, repository đọc và JDBC function/9 procedure, DTO theo OpenAPI và các test nền. Frontend hiện là khung tổng quan chức năng với trạng thái kết nối thật.

Các Service/Controller nghiệp vụ, đăng nhập HTTP/2FA và giao diện 8 UC **chưa triển khai**. Ngoài health, backend đang từ chối truy cập HTTP. Mật khẩu seed `123456` chỉ phục vụ dữ liệu demo và test DB; chưa có màn hình đăng nhập trên client. Không coi frontend scaffold hay CI xanh là nghiệm thu 8 UC. TODO giữ nguyên.

## Cấu trúc

| Thư mục | Nội dung |
|---|---|
| `server/src/main/java/` | Spring Boot, config, entity/read repository, procedure repository, DTO |
| `server/src/main/resources/` | Cấu hình ứng dụng và profile production |
| `server/src/test/java/` | JUnit unit test và integration test PostgreSQL |
| `server/pom.xml`, `server/Dockerfile` | Build/test và đóng gói backend JAR |
| `client/src/`, `client/tests/` | Frontend React và test trạng thái kết nối |
| `client/Dockerfile`, `client/nginx.conf` | Build SPA và reverse proxy |
| `database/` | Schema/seed PostgreSQL chuẩn; không chạy lại trên DB có dữ liệu |
| `scripts/` | Reset DB demo và deploy cặp backend/frontend |
| `docs/` | SRS, đặc tả, sơ đồ, truy vết và test case thiết kế |

## Chạy local

```bash
cp .env.example .env
docker compose up -d --build
# Frontend: http://localhost:8091
# Readiness backend qua Nginx: http://localhost:8091/api/health
```

PostgreSQL host port mặc định54321. Backend chỉ mở8080 trong network Compose; frontend mở8091. Với volume DB mới, entrypoint nạp01/02; volume hiện hữu không tự cập nhật schema. Reset demo xóa dữ liệu và yêu cầu flag:

```bash
bash scripts/reset-db.sh --reset-demo
```

Chạy ngoài Docker khi phát triển:

```bash
# Export DB_* hoặc SPRING_DATASOURCE_* phù hợp trước khi chạy backend.
mvn -f server/pom.xml spring-boot:run
cd client
pnpm install --frozen-lockfile
pnpm dev
```

Vite proxy `/api` đến localhost8080; Nginx triển khai dùng cùng origin nên không cần CORS khác domain.

## Kiểm thử

```bash
mvn -f server/pom.xml clean verify
# Chỉ dùng DB PostgreSQL16 test riêng, đã nạp schema/seed:
mvn -f server/pom.xml -Pdb-integration verify
cd client
pnpm install --frozen-lockfile
pnpm test
pnpm build
```

Integration test rollback thay đổi nghiệp vụ; SERIAL vẫn có thể tiến và test cần fixture demo. Không chạy lên DB production. CI tự tạo DB/container/network/cổng test riêng. Chi tiết giới hạn test và deploy: [CI/CD](docs/main/CI_CD.md).

## Quy trình phát triển

```bash
git switch -c feat/ten-chuc-nang
git push -u origin feat/ten-chuc-nang
```

Làm việc trên nhánh riêng, PR vào develop; main dành cho release. Xem [Git workflow](docs/main/GIT_WORKFLOW.md). Mọi push/PR chạy CI trên server; chỉ push develop deploy hai image đúng SHA đã qua kiểm thử, với rollback và bảo toàn DB.
