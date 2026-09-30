# KẾ HOẠCH & DANH SÁCH TO-DO HOÀN THIỆN TÀI LIỆU TOÀN BỘ HỆ THỐNG (KTX SMART HUB)

> **Mục tiêu:** Mở rộng và hoàn thiện tài liệu kỹ thuật bao phủ **toàn bộ 28 Yêu cầu Chức năng (Functional Requirements - REQ-001 $\rightarrow$ REQ-038) / Toàn bộ Use Cases của 5 vai trò** theo chuẩn đặc tả [`SRS_HeThongQuanLyKTX_v3.docx`](docs/main/SRS_HeThongQuanLyKTX_v3.docx), [`DanhSach_YeuCau_Nhom3.xlsx`](docs/main/DanhSach_YeuCau_Nhom3.xlsx) và **tiến trình chuẩn 5 bước thiết kế phần mềm (Chương 11 - Thiết kế xử lý)**.

---

## Phạm vi demo hiện tại

**Đích công nghệ:** Spring Boot4.1.1/Java17, Thymeleaf, Spring Security, Spring JDBC và PostgreSQL16; xem [kiến trúc và chuyển đổi](docs/demo/SPRING_BOOT.md). Code chưa chuyển.

Theo phạm vi đã chốt: dùng **8 UC trong SRS cũ** — UC-04, UC-08, UC-30,
UC-12, UC-17, UC-22, UC-25, UC-14. Các mục 28 yêu cầu phía dưới được giữ
làm **backlog toàn hệ thống**, không là điều kiện hoàn thành đợt demo này.

- [x] Chốt SRS nguồn và bảng UC/REQ: [nguồn demo](docs/demo/NGUON.md).
- [x] Tạo 8 Sequence Diagram riêng: [mục lục](docs/sequence-diagrams/README.md).
- [x] Viết thiết kế xử lý 5 bước cho 8 UC: [thiết kế](docs/demo/THIET_KE_XU_LY.md).
- [x] Viết kịch bản demo và bảng khoảng cách triển khai: [kế hoạch](docs/demo/README.md).
- [x] Class Diagram 8 UC: [kiến trúc và miền](docs/demo/class-diagrams/README.md).
- [x] Chuẩn PostgreSQL, dữ liệu/SP và quyết định nghiệp vụ: [PostgreSQL](docs/demo/POSTGRESQL.md).
- [x] API/OpenAPI, phân quyền và tích hợp: [API](docs/demo/API_VA_PHAN_QUYEN.md), [tích hợp](docs/demo/TICH_HOP.md).
- [x] Test case/fixture, truy vết và tiêu chí nghiệm thu: [kiểm thử](docs/demo/KIEM_THU.md), [triển khai](docs/demo/TRIEN_KHAI_VA_NGHIEM_THU.md).
- [ ] Hoàn thiện auth/phân quyền, dữ liệu demo và các phần thiếu trong code.
- [ ] Triển khai module còn thiếu UC-14 và UC-30, tích hợp thanh toán UC-08.
- [ ] Chạy kịch bản demo, lưu bằng chứng thực thi và nghiệm thu đủ 8 UC.

Hoàn thành tài liệu không đồng nghĩa ứng dụng đã demo được.

## 🚀 TIẾN TRÌNH THIẾT KẾ PHẦN MỀM CHUẨN (CHƯƠNG 11) CHO TỪNG USE CASE

