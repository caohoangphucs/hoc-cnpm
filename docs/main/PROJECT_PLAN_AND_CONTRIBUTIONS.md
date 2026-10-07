# 📋 Kế Hoạch Dự Án & Phân Công Đóng Góp Chi Tiết

**Dự án:** Hệ thống Quản lý Ký túc xá (CNPM - KTX Smart Hub)  
**Thời gian thực hiện:** 30/09/2026 – 06/10/2026 (Sprint tập trung 1 tuần)  
**Mô hình phát triển:** Agile / Scrum – Triển khai theo từng phân hệ & Feature Branch

---

## 👥 1. Danh sách Thành viên & Phân công Trọng tâm

| Thành viên | Git Username & Email | Vai trò chính | Trọng tâm công việc |
| :--- | :--- | :--- | :--- |
| **Cao Hoàng Phúc** | `phuchoang <phuchoang.src@gmail.com>` | **Team Lead / DevOps & Lead Backend** | Kiến trúc hệ thống Spring Boot & PostgreSQL, hạ tầng Docker Compose, CI/CD pipeline, bảo mật JWT/2FA, điều phối định tuyến API, 4 phân hệ Backend chính: Đăng ký & Duyệt phòng (#UC-12), Hóa đơn & VNPAY (#UC-17, #UC-08), Cổng ra vào (#UC-25), Kỷ luật sinh viên (#UC-14), điều phối và review PR toàn dự án. |
| **Võ Thị Hồng Gấm** | `honpgam098-source <honpgam098@gmail.com>` | **Business Analyst (BA/SA) & Backend Developer** | Đặc tả yêu cầu nghiệp vụ SRS v3 (28 Use Cases), Sequence Diagrams, quy tắc nghiệp vụ; phụ trách 4 phân hệ Backend chính: Tra cứu phòng & giường (#UC-04), Quản lý hồ sơ sinh viên, Quản lý sửa chữa cơ sở vật chất (#UC-22), Quản lý hoạt động công ích (#UC-30). |
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
* **Phúc (Lead/DevOps & Backend):** Triển khai Security JWT, Session Filter, 2FA Authenticator QR, Core API routing, phân hệ Đăng ký phòng (`#UC-12`) và cấu hình nền tảng.
* **Gấm (BA/SA & Backend):** Thiết kế sơ đồ tuần tự (Sequence Diagrams); hiện thực Backend phân hệ Tra cứu danh mục phòng & giường (`#UC-04`: `RoomController`, `RoomCatalogService`, `RoomCatalogRepository`) và quản lý Hồ sơ sinh viên (`StudentProfileDto`).
* **Linh (Database):** Ánh xạ 21 JPA Jakarta Entities, bổ sung dữ liệu seed phong phú (50+ sinh viên, 84 giường, hóa đơn mẫu).
* **Đạt (Frontend):** Xây dựng màn hình Đăng nhập sinh động (Login sticker, Theme reveal), màn hình Quản lý tài khoản, Đăng ký phòng (`#UC-04`).
* **Khoa (QA):** Viết bộ kiểm thử xác thực bảo mật và phân quyền API Controllers (Chapter 6 Testing).

---

### 📌 Giai đoạn 3: Tích hợp Dịch vụ, Nghiệp vụ Nâng cao & Kiểm thử
* **Phúc (Lead/DevOps & Backend):** Triển khai phân hệ Cổng ra vào (`#UC-25`), Xử lý kỷ luật (`#UC-14`), Hóa đơn & Thanh toán VNPAY Sandbox (`#UC-17`, `#UC-08`), điều phối tích hợp API.
* **Gấm (BA/SA & Backend):** Hiện thực Backend phân hệ Sửa chữa cơ sở vật chất (`#UC-22`: `RepairController`, `RepairService`, `RepairRepository`) và phân hệ Hoạt động công ích (`#UC-30`: `CommunityController`, `CommunityService`, `CommunityRepository`), đặc tả tài liệu tích hợp và quy tắc nghiệp vụ.
* **Đạt (Frontend):** Hoàn thiện các màn hình Duyệt hồ sơ (`#UC-12`), Thanh toán hóa đơn, Quản lý sự cố, Quản lý kỷ luật và Báo cáo.
* **Linh (Database):** Tối ưu hóa truy vấn danh sách phòng/giường trống, mã hóa dữ liệu cá nhân khách thăm (AES-GCM).
* **Khoa (QA):** Xây dựng Black-box test suite toàn diện cho 8 Use Cases cốt lõi (`test/khoa-...`), kiểm thử đối soát hóa đơn và dữ liệu.

---

### 📌 Giai đoạn 4: Tối ưu hóa, Đóng gói & Hoàn thiện Hồ sơ Nghiệm thu
* **Phúc (Lead/DevOps & Backend):** Tinh gọn mã nguồn, tối ưu Docker Compose release, hợp nhất toàn bộ feature branches vào `develop` và `main`.
* **Gấm (BA/SA & Backend):** Tối ưu hóa, refactor các service của 4 phân hệ backend phụ trách, rà soát tính khớp nối giữa tài liệu SRS, Sequence Diagram và API thực tế, chuẩn bị tài liệu thuyết trình.
* **Đạt (Frontend):** Visual polish toàn bộ giao diện, hoàn thiện màn hình Profile cá nhân và ẩn form khi sinh viên đã có phòng.
* **Linh (Database):** Hoàn thiện schema và dữ liệu mẫu nghiệm thu.
* **Khoa (QA):** Chạy Regression Testing toàn bộ hệ thống, kiểm chứng các bug fixes và tổng hợp báo cáo kiểm thử.

---

## 📊 3. Tổng kết Phân bổ Đóng góp & Nghiệm thu Sprint (06/10/2026)

| Thành viên | Vai trò chính | Tỷ lệ đóng góp | Hạng mục bàn giao chính | Trạng thái |
| :--- | :--- | :---: | :--- | :---: |
| **Cao Hoàng Phúc** | **Team Lead / DevOps & Lead Backend** | **26.5%** | Kiến trúc Spring Boot, Docker Compose, CI/CD, Core Services, Security JWT/2FA, 4 phân hệ Backend (Booking, Invoice/VNPAY, Gate, Discipline), Release. | ✅ Hoàn thành |
| **Võ Thị Hồng Gấm** | **Business Analyst (BA/SA) & Backend Developer** | **25.0%** | Tài liệu SRS v3, Use Case Specs, Sequence Diagrams, Business Rules; 4 phân hệ Backend (Tra cứu phòng/giường, Hồ sơ SV, Sửa chữa, Hoạt động công ích). | ✅ Hoàn thành |
| **Nguyễn Tiến Đạt** | **Frontend Engineer** | **22.5%** | Giao diện React Vite, UI 5 vai trò, màn hình các Use Cases, Visual Polish. | ✅ Hoàn thành |
| **Nguyễn Thị Linh** | **Database Specialist** | **18.0%** | Schema 26 bảng, JPA Entities, Flyway Migrations, Seed Data, Tối ưu hóa DB. | ✅ Hoàn thành |
| **Nguyễn Công Khoa** | **QA / Tester** | **8.0%** | Black-box Test Suites, C06-D2 Test Assignment, Regression Testing, Issue Tracking. | ✅ Hoàn thành |
| **Tổng cộng** | | **100%** | **Hệ thống hoàn chỉnh sẵn sàng báo cáo nghiệm thu** | ✅ **Nghiệm thu** |

> *Kết luận nghiệm thu: Toàn bộ 5 phân hệ, 8 Use Cases trọng tâm và 28 yêu cầu chức năng đã được hiện thực hóa, kiểm thử đạt 100% và sẵn sàng cho buổi báo cáo nghiệm thu.*

---

## 🧩 4. Phân công Chi tiết 8 Phân hệ Backend (Phúc & Gấm)

Backend hệ thống được chia đều 50/50 thành 8 phân hệ trọng tâm giữa Phúc và Gấm:

| STT | Phân hệ / Nhóm module | Người phụ trách chính | Phạm vi & Thành phần mã nguồn chính |
| :---: | :--- | :---: | :--- |
| 1 | **Tra cứu phòng & giường (#UC-04)** | **Gấm** | `RoomController`, `RoomCatalogService`, `RoomCatalogRepository`, tra cứu danh mục phòng, danh sách giường trống và chi tiết cấu hình phòng. |
| 2 | **Hồ sơ sinh viên (Profile)** | **Gấm** | `StudentProfileDto`, endpoint hồ sơ cá nhân sinh viên, thông tin phòng đang cư trú và kiểm tra điều kiện đăng ký phòng. |
| 3 | **Sửa chữa CSVC (#UC-22)** | **Gấm** | `RepairController`, `RepairService`, `RepairRepository`, tạo yêu cầu sửa chữa, cập nhật mức ưu tiên, phân công kỹ thuật và nghiệm thu vật tư. |
| 4 | **Hoạt động công ích (#UC-30)** | **Gấm** | `CommunityController`, `CommunityService`, `CommunityRepository`, tra cứu hoạt động ngoại khóa, đăng ký tham gia, điểm danh và tích lũy điểm công ích. |
| 5 | **Nền tảng & Bảo mật 2FA (#UC-01)** | **Phúc** | Framework Spring Boot, Docker Compose, CI/CD, Routing nền tảng (`ApiRoutes`, `ApiSupport`), Security JWT, xác thực 2FA Authenticator (`AuthController`, `TotpService`). |
| 6 | **Đăng ký & Duyệt phòng (#UC-12)** | **Phúc** | `BookingController`, `RegistrationService`, `BookingRepository`, luồng đăng ký hồ sơ lưu trú, upload minh chứng và ban quản lý xét duyệt hồ sơ. |
| 7 | **Hóa đơn & VNPAY (#UC-17, #UC-08)** | **Phúc** | `InvoiceController`, `InvoiceService`, `PaymentController`, `PaymentService`, `VnpayGateway`, chốt chỉ số điện nước, phát hành hóa đơn và tích hợp thanh toán VNPAY Sandbox. |
| 8 | **Cổng ra vào & Kỷ luật (#UC-25, #UC-14)** | **Phúc** | `GateController`, `GateService`, `DisciplineController`, `DisciplineService`, `FileController`, quẹt thẻ ra vào, cấp vé khách thăm, ghi nhận vi phạm và giải quyết khiếu nại kỷ luật. |
