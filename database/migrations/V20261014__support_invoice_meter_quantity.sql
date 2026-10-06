-- Supports every nonnegative INT meter delta with two decimal places.
DROP VIEW IF EXISTS vw_chitiethoadon;
ALTER TABLE chi_tiet_hoa_don
    ALTER COLUMN soluong TYPE DECIMAL(12, 2);
CREATE OR REPLACE VIEW vw_ChiTietHoaDon AS
SELECT
    hd.MaHoaDon,
    hd.MaPhong,
    hd.KyThang,
    hd.TongTien,
    hd.DaThanhToan,
    (hd.TongTien - hd.DaThanhToan) AS ConLai,
    hd.HanThanhToan,
    hd.TrangThai,
    ct.TenMuc,
    ct.SoLuong,
    ct.DonGia,
    ct.ThanhTien
FROM HOA_DON hd
JOIN CHI_TIET_HOA_DON ct ON hd.MaHoaDon = ct.MaHoaDon;