Mỗi Use Case đều được thực hiện đầy đủ theo chu trình 5 bước:
1. **Bước 1: Xác định danh sách sự kiện giao diện (UI Events)** (onClick, onChange, onLoad, onSubmit...).
2. **Bước 2: Phân loại sự kiện & Xác định dữ liệu vào/ra** (Validation, Request Payload, Response Model, Trạng thái Loading/Empty).
3. **Bước 3: Viết mã giả (Pseudocode / Algorithm)** cho các sự kiện chính (xử lý logic, kiểm tra điều kiện, gọi SP/Service, cập nhật UI).
4. **Bước 4: Xây dựng Sơ đồ Tuần tự (Sequence Diagram)** chi tiết tương ứng (UI $\rightarrow$ Controller $\rightarrow$ Service $\rightarrow$ DAO/SP/Trigger $\rightarrow$ Database).
5. **Bước 5: Cập nhật & Hoàn thiện Sơ đồ Lớp (Class Diagram)** (Bổ sung methods, attributes, Controller/Service/DAO mới phát sinh).

---

## 📌 PHẦN I: BACKLOG TÀI LIỆU TOÀN HỆ THỐNG (DOC-LEVEL TASKS)

- [x] **Doc Task 0: Hoàn thành Đặc tả Use Case chi tiết 100% (28 Functional Use Cases)**
  - **Đính kèm tài liệu:** [`docs/main/SRS_HeThongQuanLyKTX_v3.docx`](docs/main/SRS_HeThongQuanLyKTX_v3.docx) (Mục 5: Bảng tổng quan Table 17 & 28 bảng đặc tả 12 trường chuẩn).
  - **Trạng thái:** ✅ Đã hoàn thành và commit (`c8849a0`).

- [ ] **Doc Task 1: Hoàn thiện Sequence Diagram toàn bộ 28 Functional Use Cases**
  - **Đính kèm tài liệu:** [`docs/sequence-diagrams/README.md`](docs/sequence-diagrams/README.md), [`docs/main/SRS_HeThongQuanLyKTX_v3.docx`](docs/main/SRS_HeThongQuanLyKTX_v3.docx).
  - **Nội dung:** Mở rộng vẽ đầy đủ Sequence Diagram cho toàn bộ 28 Use Cases chức năng thuộc 5 phân hệ (Sinh viên, Admin, Kế toán, Kỹ thuật, Bảo vệ, Bảo trì hệ thống).

- [ ] **Doc Task 2: Mở rộng Class Diagram chi tiết toàn hệ thống**
  - **Đính kèm tài liệu:** [`docs/main/ClassDiagram_11Classes_SRS.drawio`](docs/main/ClassDiagram_11Classes_SRS.drawio), [`docs/main/D10/ClassDiagram_QuanLyKTX.drawio`](docs/main/D10/ClassDiagram_QuanLyKTX.drawio), [`docs/main/ClassDiagram_ERD_ThuyetMinh.docx`](docs/main/ClassDiagram_ERD_ThuyetMinh.docx).
  - **Nội dung:** Bổ sung đầy đủ Controller, Service, Repository/DAO, DTO cho toàn bộ các Use Cases phát sinh.

- [ ] **Doc Task 3: Cập nhật Báo cáo Thiết kế xử lý tổng thể (Chương 11)**
  - **Đính kèm tài liệu:** [`docs/main/D10/ThietKeXuLy_BaoCao.docx`](docs/main/D10/ThietKeXuLy_BaoCao.docx), [`database/01_schema.sql`](database/01_schema.sql), [`database/02_seed.sql`](database/02_seed.sql).
  - **Nội dung:** Hợp nhất phân tích 5 bước, mã giả (Pseudocode) và luồng Stored Procedures cho tất cả 28 Use Cases.

- [ ] **Doc Task 4: Rà soát & Đồng bộ Traceability Matrix (Bảng phân loại yêu cầu)**
  - **Đính kèm tài liệu:** [`docs/main/DanhSach_YeuCau_Nhom3.xlsx`](docs/main/DanhSach_YeuCau_Nhom3.xlsx), [`docs/main/ScreenFlow_QuanLyKTX.drawio`](docs/main/ScreenFlow_QuanLyKTX.drawio), [`docs/main/UseCaseDiagram_QuanLyKTX.png`](docs/main/UseCaseDiagram_QuanLyKTX.png), [`docs/main/GIT_WORKFLOW.md`](docs/main/GIT_WORKFLOW.md).
  - **Nội dung:** Đảm bảo mã hóa Use Case (`#UC-XX` / `#REQ-XX`) khớp 100% giữa tài liệu SRS, danh sách yêu cầu, sơ đồ UML và commit git.

