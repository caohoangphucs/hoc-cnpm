# KẾ HOẠCH & DANH SÁCH TO-DO HOÀN THIỆN TÀI LIỆU TOÀN BỘ HỆ THỐNG (KTX SMART HUB)

> **Mục tiêu:** Mở rộng và hoàn thiện tài liệu kỹ thuật từ 8 Use Cases cơ bản lên **toàn bộ 24+ Use Cases (5 vai trò)** theo chuẩn đặc tả [`SRS_HeThongQuanLyKTX_v3.docx`](docs/main/SRS_HeThongQuanLyKTX_v3.docx).

---

## 📌 Danh Sách Nhiệm Vụ Chi Tiết & Tài Liệu Đính Kèm

### 1. Sơ đồ Tuần tự (Sequence Diagram) — Toàn bộ Use Cases
- [ ] **Task 1.1:** Viết script PlantUML Sequence Diagram chi tiết cho Phân hệ Sinh viên (Đặt giữ giường, Hủy đặt, Thanh toán VNPAY, Báo hỏng, Đăng ký tạm vắng).
- [ ] **Task 1.2:** Viết Sequence Diagram cho Phân hệ Admin (Duyệt hồ sơ, Điều chuyển phòng, Khóa/Mở tài khoản, Cấu hình năm học & đơn giá).
- [ ] **Task 1.3:** Viết Sequence Diagram cho Phân hệ Kế toán (Ghi chỉ số điện nước, Phát hành hóa đơn hàng loạt, Ghi nhận thu tiền mặt, Hoàn tiền/Hủy hóa đơn).
- [ ] **Task 1.4:** Viết Sequence Diagram cho Phân hệ Kỹ thuật (Tiếp nhận sự cố, Đề xuất vật tư, Xuất kho linh kiện, Báo cáo hoàn thành sửa chữa).
- [ ] **Task 1.5:** Viết Sequence Diagram cho Phân hệ Bảo vệ (Quét thẻ ra vào, Tự động ghi nhận vi phạm sau 23h, Điểm danh đột xuất, Xử lý khách thăm).
- **📎 Tài liệu đính kèm liên quan:**
  - [`docs/main/D10/SequenceDiagram.puml`](docs/main/D10/SequenceDiagram.puml)
  - [`docs/main/SRS_HeThongQuanLyKTX_v3.docx`](docs/main/SRS_HeThongQuanLyKTX_v3.docx) (Mục 3: Yêu cầu chức năng)

---

### 2. Sơ đồ Lớp (Class Diagram) & Thiết kế Xử lý
- [ ] **Task 2.1:** Mở rộng Class Diagram chi tiết toàn hệ thống bao gồm đầy đủ Controller, Service, DAO/Repository và DTO cho toàn bộ 5 phân hệ.
- [ ] **Task 2.2:** Cập nhật thuyết minh Class Diagram và mối tương quan với 10 bảng CSDL 3NF.
- **📎 Tài liệu đính kèm liên quan:**
  - [`docs/main/ClassDiagram_11Classes_SRS.drawio`](docs/main/ClassDiagram_11Classes_SRS.drawio)
  - [`docs/main/D10/ClassDiagram_QuanLyKTX.drawio`](docs/main/D10/ClassDiagram_QuanLyKTX.drawio)
  - [`docs/main/ClassDiagram_ERD_ThuyetMinh.docx`](docs/main/ClassDiagram_ERD_ThuyetMinh.docx)
  - [`docs/main/ERD_QuanLyKTX.drawio`](docs/main/ERD_QuanLyKTX.drawio)

---

### 3. Báo Cáo Thiết Kế Xử Lý (Thuyết Minh Kiến Trúc & Luồng Dữ Liệu)
- [ ] **Task 3.1:** Tổng hợp luồng xử lý Stored Procedures & Triggers tương ứng với từng Use Case vào báo cáo.
- [ ] **Task 3.2:** Hoàn thiện báo cáo thiết kế chi tiết các thuật toán kiểm tra tranh chấp phòng/giường, tính tiền lũy tiến điện nước.
- **📎 Tài liệu đính kèm liên quan:**
  - [`docs/main/D10/ThietKeXuLy_BaoCao.docx`](docs/main/D10/ThietKeXuLy_BaoCao.docx)
  - [`database/01_schema.sql`](database/01_schema.sql)
  - [`database/02_seed.sql`](database/02_seed.sql)

---

### 4. Traceability Matrix & Ma Trận Yêu Cầu Chức Năng
- [ ] **Task 4.1:** Chuẩn hóa mã hóa định danh Use Case (`#UC-01` $\rightarrow$ `#UC-XX`) đồng bộ giữa bảng tính, tài liệu SRS và mã nguồn.
- [ ] **Task 4.2:** Đánh xạ mức độ hoàn thành và phân công thành viên phụ trách từng module.
- **📎 Tài liệu đính kèm liên quan:**
  - [`docs/main/DanhSach_YeuCau_Nhom3.xlsx`](docs/main/DanhSach_YeuCau_Nhom3.xlsx)
  - [`docs/main/ScreenFlow_QuanLyKTX.drawio`](docs/main/ScreenFlow_QuanLyKTX.drawio)
  - [`docs/main/UseCaseDiagram_QuanLyKTX.png`](docs/main/UseCaseDiagram_QuanLyKTX.png)
  - [`docs/main/GIT_WORKFLOW.md`](docs/main/GIT_WORKFLOW.md)
