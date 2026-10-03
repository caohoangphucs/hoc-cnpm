# KTX Smart Hub

Hệ thống quản lý ký túc xá, phạm vi demo **8 UC**: UC-04, UC-12, UC-17, UC-08, UC-22, UC-25, UC-14 và UC-30.

- Backend: **Spring Boot 4.1.1 / Java17**, Spring JDBC, JPA read model và PostgreSQL16.
- Frontend: **React / TypeScript / Vite**, Nginx phục vụ SPA và proxy API cùng origin.
- [Production](https://cnpm.caohoangphuc.id.vn) · [Thiết kế](docs/demo/README.md) · [CI/CD](docs/main/CI_CD.md).

## Trạng thái triển khai

Đã thay nền Servlet/JSP bằng Spring Boot executable JAR, cấu hình DataSource, 27 entity theo schema hiện hành, repository đọc, DTO theo OpenAPI và các test nền. Tầng service/repository cho 8 UC dùng Java và SQL trực tiếp; schema hiện hành không định nghĩa function hoặc procedure nghiệp vụ. Frontend hiện là khung tổng quan chức năng với trạng thái kết nối thật.

Tầng Java service/repository cho 8 UC đã được bổ sung; quy tắc nghiệp vụ nằm trong service Java, còn repository chỉ đọc/ghi SQL đơn giản. API đang bắt đầu được nối: auth routes tập trung trong `ApiRoutes`, login/session/logout và đăng ký phòng có handler; 2FA chưa cấu hình secret TOTP nên tài khoản ADMIN/KETOAN chưa thể vào API nghiệp vụ. Controller các UC còn lại và giao diện nghiệp vụ chưa hoàn tất. Adapter thanh toán và ký chứng nhận cũng cần tích hợp cấu hình provider trước khi dùng. Mật khẩu seed `123456` chỉ phục vụ dữ liệu demo và test DB. Không coi frontend scaffold hay CI xanh là nghiệm thu end-to-end 8 UC.

## Cấu trúc

| Thư mục | Nội dung |
|---|---|
| `server/src/main/java/` | Spring Boot, config, entity/read repository, service/repository cho 8 UC, DTO |
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

## Cách viết backend đơn giản

Repository gọi SQL là class Java thông thường, được tạo bằng constructor trong
`DatabaseConfiguration`. Kiểm tra tham số dùng các hàm `requiredText`, `positive`,
`optionalText`; không cần `@Repository`, `@Validated` hay `@Transactional` trên các class này.
Tham số sai phát sinh `IllegalArgumentException`; DTO sai vẫn phát sinh
`ConstraintViolationException` từ Validator được gọi trực tiếp.

Các service điều phối quy tắc và transaction qua `ServiceContext`; repository dùng
`JdbcTemplate` cho SQL trực tiếp. HTTP routes được khai báo trong `ApiRoutes`,
còn hàm xử lý nằm riêng trong controller. Annotation JPA trên entity và
validation trên DTO được giữ vì phục vụ ánh xạ database và hợp đồng API.

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