---

## 📌 PHẦN II: BACKLOG TOÀN BỘ 28 FUNCTIONAL USE CASES (5 VAI TRÒ & 5 BƯỚC CHƯƠNG 11)

### 🎓 1. Phân Hệ 1: Sinh Viên (Student Actor - 9 Use Cases)
- [ ] **Task 1.1 (REQ-001 / UC-04): Đăng ký phòng trực tuyến & Tải hồ sơ minh chứng**
  - Áp dụng 5 bước: Sự kiện `onClick_DangKy`, `onUpload_Docs` $\rightarrow$ Pseudocode `usp_DatGiuong` $\rightarrow$ Sequence Diagram $\rightarrow$ `BookingController`, `BookingService`.
- [ ] **Task 1.2 (REQ-002 / UC-02): Xem hóa đơn điện nước & Tải hóa đơn PDF**
  - Áp dụng 5 bước: Sự kiện `onClick_XemHoaDon`, `onClick_ExportPDF` $\rightarrow$ Sequence Diagram $\rightarrow$ `InvoiceController`.
- [ ] **Task 1.3 (REQ-003 / UC-03): Gửi yêu cầu sửa chữa thiết bị trong phòng kèm ảnh**
  - Áp dụng 5 bước: Sự kiện `onSubmit_BaoHong` $\rightarrow$ Sequence Diagram $\rightarrow$ `MaintenanceRequestController`.
- [ ] **Task 1.4 (REQ-004 / UC-08): Thanh toán tiền phòng & điện nước qua VNPAY / Ngân hàng**
  - Áp dụng 5 bước: Sự kiện `onClick_ThanhToan` $\rightarrow$ Pseudocode tạo URL VNPAY & IPN Callback $\rightarrow$ Sequence Diagram $\rightarrow$ `PaymentController`, `VNPayService`.
- [ ] **Task 1.5 (REQ-005 / UC-05): Nhận & Xem thông báo quan trọng từ Ban Quản lý**
  - Áp dụng 5 bước: Sự kiện `onLoad_Notifications`, `onClick_MarkRead` $\rightarrow$ Sequence Diagram $\rightarrow$ `NotificationService`.
- [ ] **Task 1.6 (REQ-006 / UC-06): Khai báo thông tin khách đến thăm phòng**
  - Áp dụng 5 bước: Sự kiện `onSubmit_KhaiBaoKhach` $\rightarrow$ Sequence Diagram $\rightarrow$ `VisitorController`.
- [ ] **Task 1.7 (REQ-007 / UC-07): Gửi đơn yêu cầu đổi phòng KTX**
  - Áp dụng 5 bước: Sự kiện `onSubmit_DoiPhong` $\rightarrow$ Sequence Diagram $\rightarrow$ `RoomTransferController`.
- [ ] **Task 1.8 (REQ-017 / UC-30): Đăng ký tham gia hoạt động công ích KTX**
  - Áp dụng 5 bước: Sự kiện `onClick_DangKyCongIch` $\rightarrow$ Sequence Diagram $\rightarrow$ `CommunityServiceController`.
- [ ] **Task 1.9 (REQ-029 & REQ-038): Quy đổi điểm công ích sang điểm rèn luyện & Xuất giấy xác nhận PDF**
  - Áp dụng 5 bước: Sự kiện `onClick_QuyDoiDiem`, `onClick_XuatGiayXacNhan` $\rightarrow$ Pseudocode thuật toán quy đổi (10 điểm = 1 ĐRL, max 5 ĐRL/kỳ) $\rightarrow$ Sequence Diagram $\rightarrow$ `CertificateService`.

