# Kiến trúc Spring Boot cho demo 8 UC

Quyết định công nghệ ngày 02/10/2026: chuyển thiết kế đích sang **Spring Boot 4.1.1, Java 17, PostgreSQL 16**. Java 17 đáp ứng yêu cầu của phiên bản này; runtime chọn executable JAR với Tomcat 11 nhúng theo [system requirements](https://docs.spring.io/spring-boot/system-requirements.html). Nền mới đã có trong server/ (JAR, entity/read repository/JDBC/DTO); client/ dùng React/TypeScript/Vite và Nginx. Service/Controller/auth HTTP/8UC vẫn cần triển khai.

## Stack và cách đóng gói

| Thành phần | Thiết kế đích |
|---|---|
| Backend | Spring MVC: @RestController cho /api/v1; @Controller cho trang |
| Giao diện | React/TypeScript/Vite trong client/, Nginx phục vụ SPA và proxy /api; backend trong server/ |
| Phân quyền | Spring Security, session JSESSIONID, RBAC và ownership; 2FA cho ADMIN/KETOAN |
| Validation/lỗi | Jakarta Bean Validation trên DTO, @RestControllerAdvice giữ Error schema hiện hành |
| Dữ liệu | Spring JDBC JdbcTemplate/NamedParameterJdbcTemplate, HikariCP, pgJDBC; PostgreSQL procedure giữ nghiệp vụ nhiều bảng |
| Transaction | @Transactional ở public Service bean; JdbcTransactionManager, một DataSource |
| Khởi tạo DB | database/01_schema.sql → 02_seed.sql cho DB demo mới; cập nhật SQL thủ công cho DB có dữ liệu |
| Tác vụ | @Scheduled điều phối job; outbox/lease PostgreSQL cho nhiều instance |
| Đóng gói | Maven, spring-boot-maven-plugin, executable JAR; java -jar, không deploy WAR lên Tomcat10.1 |
| Kiểm thử | Spring Boot Test/MockMvc + Spring Security Test; Testcontainers PostgreSQL16; không thay bằng H2 |
| Giám sát | Actuator health/readiness và metrics; chỉ health tối thiểu public, metrics cần quyền/mạng nội bộ |

Spring MVC hỗ trợ controller theo annotation và template engine trong [hướng dẫn web chính thức](https://docs.spring.io/spring-boot/reference/web/servlet.html). Dependency để triển khai: parent spring-boot-starter-parent 4.1.1; starters webmvc, security, validation, jdbc, actuator; org.postgresql:postgresql runtime; starters test/webmvc-test/jdbc-test/security-test ở test scope và Testcontainers PostgreSQL. Boot BOM quản lý phiên bản thư viện; không giữ các version Servlet/Hikari/Hibernate từ pom legacy. Tên starter theo [danh mục chính thức](https://docs.spring.io/spring-boot/reference/using/build-systems.html).

## Cấu trúc module đích

Giữ package gốc vn.id.caohoangphuc.cnpm; KtxApplication đặt tại gốc để component scan. Mỗi module booking, registration, invoice, payment, repair, gate, discipline, community có controller/service/repository/dto. Package security chứa cấu hình/principal/2FA; integration chứa gateway/CTSV/barrier/signing; common chứa lỗi/requestId/idempotency; jobs chứa worker. Constructor injection, không dùng static DAO hoặc singleton DBContext tự tạo pool.

| Legacy / UC | Controller → Service → Repository đích |
|---|---|
| StudentServlet/RoomDAO — UC04 | BookingController → BookingService → RoomRepository |
| AdminServlet/RoomDAO — UC12 | RegistrationController → RegistrationService → RoomRepository |
| AccountingServlet/InvoiceDAO — UC17 | InvoiceController → InvoiceService → InvoiceRepository |
| PaymentServlet đề xuất/InvoiceDAO — UC08 | PaymentController + PaymentCallbackController → PaymentService → PaymentRepository/InvoiceRepository |
| MaintenanceServlet/MaintenanceDAO — UC22 | RepairController → RepairService → RepairRepository |
| SecurityServlet/SecurityDAO — UC25 | GateController → GateService → GateRepository/VisitorRepository |
| DisciplineServlet đề xuất — UC14 | DisciplineController → DisciplineService → DisciplineRepository |
| CommunityServlet đề xuất — UC30 | CommunityController → CommunityService → CommunityRepository |
| AuthServlet/AccountDAO | AuthController + SecurityFilterChain → AuthenticationManager/UserDetailsService → AccountRepository |

Legacy được ghi trong bảng để đối chiếu. Tầng service/repository Java theo 8 UC hiện nằm trong `server/src/main/java/.../service` và `persistence/service`; các sequence/class vẫn là nguồn quy tắc nghiệp vụ. Controller, auth HTTP và React API integration chưa có. Khi bổ sung, React sẽ gọi JSON/API cùng origin qua Nginx; không dùng JSP/Thymeleaf.

## Transaction và gọi PostgreSQL

@Transactional đặt trên phương thức public của Service bean, gọi từ bean khác qua proxy. Self-invocation không tạo transaction mới; phải tách bean hoặc dùng TransactionTemplate cho worker/luồng cần ranh giới riêng. Cấu hình rollbackFor=Exception.class cho thao tác ghi, chuyển status SP thất bại thành DomainException trước khi thoát service; controller advice chỉ bắt lỗi sau khi rollback. Không bắt lỗi và trả SUCCESS trong transaction. Các quy tắc proxy/rollback theo [Spring transaction](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html).

@Repository nhận JdbcTemplate bằng constructor. Query function đăng nhập bằng SELECT * FROM usp_DangNhap(?). Procedure dùng JdbcTemplate.execute với CallableStatementCreator/Callback: cú pháp {call ...}, bind IN và đăng ký OUT chính xác theo DU_LIEU_VA_SP. DataSource đặt escapeSyntaxCallMode=callIfNoReturn. Không dựa metadata tự suy chữ ký, không dùng JPA để cập nhật thay SP. JdbcTemplate dùng Connection gắn transaction của Spring; repository không tự getConnection/commit/rollback/đóng Connection được Spring quản lý. Có thể đóng Statement/ResultSet theo callback lifecycle.

Gateway tạo URL/request sau khi transaction tạo phiên PENDING commit. Callback chỉ xác minh/parsing ở adapter, rồi gọi một Service transactional để ghi nhận; trả ACK sau commit. Outbox ghi cùng transaction với nghiệp vụ, worker gọi mạng ngoài transaction và xác nhận kết quả ở transaction ngắn khác. Không chỉ dùng @Async/event trong bộ nhớ thay outbox bền vững.

## Security và HTTP

SecurityFilterChain quản lý session/CSRF, ánh xạ role DB thành ROLE_ADMIN/...; @EnableMethodSecurity và @PreAuthorize tại Service. Ownership kiểm theo KtxPrincipal(accountId,studentId,roomId,role,twoFactorVerified), không tin actorId từ body. Giữ BCrypt bằng Spring PasswordEncoder; hash lỗi phải từ chối, không plaintext fallback.

GET /api/v1/auth/session được phép trước login, trả authenticated=false và csrfToken để bootstrap. POST login vẫn phải có CSRF; sau login/2FA/logout client lấy token mới. Header thống nhất X-CSRF-TOKEN. Permit login/session không đồng nghĩa miễn CSRF. Custom JSON login dùng AuthenticationManager, SessionAuthenticationStrategy chống session fixation và lưu SecurityContext qua SecurityContextRepository; không chỉ đặt session.user như legacy. Theo [Spring Security persistence](https://docs.spring.io/spring-security/reference/7.0/servlet/authentication/persistence.html) và [CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html).

Chỉ ngoại lệ CSRF đúng /api/v1/payments/vnpay/ipn và /api/v1/payments/momo/ipn; permit callback nhưng adapter vẫn bắt buộc chữ ký/amount/ref/partner. Không disable CSRF toàn bộ API dùng cookie. Filter lỗi auth/CSRF/2FA phải trả JSON Error contract cho /api/v1, không redirect HTML; client điều phối màn hình login. Role ADMIN/KETOAN trước 2FA chỉ được gọi session/2fa/logout. CORS cùng origin; chưa có nhu cầu frontend khác origin. Session idle15min, cookie HttpOnly/Secure ở HTTPS/SameSite=Lax, requestId xuyên filter/controller/service/repository.

## Cấu hình đích

application.yml và profiles local/demo/test/prod sẽ được viết lúc code. Cấu hình chuẩn:

- spring.datasource.url lấy SPRING_DATASOURCE_URL, host local localhost:54321, container cnpm-db:5432; username/password từ SPRING_DATASOURCE_USERNAME/PASSWORD. DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASS legacy chỉ được dùng làm placeholder adapter nếu cần, không tạo pool thứ hai.
- spring.datasource.hikari.data-source-properties.escapeSyntaxCallMode=callIfNoReturn; options=-c TimeZone=Asia/Ho_Chi_Minh; maxPoolSize15, minimumIdle3, connectionTimeout10000ms.
- server.port=8080 trong container; host mapping8091; server.servlet.session.timeout=15m; multipart max-file-size5MB, max-request-size26MB cho5ảnh và metadata; limit số file/MIME còn kiểm tại service.
- spring.sql.init.mode=never; schema quản lý bằng SQL thủ công; app không khởi tạo/reset DB hoặc seed khi startup. Chỉ bật thông tin lỗi chi tiết ở log đã lọc, không trả ra HTTP.
- Backend dùng mvn -f server/pom.xml spring-boot:run; frontend dùng Vite dev server trong client/, proxy /api đến localhost8080. Không dùng JSP mount hoặc Thymeleaf.

Không tự lấy image Tomcat10.1 legacy để chạy Boot4.1. Runtime container dùng Java17 chạy JAR. Dockerfile riêng trong server/ và client/; Compose/CI điều phối cả hai.

## Quản lý schema bằng SQL

Không dùng Flyway/Liquibase hoặc migration tự động. Nguồn schema là database/01_schema.sql (27bảng,5view,1function,9procedure); seed demo là database/02_seed.sql. DB demo mới/được phép reset: nạp01 rồi02 bằng psql -v ON_ERROR_STOP=1, hoặc để Docker entrypoint nạp trên volume mới. Restart với volume hiện hữu không tự cập nhật schema.

DB cần giữ dữ liệu: backup/preflight, chuẩn bị SQL ALTER/CREATE OR REPLACE đã review theo chênh lệch schema, chạy thủ công trong transaction phù hợp và đối chiếu dữ liệu. Không chạy01 có DROP hoặc replay seed. Giữ biên bản script/checksum/người thực hiện; không tạo schema history hay V1/V2. Ứng dụng chỉ kết nối DB đã chuẩn bị; quy trình triển khai kiểm cấu trúc và dừng khi không tương thích. Nếu dùng JPA ở đợt sau, chỉ validate schema; không dùng ddl-auto=create/update để thay SQL.

## Các bước chuyển và nghiệm thu công nghệ

1. Boot skeleton/JAR, security/DTO/advice/datasource; health fail khi DB unavailable.
2. Repository gọi function và9procedure qua JdbcTemplate, Testcontainers PG16; chứng minh rollback/shared connection và status→exception.
3. Triển khai React và8module theo từng đợt, giữ API/BR/SRS IDs; kiểm requestId, schema, session/CSRF/2FA.
4. Kiểm DB mới nạp schema/seed và DB có dữ liệu được cập nhật SQL thủ công; no-drop DB cần giữ; startup không tự chạy DDL/seed.
5. Docker/CI JAR, joblease/outbox,51case nghiệp vụ + test Spring ở KIEM_THU. Nghiệm thu mock/sandbox theo TICH_HOP.

Không đánh dấu hoàn thành 8UC bằng entity/DTO hoặc scaffold. Nền Spring Boot và frontend đã có; API/UI nghiệp vụ và nghiệm thu vẫn còn phía trước.

## Nền dữ liệu đã viết

27 entity immutable ánh xạ đúng bảng/cột/kiểu ID hiện hành; FK hiện giữ bằng scalar ID để tránh tự tải graph. 27 read repository không có save/delete. JPA dùng jpaReadTransactionManager trên cùng pool, không trộn boundary JPA đọc với JDBC ghi. Write Service dùng transactionManager (JdbcTransactionManager) và gọi DormitoryProcedureRepository; repository yêu cầu MANDATORY và ném ProcedureRejectedException khi status lỗi. Gate VIOLATION_RECORDED là kết quả ghi nhận thành công có vi phạm.

DTO record được tạo thành56DTO theo OpenAPI, gồm request/response/list; không có endpoint tự động chỉ vì đã có DTO. ID API đích (bigint/UUID), version, file/profile/payment session vẫn cần adapter/schema mở rộng. Entity chỉ dùng ID varchar/int4 hiện có; không bịa version hoặc UUID cho dữ liệu chưa có. Meter hiện đổi int64 sang int4 có kiểm overflow. Money DB dùng BigDecimal, timestamp without time zone dùng LocalDateTime; API RFC3339 dùng OffsetDateTime và cần mapping timezone tại Service.

Health readiness có kiểm DB; chỉ health được public trong bước nền, các HTTP khác denyAll. Client scaffold chỉ tổng quan8UC và trạng thái kết nối, chưa có login/2FA. Không tự mở các endpoint nghiệp vụ khi chưa có Service/Security.
