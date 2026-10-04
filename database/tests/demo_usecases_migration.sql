-- Run only against the disposable CI database. Roll back both fixture data and simulated user actions.
BEGIN;
\ir ../migrations/V20261009__complete_demo_usecases.sql
DO $$ BEGIN
    IF (SELECT count(*) FROM dang_ky_phong WHERE madangky BETWEEN 900001 AND 900008) != 8
       OR (SELECT count(*) FROM svc_profile WHERE booking_id BETWEEN 900001 AND 900008) != 8
       OR (SELECT count(*) FROM luu_tru WHERE madangky BETWEEN 900005 AND 900006) != 2
       OR (SELECT count(*) FROM svc_visitor WHERE id BETWEEN 900001 AND 900003) != 3
       OR (SELECT count(*) FROM svc_appeal WHERE id=900001 AND state='OPEN') != 1 THEN
        RAISE EXCEPTION 'The demo dataset is incomplete';
    END IF;
END $$;
-- Simulate a real rejection and changed password; the next deploy must preserve them.
UPDATE dang_ky_phong SET trangthai='TU_CHOI',version=2,ghichulydo='User decision' WHERE madangky=900001;
UPDATE giuong SET trangthai='TRONG' WHERE magiuong='DEMO-A-1';
UPDATE tai_khoan SET matkhauhash='user-changed-password' WHERE tendangnhap='demo_pending_m1';
\ir ../migrations/V20261009__complete_demo_usecases.sql
DO $$ BEGIN
    IF (SELECT trangthai FROM dang_ky_phong WHERE madangky=900001) != 'TU_CHOI'
       OR (SELECT trangthai FROM giuong WHERE magiuong='DEMO-A-1') != 'TRONG'
       OR (SELECT matkhauhash FROM tai_khoan WHERE tendangnhap='demo_pending_m1') != 'user-changed-password'
       OR (SELECT count(*) FROM dang_ky_phong WHERE madangky BETWEEN 900001 AND 900008) != 8
       OR (SELECT sum(sodiemthaydoi) FROM so_diem_cong_ich WHERE masinhvien='DEMO-AF') != 57 THEN
        RAISE EXCEPTION 'Repeated deployment changed existing demo actions or duplicated fixtures';
    END IF;
END $$;
ROLLBACK;
