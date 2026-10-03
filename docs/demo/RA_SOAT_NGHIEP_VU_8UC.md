# Rà soát nghiệp vụ 8 UC

Ngày đối chiếu: 03/10/2026. Đối chiếu SRS được trích trong [NGUON.md](NGUON.md), quy tắc tại [QUY_TAC_NGHIEP_VU.md](QUY_TAC_NGHIEP_VU.md), luồng tại [THIET_KE_XU_LY.md](THIET_KE_XU_LY.md), sequence diagram và service/repository Java hiện có. Đây là rà soát mã nguồn và tài liệu; các ca kiểm thử trong [KIEM_THU.md](KIEM_THU.md) vẫn `NOT_RUN` trừ khi có báo cáo chạy riêng. Mọi thao tác DB của 8 UC do service điều phối và repository thực hiện bằng SQL trực tiếp, không gọi procedure.

| UC | Đối chiếu nghiệp vụ hiện có | Còn thiếu / rủi ro trước nghiệm thu |
|---|---|---|
| UC-04 Đăng ký phòng | Tầng service/repository xử lý giữ giường 48 giờ, khóa, kiểm tra đợt/giới tính/tình trạng/chủ sở hữu, lưu đăng ký và audit. Đã có route tạo đăng ký. | Chưa có quy trình nhân viên xác minh hồ sơ, hàng đợi ưu tiên, tải/đọc tệp và kiểm nội dung; chưa có UI. |
| UC-12 Duyệt đăng ký | Tầng service/repository khóa đơn/giường; duyệt tạo lưu trú/hợp đồng cùng transaction, từ chối giải phóng giường. Route quyết định đăng ký đã nối tới service. | Chưa có danh sách ưu tiên/batch và worker thông báo/retry; việc xác minh hồ sơ còn thiếu. |
| UC-17 Tạo hóa đơn | Preview theo chỉ số, giá hiệu lực và giảm giá; lưu snapshot/version; phát hành phòng trong transaction riêng; kiểm tra trùng. Route preview/phát hành đã được khai báo. | Batch dừng khi một phòng lỗi; thiếu một số chi phí riêng, import Excel, worker gửi hóa đơn và QR; chưa có UI. |
| UC-08 Thanh toán | Tạo phiên `PENDING`, kiểm tra quyền hóa đơn, xác minh callback chữ ký/tiền/tham chiếu, chống callback trùng và đưa bất thường sang rà soát. Đã khai báo route tạo phiên/trạng thái/callback. | Chưa cấu hình adapter nhà cung cấp nên tạo phiên trả `PAYMENT_GATEWAY_UNAVAILABLE`; thiếu timeout/đối soát, biên lai và VietQR. Callback thật chỉ hoạt động khi cấu hình adapter. |
| UC-22 Sửa chữa | Tiếp nhận/gán phiếu, khóa phiếu/vật tư, kiểm kho khi hoàn tất, ghi lịch sử/xuất kho và đề nghị mua bù. Đã nối route tạo/liệt kê/gán/đổi lịch/hoàn tất. | Thiếu hủy/trạng thái bổ sung, upload ảnh, thông báo, tích hợp lịch hẹn/bảo hành/đánh giá và UI. |
| UC-25 Ghi nhận ra vào | Kiểm tra giờ Việt Nam (`>23:00` hoặc `<05:00`), thẻ/lưu trú, chống event lặp, ghi sự kiện và mã hóa thông tin khách. Đã nối route ghi nhận ra/vào, khai báo/duyệt khách và check-in/out. | Chưa đối chiếu giấy tờ tại cổng; thiếu adapter barrier, worker/báo cáo và UI. Khai báo khách `PENDING` vẫn được dùng check-in theo SRS. |
| UC-14 Kỷ luật | Đếm vi phạm theo học kỳ; xử lý batch nguyên tử; kiểm minh chứng/version; ghi revision khi chấp nhận khiếu nại. Đã nối route hàng chờ, quyết định, khiếu nại và phản hồi. | Chưa lưu/hiển thị cảnh báo lặp cho Admin; chưa có UI. |
| UC-30 Công ích | Khóa chỗ, kiểm tra trùng lịch, hủy theo mốc 12 giờ, xác nhận điểm một lần, giới hạn quy đổi 5 ĐRL. Đã nối route hoạt động, đăng ký/hủy, điểm danh, xác nhận, điểm, quy đổi và chứng nhận. | Thiếu tự đóng khi đủ chỗ, callback/hoàn bù quy đổi, worker CTSV, PDF/QR ký-xác minh và UI. |

## Đánh giá chung

Luồng Java hiện thực phần lớn quy tắc cốt lõi và giữ transaction/khóa ở tầng service kết hợp repository SQL. Các lỗi nghiệp vụ nêu trong rà soát đã được sửa trong working tree: truy vấn danh sách vi phạm dùng đúng tên cột; quyết định khiếu nại dùng học kỳ của vi phạm; phiên thanh toán hóa đơn phòng được cấp quyền cho mọi người thuê trong snapshot; khai báo khách đang chờ vẫn cho phép bảo vệ ghi nhận check-in; quy đổi đang chờ được tính vào hạn mức.

Ba tầng backend Java đã có cho các luồng cốt lõi của cả 8 UC: API route/controller, service nghiệp vụ và persistence/repository SQL. Trong lượt cập nhật này đã khai báo các endpoint còn thiếu để gọi những thao tác service đã có. Việc này chưa đồng nghĩa 8 UC hoàn chỉnh để nghiệm thu: các thiếu sót ở cột cuối, UI, adapter ngoài, worker và một số nhánh nghiệp vụ vẫn cần triển khai. Kiểm thử API black-box hiện xác minh một số luồng đại diện; `ApiRoutesTest` kiểm tra khai báo route, không giả lập thay cho kiểm thử HTTP. Chỉ kết luận test nào đã chạy dựa trên báo cáo build/CI tương ứng.

Đăng nhập hiện tạm thời không yêu cầu 2FA cho mọi role; endpoint cấu hình authenticator vẫn được giữ lại nhưng không chặn truy cập nghiệp vụ. Đây là thay đổi cấu hình ứng dụng, không xóa bí mật TOTP đã lưu trong dữ liệu.

## Ranh giới dữ liệu

Service Java là nơi đặt quy tắc, điều phối khóa và transaction. Repository chạy câu SQL tường minh bằng Spring JDBC. Schema hiện hành không định nghĩa function/procedure nghiệp vụ; đăng nhập và các luồng nghiệp vụ chạy bằng Java service/repository SQL trực tiếp. Xem [DU_LIEU_VA_SQL.md](DU_LIEU_VA_SQL.md).
