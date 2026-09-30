# Tài liệu dự án KTX Smart Hub

Phạm vi demo hiện tại gồm **8 UC trọng tâm từ SRS cũ**. Bắt đầu tại
[bộ demo](demo/README.md), sau đó đọc [thiết kế xử lý](demo/THIET_KE_XU_LY.md)
và [Sequence Diagram](sequence-diagrams/README.md).
Chuẩn DB: [PostgreSQL 16](demo/POSTGRESQL.md); thiết kế lớp: [Class Diagram demo](demo/class-diagrams/README.md).
API, dữ liệu/SP, tích hợp, test và nghiệm thu được liên kết tại mục lục demo.

| Thư mục | Mục đích |
|---|---|
| [demo/](demo/README.md) | SRS nguồn 8 UC, kịch bản demo, thiết kế xử lý, việc cần triển khai |
| [sequence-diagrams/demo/](sequence-diagrams/README.md) | 8 nguồn PlantUML và 8 bản SVG |
| main/ | SRS hiện hành, yêu cầu và sơ đồ tổng thể toàn hệ thống |
| [main/D10/](main/D10/README.md) | Báo cáo xử lý và Class Diagram đã có từ trước |
| other/ | Bài tập, persona, template và schema cũ để tham khảo |

## Tài liệu toàn hệ thống

- [SRS hiện hành](main/SRS_HeThongQuanLyKTX_v3.docx): phạm vi rộng hơn demo.
- [Danh sách yêu cầu](main/DanhSach_YeuCau_Nhom3.xlsx).
- [Use Case Diagram](main/UseCaseDiagram_QuanLyKTX.png).
- [Screen Flow](main/ScreenFlow_QuanLyKTX.drawio).
- [ERD](main/ERD_QuanLyKTX.drawio): mô hình trong tài liệu;
  đối chiếu [schema PostgreSQL triển khai](../database/01_schema.sql) có 19 bảng.
- [Class Diagram SRS](main/ClassDiagram_11Classes_SRS.drawio) và
  [thuyết minh Class/ERD](main/ClassDiagram_ERD_ThuyetMinh.docx).
- [Git workflow](main/GIT_WORKFLOW.md): nhánh riêng, PR vào develop.
- [TODO](../TODO.md): demo hiện tại và backlog toàn hệ thống.

Mã UC demo được chốt trong [nguồn đặc tả](demo/NGUON.md). README gốc và
một số màn hình còn dùng mã UC-01…UC-08 khác; không dùng nhãn đó để suy ra
mã đặc tả SRS. Chọn 8 UC không xóa các yêu cầu còn lại.

## Tài liệu tham khảo

- Chương 6: [Đề bài](other/C06_D1_Assignment.docx),
  [triển khai và kiểm thử](other/C06_D1_Software_Implementation_and_Testing.pdf).
- Chương 4–5: [Phân tích yêu cầu](other/C04_D1_PhanTichYeuCau_Nhom3.docx),
  [thiết kế dữ liệu](other/C05_D1_ThietKeDuLieu_Nhom3.docx).
- [Thuyết trình](other/TaiLieu_ThuyetTrinh_Nhom3.docx),
  [template SRS](other/Template_SRS.docx),
  [schema cũ](other/QuanLyKTX_Old_Schema.sql).
- Persona: [Sinh viên](other/Persona/Persona_Chinh_SinhVien.docx),
  [Admin](other/Persona/Persona_Phu1_Admin.docx),
  [Kế toán](other/Persona/Persona_Phu2_KeToan.docx),
  [Kỹ thuật](other/Persona/Persona_Phu3_KyThuat.docx),
  [Bảo vệ](other/Persona/Persona_Phu4_BaoVe.docx).
