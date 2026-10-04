# Demo seed cho 8 UC

Seed gốc có tài khoản, phòng, hóa đơn và yêu cầu sửa chữa, nhưng không có `dang_ky_phong`/`svc_profile` nên trang duyệt phòng hiển thị 0. Production cũ còn thiếu đăng ký công ích và dữ liệu trong các bảng runtime. Migration `V20261009__complete_demo_usecases.sql` bổ sung bộ dữ liệu tổng hợp riêng, không ghi đè dữ liệu đã được người dùng xử lý.

## Case và tài khoản

Mật khẩu mặc định: `123456`. Admin/kế toán đã bật 2FA vẫn phải xác thực OTP của chính tài khoản đó.

| UC | Tài khoản / dữ liệu | Luồng demo |
|---|---|---|
| UC-04 đăng ký phòng | `sv041` nam, `sv042` nữ; giường số 4 của DEMO-A/DEMO-B trống | Tạo đơn mới. Không dùng tài khoản `demo_pending_*` đang có đơn chờ, hoặc `demo_resident_*` đang lưu trú để tạo đơn mới. |
| UC-12 duyệt phòng | `admin`; đơn 900001–900004 | 4 đơn chờ, hồ sơ VALID với 3 tham chiếu giấy tờ, giường GIU_CHO. Duyệt tạo lưu trú/hợp đồng và giường DANG_O; từ chối giải phóng giường. Có sẵn đơn đã duyệt 900005/900006, từ chối 900007 và hủy 900008. |
| UC-17 hóa đơn | `ketoan1`; DEMO-A/DEMO-B | Có đơn giá phòng, chỉ số tháng 10/2026, hóa đơn và dòng tiền phòng. Xem trước tháng đã phát hành để thử phát hiện trùng; phát hành tháng khác cần ghi chỉ số tháng đó. |
| UC-08 thanh toán | `demo_resident_m` / `demo_resident_f` | Hóa đơn 900001 còn nợ; 900002 đã thanh toán đủ và có session/allocation/biên nhận SUCCESS. Snapshot xác định chủ hóa đơn; sinh viên phòng khác không có quyền đọc. |
| UC-22 sửa chữa | `demo_resident_m`, `demo_resident_f`, `admin`, `kythuat1` | Yêu cầu 900001 mới; 900002 đang xử lý và đã phân công `kythuat1`. Seed gốc có các yêu cầu hoàn thành và vật tư. |
| UC-25 ra/vào, khách | `demo_resident_m`, `baove1` | Khách 900001 PENDING, 900002 APPROVED, 900003 REJECTED. Thời gian thăm được tính lúc nạp đầu tiên; lượt đã hết thời gian cần tạo mới. 55 lịch sử ra/vào gốc có bản ghi runtime để API trả về. Có thẻ demo cho sinh viên đang lưu trú. |
| UC-14 vi phạm | `admin`, `demo_resident_m` | Vi phạm 900001 chưa xử lý, cùng các vi phạm gốc đã/chưa xử lý. Quyết định mới cần lý do và tham chiếu bằng chứng hợp lệ; Có sẵn vi phạm 900002 đã xử lý của `demo_resident_f`, quyết định 900001 và khiếu nại 900001 OPEN để thử phản hồi. |
| UC-30 công ích | `demo_resident_m`, `demo_resident_f`, `admin` | Nam đã đăng ký ACT-01; nữ hoàn thành ACT-DEMO-DONE trong quá khứ, có 15 điểm từ hoạt động và 42 điểm tích lũy, tổng 57. Dùng luồng quy đổi và yêu cầu chứng nhận với học kỳ HK1-2026. |

## Dữ liệu tổng hợp và giới hạn

