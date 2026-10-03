# Test case và fixture demo

Mặc định tất cả NOT_RUN. Fixture chỉ dùng dữ liệu giả, DB local/test riêng; không sửa đồng hồ hệ thống. Adapter Clock được inject ở test; quy tắc thời gian được kiểm qua Clock/fixture hạn/biên giờ, API công khai không cho client đổi giờ.

## Bộ fixture bắt buộc

- Accounts DEMO_ADMIN/KETOAN/KYTHUAT/BAOVE, DEMO_SV_A/B/C; BCrypt của mật khẩu test, không fallback; 2FA testsecret chỉ ở env test. SV A/B chưa lưu trú, C đang lưu trú; thêm SV nữ để thử tòa NAM/NU.
- HocKy DEMO-2026-1 start2026-09-01/end2027-01-31; một đợt mở, một đóng. Tòa DEMO-NAM/DEMO-NU, phòng DEMO-R1 có2giường, R2 có1giường; G1TRONG/G2GIU_CHO trong fixture độc lập.
- Tài chính: phòng500000, đơn giá điện3500/nước15000 hiệu lực2026-09-01; meter100→120/10→12;2SV,1SV chính sách50%; expected tổng475000. Một fixture chưa meter, một kỳ đã bill. Phiênpending/success/expired,2bill gộp và callbackprovider giả có secret test riêng.
- Sửa chữa: phiếuMOI/DANG_XU_LY/HOAN_THANH, vật tư tồn50/1/0, ảnhPNG hợp lệ và ảnh giả. Gate: thẻactive/revoked/expired, kháchvalid/expired; clock tại23:00,23:00:01,00:30,04:59:59,05:00.
- Kỷ luật:2/3vi phạm kỳA,1kỳB,ảnhminh chứng,quyết định/khiếu nại. Công ích:A10-11/B10:30-12/C11-12;hoạt độngcòn1chỗ,SV57điểm,đãquyđổi4/5ĐRL,đăng kýcancelled/completed.

Tách fixture cho từng case, reset giữa case, không phụ thuộc thứ tự chạy. Dùng namespace DEMO-, ghi manifest IDs và expected row counts. SQL fixture sẽ được viết khi cấu trúc DB tương ứng hoàn thiện; tài liệu không tạo bảng mới hoặc nạp dữ liệu vào DB thật.

## Ca kiểm thử

