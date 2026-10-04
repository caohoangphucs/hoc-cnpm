-- Chỉ sửa các vi phạm seed cũ gắn nhầm lượt cổng của sinh viên khác.
-- Tạo lượt về muộn đúng sinh viên/thời điểm; không sửa lượt cổng thật hay hồ sơ đã đúng.
WITH wrong_links AS (
  SELECT v.* FROM vi_pham v JOIN ra_vao r ON r.maravao=v.maravao
  WHERE v.mavipham BETWEEN 1 AND 15 AND v.masinhvien<>r.masinhvien
    AND v.noidungvipham='Về KTX muộn sau 23h không xin phép'
    AND v.ngayvipham=TIMESTAMP '2026-09-30 23:15:00'
), corrected_entries AS (
  INSERT INTO ra_vao(masinhvien,loaisukien,thoigian,baoveghinhan,ghichu)
  SELECT masinhvien,'VAO',ngayvipham,'baove1','seed-gate-violation:'||mavipham
  FROM wrong_links RETURNING maravao,masinhvien,ghichu
)
UPDATE vi_pham v SET maravao=r.maravao
FROM corrected_entries r
WHERE r.ghichu='seed-gate-violation:'||v.mavipham AND r.masinhvien=v.masinhvien;

-- Mã sự kiện ổn định để migration chạy lại không tạo thêm dữ liệu.
INSERT INTO svc_gate_event(event_id,entry_id,device_id,payload_hash,actor_id,barrier_status)
SELECT md5(r.ghichu)::uuid,r.maravao,'DEMO-GATE',md5(r.ghichu),a.mataikhoan,'OPENED'
FROM ra_vao r CROSS JOIN tai_khoan a
WHERE r.ghichu LIKE 'seed-gate-violation:%' AND a.tendangnhap='baove1'
ON CONFLICT DO NOTHING;
