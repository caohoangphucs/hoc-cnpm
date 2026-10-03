# Rà soát nghiệp vụ 8 UC

Ngày đối chiếu: 03/10/2026. Đối chiếu SRS được trích trong [NGUON.md](NGUON.md), quy tắc tại [QUY_TAC_NGHIEP_VU.md](QUY_TAC_NGHIEP_VU.md), luồng tại [THIET_KE_XU_LY.md](THIET_KE_XU_LY.md), sequence diagram và service/repository Java hiện có. Đây là rà soát mã nguồn và tài liệu; các ca kiểm thử trong [KIEM_THU.md](KIEM_THU.md) vẫn `NOT_RUN` trừ khi có báo cáo chạy riêng. Mọi thao tác DB của 8 UC do service điều phối và repository thực hiện bằng SQL trực tiếp, không gọi procedure.

| UC | Đối chiếu nghiệp vụ hiện có | Còn thiếu / rủi ro trước nghiệm thu |
|---|---|---|
| UC-04 Đăng ký phòng | Giữ giường 48 giờ; khóa sinh viên/giường; kiểm tra đợt, giới tính, tình trạng giường và chủ sở hữu; lưu hồ sơ và audit. | Hồ sơ được đánh dấu hợp lệ ngay khi gửi, chưa có bước người kiểm duyệt xác minh; quản lý ưu tiên, upload/đọc tệp và kiểm tra nội dung thật chưa hoàn chỉnh; chưa có HTTP/API/UI. |
| UC-12 Duyệt đăng ký | Khóa đơn và giường; duyệt tạo lưu trú/hợp đồng trong cùng transaction; từ chối giải phóng giường. | Chưa có danh sách xử lý hàng loạt theo thứ tự ưu tiên và worker thông báo/retry; phụ thuộc trạng thái hồ sơ được kiểm chứng đúng. |
| UC-17 Tạo hóa đơn | Tính preview theo chỉ số, giá hiệu lực và giảm giá; lưu snapshot/version; phát hành phòng trong transaction riêng; kiểm tra phát hành trùng. | Batch hiện dừng khi một phòng lỗi thay vì bỏ qua dòng lỗi; DTO/service chưa cung cấp đầy đủ thêm chi phí riêng; chưa có import Excel, worker gửi hóa đơn hoặc QR; chưa có API. |
| UC-08 Thanh toán | Tạo phiên `PENDING`; kiểm tra chủ sở hữu hóa đơn qua snapshot/ngày lưu trú; callback xác minh chữ ký, tiền và tham chiếu; chống callback trùng, chuyển bất thường sang rà soát. | Cổng thanh toán chưa có adapter/IPN HTTP thật, timeout/đối soát và biên lai; VietQR chưa tích hợp. Callback muộn cần job đối soát với nhà cung cấp. |
| UC-22 Sửa chữa | Tiếp nhận/gán phiếu; khóa phiếu và vật tư; chỉ hoàn tất khi đủ tồn; ghi lịch sử/xuất kho; tạo đề nghị mua khi thiếu. | Còn tích hợp lịch hẹn/bảo hành/đánh giá, xử lý hủy và trạng thái ngoài, upload ảnh, thông báo và API. |
| UC-25 Ghi nhận ra vào | Kiểm tra giờ Việt Nam (`>23:00` hoặc `<05:00`), thẻ/lưu trú; chống event lặp; lưu sự kiện barrier; mã hóa thông tin khách. Khai báo khách `PENDING` vẫn được dùng cho check-in theo SRS không yêu cầu duyệt trước. | Chưa đối chiếu giấy tờ khách tại cổng; adapter barrier/worker, báo cáo và API còn thiếu. |
| UC-14 Kỷ luật | Đếm vi phạm theo học kỳ; xử lý theo batch nguyên tử, kiểm minh chứng/version và ghi revision khi chấp nhận khiếu nại. Đã sửa tên cột `ngayvipham` trong truy vấn chờ và dùng đúng `semesterId` khi tạo revision. | Chưa có lưu/hiển thị cảnh báo lặp cho màn hình Admin, API và giao diện xử lý. |
| UC-30 Công ích | Khóa chỗ và kiểm tra trùng lịch; hủy theo mốc 12 giờ; xác nhận cộng điểm một lần; yêu cầu quy đổi có giới hạn 5 ĐRL. Quy đổi `PENDING` cũng giữ chỗ trong hạn mức. | SRS yêu cầu tự đóng khi đủ chỗ nhưng chưa có; callback/hoàn bù quy đổi, worker CTSV và tạo/ký/xác minh PDF/QR còn thiếu; chưa có API. |

## Đánh giá chung

Luồng Java hiện thực phần lớn quy tắc cốt lõi và giữ transaction/khóa ở tầng service kết hợp repository SQL. Các lỗi nghiệp vụ nêu trong rà soát đã được sửa trong working tree: truy vấn danh sách vi phạm dùng đúng tên cột; quyết định khiếu nại dùng học kỳ của vi phạm; phiên thanh toán hóa đơn phòng được cấp quyền cho mọi người thuê trong snapshot; khai báo khách đang chờ vẫn cho phép bảo vệ ghi nhận check-in; quy đổi đang chờ được tính vào hạn mức.

Chưa thể kết luận 8 UC sẵn sàng sử dụng: Controller/API, xác thực HTTP, UI nghiệp vụ, adapter nhà cung cấp/thiết bị, worker/outbox và kiểm thử tích hợp đầy đủ còn thiếu. `mvn clean compile -DskipTests` đã chạy trước các sửa lỗi cuối trong lượt rà soát; cần chạy lại sau chỉnh sửa. Không chạy thử trên production.

## Ranh giới dữ liệu

Service Java là nơi đặt quy tắc, điều phối khóa và transaction. Repository chạy câu SQL tường minh bằng Spring JDBC. Schema hiện hành không định nghĩa function/procedure nghiệp vụ; đăng nhập và các luồng nghiệp vụ chạy bằng Java service/repository SQL trực tiếp. Xem [DU_LIEU_VA_SQL.md](DU_LIEU_VA_SQL.md).
