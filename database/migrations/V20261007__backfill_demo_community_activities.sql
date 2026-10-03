SET client_encoding = 'UTF8';
-- Older deployments loaded the original seed before semesters/activities were added.
-- Insert only missing demo records; preserve edited titles, capacity and registrations.
INSERT INTO hoc_ky (mahocky, tenhocky, ngaybatdau, ngayketthuc, trangthai)
VALUES ('HK1-2026', 'Học kỳ 1 (Năm học 2026 - 2027)', '2026-09-01', '2027-01-15', 'ACTIVE')
ON CONFLICT (mahocky) DO NOTHING;

INSERT INTO hoat_dong (mahoatdong, mahocky, tieude, mota, thoigianbatdau,
                      thoigianketthuc, diadiem, chitieu, dadangky, diemcongich, trangthai)
VALUES
('ACT-01', 'HK1-2026', 'Chủ nhật xanh - Vệ sinh khuôn viên KTX',
 'Thu gom rác thải, dọn dẹp vệ sinh khuôn viên sân bóng và bồn hoa KTX',
 '2026-10-10 07:30:00', '2026-10-10 11:30:00', 'Khuôn viên trung tâm KTX', 50, 0, 10, 'MO_DANG_KY'),
('ACT-02', 'HK1-2026', 'Hỗ trợ tân sinh viên nhập học và nhận phòng KTX',
 'Hướng dẫn đường đi, hỗ trợ vận chuyển hành lý cho tân sinh viên đợt nhập học',
 '2026-10-15 07:00:00', '2026-10-15 17:00:00', 'Sảnh Tòa A và Tòa B', 30, 0, 15, 'MO_DANG_KY'),
('ACT-03', 'HK1-2026', 'Tập huấn kỹ năng PCCC và thoát hiểm KTX',
 'Tham gia khóa thực hành dập lửa và kỹ năng thoát hiểm tòa nhà cao tầng',
 '2026-10-20 14:00:00', '2026-10-20 17:00:00', 'Hội trường Tòa C', 100, 0, 10, 'MO_DANG_KY')
ON CONFLICT (mahoatdong) DO NOTHING;
