-- Dedicated, synthetic demo cases. Never reset existing accounts, OTP keys or decisions.
-- Reserved identifiers isolate these cases from real users and the original sv001–sv055.
DROP TABLE IF EXISTS pg_temp.demo_booking_fixture, pg_temp.demo_new_bookings, pg_temp.demo_new_community;
CREATE TEMP TABLE demo_booking_fixture (
    number int, student text, username text, name text, gender text,
    room text, bed text, status text
) ON COMMIT DROP;
INSERT INTO demo_booking_fixture VALUES
(1,'DEMO-PM1','demo_pending_m1','Sinh viên nam chờ duyệt 1','NAM','DEMO-A','DEMO-A-1','CHO_DUYET'),
(2,'DEMO-PM2','demo_pending_m2','Sinh viên nam chờ duyệt 2','NAM','DEMO-A','DEMO-A-2','CHO_DUYET'),
(3,'DEMO-PF1','demo_pending_f1','Sinh viên nữ chờ duyệt 1','NU','DEMO-B','DEMO-B-1','CHO_DUYET'),
(4,'DEMO-PF2','demo_pending_f2','Sinh viên nữ chờ duyệt 2','NU','DEMO-B','DEMO-B-2','CHO_DUYET'),
(5,'DEMO-AM','demo_resident_m','Sinh viên nam đã duyệt','NAM','DEMO-A','DEMO-A-3','DA_DUYET'),
(6,'DEMO-AF','demo_resident_f','Sinh viên nữ đã duyệt','NU','DEMO-B','DEMO-B-3','DA_DUYET'),
(7,'DEMO-RM','demo_rejected','Sinh viên bị từ chối','NAM','DEMO-A','DEMO-A-4','TU_CHOI'),
(8,'DEMO-CF','demo_cancelled','Sinh viên đã hủy đơn','NU','DEMO-B','DEMO-B-4','HUY');

DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM dang_ky_phong b
               JOIN demo_booking_fixture f ON b.madangky=900000+f.number
               WHERE b.masinhvien!=f.student OR b.magiuong!=f.bed) THEN
        RAISE EXCEPTION 'Reserved demo booking IDs overlap unrelated data; refusing to seed';
    END IF;
END $$;

INSERT INTO tai_khoan(tendangnhap,matkhauhash,hoten,email,vaitro,trangthai)
SELECT username, '$2a$10$bzSt4ZQZn5e6T.8sC8FwWemE5XHWv/tCsZEPv2vXgWICD.wy6ug0i',
       name,username||'@demo.ktx.invalid','SINHVIEN','ACTIVE'
FROM demo_booking_fixture ON CONFLICT (tendangnhap) DO NOTHING;
INSERT INTO sinh_vien(masinhvien,mataikhoan,hoten,ngaysinh,gioitinh,cccd,email,sodienthoai,lop,khoa,quequan,trangthai)
SELECT f.student,a.mataikhoan,f.name,'2004-01-01',f.gender,
       '99000000000'||f.number,a.email,'0990000000'||f.number,'DEMO','Công nghệ thông tin','Dữ liệu demo','CHO_DUYET'
FROM demo_booking_fixture f JOIN tai_khoan a ON a.tendangnhap=f.username
ON CONFLICT (masinhvien) DO NOTHING;

INSERT INTO phong(maphong,matoanha,maloaiphong,sophong,tang,succhua,giaphongcoban)
VALUES ('DEMO-A','TOA-A','LP-4','DEMO-A',1,4,500000),
       ('DEMO-B','TOA-B','LP-4','DEMO-B',1,4,500000)
ON CONFLICT (maphong) DO NOTHING;
INSERT INTO giuong(magiuong,maphong,sogiuong,vitri,trangthai)
SELECT bed,room,'G'||right(bed,1),'Giường demo','TRONG'
FROM demo_booking_fixture ON CONFLICT (magiuong) DO NOTHING;

INSERT INTO dot_dang_ky(madot,mahocky,tendot,thoigianmo,thoigiandong,trangthai)
VALUES (900001,'HK1-2026','Đợt đăng ký demo đầy đủ nghiệp vụ',
        '2026-09-01','2027-01-15 23:59:59','ACTIVE')