---

### 🏛️ 2. Phân Hệ 2: Ban Quản Lý (Admin Actor - 7 Use Cases)
- [ ] **Task 2.1 (REQ-008 / UC-08b): Cấu hình mức giảm giá tiền phòng cho SV diện chính sách**
  - Áp dụng 5 bước: Sự kiện `onSubmit_ChinhSachGiamGia` $\rightarrow$ Sequence Diagram $\rightarrow$ `PolicyController`.
- [ ] **Task 2.2 (REQ-009 / UC-09): Quản lý sơ đồ phòng, trạng thái lưu trú & cấu hình giường**
  - Áp dụng 5 bước: Sự kiện CRUD Tòa nhà/Phòng/Giường $\rightarrow$ Sequence Diagram $\rightarrow$ `RoomManagementController`.
- [ ] **Task 2.3 (REQ-011 / UC-12): Duyệt/Từ chối hồ sơ đăng ký phòng & Gán hợp đồng**
  - Áp dụng 5 bước: Sự kiện `onClick_Duyet`, `onClick_TuChoi` $\rightarrow$ Pseudocode `usp_DuyetDangKy` $\rightarrow$ Sequence Diagram $\rightarrow$ `AdminRegistrationController`.
- [ ] **Task 2.4 (REQ-012 / UC-14): Quản lý vi phạm, xử lý kỷ luật & Cảnh báo vi phạm nhiều lần**
  - Áp dụng 5 bước: Sự kiện `onSubmit_XuLyViPham` $\rightarrow$ Sequence Diagram $\rightarrow$ `DisciplineController`.
- [ ] **Task 2.5 (REQ-014 / UC-01): Phân quyền truy cập 5 vai trò & Khóa/Mở tài khoản**
  - Áp dụng 5 bước: Sự kiện `onChange_Role`, `onClick_ToggleStatus` $\rightarrow$ Sequence Diagram $\rightarrow$ `AccountController`.
- [ ] **Task 2.6 (REQ-034 / UC-34): Phê duyệt đề xuất thay đổi đơn giá dịch vụ từ Kế toán**
  - Áp dụng 5 bước: Sự kiện `onClick_DuyetDonGia` $\rightarrow$ Sequence Diagram $\rightarrow$ `PriceApprovalController`.
- [ ] **Task 2.7 (REQ-037 / UC-37): Báo cáo tổng hợp vi phạm về trễ theo SV và theo phòng**
  - Áp dụng 5 bước: Sự kiện `onChange_FilterViolation` $\rightarrow$ Sequence Diagram $\rightarrow$ `ViolationReportController`.

---

### 💰 3. Phân Hệ 3: Kế Toán (Accounting Actor - 4 Use Cases)
- [ ] **Task 3.1 (REQ-013 / UC-13): Xuất báo cáo tài chính hàng tháng & Thống kê doanh thu**
  - Áp dụng 5 bước: Sự kiện `onClick_ExportFinanceReport` $\rightarrow$ Sequence Diagram gọi `vw_DoanhThuThang` $\rightarrow$ `FinanceReportController`.
- [ ] **Task 3.2 (REQ-018 / UC-18): Đề xuất thay đổi đơn giá dịch vụ (điện, nước, wifi, tiền phòng)**
  - Áp dụng 5 bước: Sự kiện `onSubmit_DeXuatDonGia` $\rightarrow$ Sequence Diagram $\rightarrow$ `ServicePriceController`.
- [ ] **Task 3.3 (REQ-033 / UC-17): Tự động tính tiền điện nước theo biểu giá lũy tiến & Phát hành hóa đơn**
  - Áp dụng 5 bước: Sự kiện `onClick_PhatHanhHoaDon` $\rightarrow$ Pseudocode gọi `usp_PhatHanhHoaDon` $\rightarrow$ Sequence Diagram $\rightarrow$ `InvoiceBatchService`.
