-- Kỳ demo 11/2026 chưa có hóa đơn. Giữ D-102 thiếu chỉ số để thử luồng cảnh báo.
-- Không ghi đè số đo hoặc hóa đơn mà người dùng đã tạo.
INSERT INTO chi_so_dien_nuoc(maphong,kythang,diencu,dienmoi,nuoccu,nuocmoi,nguoighi)
SELECT p.maphong,'2026-11',COALESCE(c.dienmoi,100),COALESCE(c.dienmoi,100)+40,
       COALESCE(c.nuocmoi,20),COALESCE(c.nuocmoi,20)+5,'demo-seed'
FROM phong p LEFT JOIN chi_so_dien_nuoc c ON c.maphong=p.maphong AND c.kythang='2026-10'
WHERE p.maphong <> 'D-102'
  AND NOT EXISTS(SELECT 1 FROM hoa_don h WHERE h.maphong=p.maphong AND h.kythang='2026-11')
ON CONFLICT(maphong,kythang) DO NOTHING;
