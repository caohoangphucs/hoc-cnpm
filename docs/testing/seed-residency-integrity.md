# Liên kết dữ liệu lưu trú mẫu

Seed phải bảo đảm sinh viên đang ở có hồ sơ đã duyệt, bản ghi lưu trú đang hoạt động,
giường đang ở và phòng hiện tại khớp nhau. Số người ở của phòng được tính từ lưu trú.

- 46 sinh viên trong các phòng gốc, gồm sv001–sv040 và demo_room_41–demo_room_46.
- Hai sinh viên demo_resident_m và demo_resident_f ở DEMO-A và DEMO-B.
- A-201: sv013 / G-A-201-1, sv015 / G-A-201-2, sv017 / G-A-201-3.
- Không phân bổ sv041–sv055 vào lưu trú để giữ các tài khoản kiểm thử đăng ký mới.
- Giường giữ chỗ phải có hồ sơ chờ duyệt còn thời hạn.
- Mỗi khoản tiền đã thu trong seed có lịch sử thanh toán SUCCESS tương ứng.

## Khởi tạo và cập nhật

Docker khởi tạo DB mới và scripts/reset-db.sh chạy 08_residency_links.sql sau các seed khác.
Deploy áp dụng V20261015__link_seed_residencies.sql mà không reset database.
Migration chỉ bổ sung các trường hợp chưa có lịch sử hồ sơ/lưu trú trên sinh viên và giường;
không ghi đè mật khẩu, 2FA, thanh toán hoặc đưa sinh viên đã trả phòng trở lại.
Các trường hợp có lịch sử người dùng nhưng không nhất quán cần rà riêng, không tự sửa bằng seed.

## Kiểm tra

Chạy database/tests/residency_links.sql trên DB seed dùng một lần, không chạy trên DB vận hành.
Test kiểm tra liên kết, sức chứa, giới tính, hợp đồng, giữ chỗ, yêu cầu sửa chữa và tiền đã thu.
Các thao tác mô phỏng trả phòng và đổi mật khẩu được ROLLBACK khi test kết thúc.
DemoSeedApiIT gọi HTTP thật để kiểm tra sv013 có phòng A-201 và không được đăng ký thêm.
CI chạy kiểm tra SQL sau khi tạo đủ seed demo, rồi chạy test HTTP.
