# Thiết kế xử lý cho 8 UC demo

Áp dụng 5 bước theo TODO cho từng đặc tả [SRS cũ](NGUON.md). Đây là thiết kế
đích; [kế hoạch demo](README.md) phân biệt rõ code hiện có và phần đề xuất.
Class Diagram đích: [thiết kế lớp demo](class-diagrams/README.md).
Quy tắc chi tiết và ranh giới SQL/repository tại [quy tắc](QUY_TAC_NGHIEP_VU.md) và [dữ liệu](DU_LIEU_VA_SQL.md).

Các luồng bên dưới là thiết kế theo SRS; mức hiện thực được đối chiếu trong [báo cáo rà soát 8 UC](RA_SOAT_NGHIEP_VU_8UC.md). Tầng Java service/repository cho 8 UC hiện đã có. Service điều phối transaction và quy tắc; repository dùng SQL qua JdbcTemplate. Controller/API, xác thực HTTP, giao diện nghiệp vụ và adapter/job ngoài hệ thống chưa hoàn tất. Các nhãn procedure trong sequence lịch sử chỉ là tên bước nghiệp vụ; sequence hiện hành và service mới dùng SQL trực tiếp.

## UC-04 Đăng ký phòng

**Bước 1 — Sự kiện UI:** onLoad phòng; onChange tòa/loại; onClick giường; onUpload; onSubmit; onBeforeLeave.

**Bước 2 — Dữ liệu vào/ra và kiểm tra:** bedId, hồ sơ/ảnh CCCD và thẻ SV, cam kết, minh chứng tùy chọn; SV từ session. Ra mã đơn, CHO_DUYET, hạn 48 giờ; lỗi theo trường.

**Bước 3 — Mã giả:**

```text
kiểm tra quyền SV, đợt mở, hợp đồng, giới tính, cam kết
kiểm tra JPG/PNG <=5MB; mã hóa dữ liệu nhạy cảm
BookingService kiểm tra và ghi đơn/hồ sơ/trạng thái giường bằng repository SQL trong một transaction
status != SUCCESS: rollback hồ sơ mới; trả lỗi; làm mới giường
SUCCESS: ghi audit; commit; trả mã đơn và hạn 48 giờ theo SRS
rời trang trước submit: cảnh báo; không tạo giữ chỗ từ bản nháp
```

**Bước 4 — Sequence Diagram:** [PlantUML](../sequence-diagrams/demo/UC-04_dang-ky-phong.puml)
· [SVG](../sequence-diagrams/demo/UC-04_dang-ky-phong.svg).

**Bước 5 — Lớp tham gia và phần bổ sung:** BookingService và BookingRepository đã có; BookingController/API, tra cứu sơ đồ phòng, xem hồ sơ/ảnh, xác minh nội dung hồ sơ và xử lý ưu tiên còn thiếu. Giữ chỗ 48 giờ và ghi audit đã có trong service.

## UC-12 Duyệt đăng ký

**Bước 1 — Sự kiện UI:** onLoad hồ sơ; onChange bộ lọc; onClick chi tiết/duyệt/từ chối; onConfirm; duyệt hàng loạt.

**Bước 2 — Dữ liệu vào/ra và kiểm tra:** registrationId, quyết định/lý do; Admin từ session. Ra quyết định, trạng thái giường/phòng, hợp đồng và thông báo.

**Bước 3 — Mã giả:**

```text
kiểm tra Admin + 2FA; nạp hồ sơ và minh chứng
từng đơn: khóa đơn/giường/phòng; kiểm tra CHO_DUYET, hạn, giường không khóa
RegistrationService lưu quyết định bằng repository SQL
duyệt: service tạo lưu trú/hợp đồng; cập nhật SV, giường và sức chứa phòng
từ chối: service trả giường về TRONG
ghi người duyệt, audit, outbox; commit
gửi email sau commit; lỗi kênh gửi thì retry, không đảo quyết định
```

