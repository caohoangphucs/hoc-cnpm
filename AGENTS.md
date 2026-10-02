# HƯỚNG DẪN DÀNH CHO AI ASSISTANT & DEVELOPER (AI AGENT CONTEXT)
> **Dự Án:** Hệ Thống Quản Lý Ký Túc Xá (KTX Smart Hub) - Đồ Án CNPM  
> **Mục đích:** File này cung cấp ngữ cảnh kiến trúc, quy tắc bắt buộc và hướng dẫn vận hành cho các AI Agent (Cursor, GitHub Copilot, Gemini CLI, Antigravity, Claude Code, v.v.) và thành viên nhóm khi tham gia phát triển dự án.

---

## 1. TỔNG QUAN HỆ THỐNG & TECH STACK
* **Mục tiêu:** Hệ thống quản lý toàn diện KTX: duyệt phòng, đặt giữ giường chống race-condition, tính toán phát hành hóa đơn tự động, báo hỏng xuất kho vật tư và kiểm soát an ninh ra vào sau 23h.
* **Ngôn ngữ & Runtime đích:** Java17, Spring Boot4.1.1 executable JAR, Tomcat11 nhúng.
* **Web đích:** Spring MVC @RestController/@Controller, Thymeleaf/Bootstrap5; Spring Security, DTO @Valid và @RestControllerAdvice.
* **Database & Pool:** PostgreSQL 16, Spring JDBC/JdbcTemplate, HikariCP/DataSource auto-configured; BCrypt qua Spring Security.
* **Ảo hóa & CI/CD:** Docker multi-stage build, Docker Compose, GitHub Actions.

---

> Chỉ tài liệu đã đổi công nghệ; code hiện tại còn Servlet/JSP WAR. Mapping và hướng chuyển: [Spring Boot](docs/demo/SPRING_BOOT.md).

## 2. QUY TẮC KIẾN TRÚC BẮT BUỘC (CRITICAL ARCHITECTURE RULES)

Khi hỗ trợ viết code hoặc refactor, AI **BẮT BUỘC** tuân thủ các quy tắc sau:

1. **Kiến trúc đích Spring Boot:**
   * **Presentation:** Thymeleaf templates trong `src/main/resources/templates`, static trong resources/static; không dùng JSP làm đích mới.
   * **Controller:** Spring @RestController nhận DTO @Valid; @Controller phục vụ trang. SecurityFilterChain kiểm session/CSRF, @PreAuthorize và ownership ở Service.
   * **Service:** Constructor injection, public @Transactional điều phối; status SP lỗi chuyển thành exception để rollback. Không gọi mạng khi giữ transaction.
   * **Repository:** @Repository/JdbcTemplate gọi PostgreSQL function/procedure; không tạo pool/commit/Connection riêng. DBContext/static DAO chỉ là legacy để chuyển đổi.

2. **Nghiệp vụ cốt lõi nằm ở Stored Procedures:**
   * Nghiệp vụ phức tạp có transaction, khóa dòng (`FOR UPDATE`) và cập nhật liên bảng (đặt giường, duyệt phòng, phát hành hóa đơn, xuất kho vật tư, quét thẻ vi phạm) **CÓ NỀN STORED PROCEDURES** tại `database/01_schema.sql`.
   * **KHÔNG** tự ý viết các câu lệnh raw SQL insert/update thủ công trong DAO làm phá vỡ logic ràng buộc hoặc bỏ qua Stored Procedures có sẵn.

3. **Bảo toàn các file hệ thống:**
   * **KHÔNG** chỉnh sửa `Dockerfile`, `docker-compose.yml`, `.github/workflows/deploy.yml` trừ khi có yêu cầu trực tiếp từ Tech Lead.
   * Schema/seed PostgreSQL dùng `database/01_schema.sql` và `database/02_seed.sql`; không dùng framework migration. 01 có DROP chỉ dùng khởi tạo/reset DB demo có chủ đích. DB cần giữ dữ liệu cập nhật bằng SQL thủ công đã review/backup; không chạy lại 01/02 khi startup. Không tạo V1/V2 hoặc resources/db/migration.


---

## 3. TIỆN ÍCH DÀNH CHO DEVELOPER (CÁCH VẬN HÀNH DỰ ÁN)

Khi giải thích hoặc hướng dẫn cho lập trình viên, AI cần hướng dẫn sử dụng các công cụ sẵn có sau:

* **Hot reload legacy (chưa chuyển code):**
  * File `docker-compose.override.yml` đã mount thư mục JSP vào Tomcat container.
  * **Sửa JSP/HTML/CSS:** Chỉ cần lưu file và **bấm F5 trên trình duyệt** (không cần rebuild container).
* **Reset Database sạch sẽ khi test:**
  * `scripts/reset-db.ps1` hiện còn lệnh MySQL, cần sửa trước khi dùng cho PostgreSQL. Chuẩn kết nối/reset/schema: [docs/demo/POSTGRESQL.md](docs/demo/POSTGRESQL.md). Reset xóa dữ liệu local; không dùng cho DB cần giữ.
* **Tài khoản test (mật khẩu công bố `123456`; cần sửa/xác minh hash seed trước khi demo):**
  * `admin` (Quản trị viên) | `sv001` (Sinh viên) | `ketoan1` (Kế toán) | `kythuat1` (Kỹ thuật) | `baove1` (Bảo vệ).

---

## 4. QUY TẮC GIT WORKFLOW (BẮT BUỘC CHO MỌI COMMIT & PR)

Xem tài liệu đầy đủ tại: [Git workflow](docs/main/GIT_WORKFLOW.md)

1. **Tuyệt đối không commit/push lên `main` hay `develop`:**
   * Mọi công việc phải thực hiện trên nhánh riêng.
2. **Quy tắc đặt tên nhánh:**
   * Cú pháp: `<loại>/<tên-bạn>-<chức-năng>` (chữ thường, gạch ngang `-`, không dấu).
   * Ví dụ: `feat/dat-vnpay`, `fix/dat-invoice-calc`, `docs/dat-guide`.
   * Tool tạo nhánh tự động: `.\scripts\git-branch.ps1 -Type feat -Author dat -Feature vnpay`.
3. **Quy tắc viết Commit Message (Conventional Commits):**
   * Cú pháp: `<type>(<scope>): <mô tả ngắn>`
   * Ví dụ: `feat(payment): tich hop cong vnpay sandbox`, `fix(invoice): sua loi am tien dien`.
4. **Quy trình tạo PR:**
   * Push lên remote: `git push -u origin feat/<tên-bạn>-<chức-năng>`.
   * Tạo Pull Request trên GitHub hướng vào nhánh **`develop`** (không vào `main`).

---

## 5. PHONG CÁCH GIAO TIẾP CỦA AI VỚI DEVELOPER
* Luôn trao đổi bằng **tiếng Việt**, ngắn gọn, rõ ràng, tập trung vào trọng tâm kỹ thuật.
* Dẫn link markdown trực tiếp đến các file mã nguồn liên quan khi đề xuất thay đổi.
* Trước khi đề xuất viết code mới, hãy kiểm tra xem DAO hoặc Stored Procedure tương ứng đã tồn tại trong dự án hay chưa.
