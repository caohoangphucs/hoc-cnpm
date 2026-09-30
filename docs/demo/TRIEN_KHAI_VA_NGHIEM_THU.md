# Kế hoạch triển khai và nghiệm thu

Bộ tài liệu áp dụng cho 8 UC trong NGUON.md. Tất cả class/API/bảng bổ sung là thiết kế đích; hiện tại chỉ chỉnh docs và AGENTS mô tả PostgreSQL. Không dùng checklist tài liệu để đánh dấu code đã hoàn thành.

## Thứ tự thực hiện

| Đợt | Sản phẩm code phải có | Điều kiện qua đợt |
|---|---|---|
| 0 | Sửa các sai lệch PostgreSQL trong POSTGRESQL.md; auth/2FA/RBAC/CSRF; fixture riêng; quản lý secret | Build Java17; schema/seed/migration nạp PG16; kiểm function và 7 procedure qua JDBC; login sai/hash lỗi bị chặn |
| 1 | Dữ liệu học kỳ/đợt/hồ sơ/hợp đồng; UC04/12 | Tranh chấp một giường, hết hạn48h, duyệt/từ chối rollback; email lỗi vẫn giữ quyết định |
| 2 | Giá/chính sách/snapshot/draft; UC17/08 | Hóa đơn khớp tiền; IPN sandbox thật, chữ ký sai/lặp/tiền sai không gạch sai; gộp và đối soát |
| 3 | Phân công/lịch sử/sửa chữa; thẻ/log/khách/barrier; UC22/25 | Không âm kho; hoàn thành lặp không xuất lại; mốc giờ Việt Nam; mock barrier được gắn nhãn |
| 4 | Quyết định/khiếu nại; UC14 | Ngưỡng học kỳ, quyền Admin, quyết định nhóm nguyên tử và lịch sử bất biến |
| 5 | Công ích/điểm/CTSV/PDF; UC30 | Cuối chỗ/trùng lịch, điểm cộng một lần, cap5ĐRL, ký/verify/thu hồi giấy |

Service trong Class Diagram chỉ điều phối validation/adapter/transaction; nghiệp vụ khóa và cập nhật nhiều bảng nằm trong SP. DAO cùng module nhận Connection chung khi cần. Không tự thêm ORM persistence song song JDBC cho cùng transaction.

## Migration và rollback

1. Backup dữ liệu và xác nhận phục hồi trên DB riêng; ghi schema version/checksum.
2. Chạy preflight duplicate active residency/registration, tiền âm/trạng thái sai, liên kết orphan; sửa bằng kế hoạch dữ liệu có log, không xóa âm thầm.
3. Tạo bảng/cột nullable trước, backfill có giới hạn batch; kiểm count và FK, rồi thêm NOT NULL/CHECK/unique/index. Timestamp cũ chuyển bằng AT TIME ZONE 'Asia/Ho_Chi_Minh', không giả là UTC.
4. Tạo SP mới/CREATE OR REPLACE giữ signature; triển khai Java tương thích cột cũ/mới trước khi bật tính năng.
5. Chạy test, bật từng module. Rollback app trước; migration down không drop chứng từ mới có dữ liệu. Khôi phục SP version trước nếu tương thích; lỗi dữ liệu dùng migration sửa tiến, không replay seed.

Đây là kế hoạch tài liệu; migration SQL thật sẽ được viết ở giai đoạn code. Không chạy 01_schema trên DB có dữ liệu cần giữ.

## Bằng chứng nghiệm thu

Mỗi test có mã UC/BR, fixture, requestId, request/response đã che dữ liệu, truy vấn hậu điều kiện, ảnh UI và trạng thái PASS/FAIL/BLOCKED. Mặc định NOT_RUN. Lưu kết quả trong docs/demo/evidence/YYYY-MM-DD khi thực hiện; không thêm secret hoặc CCCD thật.

- 8 UC main flow và các ca lỗi trong KIEM_THU.md đạt; mọi BR có ít nhất một test. Không có lỗi mất dữ liệu/gạch nợ sai/âm kho/chiếm giường kép/quyền vượt vai trò.
- PG16 nạp schema/migration sạch; chạy lại migration được quy định idempotent không đổi dữ liệu; nâng cấp từ baseline có seed và preflight.
- API đáp ứng contract và schema; response phải khớp schema typed trong OpenAPI. Kiểm 401/403/404 ownership, CSRF/2FA và timeout15min.
- Transaction cạnh tranh với 20 yêu cầu đồng thời cho giường/chỗ cuối: đúng một thành công; callback lặp20 lần tiền chỉ tăng một lần. Deadlock trả lỗi/retry hữu hạn, không treo.
- Với dataset demo (100 SV,100 phòng,1000 log), 20 concurrent user, p95 đọc <=2s, ghi nội bộ <=3s, lỗi server <1%; tính riêng thời gian redirect/provider. Ghi cấu hình máy và kết quả, không tuyên bố mức này đã đo.
- PDF xác minh chữ ký số, QR valid/revoked; CTSV/barrier/SMS mock ghi rõ trong báo cáo. Thiếu credential sandbox thật làm hạng mục tích hợp BLOCKED, không biến thành PASS.
- Phục hồi backup thành công, thời gian mục tiêu <=30phút cho dataset demo; đối chiếu count/số tiền/ledger. Log không lộ secret/CCCD/hash.

## Trạng thái giao nộp

Tài liệu là đầu vào triển khai; ứng dụng chỉ sẵn sàng demo khi các điều kiện trên có bằng chứng. SRS lịch sử giữ nguyên. Các UC ngoài 8 UC vẫn nằm backlog. Bộ docs không tạo tài khoản sandbox, chứng thư ký số hoặc tích hợp nhà cung cấp thay nhóm triển khai.
