# Demo 8 Use Case KTX Smart Hub

Demo chọn **đúng 8 đặc tả trọng tâm trong SRS cũ**, bao phủ 5 vai trò và
3 chuỗi nghiệp vụ: lưu trú, tài chính, vận hành/an ninh. Bộ tài liệu chốt
đích Spring Boot 4.1.1 + Thymeleaf + PostgreSQL16; chưa xác nhận ứng dụng hiện tại chạy đủ 8 UC.

Đọc [kiến trúc Spring Boot](SPRING_BOOT.md), [SRS nguồn](NGUON.md), [thiết kế xử lý 5 bước](THIET_KE_XU_LY.md),
[PostgreSQL](POSTGRESQL.md), [quy tắc](QUY_TAC_NGHIEP_VU.md),
[dữ liệu/SP](DU_LIEU_VA_SP.md), [API và quyền](API_VA_PHAN_QUYEN.md),
[Class Diagram](class-diagrams/README.md), [tích hợp](TICH_HOP.md),
[test case](KIEM_THU.md), [triển khai/nghiệm thu](TRIEN_KHAI_VA_NGHIEM_THU.md),
[truy vết](TRUY_VET.md), [8 Sequence Diagram](../sequence-diagrams/README.md) và [TODO](../../TODO.md).

## Phạm vi

| UC | Nghiệp vụ | Actor chính | REQ |
|---|---|---|---|
| UC-04 | Đăng ký phòng trực tuyến | Sinh viên | REQ-001 |
| UC-12 | Duyệt đơn đăng ký phòng | Admin | REQ-011 |
| UC-17 | Tạo hóa đơn tháng tự động | Kế toán | REQ-033 |
| UC-08 | Thanh toán hóa đơn trực tuyến | Sinh viên | REQ-004 |
| UC-22 | Tiếp nhận và xử lý sửa chữa | Kỹ thuật | REQ-030 |
| UC-25 | Ghi nhận giờ SV về KTX | Bảo vệ | REQ-036 |
| UC-14 | Xử lý vi phạm kỷ luật | Admin | REQ-012 |
| UC-30 | Đăng ký hoạt động công ích | Sinh viên | REQ-017 |

Đăng nhập/phân quyền là tiền điều kiện. Nhập chỉ số, báo hỏng, xem hóa đơn
và thông báo là nghiệp vụ hỗ trợ. Các nhánh chính sách, khách thăm, quy đổi
điểm và giấy xác nhận giữ trong sơ đồ theo SRS, không tính thành UC demo mới.

## Khoảng cách triển khai

Bảng dưới là nền legacy Java17/JSP/Servlet trên develop **0c08ae7**, chưa chuyển sang Spring Boot. Mapping class đích trong SPRING_BOOT.md.
Có nền mã/SQL không đồng nghĩa đã kiểm thử hay nghiệm thu.

| UC | Nền hiện có | Cần hoàn thiện theo SRS |
|---|---|---|
| UC-04 | StudentServlet, RoomDAO, usp_DatGiuong | Hồ sơ/ảnh mã hóa; giường trống thực; giới tính/đợt/hợp đồng; giữ 48 giờ; audit; lỗi upload/rời trang |
| UC-12 | AdminServlet, RoomDAO, usp_DuyetDangKy | Khóa khi duyệt; hạn giữ chỗ/giường khóa; người duyệt; hợp đồng dự thảo; hàng loạt ưu tiên; email/retry |
| UC-17 | AccountingServlet, InvoiceDAO, usp_PhatHanhHoaDon | Preview; giá theo kỳ; giảm giá; chi phí riêng; batch; Excel nháp; QR; gửi hóa đơn |
| UC-08 | InvoiceDAO.confirmPayment, usp_XacNhanThanhToan | Tạo PENDING; gateway/IPN sandbox; chữ ký/tiền; chống lặp; timeout/đối soát; biên lai; gộp hóa đơn |
| UC-22 | MaintenanceServlet, MaintenanceDAO, usp_HoanThanhSuaChua | Tiếp nhận/gán người; ảnh; lịch hẹn/bảo hành; kiểm tra tồn; đề xuất mua; lịch sử; thông báo/link đánh giá |
| UC-25 | SecurityServlet, SecurityDAO, usp_GhiNhanRaVao | Thẻ/QR; hợp đồng; mốc giờ/timezone; barrier; khách thăm; app/báo cáo |
| UC-14 | VI_PHAM, getViolations(), usp_XuLyKyLuat | Chưa có module xử lý Admin; minh chứng; đếm theo học kỳ; quyết định/lịch sử; nhóm; khiếu nại; rollback |
| UC-30 | Có4bảng công ích và usp_DangKyCongIch; chưa có UI/module Java | Hoạt động/chỉ tiêu; đăng ký; chống trùng lịch; QR; xác nhận/cộng điểm; hủy; CTSV; PDF chữ ký/QR |

Các điểm phải xử lý trước demo ứng dụng:

- Auth chưa có 2FA; bổ sung kiểm tra quyền server. AccountDAO có fallback
  mật khẩu mẫu khi BCrypt lỗi: cần bỏ và dùng hash hợp lệ.
- rooms.jsp sinh cố định 4 lựa chọn giường, chưa lọc theo trạng thái giường.
- StudentServlet ghi báo hỏng bằng SQL trực tiếp và ID mặc định khi session
  thiếu: đưa xuống DAO, kiểm tra lưu trú thực tế.
