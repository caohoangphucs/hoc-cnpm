# TÀI LIỆU DỰ ÁN HỆ THỐNG QUẢN LÝ KÝ TÚC XÁ (KTX SMART HUB)

Thư mục tài liệu được chuẩn hóa tên file và cấu trúc phân cấp:

---

## 📂 1. `docs/main/` — Tài Liệu Thiết Kế & Quy Chuẩn Phát Triển Chính
Nhóm tài liệu cốt lõi phục vụ trực tiếp cho việc phát triển, đối chiếu Traceability và báo cáo đồ án:

- **Quy chuẩn Git & Commit:**
  - [`GIT_WORKFLOW.md`](main/GIT_WORKFLOW.md): Quy chuẩn đặt tên nhánh, commit message bắt buộc kèm mã `#UC-XX` theo C6-D1.
- **Đặc tả & Yêu cầu:**
  - [`SRS_HeThongQuanLyKTX_v3.docx`](main/SRS_HeThongQuanLyKTX_v3.docx): Tài liệu đặc tả yêu cầu phần mềm chính thức (SRS).
  - [`DanhSach_YeuCau_Nhom3.xlsx`](main/DanhSach_YeuCau_Nhom3.xlsx): Danh sách phân loại yêu cầu chức năng & phi chức năng.
- **Sơ đồ thiết kế hệ thống:**
  - [`ClassDiagram_ERD_ThuyetMinh.docx`](main/ClassDiagram_ERD_ThuyetMinh.docx): Thuyết minh Class Diagram & ERD.
  - [`ERD_QuanLyKTX.drawio`](main/ERD_QuanLyKTX.drawio) / [`ERD_QuanLyKTX_3NF.drawio`](main/ERD_QuanLyKTX_3NF.drawio) / [`ERD_QuanLyKTX.png`](main/ERD_QuanLyKTX.png): Sơ đồ quan hệ thực thể chuẩn 3NF.
  - [`ScreenFlow_QuanLyKTX.drawio`](main/ScreenFlow_QuanLyKTX.drawio): Sơ đồ luồng chuyển trang và giao diện.
  - [`D10/`](main/D10/): Bộ tài liệu thiết kế xử lý chi tiết:
    - [`ClassDiagram_QuanLyKTX.drawio`](main/D10/ClassDiagram_QuanLyKTX.drawio)
    - [`SequenceDiagram.puml`](main/D10/SequenceDiagram.puml)
    - [`ThietKeXuLy_BaoCao.docx`](main/D10/ThietKeXuLy_BaoCao.docx)
- **Đề bài & Tiêu chí Chapter 6:**
  - [`C06_D1_Assignment.docx`](main/C06_D1_Assignment.docx) & [`C06_D1_Software_Implementation_and_Testing.pdf`](main/C06_D1_Software_Implementation_and_Testing.pdf): Hướng dẫn thực hành cài đặt 6–8 Use Cases và kiểm thử.

---

## 📁 2. `docs/other/` — Tài Liệu Tham Khảo, Bài Tập Cũ & Nháp
- [`C05_D1_ThietKeDuLieu_Nhom3.docx`](other/C05_D1_ThietKeDuLieu_Nhom3.docx): Bài thực hành thiết kế dữ liệu (Chương 5).
- [`C04_D1_PhanTichYeuCau_Nhom3.docx`](other/C04_D1_PhanTichYeuCau_Nhom3.docx): Bài thực hành phân tích yêu cầu (Chương 4).
- [`TaiLieu_ThuyetTrinh_Nhom3.docx`](other/TaiLieu_ThuyetTrinh_Nhom3.docx): Slide thuyết trình các giai đoạn trước.
- [`Template_SRS.docx`](other/Template_SRS.docx): Mẫu template tài liệu SRS ban đầu.
- [`Draft_HeThongKTX.drawio`](other/Draft_HeThongKTX.drawio): Bản vẽ nháp sơ bộ.
- [`QuanLyKTX_Old_Schema.sql`](other/QuanLyKTX_Old_Schema.sql): Script SQL cũ (đã chuẩn hóa vào `/database/01_schema.sql`).
- [`Persona/`](other/Persona/): Bộ tài liệu Persona người dùng chi tiết:
  - [`Persona_Chinh_SinhVien.docx`](other/Persona/Persona_Chinh_SinhVien.docx)
  - [`Persona_Phu1_Admin.docx`](other/Persona/Persona_Phu1_Admin.docx)
  - [`Persona_Phu2_KeToan.docx`](other/Persona/Persona_Phu2_KeToan.docx)
  - [`Persona_Phu3_KyThuat.docx`](other/Persona/Persona_Phu3_KyThuat.docx)
  - [`Persona_Phu4_BaoVe.docx`](other/Persona/Persona_Phu4_BaoVe.docx)
