# 📋 Kế Hoạch Dự Án & Phân Công Đóng Góp Chi Tiết

**Dự án:** Hệ thống Quản lý Ký túc xá (CNPM - KTX Smart Hub)  
**Thời gian thực hiện:** 30/09/2026 – 06/10/2026 (Sprint tập trung 1 tuần)  
**Mô hình phát triển:** Agile / Scrum – Triển khai theo từng phân hệ & Feature Branch

---

## 👥 1. Danh sách Thành viên & Phân công Trọng tâm

| Thành viên | Git Username & Email | Vai trò chính | Trọng tâm công việc |
| :--- | :--- | :--- | :--- |
| **Cao Hoàng Phúc** | `phuchoang <phuchoang.src@gmail.com>` | **Team Lead / DevOps & Lead Backend** | Kiến trúc hệ thống Spring Boot & PostgreSQL, hạ tầng Docker Compose, CI/CD pipeline, triển khai Backend Core Services, bảo mật JWT/2FA, API Controller Routing, điều phối và review PR toàn dự án. |
| **Võ Thị Hồng Gấm** | `honpgam098-source <honpgam098@gmail.com>` | **Business Analyst (BA) & System Analyst (SA)** | Phân tích yêu cầu nghiệp vụ, hoàn thiện tài liệu SRS v3 (đặc tả 28 Use Cases), thiết kế Use Case Diagram, Sequence Diagrams, đặc tả tài liệu tích hợp (VNPAY Sandbox, Kỷ luật, Phòng ở), quản lý danh mục quy tắc nghiệp vụ. |
| **Nguyễn Tiến Đạt** | `t1endatj <107497022+t1endatj@users.noreply.github.com>` | **Frontend Engineer** | Khởi tạo React + Vite + TypeScript, phân quyền giao diện (5 vai trò), phát triển màn hình Use Cases, components UI, theme reveal & responsive layout. |
| **Nguyễn Thị Linh** | `thuylinh <thuylinh3106@gmail.com>` | **Database & Data Specialist** | Thiết kế CSDL 26 bảng chuẩn quan hệ, migrations, tạo bộ dữ liệu Seed data (50+ SV, 84 giường...), JPA Hibernate Entities mapping & tối ưu hóa truy vấn CSDL. |
| **Nguyễn Công Khoa** | `Nguyen Cong Khoa <nguyencongkhoa.it@gmail.com>` | **QA / Tester & Quality Assurance** | Xây dựng test suites (Black-box checks, API tests, Test fixtures NFR & UCs), tài liệu bài tập C06-D2 Testing, kiểm thử nghiệp vụ & báo cáo lỗi issue. |

---

## 📅 2. Kế hoạch Triển khai Chi tiết theo Phân hệ

### 📌 Giai đoạn 1: Phân tích Yêu cầu, Kiến trúc & Nền tảng
* **Gấm (BA/SA):** Khảo sát nghiệp vụ ký túc xá, hoàn thiện tài liệu SRS v3 đặc tả chi tiết 28 Use Cases, xây dựng Use Case Diagram và danh mục quy tắc nghiệp vụ.
* **Phúc (Lead/DevOps & Backend):** Khởi tạo repository, thiết lập Git workflow, cấu hình Docker Compose (Web, DB, API), thiết lập Spring Boot 3 Core framework và CI/CD.
* **Linh (Database):** Thiết kế CSDL 26 bảng (`01_schema.sql`), tạo Flyway migration và nạp seed data mẫu ban đầu.
* **Đạt (Frontend):** Khởi tạo khung dự án React Vite TypeScript, cấu hình Tailwind CSS, Router và Layout 5 vai trò.
* **Khoa (QA):** Lập kế hoạch kiểm thử tổng thể, ma trận truy vết yêu cầu (Traceability Matrix).

---

### 📌 Giai đoạn 2: Phát triển Backend Core, Giao diện & Nghiệp vụ
* **Phúc (Lead/DevOps & Backend):** Triển khai Security JWT, Session Filter, 2FA Authenticator QR, Core API Controllers và các dịch vụ nền tảng.
* **Gấm (BA/SA):** Thiết kế chi tiết sơ đồ tuần tự (Sequence Diagrams) cho từng luồng: Đăng ký phòng, Chốt chỉ số điện nước, Thanh toán hóa đơn, Ra vào cổng.
* **Linh (Database):** Ánh xạ 21 JPA Jakarta Entities, bổ sung dữ liệu seed phong phú (50+ sinh viên, 84 giường, hóa đơn mẫu).
* **Đạt (Frontend):** Xây dựng màn hình Đăng nhập sinh động (Login sticker, Theme reveal), màn hình Quản lý tài khoản, Đăng ký phòng (`#UC-04`).
* **Khoa (QA):** Viết bộ kiểm thử xác thực bảo mật và phân quyền API Controllers (Chapter 6 Testing).

---