ON CONFLICT (madot) DO NOTHING;

-- Capture ONLY newly inserted bookings, so future deployments preserve user actions.
CREATE TEMP TABLE demo_new_bookings ON COMMIT DROP AS
WITH inserted AS (
    INSERT INTO dang_ky_phong(madangky,masinhvien,magiuong,ngaydangky,ngayhethan,trangthai,ghichulydo)
    SELECT 900000+number,student,bed,now(),now()+interval '30 days',status,
           CASE status WHEN 'TU_CHOI' THEN 'Demo: hồ sơ không đủ điều kiện'
                       WHEN 'HUY' THEN 'Demo: sinh viên chủ động hủy'
                       ELSE 'Demo: hồ sơ đăng ký phòng' END
    FROM demo_booking_fixture ON CONFLICT (madangky) DO NOTHING RETURNING *
) SELECT * FROM inserted;

-- Synthetic document metadata follows the existing upload/scan fixture contract.
-- These are demo references, never copies of real identity documents.
INSERT INTO svc_file(id,owner_id,kind,object_key,mime,size_bytes,sha256,key_id,nonce,tag,state,scanned_at,expires_at)
SELECT md5('demo-profile-'||f.number||'-'||k.kind)::uuid,a.mataikhoan,k.kind,
       'demo/profile/'||f.student||'/'||k.kind||'.png','image/png',68,
       repeat('0',64),'demo-only',decode(repeat('0',24),'hex'),decode(repeat('0',32),'hex'),
       'ATTACHED',now(),now()+interval '365 days'
FROM demo_booking_fixture f JOIN tai_khoan a ON a.tendangnhap=f.username
CROSS JOIN (VALUES ('CCCD_FRONT'),('CCCD_BACK'),('STUDENT_CARD')) k(kind)
ON CONFLICT (id) DO NOTHING;
INSERT INTO svc_profile(booking_id,window_id,file_ids,accepted_at,state,approved_by)
SELECT 900000+f.number,900001,
       (SELECT jsonb_agg(md5('demo-profile-'||f.number||'-'||k.kind)::uuid)
        FROM (VALUES ('CCCD_FRONT'),('CCCD_BACK'),('STUDENT_CARD')) k(kind)),
       now(),'VALID',CASE WHEN f.status IN ('DA_DUYET','TU_CHOI')
                        THEN (SELECT mataikhoan FROM tai_khoan WHERE tendangnhap='admin') END
FROM demo_booking_fixture f JOIN demo_new_bookings b ON b.masinhvien=f.student
ON CONFLICT (booking_id) DO NOTHING;
UPDATE giuong g SET trangthai=CASE b.trangthai WHEN 'CHO_DUYET' THEN 'GIU_CHO' ELSE 'DANG_O' END
FROM demo_new_bookings b WHERE g.magiuong=b.magiuong AND b.trangthai IN ('CHO_DUYET','DA_DUYET');
INSERT INTO luu_tru(madangky,masinhvien,magiuong,ngaybatdau,ngayketthuc,trangthai)
SELECT madangky,masinhvien,magiuong,'2026-09-01','2027-01-15','DANG_O'
FROM demo_new_bookings WHERE trangthai='DA_DUYET';
INSERT INTO svc_contract(residency_id,semester_id,state,start_date,end_date)
SELECT l.maluutru,h.public_id,'DRAFT',l.ngaybatdau,l.ngayketthuc
FROM luu_tru l JOIN demo_new_bookings b ON b.madangky=l.madangky
JOIN hoc_ky h ON h.mahocky='HK1-2026' ON CONFLICT (residency_id) DO NOTHING;
UPDATE sinh_vien s SET maphonghientai=g.maphong,trangthai='DANG_O'
FROM demo_new_bookings b JOIN giuong g ON g.magiuong=b.magiuong
WHERE s.masinhvien=b.masinhvien AND b.trangthai='DA_DUYET';
UPDATE phong p SET dango=(SELECT count(*) FROM giuong g WHERE g.maphong=p.maphong AND g.trangthai='DANG_O'),
                   trangthai='CON_CHO'
WHERE p.maphong IN (SELECT g.maphong FROM giuong g JOIN demo_new_bookings b ON b.magiuong=g.magiuong);