**Bước 4 — Sequence Diagram:** [PlantUML](../sequence-diagrams/demo/UC-12_duyet-dang-ky.puml)
· [SVG](../sequence-diagrams/demo/UC-12_duyet-dang-ky.svg).

**Bước 5 — Lớp tham gia và phần bổ sung:** RegistrationService/BookingRepository và ghi outbox đã có. Controller/API, danh sách hồ sơ, kiểm duyệt nội dung hồ sơ, duyệt hàng loạt theo ưu tiên và worker email/SMS còn thiếu.

## UC-17 Tạo hóa đơn tháng

**Bước 1 — Sự kiện UI:** onChange kỳ/phòng; onClick preview/Excel nháp; onChange chi phí; onConfirm phát hành.

**Bước 2 — Dữ liệu vào/ra và kiểm tra:** Kỳ, tập phòng, phiên bản draft, chi phí có căn cứ. Ra chỉ số/giá/giảm/tổng; phòng thiếu số đo, trùng kỳ và kết quả từng phòng.

**Bước 3 — Mã giả:**

```text
nạp chỉ số và giá có hiệu lực theo kỳ; kiểm tra hóa đơn hiện hữu
cảnh báo phòng thiếu số đo; cho chọn bỏ qua theo SRS
tính (mới-cũ)*đơn giá; cộng tiền phòng/dịch vụ; giảm theo hồ sơ đã duyệt
preview và Excel nháp; nhận chi phí có ghi chú
khi phát hành: nạp lại dữ liệu/kiểm tra phiên bản
từng phòng: InvoiceService khóa phòng, đối chiếu snapshot và ghi hóa đơn/chi tiết bằng repository SQL
ghi QR/audit/outbox; commit hoặc rollback phòng lỗi
gửi hóa đơn; trả thành công/bỏ qua/lỗi
```

**Bước 4 — Sequence Diagram:** [PlantUML](../sequence-diagrams/demo/UC-17_tao-hoa-don.puml)
· [SVG](../sequence-diagrams/demo/UC-17_tao-hoa-don.svg).

**Bước 5 — Lớp tham gia và phần bổ sung:** InvoiceService/InvoiceRepository, giá theo kỳ, giảm giá, snapshot và chi phí riêng đã có. Controller/API, nhập chi phí riêng, chọn bỏ qua phòng lỗi, Excel, QR và worker gửi hóa đơn còn thiếu. Phát hành hiện độc lập theo phòng nhưng lỗi ở một phòng làm dừng batch.

## UC-08 Thanh toán online

**Bước 1 — Sự kiện UI:** onLoad hóa đơn; onSelect một/nhiều; onChange phương thức; onClick trả tiền/hủy/thử lại; onPoll; IPN; job timeout.

**Bước 2 — Dữ liệu vào/ra và kiểm tra:** Client gửi IDs/method/khóa request; số tiền và quyền từ DB. Cổng gửi ref, amount, status, signature. Ra URL/QR hạn 15 phút, kết quả đối soát, biên lai.

**Bước 3 — Mã giả:**

```text
đọc hóa đơn thuộc SV; tính dư nợ từ DB
tạo PENDING duy nhất; gộp cần bảng phân bổ hóa đơn
tạo URL/QR theo hợp đồng cổng; không lưu thẻ trong app KTX
IPN: xác minh chữ ký, ref, số tiền và kết quả
transaction: khóa giao dịch/hóa đơn; đọc lại trạng thái
đã SUCCESS: trả kết quả cũ, không cộng DaThanhToan lần hai
thành công mới: PaymentService đối soát dư nợ; repository cập nhật thanh toán/hóa đơn và outbox trong transaction
thất bại: giữ dư nợ; ghi kết quả; cho thử lại
ACK đúng giao thức; biên lai sau commit
quá 15 phút: hết hạn phiên; IPN muộn vẫn phải đối soát tiền thực thu
```

