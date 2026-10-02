# Nguồn đặc tả demo

Bộ demo dùng đúng 8 đặc tả trọng tâm trong SRS cũ ở commit `265ecc8`.
File [SRS nguồn 8 UC](SRS_8UC_nguon.docx) được lấy nguyên bản từ
`docs/main/SRS_HeThongQuanLyKTX_v3.docx` tại commit này, không chỉnh nội dung.
File có cả danh sách yêu cầu và tài liệu phân tích; phần đặc tả chi tiết có 8 UC.

| UC | Yêu cầu chính | Vai trò |
|---|---|---|
| UC-04 | REQ-001 | Sinh viên |
| UC-08 | REQ-004 | Sinh viên, cổng thanh toán |
| UC-30 | REQ-017 | Sinh viên, Admin xác nhận, CTSV |
| UC-12 | REQ-011 | Admin |
| UC-17 | REQ-033 | Kế toán |
| UC-22 | REQ-030 | Kỹ thuật |
| UC-25 | REQ-036 | Bảo vệ |
| UC-14 | REQ-012 | Admin |

SRS hiện hành ở [docs/main](../main/SRS_HeThongQuanLyKTX_v3.docx) vẫn là tài liệu
toàn hệ thống. Demo là phạm vi con; chọn 8 UC không xóa các yêu cầu còn lại.

Các bước chi tiết căn cứ vào Main Flow, Alternative Flow, Exception Flow,
Extension Points và Post-condition của từng đặc tả cũ. Các biện pháp khóa,
outbox, chống xử lý lặp là thiết kế đề xuất để bảo toàn các hậu điều kiện,
không được mô tả là chức năng đã chạy.

## Điểm cần thống nhất khi triển khai

- UC-03 được SRS cũ tham chiếu là “Gửi thông báo”, trong SRS mới lại là
  “Gửi yêu cầu sửa chữa”. Bộ demo dùng tên **Gửi thông báo (REQ-005)** tại các
  điểm include; không gán lại một mã UC mới.
- SRS cũ gọi UC-15 là áp dụng biểu giá mới; SRS mới dùng UC-15 cho vật tư.
  Sơ đồ dùng **REQ-034** để dẫn luồng áp dụng giá đã duyệt.
- UC-04: SRS giữ giường 48 giờ, SP hiện giữ 3 ngày; đích demo theo SRS là 48 giờ.
- UC-25: SRS ghi “sau 23:00”; SP hiện dùng `>= 23:00` hoặc `< 05:00`.
  Quy tắc đích chốt tại BR-12: >23:00 hoặc <05:00, Asia/Ho_Chi_Minh.
- UC-17 theo SRS cũ: **lượng tiêu thụ × đơn giá**. Không tự thêm biểu giá
  lũy tiến vào phạm vi demo vì TODO hiện có yêu cầu rộng hơn đặc tả cũ.
- SRS cũ gọi cấp giấy xác nhận và quy đổi điểm là nhánh UC-30.
  Hai nhánh này được giữ trong UC-30, không tính thành UC demo thứ 9 và 10.
- Tên SP có sẵn được lấy từ `database/01_schema.sql`; schema hiện không có
  trigger gán giường hay trigger tạo vi phạm. Các cập nhật nằm trực tiếp trong SP.

Quyết định thiết kế cuối: [quy tắc nghiệp vụ](QUY_TAC_NGHIEP_VU.md).