-- UC-17/08: residents have their own outstanding and fully paid demo invoices.
INSERT INTO hoa_don(mahoadon,maphong,kythang,tienphong,tongtien,dathanhtoan,hanthanhtoan,trangthai)
VALUES (900001,'DEMO-A','2026-10',500000,500000,0,'2026-10-31','CHUA_TT'),
       (900002,'DEMO-B','2026-10',500000,500000,500000,'2026-10-31','DA_TT')
ON CONFLICT (mahoadon) DO NOTHING;
INSERT INTO svc_invoice_snapshot(invoice_id,input_hash,inputs)
SELECT id,md5('demo-invoice-'||id),jsonb_build_object('occupants',jsonb_build_array(jsonb_build_object('studentId',student)))
FROM (VALUES (900001,'DEMO-AM'),(900002,'DEMO-AF')) f(id,student)
ON CONFLICT (invoice_id) DO NOTHING;
INSERT INTO svc_room_price(room_id,effective_from,amount)
VALUES ('DEMO-A','2026-09-01',500000),('DEMO-B','2026-09-01',500000)
ON CONFLICT DO NOTHING;

-- UC-22: requests visible to both residents and the assigned technician.
INSERT INTO yeu_cau_sua_chua(mayeucau,maphong,masinhvien,tieude,motachitiet,mucdouutien,trangthai)
VALUES (900001,'DEMO-A','DEMO-AM','Demo: bóng đèn không sáng','Cần kiểm tra bóng đèn','TRUNG_BINH','MOI'),
       (900002,'DEMO-B','DEMO-AF','Demo: vòi nước bị rò','Đã tiếp nhận và phân công','TRUNG_BINH','DANG_XU_LY')
ON CONFLICT (mayeucau) DO NOTHING;
INSERT INTO svc_repair_assignment(repair_id,technician_id,scheduled_at)
SELECT 900002,mataikhoan,now()+interval '1 day' FROM tai_khoan WHERE tendangnhap='kythuat1'
ON CONFLICT (repair_id) DO NOTHING;

-- UC-25: wrap existing gate history so it is visible through the current API.
INSERT INTO svc_gate_event(event_id,entry_id,device_id,payload_hash,actor_id,barrier_status)
SELECT md5('demo-gate-'||r.maravao)::uuid,r.maravao,'DEMO-GATE',md5('demo-gate-'||r.maravao),
       a.mataikhoan,'OPENED'
FROM ra_vao r CROSS JOIN tai_khoan a
WHERE a.tendangnhap='baove1' AND r.maravao BETWEEN 1 AND 55
ON CONFLICT DO NOTHING;

-- UC-14: an unhandled violation for a dedicated resident, without changing existing decisions.
INSERT INTO vi_pham(mavipham,masinhvien,noidungvipham,mucdo,trangthai)
VALUES (900001,'DEMO-AM','Demo: gây tiếng ồn sau giờ nghỉ','LOW','CHUA_XU_LY')
ON CONFLICT (mavipham) DO NOTHING;

-- UC-14 alternative flow: a historical decision and a pending appeal for the female resident.
INSERT INTO svc_file(id,owner_id,kind,object_key,mime,size_bytes,sha256,key_id,nonce,tag,state,scanned_at,expires_at)
SELECT md5('demo-discipline-evidence')::uuid,mataikhoan,'EVIDENCE','demo/discipline/evidence.png',
       'image/png',68,repeat('0',64),'demo-only',decode(repeat('0',24),'hex'),decode(repeat('0',32),'hex'),
       'ATTACHED',now(),now()+interval '365 days'
FROM tai_khoan WHERE tendangnhap='admin' ON CONFLICT DO NOTHING;
INSERT INTO vi_pham(mavipham,masinhvien,noidungvipham,mucdo,hinhthucxuly,ngayvipham,trangthai)
VALUES (900002,'DEMO-AF','Demo: về sau giờ quy định','LOW','Cảnh cáo',now()-interval '1 day','DA_XU_LY')
ON CONFLICT (mavipham) DO NOTHING;
INSERT INTO svc_discipline_decision(id,violation_id,semester_id,revision,actor_id,decision,reason,evidence_id,created_at)
SELECT 900001,900002,h.public_id,1,a.mataikhoan,'Cảnh cáo','Demo: quyết định lịch sử để thử khiếu nại',
       md5('demo-discipline-evidence')::uuid,now()-interval '1 day'