**Bước 4 — Sequence Diagram:** [PlantUML](../sequence-diagrams/demo/UC-08_thanh-toan-truc-tuyen.puml)
· [SVG](../sequence-diagrams/demo/UC-08_thanh-toan-truc-tuyen.svg).

**Bước 5 — Lớp tham gia và phần bổ sung:** PaymentService/PaymentRepository, phiên PENDING, phân bổ gộp và chống callback lặp đã có. Controller/IPN, adapter provider thật, đối soát callback muộn, ACK theo giao thức và biên lai/worker còn thiếu; return URL không tự gạch nợ.

## UC-22 Xử lý sửa chữa

**Bước 1 — Sự kiện UI:** onLoad phiếu/vật tư; onClick tiếp nhận; onSubmit hẹn lại/bảo hành; onChange vật tư; onSubmit hoàn thành.

**Bước 2 — Dữ liệu vào/ra và kiểm tra:** requestId, supplyId, quantity, note; kỹ thuật từ session. Ra người phụ trách/trạng thái, tồn, lịch sử, thông báo và link đánh giá.

**Bước 3 — Mã giả:**

```text
tiếp nhận có điều kiện MOI->DANG_XU_LY; gán kỹ thuật; chống nhận đồng thời
phòng khóa: lưu lịch hẹn; giữ DANG_XU_LY; gửi hẹn lại
sửa điện/ngắt cầu dao; sửa nước/khóa van; bảo hành ngoài lưu ngày dự kiến
hoàn thành: kiểm tra người phụ trách, trạng thái và số lượng
RepairService khóa phiếu/vật tư; repository xuất kho và cập nhật phiếu trong transaction
thiếu tồn: không trừ kho/không hoàn thành; tạo đề xuất mua
thành công: lịch sử/audit/outbox; commit; gửi thông báo và link đánh giá
```

**Bước 4 — Sequence Diagram:** [PlantUML](../sequence-diagrams/demo/UC-22_xu-ly-sua-chua.puml)
· [SVG](../sequence-diagrams/demo/UC-22_xu-ly-sua-chua.svg).

**Bước 5 — Lớp tham gia và phần bổ sung:** RepairService/RepairRepository, giao phiếu, lịch hẹn, xuất kho, đề nghị mua bù và lịch sử đã có. Controller/API, hủy phiếu, luồng bảo hành ngoài, upload ảnh và worker thông báo/link đánh giá còn thiếu.

## UC-25 Ghi nhận về KTX

**Bước 1 — Sự kiện UI:** onScan thẻ/QR; onSubmit MSSV thủ công; onLookup khách; onClick xác nhận khách.

**Bước 2 — Dữ liệu vào/ra và kiểm tra:** MSSV/type/note hoặc CCCD khách; bảo vệ từ session, giờ server theo timezone KTX. Ra hồ sơ SV, nhật ký, cảnh báo và quyết định barrier.

**Bước 3 — Mã giả:**

```text
khách: tìm khai báo còn hiệu lực; thiếu thì liên hệ SV/khai báo
SV: tra thẻ/hợp đồng; hết hạn thì báo đỏ và không mở cổng
GateService ghi RA_VAO, VI_PHAM khi về trễ và trạng thái barrier chờ xử lý
outbox cảnh báo; commit; barrier chỉ mở sau ghi thành công
gửi cảnh báo SV; vi phạm xuất hiện trong báo cáo Admin
kiểm tra đúng 23:00, sau 23:00, qua nửa đêm theo quy tắc được chốt
```

**Bước 4 — Sequence Diagram:** [PlantUML](../sequence-diagrams/demo/UC-25_ghi-nhan-ra-vao.puml)
· [SVG](../sequence-diagrams/demo/UC-25_ghi-nhan-ra-vao.svg).

**Bước 5 — Lớp tham gia và phần bổ sung:** GateService/GateRepository đã có ghi nhận thẻ, vi phạm, khách mã hóa và check-in/out. Controller, đối chiếu CCCD khách tại cổng, phân quyền/duyệt khách đúng SRS và adapter barrier/outbox worker còn thiếu. Lượt khách được lưu riêng, không dùng RA_VAO bắt buộc MSSV.