- Các phòng, sinh viên và mã `DEMO-*` tách riêng khỏi dữ liệu gốc. Không dùng thông tin cá nhân thật.
- Mỗi tham chiếu giấy tờ seed có ảnh PNG tổng hợp, ghi rõ DỮ LIỆU DEMO. Nội dung được mã hóa và lưu trong PostgreSQL; chủ hồ sơ và admin được phép xem/tải ảnh qua API riêng tư.
- Thanh toán SUCCESS là lịch sử tổng hợp có tiền tố DEMO, không có giao dịch với ngân hàng. Tạo thanh toán mới/callback thật cần adapter provider đã cấu hình. Seed không giả lập cấu hình provider.
- Khách dùng ciphertext AES-GCM của dữ liệu giả với khóa fixture riêng; API hiện chỉ trả trạng thái/thời hạn, không trả thông tin định danh. Không thay khóa mã hóa của server.
- Thẻ `DEMO-CARD-NAM-2026` chỉ dành cho demo. Ghi sự kiện mới vẫn cần cấu hình adapter barrier; không suy ra cổng vật lý đã mở từ dữ liệu seed.
- Quy đổi điểm và ký chứng nhận vẫn cần adapter tương ứng. Không tạo chứng nhận giả đã ký.
- Đợt demo mở từ 01/09/2026 đến 15/01/2027; đơn chờ giữ chỗ 30 ngày từ lần nạp đầu tiên. Seed không tự gia hạn đơn đã hết hạn.

## Nạp và kiểm thử

Volume PostgreSQL mới chạy `01_schema.sql`, `02_seed.sql`, `04_demo_usecases.sql`, `05_evidence_file_content.sql`. Backend bổ sung nội dung ảnh seed khi khởi động, giữ nguyên ID tệp và trạng thái đơn. Reset local có cờ xác nhận qua `scripts/reset-db.sh --reset-demo`. Deploy database hiện hữu chỉ áp dụng migration bổ sung trong transaction; không chạy lại schema/seed gốc trên production.

CI chạy các test nghiệp vụ/API gốc trước, sau đó kiểm tra migration chạy hai lần không tạo trùng hay đổi mật khẩu/quyết định người dùng. `DemoSeedApiIT` dùng Java HttpClient gọi server thật với PostgreSQL tạm: danh sách hồ sơ, duyệt/từ chối, quyền admin, version sai, thiếu lý do, quyền hóa đơn, sửa chữa, thanh toán, khách, ra/vào, vi phạm và điểm công ích. Các đơn bị thao tác trong test được khôi phục sau mỗi case. Đây là kiểm tra dữ liệu và endpoint, không phải nghiệm thu giao dịch provider hoặc thiết bị vật lý.

## Kiểm tra đăng ký và minh chứng trên giao diện

1. Đăng nhập `sv041`/`123456`, mở Phòng & lưu trú, chọn giường trống phù hợp giới tính bằng nút **Chọn đăng ký**.
2. Form hiển thị đợt đang mở. Tải ảnh CCCD mặt trước, mặt sau và thẻ sinh viên; PNG/JPEG, tối đa 5 MiB mỗi ảnh. Có thể dùng ảnh mẫu trong `server/src/main/resources/demo-evidence/` trên môi trường demo.
3. Kiểm tra ảnh xem trước, đánh dấu đồng ý nội quy và gửi hồ sơ. Thành công chuyển sang Hồ sơ của tôi, đơn CHO_DUYET và giường GIU_CHO.
4. Admin đăng nhập, xác thực OTP nếu đã bật, mở chi tiết đơn: ba ảnh xuất hiện, có thể mở lớn hoặc tải ảnh. Đối chiếu, chọn duyệt/từ chối và lưu.
5. Tài khoản sinh viên khác không xem được hồ sơ/tệp này; API trả 404. Admin không đọc được tệp nháp chưa gắn vào hồ sơ.

Nội dung mã hóa nằm trong `svc_file_content`, metadata trong `svc_file`. Khóa `FILE_ENCRYPTION_KEY` mặc định lấy từ `TOTP_ENCRYPTION_KEY`; giữ khóa ổn định giữa các deployment để đọc lại ảnh. Không thay khóa mà không có kế hoạch chuyển mã dữ liệu. Kiểm tra ảnh gồm MIME thực, giải mã raster, giới hạn kích thước và ghi lại ảnh để bỏ metadata; không phải tích hợp dịch vụ antivirus bên ngoài.

## UC-17: tháng chưa phát hành

Migration V20261011 bổ sung chỉ số tháng **11/2026** cho các phòng hiện có, không ghi đè số đo hoặc hóa đơn đã được người dùng tạo. Kế toán chọn tháng 11/2026, tích DEMO-A/DEMO-B và tính bản nháp để thử phát hành. D-102 cố ý thiếu số đo: mở Nhập / sửa chỉ số, điền bốn số thực tế rồi Lưu & tính lại. Các tháng ngoài seed cần nhập số đo trước phát hành. Reset local nạp thêm 06_invoice_meter_demo.sql. Deploy tiếp theo không xóa hóa đơn vừa phát hành.