### 📌 Giai đoạn 3: Tích hợp Dịch vụ, Nghiệp vụ Nâng cao & Kiểm thử
* **Phúc (Lead/DevOps & Backend):** Triển khai Gate Access Service, đồng bộ dữ liệu vào ra, xử lý batch quyết định kỷ luật và điều phối API.
* **Gấm (BA/SA):** Đặc tả tài liệu tích hợp cổng thanh toán VNPAY Sandbox (`VNPAY_SANDBOX.md`), quy trình xử lý vi phạm kỷ luật và biểu mẫu nghiệm thu.
* **Đạt (Frontend):** Hoàn thiện các màn hình Duyệt hồ sơ (`#UC-12`), Thanh toán hóa đơn, Quản lý sự cố, Quản lý kỷ luật và Báo cáo.
* **Linh (Database):** Tối ưu hóa truy vấn danh sách phòng/giường trống, mã hóa dữ liệu cá nhân khách thăm (AES-GCM).
* **Khoa (QA):** Xây dựng Black-box test suite toàn diện cho 8 Use Cases cốt lõi (`test/khoa-...`), kiểm thử đối soát hóa đơn và dữ liệu.

---

### 📌 Giai đoạn 4: Tối ưu hóa, Đóng gói & Hoàn thiện Hồ sơ Nghiệm thu
* **Phúc (Lead/DevOps & Backend):** Tinh gọn mã nguồn, tối ưu Docker Compose release, hợp nhất toàn bộ feature branches vào `develop` và `main`.
* **Gấm (BA/SA):** Rà soát tính khớp nối giữa tài liệu SRS, Sequence Diagram và sản phẩm thực tế, chuẩn bị tài liệu thuyết trình.
* **Đạt (Frontend):** Visual polish toàn bộ giao diện, hoàn thiện màn hình Profile cá nhân và ẩn form khi sinh viên đã có phòng.
* **Linh (Database):** Hoàn thiện schema và dữ liệu mẫu nghiệm thu.
* **Khoa (QA):** Chạy Regression Testing toàn bộ hệ thống, kiểm chứng các bug fixes và tổng hợp báo cáo kiểm thử.

---

## 📊 3. Tổng kết Phân bổ Đóng góp & Nghiệm thu Sprint (06/10/2026)

| Thành viên | Vai trò chính | Tỷ lệ đóng góp | Hạng mục bàn giao chính | Trạng thái |
| :--- | :--- | :---: | :--- | :---: |
| **Cao Hoàng Phúc** | **Team Lead / DevOps & Lead Backend** | **27.0%** | Kiến trúc Spring Boot, Docker Compose, CI/CD, Core Services, Security JWT/2FA, Release. | ✅ Hoàn thành |
| **Võ Thị Hồng Gấm** | **Business Analyst (BA) & System Analyst (SA)** | **24.5%** | Tài liệu SRS v3, Use Case Specs, Sequence Diagrams, VNPAY Specs, Business Rules. | ✅ Hoàn thành |
| **Nguyễn Tiến Đạt** | **Frontend Engineer** | **22.5%** | Giao diện React Vite, UI 5 vai trò, màn hình các Use Cases, Visual Polish. | ✅ Hoàn thành |
| **Nguyễn Thị Linh** | **Database Specialist** | **18.0%** | Schema 26 bảng, JPA Entities, Flyway Migrations, Seed Data, Tối ưu hóa DB. | ✅ Hoàn thành |
| **Nguyễn Công Khoa** | **QA / Tester** | **8.0%** | Black-box Test Suites, C06-D2 Test Assignment, Regression Testing, Issue Tracking. | ✅ Hoàn thành |
| **Tổng cộng** | | **100%** | **Hệ thống hoàn chỉnh sẵn sàng báo cáo nghiệm thu** | ✅ **Nghiệm thu** |

> *Kết luận nghiệm thu: Toàn bộ 5 phân hệ, 8 Use Cases trọng tâm và 28 yêu cầu chức năng đã được hiện thực hóa, kiểm thử đạt 100% và sẵn sàng cho buổi báo cáo nghiệm thu.*


## Phân công phụ trách BE cập nhật ngày 08/10/2026

Gấm phụ trách bốn nhóm module tương đối dễ để bảo trì, hoàn thiện và trình bày luồng:

| Nhóm module | Phạm vi phụ trách | File chính |
|---|---|---|
| Tra cứu phòng và giường | Tra cứu danh sách, chi tiết phòng và giường trống | RoomController, RoomCatalogService, RoomCatalogRepository |
| Hồ sơ sinh viên | Thông tin cá nhân, phòng hiện tại và quyền xem hồ sơ của bản thân | StudentProfileDto và các endpoint hồ sơ sinh viên |
| Sửa chữa | Tạo yêu cầu, cập nhật ưu tiên, phân công và hoàn tất sửa chữa | RepairController, RepairService, RepairRepository |
| Hoạt động công ích | Tra cứu, đăng ký, hủy đăng ký và theo dõi kết quả hoạt động | CommunityController, CommunityService, CommunityRepository |

Phúc tiếp tục phụ trách nền tảng, xác thực, đăng ký/duyệt phòng, hóa đơn, thanh toán,
kiểm soát cổng và kỷ luật; hỗ trợ Gấm review các trường hợp giao dịch phức tạp.
Đây là phân công phụ trách cập nhật, không phải bằng chứng về người thực hiện toàn bộ code trước đó.
Metadata author của một số commit độc lập đã được điều chỉnh theo yêu cầu chủ repository;
các commit gộp nhiều module được giữ nguyên tác giả.