- [ ] **Task 3.4 (REQ-033b / UC-17b): Thu tiền mặt trực tiếp & In phiếu thu biên lai**
  - Áp dụng 5 bước: Sự kiện `onClick_ThuTienMat` $\rightarrow$ Pseudocode gọi `usp_XacNhanThanhToan` $\rightarrow$ Sequence Diagram $\rightarrow$ `CashierController`.

---

### 🔧 4. Phân Hệ 4: Kỹ Thuật (Maintenance Actor - 3 Use Cases)
- [ ] **Task 4.1 (REQ-010 / UC-10): Nhập chỉ số điện nước hàng loạt qua file Excel**
  - Áp dụng 5 bước: Sự kiện `onUpload_ExcelChiSo` $\rightarrow$ Pseudocode parse Excel + gọi `usp_GhiChiSoDienNuoc` $\rightarrow$ Sequence Diagram $\rightarrow$ `UtilityImportController`.
- [ ] **Task 4.2 (REQ-015 / UC-15): Quản lý danh mục vật tư & Kiểm kê tồn kho thiết bị**
  - Áp dụng 5 bước: Sự kiện CRUD Vật tư $\rightarrow$ Sequence Diagram $\rightarrow$ `MaterialController`.
- [ ] **Task 4.3 (REQ-030 / UC-22): Tiếp nhận, cập nhật trạng thái sửa chữa & Xuất kho vật tư**
  - Áp dụng 5 bước: Sự kiện `onClick_TiepNhan`, `onSubmit_HoanThanh` $\rightarrow$ Pseudocode gọi `usp_HoanThanhSuaChua` $\rightarrow$ Sequence Diagram $\rightarrow$ `MaintenanceExecutionController`.

---

### 🛡️ 5. Phân Hệ 5: Bảo Vệ (Security Actor - 2 Use Cases)
- [ ] **Task 5.1 (REQ-035 / UC-35): Xác nhận và kiểm soát thông tin khách thăm tại cổng KTX**
  - Áp dụng 5 bước: Sự kiện `onLookup_Visitor`, `onClick_XacNhanVao` $\rightarrow$ Sequence Diagram $\rightarrow$ `GateVisitorController`.
- [ ] **Task 5.2 (REQ-036 / UC-25): Quét mã vào cổng & Tự động ghi nhận vi phạm về trễ sau 23:00**
  - Áp dụng 5 bước: Sự kiện `onScan_Card` $\rightarrow$ Pseudocode gọi `usp_GhiNhanRaVao` (Trigger phạt trễ) $\rightarrow$ Sequence Diagram $\rightarrow$ `GateAccessController`.

---

### ⚙️ 6. Phân Hệ 6: Chung & Bảo Trì Hệ Thống (System / Maintainer - 3 Use Cases)
- [ ] **Task 6.1 (REQ-016 / UC-16): Quên mật khẩu & Khôi phục tài khoản qua Email OTP / Reset Token**
  - Áp dụng 5 bước: Sự kiện `onSubmit_ForgotPassword`, `onSubmit_ResetPassword` $\rightarrow$ Sequence Diagram $\rightarrow$ `PasswordResetService`.
- [ ] **Task 6.2 (REQ-031 / UC-31): Tra cứu & Xem nhật ký lỗi hệ thống (Error / Audit Logs)**
  - Áp dụng 5 bước: Sự kiện `onFilter_Logs` $\rightarrow$ Sequence Diagram $\rightarrow$ `AuditLogController`.
- [ ] **Task 6.3 (REQ-032 / UC-32): Cảnh báo tự động khi phát sinh sự cố máy chủ hoặc sao lưu thất bại**
  - Áp dụng 5 bước: Background Monitoring Job $\rightarrow$ Pseudocode gửi Email/SMS Alert $\rightarrow$ Sequence Diagram $\rightarrow$ `SystemAlertService`.