## UC-14 Xử lý kỷ luật

**Bước 1 — Sự kiện UI:** onLoad vi phạm; onClick chi tiết; onSelect nhóm; onSubmit quyết định/khiếu nại/xét lại.

**Bước 2 — Dữ liệu vào/ra và kiểm tra:** violationIds, decision, reason/minh chứng; Admin từ session. Ra lịch sử, số lần trong kỳ, cờ >=3, quyết định/người xử lý/trạng thái.

**Bước 3 — Mã giả:**

```text
kiểm tra Admin; nạp minh chứng và số lần trong học kỳ
thiếu minh chứng: tạm dừng; yêu cầu bổ sung
>=3 lần: hiển thị đề xuất; Admin ra quyết định
từng vi phạm: khóa; kiểm tra chưa xử lý và phiên bản
lưu quyết định/người xử lý/lịch sử/audit; đổi DA_XU_LY; commit
lỗi DB: rollback, giữ CHUA_XU_LY
khiếu nại: chỉ đúng SV sở hữu; lưu minh chứng và lịch sử
Admin giữ nguyên hoặc tạo phiên bản quyết định mới; không xóa lịch sử
```

**Bước 4 — Sequence Diagram:** [PlantUML](../sequence-diagrams/demo/UC-14_xu-ly-ky-luat.puml)
· [SVG](../sequence-diagrams/demo/UC-14_xu-ly-ky-luat.svg).

**Bước 5 — Lớp tham gia và phần bổ sung:** DisciplineService/DisciplineRepository, minh chứng, quyết định theo revision, đếm theo học kỳ, batch và khiếu nại đã có. Controller/API, báo cáo cảnh báo bền vững và xử lý adapter thông báo còn thiếu.

## UC-30 Đăng ký công ích

**Bước 1 — Sự kiện UI:** onLoad hoạt động; onClick đăng ký/hủy; Admin onConfirm hoàn thành; onClick quy đổi/PDF.

**Bước 2 — Dữ liệu vào/ra và kiểm tra:** activityId/registrationId/học kỳ; SV/Admin từ session. Ra đăng ký/QR, điểm sau xác nhận, quy đổi và giấy ký số/QR. Hết chỗ/trùng lịch không ghi danh.

**Bước 3 — Mã giả:**

```text
khóa chỉ tiêu và đăng ký/lịch SV; không trùng; còn chỗ mới ghi danh
lưu đăng ký duy nhất, giảm chỗ, QR và outbox; commit
hủy trước >=12 giờ: đánh dấu hủy và hoàn đúng một chỗ
Admin xác nhận hoàn thành: cộng điểm một lần; không cộng lúc đăng ký
SV quy đổi: khóa số dư và tổng ĐRL đã nhận kỳ này
n = min(floor(số dư/10), 5-ĐRL đã quy đổi kỳ này)
n>0: trừ 10*n, cộng n ĐRL; outbox CTSV; commit
n=0: thiếu điểm/đạt trần; bảo lưu số dư
CTSV lỗi: retry cùng khóa, không trừ điểm lại
PDF: chỉ hoạt động hoàn thành; chữ ký số BQL và QR kiểm chứng
```

**Bước 4 — Sequence Diagram:** [PlantUML](../sequence-diagrams/demo/UC-30_dang-ky-cong-ich.puml)
· [SVG](../sequence-diagrams/demo/UC-30_dang-ky-cong-ich.svg).

**Bước 5 — Lớp tham gia và phần bổ sung:** CommunityService/CommunityRepository, hoạt động, đăng ký, hủy, điểm danh/xác nhận, sổ điểm, yêu cầu quy đổi và yêu cầu chứng nhận đã có. Controller/API, worker CTSV và bù điểm, ký PDF/QR, tự đóng hoạt động hết chỗ và adapter thông báo còn thiếu.
