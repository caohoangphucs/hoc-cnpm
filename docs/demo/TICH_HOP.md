# Tích hợp và vận hành tác vụ

Môi trường demo dùng adapter mock có nhãn rõ cho CTSV/SMS/barrier nếu chưa có sandbox; thanh toán nghiệm thu bằng sandbox thật. Mock hỗ trợ success, timeout, bad signature, duplicate, failure; kết quả mock không thay bằng chứng tích hợp thật.

## Thanh toán

VNPay: adapter xây dựng URL sandbox bằng protocol 2.1.0, mã ref duy nhất và tiền VND ×100; sort/encode tham số và ký HMAC SHA512. IPN kiểm chữ ký, ref, tiền; chỉ mã phản hồi và trạng thái giao dịch đều 00 mới xác nhận thành công. ACK HTTP200 JSON dùng RspCode 00 thành công, 02 đã xử lý, 01 không thấy đơn, 04 sai tiền, 97 sai chữ ký, 99 lỗi khác; commit trước ACK. Return URL không gạch nợ. Chi tiết giao thức lấy từ [VNPay sandbox](https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.html); không dùng thuật toán/chữ ký giả tự đặt.

MoMo: sandbox `https://test-payment.momo.vn`, POST `/v2/gateway/api/create`, requestType captureWallet; adapter theo chuỗi tham số/chữ ký HMAC SHA256 trong [API chính thức](https://developers.momo.vn/v3/vi/docs/payment/api/wallet/onetime/). IPN POST JSON, kiểm chữ ký và partnerCode/orderId/amount theo dữ liệu đã lưu; resultCode=0 là thành công. ACK HTTP204 rỗng sau ghi nhận bền vững trong 15 giây theo [quy định notification](https://developers.momo.vn/v3/vi/docs/payment/api/result-handling/notification/). Payload thiếu/sai chữ ký không được ACK thành công nội bộ; ghi lỗi đã lọc dữ liệu và trả 400/401 theo adapter.

Tạo phiên thời hạn 15 phút; HTTP connect timeout 3 giây/read 10 giây. Timeout tạo phiên giữ ref để query, không tạo giao dịch khác tự động. Đối soát phiên chưa rõ kết quả mỗi 5 phút tối đa 24 giờ rồi NEEDS_REVIEW; chỉ xác nhận qua API query có xác thực của provider. Callback muộn theo BR-10; không biến return success trên trình duyệt thành thanh toán đã nhận. Lưu reference và hash payload phục vụ đối soát, không log secret. Các giá trị merchant/partner và khóa là đầu vào triển khai: VNPAY_TMN_CODE/HASH_SECRET/RETURN_URL/IPN_URL; MOMO_PARTNER_CODE/ACCESS_KEY/SECRET_KEY/REDIRECT_URL/IPN_URL. URL callback phải HTTPS truy cập được từ sandbox.

## Adapter còn lại

| Adapter | Request và xác thực | Kết quả và lỗi |
|---|---|---|
| Mail/SMS | outboxId, template, recipient, variables; SMTP TLS/API token qua secret | receiptId hoặc error; retry theo lịch, không rollback nghiệp vụ |
| CTSV | transferId UUID, studentId, semesterCode, drl integer1..5; API key/mTLS tùy hợp đồng | externalRef, ACCEPTED/PENDING/REJECTED; cùng transferId cùng kết quả; rejected tạo bút toán bù |
| Barrier | eventId, deviceId, command OPEN, expiresAt=now+10s; mTLS/chữ ký thiết bị | OPENED/FAILED/TIMEOUT; retry cùng eventId 1 lần, sau đó bảo vệ mở thủ công và lưu lý do |
| PDF | certificateId, snapshot hoàn thành, verificationURL | PDF SHA256; ký CMS bằng chứng thư được cấp; verify tính toàn vẹn, danh tính signer và hạn chứng thư |
| QR | token ngẫu nhiên >=128 bit, lưu SHA256; token khác nhau cho thẻ/hoạt động/giấy | Token bị thu hồi/hết hạn trả INVALID; không embed CCCD hoặc secret |

CTSV/barrier chưa có API nhà cung cấp trong repo: bảng là contract nội bộ bắt buộc của adapter, không tuyên bố là giao thức bên ngoài. Trong demo chưa có provider dùng mock và ghi kết quả mock vào báo cáo nghiệm thu. Chứng thư số do môi trường triển khai cung cấp; ảnh chữ ký không được coi là chữ ký số. Giấy đang tạo trả 202/status PENDING; chỉ cho tải khi ký thành công.

## Outbox và việc nền

Worker lấy batch bằng SELECT FOR UPDATE SKIP LOCKED, đánh dấu lease trong transaction ngắn; không giữ lock khi gọi mạng. Retry 1 phút, 5 phút, 15 phút, 1 giờ, 6 giờ; tối đa 5 lần rồi DEAD, Admin có thể retry cùng dedupe key sau sửa lỗi. Worker crash sau gửi trước cập nhật có thể gửi lại: downstream phải dùng idempotency key; email dùng Message-ID ổn định và chấp nhận ít nhất một lần, không hứa exactly-once.

Các job: expire hold mỗi phút; outbox mỗi 10 giây; đối soát mỗi 5 phút; dọn draft upload quá 24 giờ hàng ngày; overdue invoice hàng ngày theo giờ Việt Nam. Job nhiều instance dùng advisory lock/job lease. Shutdown ngừng nhận job, trả lease hết hạn để instance khác nhận.

Logs có requestId, actorId, UC, status, duration; không có CCCD/password/full payload gateway. Metrics: IPN rejected/duplicate, outbox DEAD, stock conflict, booking conflict, reconcile pending. Cảnh báo khi outbox DEAD>0, callback sai chữ ký tăng hoặc DB unavailable; không gửi thông báo giả khi provider chưa xác nhận.
