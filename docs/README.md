# TÀI LIỆU DỰ ÁN HỆ THỐNG QUẢN LÝ KÝ TÚC XÁ (KTX SMART HUB)

Thư mục tài liệu được phân loại và chuẩn hóa chính xác theo từng loại sơ đồ và mục đích sử dụng:

---

## 📂 1. `docs/main/` — Tài Liệu Thiết Kế & Quy Chuẩn Triển Khai Toàn Diện Dự Án
Nhóm tài liệu cốt lõi phục vụ trực tiếp cho việc triển khai **toàn bộ các Use Case** theo đặc tả SRS của hệ thống:

### 📐 Danh Mục Sơ Đồ Thiết Kế (UML & Database)
- **Sơ đồ Use Case (Use Case Diagram):**
  - [`UseCaseDiagram_QuanLyKTX.png`](main/UseCaseDiagram_QuanLyKTX.png): Sơ đồ Use Case tổng thể toàn hệ thống (5 vai trò: Sinh viên, Admin, Kế toán, Kỹ thuật, Bảo vệ và các hệ thống liên kết).
- **Sơ đồ Cơ sở Dữ liệu (ERD - 3NF):**
  - [`ERD_QuanLyKTX.drawio`](main/ERD_QuanLyKTX.drawio): Sơ đồ thực thể quan hệ CSDL chuẩn hóa 3NF (10 bảng thực thể).
- **Sơ đồ Lớp (Class Diagram):**
  - [`ClassDiagram_11Classes_SRS.drawio`](main/ClassDiagram_11Classes_SRS.drawio): Sơ đồ Class Diagram 11 lớp thực thể cốt lõi chuẩn SRS.
  - [`ClassDiagram_ERD_ThuyetMinh.docx`](main/ClassDiagram_ERD_ThuyetMinh.docx): Tài liệu thuyết minh chi tiết Class Diagram & ERD.
  - [`D10/ClassDiagram_QuanLyKTX.drawio`](main/D10/ClassDiagram_QuanLyKTX.drawio): Sơ đồ Class Diagram mở rộng theo thiết kế xử lý.
- **Sơ đồ Tuần tự (Sequence Diagram):**
  - [`D10/SequenceDiagram.puml`](main/D10/SequenceDiagram.puml): Script PlantUML mô tả Sequence Diagram các luồng nghiệp vụ.
  - [`D10/ThietKeXuLy_BaoCao.docx`](main/D10/ThietKeXuLy_BaoCao.docx): Báo cáo thiết kế xử lý chi tiết.
- **Sơ đồ Luồng Màn Hình (Screen Flow):**
  - [`ScreenFlow_QuanLyKTX.drawio`](main/ScreenFlow_QuanLyKTX.drawio): Sơ đồ luồng chuyển trang và giao diện người dùng.

### 📋 Đặc Tả & Quy Chuẩn Phát Triển
- **Đặc tả Yêu cầu phần mềm:**
  - [`SRS_HeThongQuanLyKTX_v3.docx`](main/SRS_HeThongQuanLyKTX_v3.docx): Bản đặc tả yêu cầu SRS chính thức toàn hệ thống.
  - [`DanhSach_YeuCau_Nhom3.xlsx`](main/DanhSach_YeuCau_Nhom3.xlsx): Danh sách phân loại yêu cầu chức năng & phi chức năng.
- **Quy chuẩn Git & Commit:**
  - [`GIT_WORKFLOW.md`](main/GIT_WORKFLOW.md): Quy chuẩn phân nhánh (`main`, `develop`, `feat/...`), cấm commit trực tiếp trên `main`, commit message kèm mã `#UC-XX`.

---

## 📁 2. `docs/other/` — Tài Liệu Tham Khảo, Bài Tập Từng Chương & Nháp
- **Đề bài & Yêu cầu thực hành Chapter 6 (C6-D1):**
  - [`C06_D1_Assignment.docx`](other/C06_D1_Assignment.docx)
  - [`C06_D1_Software_Implementation_and_Testing.pdf`](other/C06_D1_Software_Implementation_and_Testing.pdf)
- **Bài tập các chương trước:**
  - [`C05_D1_ThietKeDuLieu_Nhom3.docx`](other/C05_D1_ThietKeDuLieu_Nhom3.docx): Bài thực hành thiết kế dữ liệu (Chương 5).
  - [`C04_D1_PhanTichYeuCau_Nhom3.docx`](other/C04_D1_PhanTichYeuCau_Nhom3.docx): Bài thực hành phân tích yêu cầu (Chương 4).
  - [`TaiLieu_ThuyetTrinh_Nhom3.docx`](other/TaiLieu_ThuyetTrinh_Nhom3.docx): Slide thuyết trình các giai đoạn trước.
- **File mẫu & Nháp:**
  - [`Template_SRS.docx`](other/Template_SRS.docx): Mẫu template tài liệu SRS ban đầu.
  - [`QuanLyKTX_Old_Schema.sql`](other/QuanLyKTX_Old_Schema.sql): Script SQL cũ (đã chuẩn hóa vào `/database/01_schema.sql`).
- **Persona người dùng chi tiết:**
  - [`Persona_Chinh_SinhVien.docx`](other/Persona/Persona_Chinh_SinhVien.docx)
  - [`Persona_Phu1_Admin.docx`](other/Persona/Persona_Phu1_Admin.docx)
  - [`Persona_Phu2_KeToan.docx`](other/Persona/Persona_Phu2_KeToan.docx)
  - [`Persona_Phu3_KyThuat.docx`](other/Persona/Persona_Phu3_KyThuat.docx)
  - [`Persona_Phu4_BaoVe.docx`](other/Persona/Persona_Phu4_BaoVe.docx)