- Nút thu tiền mặt tạo ref CASH-... nhưng chưa tạo giao dịch tương ứng;
  usp_XacNhanThanhToan cần giao dịch tồn tại. Không dùng nút này để chứng
  minh thanh toán online đã chạy.
- Mở rộng DB bằng SQL cập nhật thủ công đã review, dùng lại SP và transaction; không bỏ qua
  nghiệp vụ SP bằng insert/update nhiều bảng trực tiếp trong DAO.

## Chuẩn bị

1. Đọc POSTGRESQL.md trước: cấu hình hiện tại còn cổng MySQL và JDBC cần sửa.
   Sau khi sửa ở giai đoạn code, chạy DB/app riêng và kiểm tra kết nối trước demo.
2. README công bố admin, sv001, ketoan1, kythuat1, baove1 với mật khẩu
   123456. Kiểm tra đăng nhập thực tế trước demo; chưa xác minh hash seed.
3. Seed sv001 → MSSV 21110001 đang ở A-102. **Không dùng ngay tài khoản
   đang lưu trú để chứng minh đăng ký mới hợp lệ.** Chuẩn bị SV chưa có
   hợp đồng/đơn hiện hữu và SV thứ hai để thử tranh chấp.
4. G-A-101-4 là giường TRONG trong seed; đọc lại trạng thái trước demo.
   Chọn kỳ chưa có hóa đơn, chỉ số hợp lệ, ví dụ điện 100→120, nước 10→12.
5. VT-BONG-DEN-18W có tồn 50 trong seed; đọc tồn hiện tại và chọn phiếu
   chưa hoàn thành để thử xuất 1 hoặc xuất vượt tồn.
6. Cần fixture riêng cho giao dịch sandbox, vi phạm CHUA_XU_LY/3 lần trong
   học kỳ, hoạt động còn 1 chỗ. Không coi seed đã có đủ những tình huống này.
7. Dùng clock điều khiển trong môi trường test/fixture cho giờ về trễ;
   không đổi giờ hệ thống hay production. Quy tắc đúng 23:00/qua nửa đêm
   đã chốt theo BR-12 trong QUY_TAC_NGHIEP_VU.md.

Script scripts/reset-db.ps1 hiện còn lệnh MySQL; phải sửa trước khi dùng reset PostgreSQL.
Bộ tài liệu này không thực hiện reset hoặc thay dữ liệu chạy thật.

## Kịch bản demo

| Bước | Thao tác | Bằng chứng thành công | Ca lỗi |
|---|---|---|---|
| 1 — UC-04 | SV mới chọn giường, tải hồ sơ, gửi đơn | CHO_DUYET; GIU_CHO; hạn 48 giờ | SV thứ hai tranh giường bị từ chối; ảnh >5MB không tạo đơn |
| 2 — UC-12 | Admin xem và duyệt hồ sơ | DA_DUYET; DANG_O; LUU_TRU/phòng SV đồng bộ | Từ chối trả giường; giường khóa không duyệt; email lỗi vẫn lưu quyết định |
| 3 — UC-17 | Preview rồi phát hành kỳ mới | Tiền lượng × giá; hóa đơn/chi tiết khớp | Thiếu số đo cảnh báo; phát hành lại không trùng |
| 4 — UC-08 | SV thanh toán qua sandbox | IPN gạch nợ một lần; biên lai | IPN lặp không cộng tiền; chữ ký sai không gạch nợ; hủy giữ nợ |
| 5 — UC-22 | Nhận phiếu, hoàn thành và xuất 1 vật tư | HOAN_THANH; tồn giảm 1; phiếu xuất | Vượt tồn không trừ kho/không hoàn thành |
| 6 — UC-25 | Bảo vệ quét/nhập MSSV | RA_VAO; vi phạm trễ liên kết đúng SV | Thẻ hết hạn không vào; mốc 23:00 và qua nửa đêm |
| 7 — UC-14 | Admin xem lịch sử, xử lý | DA_XU_LY; quyết định/người xử lý; cờ >=3 lần/kỳ | Thiếu minh chứng tạm dừng; lỗi DB giữ CHUA_XU_LY |
| 8 — UC-30 | SV đăng ký, Admin xác nhận hoàn thành | Chỗ giảm 1; QR; điểm cộng sau xác nhận | Cuối chỗ đồng thời chỉ 1 thành công; trùng lịch bị từ chối |

UC-30 kiểm tra thêm: hủy trước >=12 giờ hoàn lại đúng một chỗ; quy đổi
10 điểm công ích = 1 ĐRL, tổng tối đa 5 ĐRL/kỳ; CTSV retry không trừ điểm
lại; PDF chỉ có hoạt động hoàn thành, chữ ký số và QR kiểm chứng.
Các nhánh tích hợp phải có bằng chứng thực thi, không hiển thị thành công giả.

## Thứ tự triển khai

1. Auth/phân quyền và fixture, sau đó UC-04 → UC-12.
2. UC-17 → UC-08, ưu tiên transaction và IPN chống lặp.
3. UC-22, UC-25, rồi module mới UC-14.
4. Module mới UC-30 và các nhánh theo SRS.
5. Chạy kịch bản, lưu kết quả DB/API và ảnh theo UC; chỉ đánh dấu ứng dụng
   sẵn sàng khi có bằng chứng thực thi.

Các UC ngoài phạm vi demo giữ trong backlog toàn hệ thống.