| ID | UC | BR | Tiền điều kiện | Thao tác | Hậu điều kiện | Trạng thái |
|---|---|---|---|---|---|---|
| TC-04-01 | UC-04 | BR-03/04 | SV mới NAM;  đợt mở;  giường tòa NAM TRONG;  2 ảnh hợp lệ | Gửi đăng ký | CHO_DUYET/GIU_CHO;  hết hạn=tạo+48 h;  audit1 | NOT_RUN |
| TC-04-02 | UC-04 | BR-04 | 2 SV hợp lệ;  cùng giường TRONG | 20 request đồng thời | 1 đơn thành công; 19 x409;  không giường kép | NOT_RUN |
| TC-04-03 | UC-04 | BR-04 | 1 SV; 2 giường TRONG | 2 đăng ký đồng thời | 1 đơn CHO_DUYET | NOT_RUN |
| TC-04-04 | UC-04 | BR-03 | Hold hết hạn đúng now | Chạy expiry và gọi duyệt | HUY/TRONG;  duyệt409;  không LUU_TRU | NOT_RUN |
| TC-04-05 | UC-04 | BR-04 | SV NAM,  tòa NU hoặc đợt đóng | Gửi đơn | 422;  không tạo đơn/hold | NOT_RUN |
| TC-04-06 | UC-04 | BR-17 | Ảnh >5 MB hoặc PNG giả/script | Upload rồi submit | 413/415;  không có hồ sơ hợp lệ/đơn | NOT_RUN |
| TC-04-07 | UC-04 | BR-04 | SV đã có LUU_TRU DANG_O | Gửi đơn | 409;  không tạo đơn | NOT_RUN |
| TC-12-01 | UC-12 | BR-05 | Đơn còn hạn/hồ sơ VALID/giường GIU_CHO | Admin duyệt | DA_DUYET, giường DANG_O, SV/phòng đồng bộ, 1 LUU_TRU/contract | NOT_RUN |
| TC-12-02 | UC-12 | BR-05 | Đơn chờ | Admin từ chối có lý do | TU_CHOI;  giường TRONG;  không lưu trú | NOT_RUN |
| TC-12-03 | UC-12 | BR-05 | Giường KHOA hoặc hồ sơ thiếu | Duyệt | 409/422;  không đổi trạng thái | NOT_RUN |
| TC-12-04 | UC-12 | BR-05 | Đơn chờ;  inject lỗi insert contract | Duyệt | Rollback toàn bộ;  vẫn CHO_DUYET/GIU_CHO | NOT_RUN |
| TC-12-05 | UC-12 | BR-17 | SMTP timeout | Duyệt | Quyết định commit; outbox retry/DEAD;  không duyệt lại | NOT_RUN |
| TC-17-01 | UC-17 | BR-06/07 | Phòng500000;  điện100→120 giá3500;  nước10→12 giá15000; 2 SV, 1 SV giảm50% | Preview/phát hành | Tổng475000;  giảm125000;  các line và snapshot khớp | NOT_RUN |
| TC-17-02 | UC-17 | BR-08 | Thiếu meter hoặc mới<cũ | Preview/ghi chỉ số | Cảnh báo/422;  không phát hành | NOT_RUN |
| TC-17-03 | UC-17 | BR-08 | 2 request cùng phòng/kỳ | Phát hành đồng thời | 1 hóa đơn/chi tiết;  request khác 409 | NOT_RUN |
| TC-17-04 | UC-17 | BR-08 | Preview version 1;  đổi giá/draft version 2 | Phát hành version 1 | 409;  không lưu hóa đơn sai snapshot | NOT_RUN |
| TC-17-05 | UC-17 | BR-07 | SV có2 chính sách;  giá đổi sau ngày đầu kỳ | Phát hành | Chọn giảm cao nhất, không cộng; giá đúng ngày đầu kỳ | NOT_RUN |
| TC-17-06 | UC-17 | BR-08 | Kỳ đã phát hành | Ghi meter cùng kỳ | 409;  snapshot không đổi | NOT_RUN |
| TC-08-01 | UC-08 | BR-09/10 | Hóa đơn còn nợ475000; PENDING | IPN sandbox success đúng tiền/ref/chữ ký | SUCCESS; paid tăng475000 một lần;  biên lai/outbox | NOT_RUN |
| TC-08-02 | UC-08 | BR-10 | Phiên SUCCESS | Lặp IPN 20 lần | ACKalready;  paid không đổi;  không thêm ledger | NOT_RUN |
| TC-08-03 | UC-08 | BR-10 | PENDING | IPN sai chữ ký hoặc sai tiền/partner | Không gạch nợ;  lỗi ghi request Id | NOT_RUN |
| TC-08-04 | UC-08 | BR-09 | PENDING | Return URLsuccess nhưng chưa IPN | UI pending; paid không đổi | NOT_RUN |
| TC-08-05 | UC-08 | BR-09 | Phiên pending;  SV hủy/timeout | Hủy ở provider | Nợ giữ nguyên; đối soát có trạng thái | NOT_RUN |
| TC-08-06 | UC-08 | BR-10 | Phiên hết hạn;  nợ đã trả bằng phiên khác | IPN success muộn | NEEDS_REVIEW;  không tăng paid vượt tổng | NOT_RUN |
| TC-08-07 | UC-08 | BR-09 | 2 hóa đơn thuộc cùng SV,  tổng600000 | Gộp và IPN success | 1 phiên, 2 phân bổ;  tổngpaid tăng600000 đúng mỗi hóa đơn | NOT_RUN |
| TC-22-01 | UC-22 | BR-11 | Phiếu DANG_XU_LY;  tồn50 | Hoàn thành, xuất1 | HOAN_THANH;  tồn49; 1 phiếu xuất/lịch sử | NOT_RUN |
| TC-22-02 | UC-22 | BR-11 | Tồn1; 2 phiếu cùng dùng1 | Hoàn thành đồng thời | 1 thành công; 1 x409; tồn0 | NOT_RUN |
| TC-22-03 | UC-22 | BR-11 | Phiếu chưa hoàn thành;  tồn50 | Xuất51 hoặc lượng<=0 | 409/422;  tồn và phiếu không đổi | NOT_RUN |
| TC-22-04 | UC-22 | BR-11 | Phiếu HOAN_THANH | Gọi lại cùng key | Không xuất/cộng lịch sử lần hai | NOT_RUN |
| TC-22-05 | UC-22 | BR-11/17 | Thiếu hàng hoặc cần bảo hành | Tạo đề nghị mua/hẹn và thử notificationfail | Không tăng tồn;  lịch/hồ sơ giữ;  outboxretry | NOT_RUN |
| TC-25-01 | UC-25 | BR-12 | Clock Việt Nam23:00:00; thẻ valid | Quét VAO | RA_VAO1; VI_PHAM0 | NOT_RUN |
| TC-25-02 | UC-25 | BR-12 | Clock 23:00:01/00:30/04:59:59 | Quét VAO từng fixture | Mỗi event tạo1 vi phạm liên kết đúng log | NOT_RUN |
| TC-25-03 | UC-25 | BR-12 | Clock05:00:00 hoặc type RA lúc23:30 | Quét | Không vi phạm trễ | NOT_RUN |
| TC-25-04 | UC-25 | BR-12 | Thẻrevoked/hết hạn hoặc không lưu trú | Quét VAO | 409; không mở barrier/ghi log vào hợp lệ | NOT_RUN |
| TC-25-05 | UC-25 | BR-12 | 1 event Id; barrier timeout | Retry cùng event Id | 1 log/vi phạm; barrier FAILED/nhắc bảo vệ | NOT_RUN |
| TC-25-06 | UC-25 | BR-12 | Khách được duyệt trong hạn/khách hết hạn | Checkin khách | Valid ghi riêng; expired bị chặn; không ghi là SV | NOT_RUN |
| TC-14-01 | UC-14 | BR-13 | SV 2 vi phạm kỳ A+1 kỳ B | Xem ngưỡng | Kỳ A count2; không cờ3; không khóa tự động | NOT_RUN |
| TC-14-02 | UC-14 | BR-13 | SV 3 vi phạm xác nhận cùng kỳ | Admin xử lý có minh chứng | Cờ>=3; DA_XU_LY; quyết định/audit/người xử lý | NOT_RUN |
| TC-14-03 | UC-14 | BR-13 | Thiếu minh chứng hoặc inject lỗi giữa nhóm2 SV | Xử lý | 422 hoặc rollback nhóm; không quyết định dở dang | NOT_RUN |
| TC-14-04 | UC-14 | BR-13 | SV có quyết định | Khiếu nại, Admin chấp nhận | Lưu khiếu nại+revision mới; không xóa quyết định cũ | NOT_RUN |
| TC-30-01 | UC-30 | BR-14 | Hoạt động còn1 chỗ; 2 SV | 20 request đồng thời | 1 đăng ký thành công; occupied không vượt capacity | NOT_RUN |
| TC-30-02 | UC-30 | BR-14 | SV có A10:00-11:00 | Đăng ký B10:30-12:00 và C11:00-12:00 | B409; Cđược phép | NOT_RUN |
| TC-30-03 | UC-30 | BR-15 | Đăng ký, clock cáchstart đúng 12 h | Hủy 2 lần cùng key | CANCELLED; hoàn1 chỗ | NOT_RUN |
| TC-30-04 | UC-30 | BR-15 | Cáchstart11 h59 m | Hủy | 409; không hoànchỗ | NOT_RUN |
| TC-30-05 | UC-30 | BR-15 | Đăng ký QR valid | Quét QRrồi Adminconfirm2 lần | Quét chỉ ATTENDED; confirm1 EARN | NOT_RUN |
| TC-30-06 | UC-30 | BR-16 | SV 57 điểm chưa quyđổi | Quyđổi5 Đ RL rồi thêm 1 | Trừ50, còn7; tổng5; thêm 1 bị409 | NOT_RUN |
| TC-30-07 | UC-30 | BR-16 | Transfer PENDING | CTSVtimeout/retry/reject | Mộtdebit; retrycùng ID; reject1 compensation | NOT_RUN |
| TC-30-08 | UC-30 | BR-16 | Cócompleted vàcancelled | Cấpgiấy, ký, verify, rồirevoke | PDFchỉcompleted; chữkývalid; QRrevoked sau thu hồi | NOT_RUN |
| TC-04-08 | UC-04 | BR-01/17 | SV khác chủ hồ sơ;  không CSRF;  sessionidle15 m | Truy cập tài nguyên/submit | 404/403/401 tương ứng; không dữ liệu lộ | NOT_RUN |
| TC-12-06 | UC-12 | BR-17 | SV hoặc Admin chưa2 FA | Gọi quyết định | 403/401; không thay đổi | NOT_RUN |
| TC-08-08 | UC-08 | BR-10/17 | Cùng Idempotency Key khácbody | Gửi lại | 409; không phiên mới | NOT_RUN |