FROM hoc_ky h CROSS JOIN tai_khoan a WHERE h.mahocky='HK1-2026' AND a.tendangnhap='admin'
ON CONFLICT DO NOTHING;
INSERT INTO svc_appeal(id,decision_id,student_id,content,state)
VALUES (900001,900001,'DEMO-AF','Demo: đề nghị xem xét lại vì có lý do chính đáng','OPEN')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('svc_discipline_decision','id'),
              GREATEST((SELECT last_value FROM svc_discipline_decision_id_seq),(SELECT max(id) FROM svc_discipline_decision)));
SELECT setval(pg_get_serial_sequence('svc_appeal','id'),
              GREATEST((SELECT last_value FROM svc_appeal_id_seq),(SELECT max(id) FROM svc_appeal)));

INSERT INTO hoat_dong(mahoatdong,mahocky,tieude,mota,thoigianbatdau,thoigianketthuc,diadiem,chitieu,dadangky,diemcongich,trangthai)
VALUES ('ACT-DEMO-DONE','HK1-2026','Demo: hoạt động đã hoàn thành','Dữ liệu tổng hợp cho luồng tích điểm',
        now()-interval '3 days',now()-interval '2 days','Khu demo',10,0,15,'HOAN_THANH')
ON CONFLICT (mahoatdong) DO NOTHING;

-- UC-30: registered/completed cases and a point balance eligible for conversion.
CREATE TEMP TABLE demo_new_community ON COMMIT DROP AS
WITH inserted AS (
    INSERT INTO dang_ky_cong_ich(mahoatdong,masinhvien,maqrtoken,trangthai)
    SELECT activity,student,'DEMO-QR-'||student||'-'||activity, status
    FROM (VALUES ('ACT-01','DEMO-AM','REGISTERED'),('ACT-DEMO-DONE','DEMO-AF','COMPLETED')) f(activity,student,status)
    WHERE NOT EXISTS (SELECT 1 FROM dang_ky_cong_ich d WHERE d.mahoatdong=f.activity AND d.masinhvien=f.student)
    RETURNING *
) SELECT * FROM inserted;
UPDATE hoat_dong h SET dadangky=h.dadangky+c.total
FROM (SELECT mahoatdong,count(*) AS total FROM demo_new_community GROUP BY mahoatdong) c
WHERE h.mahoatdong=c.mahoatdong;
INSERT INTO so_diem_cong_ich(masinhvien,mahocky,madangkycongich,sodiemthaydoi,loaigiaodich,ghichu)
SELECT masinhvien,'HK1-2026',madangkycongich,15,'CONG_DIEM','Demo: hoàn thành hoạt động công ích'
FROM demo_new_community WHERE trangthai='COMPLETED';
INSERT INTO so_diem_cong_ich(masinhvien,mahocky,sodiemthaydoi,loaigiaodich,ghichu)
SELECT 'DEMO-AF','HK1-2026',42,'CONG_DIEM','Demo: điểm tích lũy trước kỳ hiện tại'
WHERE NOT EXISTS (SELECT 1 FROM so_diem_cong_ich WHERE masinhvien='DEMO-AF'
                  AND ghichu='Demo: điểm tích lũy trước kỳ hiện tại');


-- UC-08: a synthetic historical successful session, never a real provider transaction.
INSERT INTO svc_payment_session(id,actor_id,gateway,provider_ref,amount,state,provider_transaction_id,expires_at)
SELECT md5('demo-payment-paid')::uuid,mataikhoan,'VNPAY','DEMO-PAID-900002',500000,'SUCCESS',
       'DEMO-TRANSACTION-900002',now()-interval '1 day'
