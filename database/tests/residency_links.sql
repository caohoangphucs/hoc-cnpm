-- Only run on disposable seed databases. Simulated user changes are rolled back.
BEGIN;
DO $$ BEGIN
    IF (SELECT count(*) FROM luu_tru WHERE trangthai='DANG_O') <> 48 THEN
        RAISE EXCEPTION 'Expected 46 original-room residents and 2 dedicated UC demo residents';
    END IF;
    IF EXISTS (
        SELECT 1 FROM giuong g LEFT JOIN luu_tru l
          ON l.magiuong=g.magiuong AND l.trangthai='DANG_O'
        WHERE (g.trangthai='DANG_O') IS DISTINCT FROM (l.maluutru IS NOT NULL)
    ) THEN RAISE EXCEPTION 'Occupied beds must have exactly one active residency'; END IF;
    IF EXISTS (
        SELECT 1 FROM sinh_vien s LEFT JOIN luu_tru l
          ON l.masinhvien=s.masinhvien AND l.trangthai='DANG_O'
        LEFT JOIN giuong g ON g.magiuong=l.magiuong
        WHERE s.maphonghientai IS DISTINCT FROM g.maphong
           OR (s.trangthai='DANG_O') IS DISTINCT FROM (l.maluutru IS NOT NULL)
    ) THEN RAISE EXCEPTION 'Student state/current room disagrees with active residency'; END IF;
    IF EXISTS (
        SELECT 1 FROM phong p WHERE p.dango<>(
            SELECT count(*) FROM luu_tru l JOIN giuong g USING(magiuong)
            WHERE g.maphong=p.maphong AND l.trangthai='DANG_O')
          OR p.dango>p.succhua
          OR p.succhua<>(SELECT count(*) FROM giuong g WHERE g.maphong=p.maphong)
    ) THEN RAISE EXCEPTION 'Room capacity/occupancy disagrees with actual beds and residents'; END IF;
    IF EXISTS (
        SELECT 1 FROM luu_tru l JOIN dang_ky_phong b USING(madangky)
        JOIN giuong g ON g.magiuong=l.magiuong JOIN phong p ON p.maphong=g.maphong
        JOIN toa_nha t USING(matoanha) JOIN sinh_vien s ON s.masinhvien=l.masinhvien
        WHERE l.trangthai='DANG_O' AND (b.trangthai<>'DA_DUYET'
            OR b.masinhvien<>l.masinhvien OR b.magiuong<>l.magiuong
            OR (t.gioitinhapdung<>'TAT_CA' AND t.gioitinhapdung<>s.gioitinh))
    ) THEN RAISE EXCEPTION 'Residency must match approved booking and building gender'; END IF;
    IF EXISTS (SELECT 1 FROM luu_tru l LEFT JOIN svc_contract c ON c.residency_id=l.maluutru
               WHERE l.trangthai='DANG_O' AND c.residency_id IS NULL)
    THEN RAISE EXCEPTION 'Active residency is missing its contract'; END IF;
    IF EXISTS (SELECT 1 FROM giuong g WHERE g.trangthai='GIU_CHO' AND NOT EXISTS (
        SELECT 1 FROM dang_ky_phong b WHERE b.magiuong=g.magiuong AND b.trangthai='CHO_DUYET'
          AND b.ngayhethan>localtimestamp))
    THEN RAISE EXCEPTION 'Held bed has no unexpired pending booking'; END IF;
    IF EXISTS (SELECT 1 FROM yeu_cau_sua_chua r JOIN sinh_vien s USING(masinhvien)
        WHERE r.mayeucau BETWEEN 1 AND 30 AND r.maphong IS DISTINCT FROM s.maphonghientai)
    THEN RAISE EXCEPTION 'Base repair reports must belong to their resident room'; END IF;
    IF EXISTS (SELECT 1 FROM hoa_don h WHERE h.dathanhtoan <> COALESCE(
        (SELECT sum(t.sotien) FROM thanh_toan t
         WHERE t.mahoadon=h.mahoadon AND t.trangthai='SUCCESS'), 0))
    THEN RAISE EXCEPTION 'Invoice amount paid disagrees with successful payment history'; END IF;
    IF (SELECT dango FROM phong WHERE maphong='A-201')<>3
       OR (SELECT count(*) FROM luu_tru l JOIN giuong g USING(magiuong)
           WHERE g.maphong='A-201' AND l.trangthai='DANG_O')<>3
    THEN RAISE EXCEPTION 'A-201 must have three linked residents'; END IF;
END $$;

-- A later deploy must not undo checkout, a manually locked bed, passwords or payments.
CREATE TEMP TABLE seed_baseline ON COMMIT DROP AS
SELECT (SELECT count(*) FROM dang_ky_phong) AS bookings,
       (SELECT count(*) FROM luu_tru) AS residencies,
       (SELECT jsonb_agg(to_jsonb(t) ORDER BY mathanhtoan) FROM thanh_toan t) AS payments;
UPDATE luu_tru SET trangthai='DA_TRA_PHONG' WHERE masinhvien='21110001';
UPDATE sinh_vien SET maphonghientai=NULL,trangthai='DA_ROI_KTX' WHERE masinhvien='21110001';
UPDATE giuong SET trangthai='KHOA' WHERE magiuong='G-A-102-1';
UPDATE tai_khoan SET matkhauhash='user-changed-password' WHERE tendangnhap='sv001';
\ir ../migrations/V20261015__link_seed_residencies.sql
\ir ../migrations/V20261015__link_seed_residencies.sql
DO $$ BEGIN
    IF (SELECT count(*) FROM dang_ky_phong)<>(SELECT bookings FROM seed_baseline)
       OR (SELECT count(*) FROM luu_tru)<>(SELECT residencies FROM seed_baseline)
       OR EXISTS (SELECT 1 FROM luu_tru WHERE masinhvien='21110001' AND trangthai='DANG_O')
       OR (SELECT trangthai FROM giuong WHERE magiuong='G-A-102-1')<>'KHOA'
       OR (SELECT matkhauhash FROM tai_khoan WHERE tendangnhap='sv001')<>'user-changed-password'
       OR (SELECT jsonb_agg(to_jsonb(t) ORDER BY mathanhtoan) FROM thanh_toan t)
          IS DISTINCT FROM (SELECT payments FROM seed_baseline)
    THEN RAISE EXCEPTION 'Repeated seed migration changed user actions or duplicated data'; END IF;
END $$;
ROLLBACK;
