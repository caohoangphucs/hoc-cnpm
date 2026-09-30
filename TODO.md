# KẾ HOẠCH & DANH SÁCH TO-DO HOÀN THIỆN TÀI LIỆU TOÀN BỘ HỆ THỐNG (KTX SMART HUB)

> **Mục tiêu:** Mở rộng và hoàn thiện tài liệu kỹ thuật từ 8 Use Cases cơ bản lên **toàn bộ 24+ Use Cases (5 vai trò)** theo chuẩn đặc tả [`SRS_HeThongQuanLyKTX_v3.docx`](docs/main/SRS_HeThongQuanLyKTX_v3.docx) và **tiến trình chuẩn 5 bước thiết kế phần mềm (Chương 11 - Thiết kế xử lý)**.

---

## 🚀 TIẾN TRÌNH THIẾT KẾ PHẦN MỀM CHUẨN (CHƯƠNG 11) CHO TỪNG USE CASE

Quy trình 5 bước áp dụng đồng bộ cho toàn bộ các màn hình và Use Case:
1. **Bước 1: Xác định danh sách sự kiện giao diện (UI Events)** (onClick, onChange, onLoad, onSubmit...).
2. **Bước 2: Phân loại sự kiện & Xác định dữ liệu vào/ra** (Validation, Request Payload, Response Model).
3. **Bước 3: Viết mã giả (Pseudocode / Algorithm)** cho các sự kiện chính (xử lý logic, kiểm tra điều kiện, gọi SP/Service, cập nhật UI).
4. **Bước 4: Xây dựng Sơ đồ Tuần tự (Sequence Diagram)** chi tiết tương ứng (UI $\rightarrow$ Controller $\rightarrow$ Service $\rightarrow$ DAO/SP/Trigger $\rightarrow$ Database).
5. **Bước 5: Cập nhật & Hoàn thiện Sơ đồ Lớp (Class Diagram)** (Bổ sung methods, attributes, Controller/Service/DAO mới phát sinh).

---

## 📌 Danh Sách Nhiệm Vụ Chi Tiết & Tài Liệu Đính Kèm