FROM tai_khoan WHERE tendangnhap='demo_resident_f' ON CONFLICT DO NOTHING;
INSERT INTO svc_payment_allocation(session_id,invoice_id,amount,child_ref)
VALUES (md5('demo-payment-paid')::uuid,900002,500000,'DEMO-CHILD-900002') ON CONFLICT DO NOTHING;
INSERT INTO thanh_toan(mahoadon,magiaodich,phuongthuc,sotien,trangthai,ghichu)
VALUES (900002,'DEMO-CHILD-900002','VNPAY',500000,'SUCCESS','Demo tổng hợp; không có giao dịch tiền thật')
ON CONFLICT (magiaodich) DO NOTHING;

-- UC-17: meter readings and invoice line items support preview and invoice inspection.
INSERT INTO chi_so_dien_nuoc(maphong,kythang,diencu,dienmoi,nuoccu,nuocmoi,nguoighi)
VALUES ('DEMO-A','2026-10',100,100,10,10,'ketoan1'),('DEMO-B','2026-10',100,100,10,10,'ketoan1')
ON CONFLICT (maphong,kythang) DO NOTHING;
INSERT INTO chi_tiet_hoa_don(mahoadon,tenmuc,soluong,dongia,thanhtien)
SELECT id,'Tiền phòng demo',1,500000,500000 FROM (VALUES (900001),(900002)) f(id)
WHERE NOT EXISTS (SELECT 1 FROM chi_tiet_hoa_don WHERE mahoadon=f.id);

-- UC-25: synthetic visitors cover review, approval/check-in and rejection.
-- Ciphertexts contain fake data only, encrypted with the dedicated demo fixture key.
-- No real guest identity is introduced and no production encryption key is changed.
INSERT INTO svc_visitor(id,host_student_id,name_encrypted,identity_encrypted,valid_from,valid_to,state,approved_by)
SELECT id,'DEMO-AM','BIu6hJAtHg0Z/jfd.mlt5T4Y9SURcLfajCpSTdYJphuSm0JE8D6CPQ+Dq+xEnsUOxihXUow==','AKSKxFIdrKFMwVeq.NSLq3Mjq19cHmvXdMmaIGylVbLO01LGhStRA9yu0xePtjw==',now()-interval '1 hour',now()+interval '4 hours',state,
       CASE WHEN state!='PENDING' THEN (SELECT mataikhoan FROM tai_khoan WHERE tendangnhap='baove1') END
FROM (VALUES (900001,'PENDING'),(900002,'APPROVED'),(900003,'REJECTED')) f(id,state)
ON CONFLICT (id) DO NOTHING;
INSERT INTO svc_card(id,student_id,token_hash,valid_from,valid_to,state)
VALUES (md5('demo-card-nam')::uuid,'DEMO-AM','fcf37a869b57f35a763e0a169a4381b6a080c27aab7afe5565b072eb2f1b7402','2026-09-01','2027-01-15','ACTIVE')
ON CONFLICT DO NOTHING;
SELECT setval(pg_get_serial_sequence('svc_visitor','id'),
              GREATEST((SELECT last_value FROM svc_visitor_id_seq),(SELECT max(id) FROM svc_visitor)));

-- Explicit demo IDs must not collide with later service-generated IDs.
SELECT setval(pg_get_serial_sequence('dang_ky_phong','madangky'),
              GREATEST((SELECT last_value FROM dang_ky_phong_madangky_seq),(SELECT max(madangky) FROM dang_ky_phong)));
SELECT setval(pg_get_serial_sequence('dot_dang_ky','madot'),
              GREATEST((SELECT last_value FROM dot_dang_ky_madot_seq),(SELECT max(madot) FROM dot_dang_ky)));
SELECT setval(pg_get_serial_sequence('hoa_don','mahoadon'),
              GREATEST((SELECT last_value FROM hoa_don_mahoadon_seq),(SELECT max(mahoadon) FROM hoa_don)));
SELECT setval(pg_get_serial_sequence('yeu_cau_sua_chua','mayeucau'),
              GREATEST((SELECT last_value FROM yeu_cau_sua_chua_mayeucau_seq),(SELECT max(mayeucau) FROM yeu_cau_sua_chua)));
SELECT setval(pg_get_serial_sequence('vi_pham','mavipham'),
              GREATEST((SELECT last_value FROM vi_pham_mavipham_seq),(SELECT max(mavipham) FROM vi_pham)));