## Kiểm tra chung

Lặp quyền cho cả5roles ở mọi endpoint, ownership SV A→B, CSRF,uploadmagicbytes/XSS/SQLinjection tham số, idle15min/2FA/hashlỗi, logsecret. Kiểm fault injection trước/giữa/sau commit và worker crash; DB mất kết nối trả503/rollback; gateway/IPN trễ/lặp/sai chữ ký. Thử constraint trực tiếp để chứng minh DB chặn bản ghi sai (tồn âm/unique/FK) sau cập nhật SQL.

Nhánh thêm: duyệt hàng loạt phải có kết quả từng đơn và ưu tiên cấu hình, Excel chỉ tạo draft với lỗi dòng, warranty/review link chỉ gửi sau hoàn thành, báo cáo cổng lọc ngày theo Việt Nam và khách tách SV. Mỗi nhánh có request và dữ liệu trước/sau trong evidence.

## Kiểm thử chuyển công nghệ Spring Boot

| ID | Thao tác | Kết quả bắt buộc | Trạng thái |
|---|---|---|---|
| SB-01 | @SpringBootTest khởi động JAR/profile test với PostgreSQL 16 | Context healthy, đúng DataSource/timezone, không static DBContext | NOT_RUN |
| SB-02 | MockMvc role/ownership/2FA/sessionidle15min | Đúng401/403/404/Error JSON, trangHTMLredirect đúng | NOT_RUN |
| SB-03 | GET session trước login; POST login thiếu CSRF/có CSRF; rotate token | Thiếu403, hợp lệ có session; token mới sau login/logout | NOT_RUN |
| SB-04 | Service/repository SQL trực tiếp; kiểm quy tắc và inject lỗi giữa transaction | Đúng typedDTO; DomainException rollback mọi bảng/outbox | NOT_RUN |
| SB-05 | Nạp01/02 trên DB test mới; cập nhật SQL thủ công trên bản sao DB cũ | SQL đúng hậu điều kiện; app startup không tự DDL/seed; DB cũ giữ dữ liệu | NOT_RUN |
| SB-06 | Mở 8 màn hình React, form/JSON/upload/static/fragments | Binding/validation/CSRF đầy đủ; không phụ thuộc JSPmount | NOT_RUN |
| SB-07 | Gọi IPN không session và không CSRF; sai chữký | Callback hợp lệ được adapter xử lý; sai chữ ký không ghi tiền | NOT_RUN |
| SB-08 | Worker 2 instance, crash sau gửi; Actuator/mạng public | Lease chống nhận đôi, idempotency không ghi lại; metrics được bảo vệ | NOT_RUN |

Dùng Spring Boot Test, MockMvc/Spring Security Test và Testcontainers PostgreSQL 16. Không chạy tests H2 thay nghiệp vụ PL/pgSQL. Các test SB bổ sung 51 test nghiệp vụ, chưa có test code.
