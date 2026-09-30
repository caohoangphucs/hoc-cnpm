-- =========================================================================
-- HỆ THỐNG QUẢN LÝ KÝ TÚC XÁ (KTX) - CÔNG NGHỆ PHẦN MỀM (CNPM)
-- SCHEMA, PROCEDURES, FUNCTIONS, TRIGGERS & SEED DATA (MYSQL 8.0)
-- =========================================================================

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS AUDIT_LOG;
DROP TABLE IF EXISTS XUAT_VAT_TU;
DROP TABLE IF EXISTS YEU_CAU_SUA_CHUA;
DROP TABLE IF EXISTS VAT_TU;
DROP TABLE IF EXISTS VI_PHAM;
DROP TABLE IF EXISTS RA_VAO;
DROP TABLE IF EXISTS THANH_TOAN;
DROP TABLE IF EXISTS CHI_TIET_HOA_DON;
DROP TABLE IF EXISTS HOA_DON;
DROP TABLE IF EXISTS CHI_SO_DIEN_NUOC;
DROP TABLE IF EXISTS DON_GIA_DICH_VU;
DROP TABLE IF EXISTS LUU_TRU;
DROP TABLE IF EXISTS DANG_KY_PHONG;
DROP TABLE IF EXISTS GIUONG;
DROP TABLE IF EXISTS PHONG;
DROP TABLE IF EXISTS LOAI_PHONG;
DROP TABLE IF EXISTS TOA_NHA;
DROP TABLE IF EXISTS SINH_VIEN_CHINH_SACH;
DROP TABLE IF EXISTS CHINH_SACH_GIAM_GIA;
DROP TABLE IF EXISTS SINH_VIEN;
DROP TABLE IF EXISTS TAI_KHOAN;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. TAI_KHOAN
CREATE TABLE TAI_KHOAN (
    MaTaiKhoan INT AUTO_INCREMENT PRIMARY KEY,
    TenDangNhap VARCHAR(50) NOT NULL UNIQUE,
    MatKhauHash VARCHAR(255) NOT NULL,
    HoTen VARCHAR(100) NOT NULL,
    Email VARCHAR(100) NOT NULL UNIQUE,
    SoDienThoai VARCHAR(20),
    VaiTro ENUM('ADMIN', 'KETOAN', 'KYTHUAT', 'BAOVE', 'SINHVIEN') NOT NULL,
    TrangThai ENUM('ACTIVE', 'LOCKED', 'DISABLED') DEFAULT 'ACTIVE',
    NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. TOA_NHA
CREATE TABLE TOA_NHA (
    MaToaNha VARCHAR(20) PRIMARY KEY,
    TenToa VARCHAR(100) NOT NULL,
    SoTang INT NOT NULL DEFAULT 1,
    GioiTinhApDung ENUM('NAM', 'NU', 'TAT_CA') DEFAULT 'TAT_CA',
    GhiChu VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. LOAI_PHONG
CREATE TABLE LOAI_PHONG (
    MaLoaiPhong VARCHAR(20) PRIMARY KEY,
    TenLoaiPhong VARCHAR(100) NOT NULL,
    SucChuaTieuChuan INT NOT NULL DEFAULT 4,
    DonGiaThang DECIMAL(15, 0) NOT NULL DEFAULT 500000,
    MoTa TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. PHONG
CREATE TABLE PHONG (
    MaPhong VARCHAR(20) PRIMARY KEY,
    MaToaNha VARCHAR(20) NOT NULL,
    MaLoaiPhong VARCHAR(20),
    SoPhong VARCHAR(20) NOT NULL,
    Tang INT NOT NULL DEFAULT 1,
    SucChua INT NOT NULL DEFAULT 4,
    DangO INT NOT NULL DEFAULT 0,
    GiaPhongCoBan DECIMAL(15, 0) NOT NULL DEFAULT 500000,
    TrangThai ENUM('TRONG', 'CON_CHO', 'DAY', 'DANG_SUA', 'KHOA') DEFAULT 'TRONG',
    CONSTRAINT FK_Phong_ToaNha FOREIGN KEY (MaToaNha) REFERENCES TOA_NHA(MaToaNha) ON DELETE CASCADE,
    CONSTRAINT FK_Phong_LoaiPhong FOREIGN KEY (MaLoaiPhong) REFERENCES LOAI_PHONG(MaLoaiPhong) ON DELETE SET NULL,
    CONSTRAINT UQ_Phong_Toa_SoPhong UNIQUE (MaToaNha, SoPhong)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. GIUONG
CREATE TABLE GIUONG (
    MaGiuong VARCHAR(20) PRIMARY KEY,
    MaPhong VARCHAR(20) NOT NULL,
    SoGiuong VARCHAR(20) NOT NULL,
    ViTri VARCHAR(50) DEFAULT 'Tang 1',
    TrangThai ENUM('TRONG', 'GIU_CHO', 'DANG_O', 'KHOA') DEFAULT 'TRONG',
    CONSTRAINT FK_Giuong_Phong FOREIGN KEY (MaPhong) REFERENCES PHONG(MaPhong) ON DELETE CASCADE,
    CONSTRAINT UQ_Giuong_Phong_SoGiuong UNIQUE (MaPhong, SoGiuong)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. SINH_VIEN
CREATE TABLE SINH_VIEN (
    MaSinhVien VARCHAR(20) PRIMARY KEY,
    MaTaiKhoan INT UNIQUE,
    HoTen VARCHAR(100) NOT NULL,
    NgaySinh DATE,
    GioiTinh ENUM('NAM', 'NU') NOT NULL,
    CCCD VARCHAR(20) NOT NULL UNIQUE,
    Email VARCHAR(100) NOT NULL UNIQUE,
    SoDienThoai VARCHAR(20) NOT NULL,
    Lop VARCHAR(50),
    Khoa VARCHAR(100),
    QueQuan VARCHAR(255),
    MaPhongHienTai VARCHAR(20),
    TrangThai ENUM('DANG_O', 'DA_ROI_KTX', 'CHO_DUYET') DEFAULT 'CHO_DUYET',
    CONSTRAINT FK_SinhVien_TaiKhoan FOREIGN KEY (MaTaiKhoan) REFERENCES TAI_KHOAN(MaTaiKhoan) ON DELETE SET NULL,
    CONSTRAINT FK_SinhVien_Phong FOREIGN KEY (MaPhongHienTai) REFERENCES PHONG(MaPhong) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. DANG_KY_PHONG
CREATE TABLE DANG_KY_PHONG (
    MaDangKy INT AUTO_INCREMENT PRIMARY KEY,
    MaSinhVien VARCHAR(20) NOT NULL,
    MaGiuong VARCHAR(20) NOT NULL,
    NgayDangKy DATETIME DEFAULT CURRENT_TIMESTAMP,
    NgayHetHan DATETIME NOT NULL,
    TrangThai ENUM('CHO_DUYET', 'DA_DUYET', 'TU_CHOI', 'HUY') DEFAULT 'CHO_DUYET',
    GhiChuLyDo TEXT,
    CONSTRAINT FK_DangKy_SinhVien FOREIGN KEY (MaSinhVien) REFERENCES SINH_VIEN(MaSinhVien) ON DELETE CASCADE,
    CONSTRAINT FK_DangKy_Giuong FOREIGN KEY (MaGiuong) REFERENCES GIUONG(MaGiuong) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. LUU_TRU
CREATE TABLE LUU_TRU (
    MaLuuTru INT AUTO_INCREMENT PRIMARY KEY,
    MaDangKy INT UNIQUE,
    MaSinhVien VARCHAR(20) NOT NULL,
    MaGiuong VARCHAR(20) NOT NULL,
    NgayBatDau DATE NOT NULL,
    NgayKetThuc DATE,
    TrangThai ENUM('DANG_O', 'DA_TRA_PHONG') DEFAULT 'DANG_O',
    CONSTRAINT FK_LuuTru_DangKy FOREIGN KEY (MaDangKy) REFERENCES DANG_KY_PHONG(MaDangKy) ON DELETE SET NULL,
    CONSTRAINT FK_LuuTru_SinhVien FOREIGN KEY (MaSinhVien) REFERENCES SINH_VIEN(MaSinhVien),
    CONSTRAINT FK_LuuTru_Giuong FOREIGN KEY (MaGiuong) REFERENCES GIUONG(MaGiuong)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. DON_GIA_DICH_VU
CREATE TABLE DON_GIA_DICH_VU (
    MaDonGia VARCHAR(20) PRIMARY KEY,
    LoaiDichVu ENUM('DIEN', 'NUOC', 'INTERNET', 'GUI_XE', 'KHAC') NOT NULL,
    TenDichVu VARCHAR(100) NOT NULL,
    DonViTinh VARCHAR(20) NOT NULL,
    DonGia DECIMAL(15, 0) NOT NULL,
    NgayApDung DATE NOT NULL,
    TrangThai ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. CHI_SO_DIEN_NUOC
CREATE TABLE CHI_SO_DIEN_NUOC (
    MaChiSo INT AUTO_INCREMENT PRIMARY KEY,
    MaPhong VARCHAR(20) NOT NULL,
    KyThang VARCHAR(7) NOT NULL, -- Format: YYYY-MM
    DienCu INT NOT NULL DEFAULT 0,
    DienMoi INT NOT NULL DEFAULT 0,
    NuocCu INT NOT NULL DEFAULT 0,
    NuocMoi INT NOT NULL DEFAULT 0,
    NgayGhi DATETIME DEFAULT CURRENT_TIMESTAMP,
    NguoiGhi VARCHAR(50),
    CONSTRAINT FK_CSDN_Phong FOREIGN KEY (MaPhong) REFERENCES PHONG(MaPhong) ON DELETE CASCADE,
    CONSTRAINT UQ_CSDN_Phong_KyThang UNIQUE (MaPhong, KyThang)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. HOA_DON
CREATE TABLE HOA_DON (
    MaHoaDon INT AUTO_INCREMENT PRIMARY KEY,
    MaPhong VARCHAR(20) NOT NULL,
    KyThang VARCHAR(7) NOT NULL,
    TienPhong DECIMAL(15, 0) NOT NULL DEFAULT 0,
    TienDien DECIMAL(15, 0) NOT NULL DEFAULT 0,
    TienNuoc DECIMAL(15, 0) NOT NULL DEFAULT 0,
    TienDichVuKhac DECIMAL(15, 0) NOT NULL DEFAULT 0,
    GiamGia DECIMAL(15, 0) NOT NULL DEFAULT 0,
    TongTien DECIMAL(15, 0) NOT NULL DEFAULT 0,
    DaThanhToan DECIMAL(15, 0) NOT NULL DEFAULT 0,
    HanThanhToan DATE NOT NULL,
    TrangThai ENUM('CHUA_TT', 'TT_MOT_PHAN', 'DA_TT', 'QUA_HAN', 'HUY') DEFAULT 'CHUA_TT',
    NgayPhatHanh DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_HoaDon_Phong FOREIGN KEY (MaPhong) REFERENCES PHONG(MaPhong) ON DELETE CASCADE,
    CONSTRAINT UQ_HoaDon_Phong_KyThang UNIQUE (MaPhong, KyThang)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. CHI_TIET_HOA_DON
CREATE TABLE CHI_TIET_HOA_DON (
    MaChiTiet INT AUTO_INCREMENT PRIMARY KEY,
    MaHoaDon INT NOT NULL,
    MaDonGia VARCHAR(20),
    TenMuc VARCHAR(100) NOT NULL,
    SoLuong DECIMAL(10, 2) NOT NULL DEFAULT 1,
    DonGia DECIMAL(15, 0) NOT NULL,
    ThanhTien DECIMAL(15, 0) NOT NULL,
    CONSTRAINT FK_CTHD_HoaDon FOREIGN KEY (MaHoaDon) REFERENCES HOA_DON(MaHoaDon) ON DELETE CASCADE,
    CONSTRAINT FK_CTHD_DonGia FOREIGN KEY (MaDonGia) REFERENCES DON_GIA_DICH_VU(MaDonGia) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. THANH_TOAN
CREATE TABLE THANH_TOAN (
    MaThanhToan INT AUTO_INCREMENT PRIMARY KEY,
    MaHoaDon INT NOT NULL,
    MaGiaoDich VARCHAR(100) NOT NULL UNIQUE,
    PhuongThuc ENUM('VNPAY', 'MOMO', 'VIETQR', 'TIEN_MAT') NOT NULL,
    SoTien DECIMAL(15, 0) NOT NULL,
    TrangThai ENUM('PENDING', 'SUCCESS', 'FAILED') DEFAULT 'PENDING',
    NgayGiaoDich DATETIME DEFAULT CURRENT_TIMESTAMP,
    NguoiXacNhan VARCHAR(50),
    GhiChu TEXT,
    CONSTRAINT FK_ThanhToan_HoaDon FOREIGN KEY (MaHoaDon) REFERENCES HOA_DON(MaHoaDon)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. RA_VAO
CREATE TABLE RA_VAO (
    MaRaVao INT AUTO_INCREMENT PRIMARY KEY,
    MaSinhVien VARCHAR(20) NOT NULL,
    LoaiSuKien ENUM('VAO', 'RA') NOT NULL,
    ThoiGian DATETIME DEFAULT CURRENT_TIMESTAMP,
    BaoVeGhiNhan VARCHAR(50),
    GhiChu VARCHAR(255),
    CONSTRAINT FK_RaVao_SinhVien FOREIGN KEY (MaSinhVien) REFERENCES SINH_VIEN(MaSinhVien) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. VI_PHAM
CREATE TABLE VI_PHAM (
    MaViPham INT AUTO_INCREMENT PRIMARY KEY,
    MaSinhVien VARCHAR(20) NOT NULL,
    MaRaVao INT NULL,
    NoiDungViPham TEXT NOT NULL,
    MucDo ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'LOW',
    HinhThucXuLy VARCHAR(255),
    NgayViPham DATETIME DEFAULT CURRENT_TIMESTAMP,
    TrangThai ENUM('CHUA_XU_LY', 'DA_XU_LY') DEFAULT 'CHUA_XU_LY',
    CONSTRAINT FK_ViPham_SinhVien FOREIGN KEY (MaSinhVien) REFERENCES SINH_VIEN(MaSinhVien) ON DELETE CASCADE,
    CONSTRAINT FK_ViPham_RaVao FOREIGN KEY (MaRaVao) REFERENCES RA_VAO(MaRaVao) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. VAT_TU
CREATE TABLE VAT_TU (
    MaVatTu VARCHAR(20) PRIMARY KEY,
    TenVatTu VARCHAR(100) NOT NULL,
    DonViTinh VARCHAR(20) NOT NULL,
    SoLuongTon INT NOT NULL DEFAULT 0,
    DonGiaNhap DECIMAL(15, 0) NOT NULL DEFAULT 0,
    GhiChu TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. YEU_CAU_SUA_CHUA
CREATE TABLE YEU_CAU_SUA_CHUA (
    MaYeuCau INT AUTO_INCREMENT PRIMARY KEY,
    MaPhong VARCHAR(20) NOT NULL,
    MaSinhVien VARCHAR(20) NOT NULL,
    TieuDe VARCHAR(255) NOT NULL,
    MoTaChiTiet TEXT NOT NULL,
    MucDoUuTien ENUM('THAP', 'TRUNG_BINH', 'CAO', 'KHAN_CAP') DEFAULT 'TRUNG_BINH',
    TrangThai ENUM('MOI', 'DANG_XU_LY', 'HOAN_THANH', 'HUY') DEFAULT 'MOI',
    NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    NgayHoanThanh DATETIME NULL,
    GhiChuKyThuat TEXT,
    CONSTRAINT FK_YCSC_Phong FOREIGN KEY (MaPhong) REFERENCES PHONG(MaPhong) ON DELETE CASCADE,
    CONSTRAINT FK_YCSC_SinhVien FOREIGN KEY (MaSinhVien) REFERENCES SINH_VIEN(MaSinhVien) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 18. XUAT_VAT_TU
CREATE TABLE XUAT_VAT_TU (
    MaXuat INT AUTO_INCREMENT PRIMARY KEY,
    MaVatTu VARCHAR(20) NOT NULL,
    MaYeuCau INT NULL,
    SoLuong INT NOT NULL,
    NgayXuat DATETIME DEFAULT CURRENT_TIMESTAMP,
    NguoiXuat VARCHAR(50),
    GhiChu TEXT,
    CONSTRAINT FK_XuatVT_VatTu FOREIGN KEY (MaVatTu) REFERENCES VAT_TU(MaVatTu),
    CONSTRAINT FK_XuatVT_YCSC FOREIGN KEY (MaYeuCau) REFERENCES YEU_CAU_SUA_CHUA(MaYeuCau) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 19. AUDIT_LOG
CREATE TABLE AUDIT_LOG (
    MaLog INT AUTO_INCREMENT PRIMARY KEY,
    TenBang VARCHAR(50) NOT NULL,
    HanhDong ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
    DuLieuCu JSON NULL,
    DuLieuMoi JSON NULL,
    NguoiThucHien VARCHAR(50) DEFAULT 'SYSTEM',
    ThoiGian DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================================
-- VIEWS
-- =========================================================================

CREATE OR REPLACE VIEW vw_PhongConCho AS
SELECT 
    p.MaPhong,
    p.MaToaNha,
    t.TenToa,
    p.SoPhong,
    p.Tang,
    p.SucChua,
    p.DangO,
    (p.SucChua - p.DangO) AS SoChoTrong,
    p.GiaPhongCoBan,
    p.TrangThai AS TrangThaiPhong
FROM PHONG p
JOIN TOA_NHA t ON p.MaToaNha = t.MaToaNha
WHERE p.TrangThai IN ('TRONG', 'CON_CHO');

CREATE OR REPLACE VIEW vw_TyLeLapDayPhong AS
SELECT 
    t.MaToaNha,
    t.TenToa,
    COUNT(p.MaPhong) AS TongSoPhong,
    SUM(p.SucChua) AS TongSucChua,
    SUM(p.DangO) AS TongDangO,
    ROUND((SUM(p.DangO) * 100.0 / NULLIF(SUM(p.SucChua), 0)), 2) AS TyLeLapDayPhanTram
FROM TOA_NHA t
LEFT JOIN PHONG p ON t.MaToaNha = p.MaToaNha
GROUP BY t.MaToaNha, t.TenToa;

CREATE OR REPLACE VIEW vw_DoanhThuThang AS
SELECT 
    KyThang,
    COUNT(MaHoaDon) AS TongHoaDon,
    SUM(TongTien) AS TongTienPhatHanh,
    SUM(DaThanhToan) AS TongThucThu,
    SUM(CASE WHEN TrangThai = 'DA_TT' THEN 1 ELSE 0 END) AS SoHoaDonDaThu,
    SUM(CASE WHEN TrangThai IN ('CHUA_TT', 'TT_MOT_PHAN', 'QUA_HAN') THEN (TongTien - DaThanhToan) ELSE 0 END) AS CongNoConLai
FROM HOA_DON
GROUP BY KyThang;

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

CREATE OR REPLACE VIEW vw_LichSuThanhToan AS
SELECT 
    tt.MaThanhToan,
    tt.MaGiaoDich,
    tt.MaHoaDon,
    hd.MaPhong,
    hd.KyThang,
    tt.PhuongThuc,
    tt.SoTien,
    tt.TrangThai,
    tt.NgayGiaoDich,
    tt.NguoiXacNhan
FROM THANH_TOAN tt
JOIN HOA_DON hd ON tt.MaHoaDon = hd.MaHoaDon;

-- =========================================================================
-- STORED PROCEDURES & TRIGGERS
-- =========================================================================


DROP PROCEDURE IF EXISTS usp_DangNhap;
DROP PROCEDURE IF EXISTS usp_DatGiuong;
DROP PROCEDURE IF EXISTS usp_DuyetDangKy;
DROP PROCEDURE IF EXISTS usp_GhiChiSoDienNuoc;
DROP PROCEDURE IF EXISTS usp_PhatHanhHoaDon;
DROP PROCEDURE IF EXISTS usp_XacNhanThanhToan;
DROP PROCEDURE IF EXISTS usp_HoanThanhSuaChua;
DROP PROCEDURE IF EXISTS usp_GhiNhanRaVao;

DELIMITER //

-- SP: Đăng nhập
CREATE PROCEDURE usp_DangNhap(
    IN p_TenDangNhap VARCHAR(50)
)
BEGIN
    SELECT 
        tk.MaTaiKhoan,
        tk.TenDangNhap,
        tk.MatKhauHash,
        tk.HoTen,
        tk.Email,
        tk.VaiTro,
        tk.TrangThai,
        sv.MaSinhVien,
        sv.MaPhongHienTai
    FROM TAI_KHOAN tk
    LEFT JOIN SINH_VIEN sv ON tk.MaTaiKhoan = sv.MaTaiKhoan
    WHERE tk.TenDangNhap = p_TenDangNhap;
END //

-- SP: Đặt giường (giữ chỗ)
CREATE PROCEDURE usp_DatGiuong(
    IN p_MaSinhVien VARCHAR(20),
    IN p_MaGiuong VARCHAR(20),
    OUT p_MaDangKy INT,
    OUT p_Status VARCHAR(50),
    OUT p_Message VARCHAR(255)
)
BEGIN
    DECLARE v_TrangThaiGiuong VARCHAR(20);
    DECLARE v_ExistActive INT;

    -- Dọn đăng ký hết hạn trước
    UPDATE GIUONG g
    JOIN DANG_KY_PHONG dk ON g.MaGiuong = dk.MaGiuong
    SET g.TrangThai = 'TRONG', dk.TrangThai = 'HUY'
    WHERE dk.TrangThai = 'CHO_DUYET' AND dk.NgayHetHan < NOW();

    -- Kiểm tra sinh viên đã có đăng ký hiệu lực chưa
    SELECT COUNT(*) INTO v_ExistActive 
    FROM DANG_KY_PHONG 
    WHERE MaSinhVien = p_MaSinhVien AND TrangThai IN ('CHO_DUYET', 'DA_DUYET');

    IF v_ExistActive > 0 THEN
        SET p_Status = 'ERROR_50002';
        SET p_Message = 'Sinh viên đã có yêu cầu đăng ký đang chờ duyệt hoặc đang có hiệu lực.';
    ELSE
        SELECT TrangThai INTO v_TrangThaiGiuong 
        FROM GIUONG WHERE MaGiuong = p_MaGiuong FOR UPDATE;

        IF v_TrangThaiGiuong IS NULL OR v_TrangThaiGiuong != 'TRONG' THEN
            SET p_Status = 'ERROR_50001';
            SET p_Message = 'Giường đã có người giữ chỗ hoặc đang sử dụng.';
        ELSE
            -- Cập nhật giường sang giữ chỗ
            UPDATE GIUONG SET TrangThai = 'GIU_CHO' WHERE MaGiuong = p_MaGiuong;

            -- Tạo bản ghi đăng ký phòng (hạn duyệt 3 ngày)
            INSERT INTO DANG_KY_PHONG(MaSinhVien, MaGiuong, NgayDangKy, NgayHetHan, TrangThai)
            VALUES (p_MaSinhVien, p_MaGiuong, NOW(), DATE_ADD(NOW(), INTERVAL 3 DAY), 'CHO_DUYET');

            SET p_MaDangKy = LAST_INSERT_ID();
            SET p_Status = 'SUCCESS';
            SET p_Message = 'Đặt giữ chỗ giường thành công. Vui lòng chờ Admin duyệt.';
        END IF;
    END IF;
END //

-- SP: Admin Duyệt đăng ký
CREATE PROCEDURE usp_DuyetDangKy(
    IN p_MaDangKy INT,
    IN p_QuyetDinh VARCHAR(20), -- 'DUYET' hoặc 'TU_CHOI'
    IN p_GhiChuLyDo TEXT,
    OUT p_Status VARCHAR(50),
    OUT p_Message VARCHAR(255)
)
BEGIN
    DECLARE v_MaSinhVien VARCHAR(20);
    DECLARE v_MaGiuong VARCHAR(20);
    DECLARE v_MaPhong VARCHAR(20);
    DECLARE v_TrangThaiDK VARCHAR(20);

    SELECT MaSinhVien, MaGiuong, TrangThai 
    INTO v_MaSinhVien, v_MaGiuong, v_TrangThaiDK
    FROM DANG_KY_PHONG WHERE MaDangKy = p_MaDangKy;

    IF v_TrangThaiDK != 'CHO_DUYET' THEN
        SET p_Status = 'ERROR';
        SET p_Message = 'Yêu cầu đăng ký không ở trạng thái chờ duyệt.';
    ELSE
        SELECT MaPhong INTO v_MaPhong FROM GIUONG WHERE MaGiuong = v_MaGiuong;

        IF p_QuyetDinh = 'DUYET' THEN
            UPDATE DANG_KY_PHONG 
            SET TrangThai = 'DA_DUYET', GhiChuLyDo = p_GhiChuLyDo 
            WHERE MaDangKy = p_MaDangKy;

            UPDATE GIUONG SET TrangThai = 'DANG_O' WHERE MaGiuong = v_MaGiuong;

            INSERT INTO LUU_TRU(MaDangKy, MaSinhVien, MaGiuong, NgayBatDau, TrangThai)
            VALUES (p_MaDangKy, v_MaSinhVien, v_MaGiuong, CURDATE(), 'DANG_O');

            UPDATE SINH_VIEN 
            SET MaPhongHienTai = v_MaPhong, TrangThai = 'DANG_O' 
            WHERE MaSinhVien = v_MaSinhVien;

            -- Cập nhật số người đang ở trong phòng
            UPDATE PHONG 
            SET DangO = (SELECT COUNT(*) FROM GIUONG WHERE MaPhong = v_MaPhong AND TrangThai = 'DANG_O')
            WHERE MaPhong = v_MaPhong;

            UPDATE PHONG 
            SET TrangThai = CASE WHEN DangO >= SucChua THEN 'DAY' ELSE 'CON_CHO' END 
            WHERE MaPhong = v_MaPhong;

            SET p_Status = 'SUCCESS';
            SET p_Message = 'Đã duyệt đăng ký phòng và bàn giao giường thành công.';
        ELSE
            UPDATE DANG_KY_PHONG 
            SET TrangThai = 'TU_CHOI', GhiChuLyDo = p_GhiChuLyDo 
            WHERE MaDangKy = p_MaDangKy;

            UPDATE GIUONG SET TrangThai = 'TRONG' WHERE MaGiuong = v_MaGiuong;

            SET p_Status = 'SUCCESS';
            SET p_Message = 'Đã từ chối đăng ký và hoàn trả giường về trạng thái trống.';
        END IF;
    END IF;
END //

-- SP: Ghi chỉ số điện nước
CREATE PROCEDURE usp_GhiChiSoDienNuoc(
    IN p_MaPhong VARCHAR(20),
    IN p_KyThang VARCHAR(7),
    IN p_DienCu INT,
    IN p_DienMoi INT,
    IN p_NuocCu INT,
    IN p_NuocMoi INT,
    IN p_NguoiGhi VARCHAR(50),
    OUT p_Status VARCHAR(50),
    OUT p_Message VARCHAR(255)
)
BEGIN
    IF p_DienMoi < p_DienCu OR p_NuocMoi < p_NuocCu THEN
        SET p_Status = 'ERROR_50004';
        SET p_Message = 'Chỉ số mới không được nhỏ hơn chỉ số cũ.';
    ELSE
        INSERT INTO CHI_SO_DIEN_NUOC(MaPhong, KyThang, DienCu, DienMoi, NuocCu, NuocMoi, NgayGhi, NguoiGhi)
        VALUES(p_MaPhong, p_KyThang, p_DienCu, p_DienMoi, p_NuocCu, p_NuocMoi, NOW(), p_NguoiGhi)
        ON DUPLICATE KEY UPDATE
            DienCu = p_DienCu,
            DienMoi = p_DienMoi,
            NuocCu = p_NuocCu,
            NuocMoi = p_NuocMoi,
            NgayGhi = NOW(),
            NguoiGhi = p_NguoiGhi;

        SET p_Status = 'SUCCESS';
        SET p_Message = 'Đã ghi nhận chỉ số điện nước thành công.';
    END IF;
END //

-- SP: Phát hành hóa đơn
CREATE PROCEDURE usp_PhatHanhHoaDon(
    IN p_MaPhong VARCHAR(20),
    IN p_KyThang VARCHAR(7),
    OUT p_MaHoaDon INT,
    OUT p_Status VARCHAR(50),
    OUT p_Message VARCHAR(255)
)
BEGIN
    DECLARE v_GiaPhong DECIMAL(15,0);
    DECLARE v_DienCu INT;
    DECLARE v_DienMoi INT;
    DECLARE v_NuocCu INT;
    DECLARE v_NuocMoi INT;
    DECLARE v_DonGiaDien DECIMAL(15,0) DEFAULT 3500;
    DECLARE v_DonGiaNuoc DECIMAL(15,0) DEFAULT 15000;
    DECLARE v_TienDien DECIMAL(15,0);
    DECLARE v_TienNuoc DECIMAL(15,0);
    DECLARE v_TongTien DECIMAL(15,0);
    DECLARE v_HanThanhToan DATE;

    -- Kiểm tra đã tồn tại hóa đơn chưa
    IF EXISTS(SELECT 1 FROM HOA_DON WHERE MaPhong = p_MaPhong AND KyThang = p_KyThang) THEN
        SET p_Status = 'ERROR_50005';
        SET p_Message = 'Hóa đơn kỳ này của phòng đã được phát hành trước đó.';
    ELSE
        SELECT GiaPhongCoBan INTO v_GiaPhong FROM PHONG WHERE MaPhong = p_MaPhong;
        SELECT DienCu, DienMoi, NuocCu, NuocMoi 
        INTO v_DienCu, v_DienMoi, v_NuocCu, v_NuocMoi
        FROM CHI_SO_DIEN_NUOC WHERE MaPhong = p_MaPhong AND KyThang = p_KyThang;

        IF v_DienMoi IS NULL THEN
            SET p_Status = 'ERROR_50006';
            SET p_Message = 'Phòng chưa được chốt chỉ số điện nước cho kỳ này.';
        ELSE
            SELECT DonGia INTO v_DonGiaDien FROM DON_GIA_DICH_VU WHERE LoaiDichVu = 'DIEN' AND TrangThai = 'ACTIVE' LIMIT 1;
            SELECT DonGia INTO v_DonGiaNuoc FROM DON_GIA_DICH_VU WHERE LoaiDichVu = 'NUOC' AND TrangThai = 'ACTIVE' LIMIT 1;

            SET v_TienDien = (v_DienMoi - v_DienCu) * v_DonGiaDien;
            SET v_TienNuoc = (v_NuocMoi - v_NuocCu) * v_DonGiaNuoc;
            SET v_TongTien = v_GiaPhong + v_TienDien + v_TienNuoc;
            SET v_HanThanhToan = DATE_ADD(CURDATE(), INTERVAL 15 DAY);

            INSERT INTO HOA_DON(MaPhong, KyThang, TienPhong, TienDien, TienNuoc, TongTien, HanThanhToan, TrangThai)
            VALUES(p_MaPhong, p_KyThang, v_GiaPhong, v_TienDien, v_TienNuoc, v_TongTien, v_HanThanhToan, 'CHUA_TT');

            SET p_MaHoaDon = LAST_INSERT_ID();

            INSERT INTO CHI_TIET_HOA_DON(MaHoaDon, TenMuc, SoLuong, DonGia, ThanhTien)
            VALUES 
                (p_MaHoaDon, 'Tiền phòng', 1, v_GiaPhong, v_GiaPhong),
                (p_MaHoaDon, 'Tiền điện sinh hoạt', (v_DienMoi - v_DienCu), v_DonGiaDien, v_TienDien),
                (p_MaHoaDon, 'Tiền nước sinh hoạt', (v_NuocMoi - v_NuocCu), v_DonGiaNuoc, v_TienNuoc);

            SET p_Status = 'SUCCESS';
            SET p_Message = 'Phát hành hóa đơn thành công.';
        END IF;
    END IF;
END //

-- SP: Xác nhận thanh toán (VNPAY / Tiền mặt)
CREATE PROCEDURE usp_XacNhanThanhToan(
    IN p_MaGiaoDich VARCHAR(100),
    IN p_PhuongThuc VARCHAR(20),
    IN p_SoTien DECIMAL(15,0),
    IN p_NguoiXacNhan VARCHAR(50),
    OUT p_Status VARCHAR(50),
    OUT p_Message VARCHAR(255)
)
BEGIN
    DECLARE v_MaHoaDon INT;
    DECLARE v_TongTien DECIMAL(15,0);
    DECLARE v_DaThanhToan DECIMAL(15,0);

    SELECT tt.MaHoaDon, hd.TongTien, hd.DaThanhToan 
    INTO v_MaHoaDon, v_TongTien, v_DaThanhToan
    FROM THANH_TOAN tt
    JOIN HOA_DON hd ON tt.MaHoaDon = hd.MaHoaDon
    WHERE tt.MaGiaoDich = p_MaGiaoDich;

    IF v_MaHoaDon IS NULL THEN
        SET p_Status = 'ERROR_NOT_FOUND';
        SET p_Message = 'Không tìm thấy giao dịch thanh toán.';
    ELSE
        UPDATE THANH_TOAN 
        SET TrangThai = 'SUCCESS', NguoiXacNhan = p_NguoiXacNhan 
        WHERE MaGiaoDich = p_MaGiaoDich;

        UPDATE HOA_DON 
        SET DaThanhToan = DaThanhToan + p_SoTien,
            TrangThai = CASE 
                WHEN (DaThanhToan + p_SoTien) >= TongTien THEN 'DA_TT'
                ELSE 'TT_MOT_PHAN'
            END
        WHERE MaHoaDon = v_MaHoaDon;

        SET p_Status = 'SUCCESS';
        SET p_Message = 'Xác nhận thanh toán thành công.';
    END IF;
END //

-- SP: Hoàn thành sửa chữa & xuất vật tư
CREATE PROCEDURE usp_HoanThanhSuaChua(
    IN p_MaYeuCau INT,
    IN p_MaVatTu VARCHAR(20),
    IN p_SoLuongXuat INT,
    IN p_NguoiXuat VARCHAR(50),
    IN p_GhiChu TEXT,
    OUT p_Status VARCHAR(50),
    OUT p_Message VARCHAR(255)
)
BEGIN
    DECLARE v_TonKho INT DEFAULT 0;

    IF p_MaVatTu IS NOT NULL AND p_SoLuongXuat > 0 THEN
        SELECT SoLuongTon INTO v_TonKho FROM VAT_TU WHERE MaVatTu = p_MaVatTu FOR UPDATE;

        IF v_TonKho < p_SoLuongXuat THEN
            SET p_Status = 'ERROR_50011';
            SET p_Message = 'Kho không đủ số lượng vật tư để xuất.';
        ELSE
            UPDATE VAT_TU SET SoLuongTon = SoLuongTon - p_SoLuongXuat WHERE MaVatTu = p_MaVatTu;

            INSERT INTO XUAT_VAT_TU(MaVatTu, MaYeuCau, SoLuong, NguoiXuat, GhiChu)
            VALUES (p_MaVatTu, p_MaYeuCau, p_SoLuongXuat, p_NguoiXuat, p_GhiChu);

            UPDATE YEU_CAU_SUA_CHUA 
            SET TrangThai = 'HOAN_THANH', NgayHoanThanh = NOW(), GhiChuKyThuat = p_GhiChu 
            WHERE MaYeuCau = p_MaYeuCau;

            SET p_Status = 'SUCCESS';
            SET p_Message = 'Đã hoàn tất sửa chữa và xuất kho vật tư.';
        END IF;
    ELSE
        UPDATE YEU_CAU_SUA_CHUA 
        SET TrangThai = 'HOAN_THANH', NgayHoanThanh = NOW(), GhiChuKyThuat = p_GhiChu 
        WHERE MaYeuCau = p_MaYeuCau;

        SET p_Status = 'SUCCESS';
        SET p_Message = 'Đã hoàn tất xử lý yêu cầu sửa chữa.';
    END IF;
END //

-- SP: Ghi nhận ra vào & Tự động phạt trễ
CREATE PROCEDURE usp_GhiNhanRaVao(
    IN p_MaSinhVien VARCHAR(20),
    IN p_LoaiSuKien VARCHAR(10),
    IN p_BaoVe VARCHAR(50),
    IN p_GhiChu VARCHAR(255),
    OUT p_Status VARCHAR(50),
    OUT p_Message VARCHAR(255)
)
BEGIN
    DECLARE v_MaRaVao INT;
    DECLARE v_GioHienTai TIME;

    INSERT INTO RA_VAO(MaSinhVien, LoaiSuKien, ThoiGian, BaoVeGhiNhan, GhiChu)
    VALUES (p_MaSinhVien, p_LoaiSuKien, NOW(), p_BaoVe, p_GhiChu);

    SET v_MaRaVao = LAST_INSERT_ID();
    SET v_GioHienTai = CURTIME();

    -- Giới nghiêm: nếu vào cổng sau 23:00 tự động tạo vi phạm
    IF p_LoaiSuKien = 'VAO' AND (v_GioHienTai >= '23:00:00' OR v_GioHienTai < '05:00:00') THEN
        INSERT INTO VI_PHAM(MaSinhVien, MaRaVao, NoiDungViPham, MucDo, HinhThucXuLy)
        VALUES (
            p_MaSinhVien, 
            v_MaRaVao, 
            CONCAT('Về ký túc xá muộn sau giờ giới nghiêm (', v_GioHienTai, ')'), 
            CASE WHEN v_GioHienTai >= '00:00:00' AND v_GioHienTai < '05:00:00' THEN 'MEDIUM' ELSE 'LOW' END,
            'Nhắc nhở và ghi nhận sổ kỷ luật'
        );
        SET p_Status = 'VIOLATION_RECORDED';
        SET p_Message = 'Ghi nhận ra vào thành công (Tự động ghi nhận vi phạm về muộn sau 23h).';
    ELSE
        SET p_Status = 'SUCCESS';
        SET p_Message = 'Ghi nhận ra vào thành công.';
    END IF;
END //

DELIMITER ;