### 1. Phân Hệ 1: Sinh Viên (Student Portal)
- [ ] **Task 1.1 (#UC-01): Đăng nhập & Xác thực phân quyền 5 vai trò**
  - Thực hiện 5 bước: Sự kiện `onClick_Login` $\rightarrow$ Pseudocode kiểm tra BCrypt/Role $\rightarrow$ Sequence Diagram Auth $\rightarrow$ Bổ sung `AuthController`, `AuthService`.
- [ ] **Task 1.2 (#UC-02): Tra cứu sơ đồ phòng, giường trống & xem chi tiết**
  - Thực hiện 5 bước: Sự kiện `onChange_Building`, `onClick_ViewRoom` $\rightarrow$ Sequence Diagram $\rightarrow$ `RoomController`, `RoomService`.
- [ ] **Task 1.3 (#UC-03): Đặt giữ giường trực tuyến & Chống tranh chấp (Concurreny Control)**
  - Thực hiện 5 bước: Sự kiện `onClick_DatGiuong` (Disabled+Spinner chống double-click) $\rightarrow$ Pseudocode gọi `usp_DatGiuong` $\rightarrow$ Sequence Diagram $\rightarrow$ `BookingController`, `BookingService`.
- [ ] **Task 1.4 (#UC-07): Thanh toán trực tuyến hóa đơn (VNPAY Sandbox / Tiền mặt)**
  - Thực hiện 5 bước: Sự kiện `onClick_ThanhToan` $\rightarrow$ Pseudocode tạo URL VNPAY / IPN Callback $\rightarrow$ Sequence Diagram $\rightarrow$ `PaymentController`, `VNPayService`.
- [ ] **Task 1.5 (#UC-09): Gửi yêu cầu báo hỏng & Theo dõi tiến độ sửa chữa**
  - Thực hiện 5 bước: Sự kiện `onSubmit_YeuCauSuaChua` $\rightarrow$ Sequence Diagram $\rightarrow$ `MaintenanceRequestController`.
- [ ] **Task 1.6 (#UC-10): Đăng ký tạm vắng & Xem lịch sử ra vào**
  - Thực hiện 5 bước: Sự kiện `onSubmit_TamVang` $\rightarrow$ Sequence Diagram $\rightarrow$ `AttendanceController`.
- **📎 Tài liệu đính kèm:** [`docs/main/SRS_HeThongQuanLyKTX_v3.docx`](docs/main/SRS_HeThongQuanLyKTX_v3.docx), [`docs/main/ScreenFlow_QuanLyKTX.drawio`](docs/main/ScreenFlow_QuanLyKTX.drawio), [`docs/main/D10/SequenceDiagram.puml`](docs/main/D10/SequenceDiagram.puml).

---

### 2. Phân Hệ 2: Ban Quản Lý (Admin Portal)
- [ ] **Task 2.1 (#UC-04): Xét duyệt hồ sơ đăng ký phòng & Tự động gán hợp đồng lưu trú**
  - Thực hiện 5 bước: Sự kiện `onClick_Duyet` / `onClick_TuChoi` $\rightarrow$ Pseudocode `usp_DuyetDangKy` + Trigger $\rightarrow$ Sequence Diagram $\rightarrow$ `AdminBookingController`.
- [ ] **Task 2.2 (#UC-11): Quản lý danh mục Tòa nhà, Loại phòng, Phòng & Giường**
  - Thực hiện 5 bước: Sự kiện CRUD danh mục $\rightarrow$ Pseudocode kiểm tra ràng buộc toàn vẹn $\rightarrow$ Sequence Diagram.
- [ ] **Task 2.3 (#UC-12): Điều chuyển phòng & Trả phòng/Thanh lý hợp đồng**
  - Thực hiện 5 bước: Sự kiện `onClick_ChuyenPhong`, `onClick_TraPhong` $\rightarrow$ Sequence Diagram $\rightarrow$ `ContractService`.
- [ ] **Task 2.4 (#UC-13): Quản lý tài khoản, Phân quyền & Khóa tài khoản vi phạm**
  - Thực hiện 5 bước: Sự kiện `onClick_ToggleStatus` $\rightarrow$ Sequence Diagram.
- [ ] **Task 2.5 (#UC-08): Báo cáo thống kê doanh thu & Tỷ lệ lấp đầy phòng KTX**
  - Thực hiện 5 bước: Sự kiện `onChange_FilterPeriod` $\rightarrow$ Sequence Diagram gọi `vw_DoanhThuThang` $\rightarrow$ `ReportController`.
- **📎 Tài liệu đính kèm:** [`docs/main/D10/ThietKeXuLy_BaoCao.docx`](docs/main/D10/ThietKeXuLy_BaoCao.docx), [`docs/main/ClassDiagram_11Classes_SRS.drawio`](docs/main/ClassDiagram_11Classes_SRS.drawio).

---

### 3. Phân Hệ 3: Kế Toán (Accounting Portal)
- [ ] **Task 3.1 (#UC-05): Ghi nhận chỉ số điện nước định kỳ hàng tháng**
  - Thực hiện 5 bước: Sự kiện `onSubmit_ChiSoDienNuoc` $\rightarrow$ Pseudocode gọi `usp_GhiChiSoDienNuoc` (Validation chỉ số mới $\ge$ cũ) $\rightarrow$ Sequence Diagram $\rightarrow$ `UtilityController`.
- [ ] **Task 3.2 (#UC-06): Phát hành hóa đơn hàng loạt (Tiền phòng + Tiền điện nước)**
  - Thực hiện 5 bước: Sự kiện `onClick_PhatHanhHoaDon` $\rightarrow$ Pseudocode `usp_PhatHanhHoaDon` $\rightarrow$ Sequence Diagram $\rightarrow$ `InvoiceController`, `InvoiceService`.
- [ ] **Task 3.3 (#UC-14): Thu tiền trực tiếp & In biên lai thu tiền mặt**
  - Thực hiện 5 bước: Sự kiện `onClick_ThuTienMat` $\rightarrow$ Pseudocode `usp_XacNhanThanhToan` $\rightarrow$ Sequence Diagram.
- [ ] **Task 3.4 (#UC-15): Quản lý cấu hình đơn giá dịch vụ & Biểu giá điện lũy tiến**
  - Thực hiện 5 bước: Sự kiện `onSubmit_DonGia` $\rightarrow$ Sequence Diagram.
- **📎 Tài liệu đính kèm:** [`database/01_schema.sql`](database/01_schema.sql), [`docs/main/D10/SequenceDiagram.puml`](docs/main/D10/SequenceDiagram.puml).

---

### 4. Phân Hệ 4: Kỹ Thuật (Maintenance & Inventory Portal)
- [ ] **Task 4.1 (#UC-16): Tiếp nhận & Phân công xử lý sự cố thiết bị**
  - Thực hiện 5 bước: Sự kiện `onClick_TiepNhan` $\rightarrow$ Sequence Diagram $\rightarrow$ `MaintenanceController`.
- [ ] **Task 4.2 (#UC-17): Xuất kho vật tư & Cập nhật tồn kho tự động**
  - Thực hiện 5 bước: Sự kiện `onSubmit_XuatKho` $\rightarrow$ Pseudocode `usp_HoanThanhSuaChua` $\rightarrow$ Sequence Diagram $\rightarrow$ `InventoryService`.
- [ ] **Task 4.3 (#UC-18): Nhập kho vật tư mới & Thống kê tồn kho**
  - Thực hiện 5 bước: Sự kiện `onSubmit_NhapVatTu` $\rightarrow$ Sequence Diagram.
- **📎 Tài liệu đính kèm:** [`docs/main/D10/ThietKeXuLy_BaoCao.docx`](docs/main/D10/ThietKeXuLy_BaoCao.docx), [`database/01_schema.sql`](database/01_schema.sql).

---

### 5. Phân Hệ 5: Bảo Vệ & An Ninh (Security Portal)
- [ ] **Task 5.1 (#UC-19): Quét mã QR/Thẻ sinh viên kiểm soát ra vào cổng**
  - Thực hiện 5 bước: Sự kiện `onScan_StudentCard` $\rightarrow$ Pseudocode gọi `usp_GhiNhanRaVao` $\rightarrow$ Sequence Diagram $\rightarrow$ `GateController`.
- [ ] **Task 5.2 (#UC-20): Tự động ghi nhận vi phạm về trễ sau 23:00 & Thông báo cảnh cáo**
  - Thực hiện 5 bước: Sequence Diagram kích hoạt trigger/logic phạt trễ $\rightarrow$ Sequence Diagram.
- [ ] **Task 5.3 (#UC-21): Điểm danh lưu trú phòng đột xuất & Đăng ký khách thăm**
  - Thực hiện 5 bước: Sự kiện `onSubmit_DiemDanh`, `onSubmit_KhachTham` $\rightarrow$ Sequence Diagram.
- **📎 Tài liệu đính kèm:** [`docs/main/D10/SequenceDiagram.puml`](docs/main/D10/SequenceDiagram.puml), [`docs/main/SRS_HeThongQuanLyKTX_v3.docx`](docs/main/SRS_HeThongQuanLyKTX_v3.docx).

---

### 6. Tổng Hợp & Đóng Gói Hồ Sơ Thiết Kế Toàn Hệ Thống
- [ ] **Task 6.1: Đồng bộ hóa toàn bộ Sequence Diagram vào PlantUML script tập trung**
  - Đích: [`docs/main/D10/SequenceDiagram.puml`](docs/main/D10/SequenceDiagram.puml).
- [ ] **Task 6.2: Hoàn thiện Sơ đồ Lớp Tổng Thể (Full System Class Diagram)**
  - Đích: [`docs/main/D10/ClassDiagram_QuanLyKTX.drawio`](docs/main/D10/ClassDiagram_QuanLyKTX.drawio), [`docs/main/ClassDiagram_11Classes_SRS.drawio`](docs/main/ClassDiagram_11Classes_SRS.drawio).
- [ ] **Task 6.3: Hoàn thiện Báo cáo Thuyết minh Thiết kế Xử lý (Chương 11)**
  - Đích: [`docs/main/D10/ThietKeXuLy_BaoCao.docx`](docs/main/D10/ThietKeXuLy_BaoCao.docx).
- [ ] **Task 6.4: Rà soát Traceability Matrix & Mã hóa Commit (`#UC-XX`)**
  - Đích: [`docs/main/DanhSach_YeuCau_Nhom3.xlsx`](docs/main/DanhSach_YeuCau_Nhom3.xlsx), [`docs/main/GIT_WORKFLOW.md`](docs/main/GIT_WORKFLOW.md).
