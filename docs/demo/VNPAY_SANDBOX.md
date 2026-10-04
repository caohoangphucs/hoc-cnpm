# Hướng dẫn & Danh sách Thẻ Test VNPAY Sandbox

Tài liệu cung cấp thông tin kết nối và danh mục thẻ kiểm thử (Test Cards) trên môi trường **VNPAY Sandbox** phục vụ tích hợp và kiểm thử cổng thanh toán (UC-08 Thanh toán hóa đơn).

---

## 1. Thông tin cấu hình môi trường Sandbox

| Tham số | Giá trị cấu hình | Ghi chú |
| :--- | :--- | :--- |
| **Terminal ID / Website Code (`vnp_TmnCode`)** | `TEF5T7KT` | Mã định danh Merchant test |
| **Secret Key (`vnp_HashSecret`)** | `KESCJUEFTXGJAJLRCUHXBHEIUAGWHHLY` | Khóa bí mật tạo checksum HMAC-SHA512 |
| **Payment Gateway URL (`vnp_Url`)** | `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html` | Cổng chuyển hướng thanh toán |
| **Merchant Admin URL** | [https://sandbox.vnpayment.vn/merchantv2/](https://sandbox.vnpayment.vn/merchantv2/) | Quản lý & tra cứu giao dịch test |
| **Kịch bản test SIT & IPN** | [https://sandbox.vnpayment.vn/vnpaygw-sit-testing/user/login](https://sandbox.vnpayment.vn/vnpaygw-sit-testing/user/login) | Giả lập callback và kịch bản lỗi |
| **Tài liệu API chính thức** | [VNPAY Payment API Docs](https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.html) | Đặc tả tham số và mã lỗi |

---

## 2. Danh sách Thẻ Test ATM Nội địa (Ngân hàng NCB)

* **Tên chủ thẻ:** `NGUYEN VAN A`
* **Ngày phát hành:** `07/15`
* **Mật khẩu OTP:** `123456`

| Trường hợp kiểm thử (Test Case) | Số thẻ test | Mã kết quả mong đợi |
| :--- | :--- | :--- |
| **Giao dịch thành công** | `9704198526191432198` | `00` (Success) |
| **Tài khoản không đủ số dư** | `9704195798459170488` | `51` (Insufficient funds) |
| **Thẻ chưa kích hoạt / chưa đăng ký Internet Banking** | `9704192181368742` | `07` (Suspected fraud / inactive) |
| **Thẻ bị khóa** | `9704193370791314` | `09` (Card blocked) |
| **Thẻ hết hạn sử dụng** | `9704194841945513` | `06` (Expired card) |

---

## 3. Danh sách Thẻ Test Quốc tế (Visa / MasterCard / JCB)

* **Tên chủ thẻ:** `NGUYEN VAN A`
* **Ngày hết hạn:** `12/26` (hoặc tháng/năm bất kỳ trong tương lai)
* **Mã bảo mật CVV/CVC:** `123`
* **Mật khẩu OTP (nếu có 3D-Secure):** `123456`
* **Địa chỉ / Thành phố test:** `22 Lang Ha` / `Ha Noi`

| Loại thẻ | Loại xác thực | Số thẻ test | Trạng thái |
| :--- | :--- | :--- | :--- |
| **VISA** | No 3DS (Thanh toán trực tiếp) | `4456530000001005` | Thành công |
| **VISA** | 3DS (Có bước nhập OTP) | `4456530000001096` | Thành công |
| **MasterCard** | No 3DS (Thanh toán trực tiếp) | `5200000000001005` | Thành công |
| **MasterCard** | 3DS (Có bước nhập OTP) | `5200000000001096` | Thành công |
| **JCB** | No 3DS (Thanh toán trực tiếp) | `3337000000000008` | Thành công |

---

## 4. Bảng mã phản hồi thường gặp (`vnp_ResponseCode`)

| Mã (`vnp_ResponseCode`) | Ý nghĩa | Xử lý hệ thống |
| :--- | :--- | :--- |
| `00` | Giao dịch thành công | Gạch nợ hóa đơn, cập nhật trạng thái `PAID` |
| `07` | Trừ tiền thành công, giao dịch bị nghi ngờ | Đưa vào trạng thái `NEEDS_REVIEW` để đối soát |
| `09` | Thẻ/Tài khoản chưa đăng ký dịch vụ InternetBanking | Báo lỗi người dùng đổi phương thức |
| `10` | Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần | Hủy phiên, yêu cầu thanh toán lại |
| `11` | Đã hết hạn chờ thanh toán | Hủy hold / đóng giao dịch |
| `24` | Khách hàng hủy giao dịch | Cập nhật giao dịch `CANCELLED` |
| `51` | Tài khoản không đủ số dư | Báo lỗi số dư không đủ |
| `97` | Chữ ký không hợp lệ (Checksum failed) | Trả lỗi HTTP 400 / Từ chối IPN |
