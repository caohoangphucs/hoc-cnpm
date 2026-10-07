-- Reconcile original demo occupancy with actual residents, approved bookings and contracts.
-- One atomic statement also works with Docker entrypoint autocommit.
-- Never reset passwords, 2FA, payments, or existing booking/residency history.
DO $$
DECLARE
    fixture record;
    resident record;
    room_id text;
    booking_id int;
    residency_id int;
BEGIN
    DROP TABLE IF EXISTS pg_temp.seed_resident_fixture;
    CREATE TEMP TABLE seed_resident_fixture (
        number int, student text, username text, bed text, new_gender text
    ) ON COMMIT DROP;
    INSERT INTO seed_resident_fixture VALUES
(1,'21110001','sv001','G-A-102-1',NULL),
(2,'21110002','sv002','G-B-103-1',NULL),
(3,'21110003','sv003','G-A-101-1',NULL),
(4,'21110004','sv004','G-B-102-1',NULL),
(5,'21110005','sv005','G-A-103-1',NULL),
(6,'21110006','sv006','G-B-101-1',NULL),
(7,'21110007','sv007','G-A-102-2',NULL),
(8,'21110008','sv008','G-B-103-2',NULL),
(9,'21110009','sv009','G-A-101-2',NULL),
(10,'21110010','sv010','G-B-102-2',NULL),
(11,'21110011','sv011','G-A-103-2',NULL),
(12,'21110012','sv012','G-B-102-3',NULL),
(13,'21110013','sv013','G-A-201-1',NULL),
(14,'21110014','sv014','G-B-103-3',NULL),
(15,'21110015','sv015','G-A-201-2',NULL),
(16,'21110016','sv016','G-B-103-4',NULL),
(17,'21110017','sv017','G-A-201-3',NULL),
(18,'21110018','sv018','G-B-103-5',NULL),
(19,'21110019','sv019','G-A-202-1',NULL),
(20,'21110020','sv020','G-B-201-1',NULL),
(21,'21110021','sv021','G-A-202-2',NULL),
(22,'21110022','sv022','G-B-202-1',NULL),
(23,'21110023','sv023','G-A-202-3',NULL),
(24,'21110024','sv024','G-B-202-2',NULL),
(25,'21110025','sv025','G-A-202-4',NULL),
(26,'21110026','sv026','G-B-203-1',NULL),
(27,'21110027','sv027','G-A-203-1',NULL),
(28,'21110028','sv028','G-B-203-2',NULL),
(29,'21110029','sv029','G-A-203-2',NULL),
(30,'21110030','sv030','G-B-203-3',NULL),
(31,'21110031','sv031','G-A-301-1',NULL),
(32,'21110032','sv032','G-C-101-1',NULL),
(33,'21110033','sv033','G-A-301-2',NULL),
(34,'21110034','sv034','G-C-102-1',NULL),
(35,'21110035','sv035','G-A-301-3',NULL),
(36,'21110036','sv036','G-C-102-2',NULL),
(37,'21110037','sv037','G-A-301-4',NULL),
(38,'21110038','sv038','G-C-201-1',NULL),
(39,'21110039','sv039','G-A-302-1',NULL),
(40,'21110040','sv040','G-C-201-2',NULL),
(41,'DEMO-ROOM-41','demo_room_41','G-C-202-1','NAM'),
(42,'DEMO-ROOM-42','demo_room_42','G-D-101-1','NAM'),
(43,'DEMO-ROOM-43','demo_room_43','G-D-101-2','NAM'),
(44,'DEMO-ROOM-44','demo_room_44','G-D-101-3','NAM'),
(45,'DEMO-ROOM-45','demo_room_45','G-D-102-1','NAM'),
(46,'DEMO-ROOM-46','demo_room_46','G-D-102-2','NAM');

    FOR fixture IN SELECT * FROM seed_resident_fixture ORDER BY number LOOP
        SELECT maphong INTO room_id FROM giuong WHERE magiuong=fixture.bed;
        IF room_id IS NULL THEN
            RAISE EXCEPTION 'Missing bed for residency fixture: %', fixture.bed;
        END IF;

        -- Six additional synthetic residents fill beds that had no corresponding student.
        IF fixture.new_gender IS NOT NULL THEN
            INSERT INTO tai_khoan(tendangnhap,matkhauhash,hoten,email,vaitro,trangthai)
            SELECT fixture.username,
                   '$2a$10$bzSt4ZQZn5e6T.8sC8FwWemE5XHWv/tCsZEPv2vXgWICD.wy6ug0i',
                   'Sinh viên demo phòng '||room_id||' giường '||right(fixture.bed,1),
                   fixture.username||'@demo.ktx.invalid','SINHVIEN','ACTIVE'
            WHERE EXISTS (SELECT 1 FROM giuong WHERE magiuong=fixture.bed AND trangthai='DANG_O')
              AND NOT EXISTS (SELECT 1 FROM luu_tru WHERE magiuong=fixture.bed)
              AND NOT EXISTS (SELECT 1 FROM dang_ky_phong WHERE magiuong=fixture.bed)
            ON CONFLICT (tendangnhap) DO NOTHING;
            INSERT INTO sinh_vien(masinhvien,mataikhoan,hoten,gioitinh,cccd,email,sodienthoai,trangthai)
            SELECT fixture.student,a.mataikhoan,a.hoten,fixture.new_gender,'9800000000'||fixture.number,a.email,'09900000'||fixture.number,'DANG_O'
            FROM tai_khoan a WHERE a.tendangnhap=fixture.username
            ON CONFLICT (masinhvien) DO NOTHING;
        END IF;

        SELECT s.* INTO resident FROM sinh_vien s JOIN tai_khoan a USING(mataikhoan)
        WHERE s.masinhvien=fixture.student AND a.tendangnhap=fixture.username;
        IF NOT FOUND THEN CONTINUE; END IF;

        -- Existing history means a user has already acted: do not relocate or re-admit them.
        IF resident.trangthai='DANG_O'
           AND NOT EXISTS (SELECT 1 FROM luu_tru WHERE masinhvien=fixture.student)
           AND NOT EXISTS (SELECT 1 FROM dang_ky_phong WHERE masinhvien=fixture.student)
           AND NOT EXISTS (SELECT 1 FROM luu_tru WHERE magiuong=fixture.bed)
           AND NOT EXISTS (SELECT 1 FROM dang_ky_phong WHERE magiuong=fixture.bed)
           AND EXISTS (SELECT 1 FROM giuong WHERE magiuong=fixture.bed AND trangthai='DANG_O') THEN
            INSERT INTO dang_ky_phong(masinhvien,magiuong,ngaydangky,ngayhethan,trangthai,ghichulydo)
            VALUES(fixture.student,fixture.bed,'2026-07-31','2026-08-02','DA_DUYET',
                   'Seed: lưu trú đã được duyệt trước kỳ hóa đơn') RETURNING madangky INTO booking_id;
            INSERT INTO luu_tru(madangky,masinhvien,magiuong,ngaybatdau,ngayketthuc,trangthai)
            VALUES(booking_id,fixture.student,fixture.bed,'2026-08-01','2027-01-15','DANG_O')
            RETURNING maluutru INTO residency_id;
            INSERT INTO svc_contract(residency_id,semester_id,state,start_date,end_date)
            SELECT residency_id,public_id,'ACTIVE','2026-08-01','2027-01-15'
            FROM hoc_ky WHERE mahocky='HK1-2026';
            UPDATE sinh_vien SET maphonghientai=room_id WHERE masinhvien=fixture.student;
        END IF;
    END LOOP;

    -- Remove orphan holds only when there has never been a booking or residency on that bed.
    UPDATE giuong g SET trangthai='TRONG',version=version+1
    WHERE g.magiuong LIKE 'G-%' AND g.trangthai='GIU_CHO'
      AND NOT EXISTS (SELECT 1 FROM dang_ky_phong b WHERE b.magiuong=g.magiuong)
      AND NOT EXISTS (SELECT 1 FROM luu_tru l WHERE l.magiuong=g.magiuong);

    -- Occupancy is derived from active residencies, preserving manually locked/repair rooms.
    UPDATE phong p SET dango=x.occupied,
        trangthai=CASE WHEN p.trangthai IN ('KHOA','DANG_SUA') THEN p.trangthai
                      WHEN x.occupied>=p.succhua THEN 'DAY'
                      WHEN x.occupied=0 THEN 'TRONG' ELSE 'CON_CHO' END
    FROM (SELECT g.maphong,count(l.maluutru)::int AS occupied
          FROM giuong g LEFT JOIN luu_tru l ON l.magiuong=g.magiuong AND l.trangthai='DANG_O'
          GROUP BY g.maphong) x
    WHERE p.maphong=x.maphong AND p.maphong IN
          (SELECT g.maphong FROM giuong g JOIN seed_resident_fixture f ON f.bed=g.magiuong);
END $$;
