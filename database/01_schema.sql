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

-- =========================================================================
-- SEED DATA (Chuẩn bị dữ liệu mẫu ban đầu)
INSERT INTO TAI_KHOAN (MaTaiKhoan, TenDangNhap, MatKhauHash, HoTen, Email, SoDienThoai, VaiTro, TrangThai) VALUES
(1, 'admin', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Quản Trị Viên KTX', 'admin@ktx.edu.vn', '0901112223', 'ADMIN', 'ACTIVE'),
(2, 'ketoan1', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Trần Thị Kế Toán', 'ketoan@ktx.edu.vn', '0902223334', 'KETOAN', 'ACTIVE'),
(3, 'kythuat1', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Nguyễn Văn Kỹ Thuật', 'kythuat@ktx.edu.vn', '0903334445', 'KYTHUAT', 'ACTIVE'),
(4, 'baove1', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Lê Văn Bảo Vệ', 'baove@ktx.edu.vn', '0904445556', 'BAOVE', 'ACTIVE'),
(5, 'sv001', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Trần Hữu Đạt', 'sv001@student.ktx.edu.vn', '0949714664', 'SINHVIEN', 'ACTIVE'),
(6, 'sv002', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Lê Thu Linh', 'sv002@student.ktx.edu.vn', '0926456681', 'SINHVIEN', 'ACTIVE'),
(7, 'sv003', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Phạm Hoàng Huy', 'sv003@student.ktx.edu.vn', '0964728935', 'SINHVIEN', 'ACTIVE'),
(8, 'sv004', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Hoàng Hồng Mai', 'sv004@student.ktx.edu.vn', '0940391159', 'SINHVIEN', 'ACTIVE'),
(9, 'sv005', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Huỳnh Quốc Nam', 'sv005@student.ktx.edu.vn', '0994462375', 'SINHVIEN', 'ACTIVE'),
(10, 'sv006', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Phan Phương Hương', 'sv006@student.ktx.edu.vn', '0963804153', 'SINHVIEN', 'ACTIVE'),
(11, 'sv007', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Vũ Thành Bảo', 'sv007@student.ktx.edu.vn', '0910961884', 'SINHVIEN', 'ACTIVE'),
(12, 'sv008', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Võ Bảo Hà', 'sv008@student.ktx.edu.vn', '0925064835', 'SINHVIEN', 'ACTIVE'),
(13, 'sv009', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Đặng Công Dũng', 'sv009@student.ktx.edu.vn', '0996968607', 'SINHVIEN', 'ACTIVE'),
(14, 'sv010', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Bùi Thị Trâm', 'sv010@student.ktx.edu.vn', '0947223374', 'SINHVIEN', 'ACTIVE'),
(15, 'sv011', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Đỗ Hữu Phong', 'sv011@student.ktx.edu.vn', '0944365948', 'SINHVIEN', 'ACTIVE'),
(16, 'sv012', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Hồ Thu Quyên', 'sv012@student.ktx.edu.vn', '0959892300', 'SINHVIEN', 'ACTIVE'),
(17, 'sv013', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Ngô Hoàng Khôi', 'sv013@student.ktx.edu.vn', '0928692061', 'SINHVIEN', 'ACTIVE'),
(18, 'sv014', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Dương Hồng Yến', 'sv014@student.ktx.edu.vn', '0984104833', 'SINHVIEN', 'ACTIVE'),
(19, 'sv015', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Nguyễn Quốc Phúc', 'sv015@student.ktx.edu.vn', '0926707386', 'SINHVIEN', 'ACTIVE'),
(20, 'sv016', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Trần Phương Trang', 'sv016@student.ktx.edu.vn', '0965386525', 'SINHVIEN', 'ACTIVE'),
(21, 'sv017', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Lê Thành Khoa', 'sv017@student.ktx.edu.vn', '0981533791', 'SINHVIEN', 'ACTIVE'),
(22, 'sv018', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Phạm Bảo Lan', 'sv018@student.ktx.edu.vn', '0933522951', 'SINHVIEN', 'ACTIVE'),
(23, 'sv019', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Hoàng Công Tùng', 'sv019@student.ktx.edu.vn', '0940348655', 'SINHVIEN', 'ACTIVE'),
(24, 'sv020', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Huỳnh Thị Thảo', 'sv020@student.ktx.edu.vn', '0981340059', 'SINHVIEN', 'ACTIVE'),
(25, 'sv021', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Phan Hữu Việt', 'sv021@student.ktx.edu.vn', '0984323679', 'SINHVIEN', 'ACTIVE'),
(26, 'sv022', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Vũ Thu Nhi', 'sv022@student.ktx.edu.vn', '0939464434', 'SINHVIEN', 'ACTIVE'),
(27, 'sv023', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Võ Hoàng Long', 'sv023@student.ktx.edu.vn', '0972740255', 'SINHVIEN', 'ACTIVE'),
(28, 'sv024', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Đặng Hồng Vy', 'sv024@student.ktx.edu.vn', '0938908262', 'SINHVIEN', 'ACTIVE'),
(29, 'sv025', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Bùi Quốc Hải', 'sv025@student.ktx.edu.vn', '0937914738', 'SINHVIEN', 'ACTIVE'),
(30, 'sv026', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Đỗ Phương Hạnh', 'sv026@student.ktx.edu.vn', '0996923685', 'SINHVIEN', 'ACTIVE'),
(31, 'sv027', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Hồ Thành Quân', 'sv027@student.ktx.edu.vn', '0946471669', 'SINHVIEN', 'ACTIVE'),
(32, 'sv028', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Ngô Bảo Tú', 'sv028@student.ktx.edu.vn', '0963416568', 'SINHVIEN', 'ACTIVE'),
(33, 'sv029', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Dương Công Thịnh', 'sv029@student.ktx.edu.vn', '0970722586', 'SINHVIEN', 'ACTIVE'),
(34, 'sv030', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Nguyễn Thị Hoa', 'sv030@student.ktx.edu.vn', '0943374197', 'SINHVIEN', 'ACTIVE'),
(35, 'sv031', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Trần Hữu Đạt', 'sv031@student.ktx.edu.vn', '0955694237', 'SINHVIEN', 'ACTIVE'),
(36, 'sv032', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Lê Thu Linh', 'sv032@student.ktx.edu.vn', '0961855246', 'SINHVIEN', 'ACTIVE'),
(37, 'sv033', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Phạm Hoàng Huy', 'sv033@student.ktx.edu.vn', '0998677250', 'SINHVIEN', 'ACTIVE'),
(38, 'sv034', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Hoàng Hồng Mai', 'sv034@student.ktx.edu.vn', '0962228824', 'SINHVIEN', 'ACTIVE'),
(39, 'sv035', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Huỳnh Quốc Nam', 'sv035@student.ktx.edu.vn', '0969949385', 'SINHVIEN', 'ACTIVE'),
(40, 'sv036', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Phan Phương Hương', 'sv036@student.ktx.edu.vn', '0913172692', 'SINHVIEN', 'ACTIVE'),
(41, 'sv037', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Vũ Thành Bảo', 'sv037@student.ktx.edu.vn', '0952734917', 'SINHVIEN', 'ACTIVE'),
(42, 'sv038', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Võ Bảo Hà', 'sv038@student.ktx.edu.vn', '0983197542', 'SINHVIEN', 'ACTIVE'),
(43, 'sv039', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Đặng Công Dũng', 'sv039@student.ktx.edu.vn', '0980121774', 'SINHVIEN', 'ACTIVE'),
(44, 'sv040', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Bùi Thị Trâm', 'sv040@student.ktx.edu.vn', '0927506013', 'SINHVIEN', 'ACTIVE'),
(45, 'sv041', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Đỗ Hữu Phong', 'sv041@student.ktx.edu.vn', '0967025445', 'SINHVIEN', 'ACTIVE'),
(46, 'sv042', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Hồ Thu Quyên', 'sv042@student.ktx.edu.vn', '0971564503', 'SINHVIEN', 'ACTIVE'),
(47, 'sv043', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Ngô Hoàng Khôi', 'sv043@student.ktx.edu.vn', '0969927692', 'SINHVIEN', 'ACTIVE'),
(48, 'sv044', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Dương Hồng Yến', 'sv044@student.ktx.edu.vn', '0924602596', 'SINHVIEN', 'ACTIVE'),
(49, 'sv045', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Nguyễn Quốc Phúc', 'sv045@student.ktx.edu.vn', '0975338304', 'SINHVIEN', 'ACTIVE'),
(50, 'sv046', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Trần Phương Trang', 'sv046@student.ktx.edu.vn', '0997439033', 'SINHVIEN', 'ACTIVE'),
(51, 'sv047', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Lê Thành Khoa', 'sv047@student.ktx.edu.vn', '0961209949', 'SINHVIEN', 'ACTIVE'),
(52, 'sv048', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Phạm Bảo Lan', 'sv048@student.ktx.edu.vn', '0979859308', 'SINHVIEN', 'ACTIVE'),
(53, 'sv049', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Hoàng Công Tùng', 'sv049@student.ktx.edu.vn', '0986131391', 'SINHVIEN', 'ACTIVE'),
(54, 'sv050', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Huỳnh Thị Thảo', 'sv050@student.ktx.edu.vn', '0942117940', 'SINHVIEN', 'ACTIVE'),
(55, 'sv051', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Phan Hữu Việt', 'sv051@student.ktx.edu.vn', '0982547538', 'SINHVIEN', 'ACTIVE'),
(56, 'sv052', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Vũ Thu Nhi', 'sv052@student.ktx.edu.vn', '0948919603', 'SINHVIEN', 'ACTIVE'),
(57, 'sv053', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Võ Hoàng Long', 'sv053@student.ktx.edu.vn', '0934565575', 'SINHVIEN', 'ACTIVE'),
(58, 'sv054', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Đặng Hồng Vy', 'sv054@student.ktx.edu.vn', '0990233835', 'SINHVIEN', 'ACTIVE'),
(59, 'sv055', '$2a$10$7EqJtq98hPqEX7fNZaFWoO./t6M.2rP9pX84qU0j490qLh8Oiqf2m', 'Bùi Quốc Hải', 'sv055@student.ktx.edu.vn', '0925745103', 'SINHVIEN', 'ACTIVE');

INSERT INTO TOA_NHA (MaToaNha, TenToa, SoTang, GioiTinhApDung, GhiChu) VALUES
('TOA-A', 'Tòa A (Khu Nam)', 5, 'NAM', 'Khu ký túc xá nam sinh viên'),
('TOA-B', 'Tòa B (Khu Nữ)', 5, 'NU', 'Khu ký túc xá nữ sinh viên'),
('TOA-C', 'Tòa C (Chất lượng cao)', 6, 'TAT_CA', 'Phòng dịch vụ đầy đủ tiện nghi'),
('TOA-D', 'Tòa D (Sau đại học)', 4, 'TAT_CA', 'Khu phòng yên tĩnh dành cho học viên cao học');

INSERT INTO LOAI_PHONG (MaLoaiPhong, TenLoaiPhong, SucChuaTieuChuan, DonGiaThang, MoTa) VALUES
('LP-4', 'Phòng Tiêu Chuẩn 4 Giường', 4, 600000, 'Máy lạnh, quạt trần, bàn học, WC khép kín'),
('LP-6', 'Phòng Tiêu Chuẩn 6 Giường', 6, 450000, 'Quạt trần, tủ đồ cá nhân, WC khép kín'),
('LP-2', 'Phòng VIP 2 Giường', 2, 1200000, 'Đầy đủ nội thất, tủ lạnh, máy lạnh, ban công');

INSERT INTO PHONG (MaPhong, MaToaNha, MaLoaiPhong, SoPhong, Tang, SucChua, DangO, GiaPhongCoBan, TrangThai) VALUES
('A-101', 'TOA-A', 'LP-4', '101', 1, 4, 2, 600000, 'CON_CHO'),
('A-102', 'TOA-A', 'LP-4', '102', 1, 4, 2, 600000, 'CON_CHO'),
('A-103', 'TOA-A', 'LP-4', '103', 1, 4, 2, 600000, 'CON_CHO'),
('A-201', 'TOA-A', 'LP-4', '201', 2, 4, 3, 600000, 'CON_CHO'),
('A-202', 'TOA-A', 'LP-4', '202', 2, 4, 4, 600000, 'DAY'),
('A-203', 'TOA-A', 'LP-4', '203', 2, 4, 2, 600000, 'CON_CHO'),
('A-301', 'TOA-A', 'LP-4', '301', 3, 4, 4, 600000, 'DAY'),
('A-302', 'TOA-A', 'LP-4', '302', 3, 4, 1, 600000, 'CON_CHO'),
('B-101', 'TOA-B', 'LP-6', '101', 1, 6, 1, 450000, 'CON_CHO'),
('B-102', 'TOA-B', 'LP-6', '102', 1, 6, 3, 450000, 'CON_CHO'),
('B-103', 'TOA-B', 'LP-6', '103', 1, 6, 5, 450000, 'CON_CHO'),
('B-201', 'TOA-B', 'LP-6', '201', 2, 6, 1, 450000, 'CON_CHO'),
('B-202', 'TOA-B', 'LP-6', '202', 2, 6, 2, 450000, 'CON_CHO'),
('B-203', 'TOA-B', 'LP-6', '203', 2, 6, 3, 450000, 'CON_CHO'),
('C-101', 'TOA-C', 'LP-2', '101', 1, 2, 1, 1200000, 'CON_CHO'),
('C-102', 'TOA-C', 'LP-2', '102', 1, 2, 2, 1200000, 'DAY'),
('C-201', 'TOA-C', 'LP-2', '201', 2, 2, 2, 1200000, 'DAY'),
('C-202', 'TOA-C', 'LP-2', '202', 2, 2, 1, 1200000, 'CON_CHO'),
('D-101', 'TOA-D', 'LP-4', '101', 1, 4, 3, 750000, 'CON_CHO'),
('D-102', 'TOA-D', 'LP-4', '102', 1, 4, 2, 750000, 'CON_CHO');

INSERT INTO GIUONG (MaGiuong, MaPhong, SoGiuong, ViTri, TrangThai) VALUES
('G-A-101-1', 'A-101', 'G-01', 'Tầng 2', 'DANG_O'),
('G-A-101-2', 'A-101', 'G-02', 'Tầng 1', 'DANG_O'),
('G-A-101-3', 'A-101', 'G-03', 'Tầng 2', 'GIU_CHO'),
('G-A-101-4', 'A-101', 'G-04', 'Tầng 1', 'TRONG'),
('G-A-102-1', 'A-102', 'G-01', 'Tầng 2', 'DANG_O'),
('G-A-102-2', 'A-102', 'G-02', 'Tầng 1', 'DANG_O'),
('G-A-102-3', 'A-102', 'G-03', 'Tầng 2', 'TRONG'),
('G-A-102-4', 'A-102', 'G-04', 'Tầng 1', 'TRONG'),
('G-A-103-1', 'A-103', 'G-01', 'Tầng 2', 'DANG_O'),
('G-A-103-2', 'A-103', 'G-02', 'Tầng 1', 'DANG_O'),
('G-A-103-3', 'A-103', 'G-03', 'Tầng 2', 'TRONG'),
('G-A-103-4', 'A-103', 'G-04', 'Tầng 1', 'TRONG'),
('G-A-201-1', 'A-201', 'G-01', 'Tầng 2', 'DANG_O'),
('G-A-201-2', 'A-201', 'G-02', 'Tầng 1', 'DANG_O'),
('G-A-201-3', 'A-201', 'G-03', 'Tầng 2', 'DANG_O'),
('G-A-201-4', 'A-201', 'G-04', 'Tầng 1', 'TRONG'),
('G-A-202-1', 'A-202', 'G-01', 'Tầng 2', 'DANG_O'),
('G-A-202-2', 'A-202', 'G-02', 'Tầng 1', 'DANG_O'),
('G-A-202-3', 'A-202', 'G-03', 'Tầng 2', 'DANG_O'),
('G-A-202-4', 'A-202', 'G-04', 'Tầng 1', 'DANG_O'),
('G-A-203-1', 'A-203', 'G-01', 'Tầng 2', 'DANG_O'),
('G-A-203-2', 'A-203', 'G-02', 'Tầng 1', 'DANG_O'),
('G-A-203-3', 'A-203', 'G-03', 'Tầng 2', 'TRONG'),
('G-A-203-4', 'A-203', 'G-04', 'Tầng 1', 'TRONG'),
('G-A-301-1', 'A-301', 'G-01', 'Tầng 2', 'DANG_O'),
('G-A-301-2', 'A-301', 'G-02', 'Tầng 1', 'DANG_O'),
('G-A-301-3', 'A-301', 'G-03', 'Tầng 2', 'DANG_O'),
('G-A-301-4', 'A-301', 'G-04', 'Tầng 1', 'DANG_O'),
('G-A-302-1', 'A-302', 'G-01', 'Tầng 2', 'DANG_O'),
('G-A-302-2', 'A-302', 'G-02', 'Tầng 1', 'TRONG'),
('G-A-302-3', 'A-302', 'G-03', 'Tầng 2', 'TRONG'),
('G-A-302-4', 'A-302', 'G-04', 'Tầng 1', 'TRONG'),
('G-B-101-1', 'B-101', 'G-01', 'Tầng 2', 'DANG_O'),
('G-B-101-2', 'B-101', 'G-02', 'Tầng 1', 'TRONG'),
('G-B-101-3', 'B-101', 'G-03', 'Tầng 2', 'TRONG'),
('G-B-101-4', 'B-101', 'G-04', 'Tầng 1', 'TRONG'),
('G-B-101-5', 'B-101', 'G-05', 'Tầng 2', 'TRONG'),
('G-B-101-6', 'B-101', 'G-06', 'Tầng 1', 'TRONG'),
('G-B-102-1', 'B-102', 'G-01', 'Tầng 2', 'DANG_O'),
('G-B-102-2', 'B-102', 'G-02', 'Tầng 1', 'DANG_O'),
('G-B-102-3', 'B-102', 'G-03', 'Tầng 2', 'DANG_O'),
('G-B-102-4', 'B-102', 'G-04', 'Tầng 1', 'TRONG'),
('G-B-102-5', 'B-102', 'G-05', 'Tầng 2', 'TRONG'),
('G-B-102-6', 'B-102', 'G-06', 'Tầng 1', 'TRONG'),
('G-B-103-1', 'B-103', 'G-01', 'Tầng 2', 'DANG_O'),
('G-B-103-2', 'B-103', 'G-02', 'Tầng 1', 'DANG_O'),
('G-B-103-3', 'B-103', 'G-03', 'Tầng 2', 'DANG_O'),
('G-B-103-4', 'B-103', 'G-04', 'Tầng 1', 'DANG_O'),
('G-B-103-5', 'B-103', 'G-05', 'Tầng 2', 'DANG_O'),
('G-B-103-6', 'B-103', 'G-06', 'Tầng 1', 'GIU_CHO'),
('G-B-201-1', 'B-201', 'G-01', 'Tầng 2', 'DANG_O'),
('G-B-201-2', 'B-201', 'G-02', 'Tầng 1', 'GIU_CHO'),
('G-B-201-3', 'B-201', 'G-03', 'Tầng 2', 'TRONG'),
('G-B-201-4', 'B-201', 'G-04', 'Tầng 1', 'TRONG'),
('G-B-201-5', 'B-201', 'G-05', 'Tầng 2', 'TRONG'),
('G-B-201-6', 'B-201', 'G-06', 'Tầng 1', 'TRONG'),
('G-B-202-1', 'B-202', 'G-01', 'Tầng 2', 'DANG_O'),
('G-B-202-2', 'B-202', 'G-02', 'Tầng 1', 'DANG_O'),
('G-B-202-3', 'B-202', 'G-03', 'Tầng 2', 'TRONG'),
('G-B-202-4', 'B-202', 'G-04', 'Tầng 1', 'TRONG'),
('G-B-202-5', 'B-202', 'G-05', 'Tầng 2', 'TRONG'),
('G-B-202-6', 'B-202', 'G-06', 'Tầng 1', 'TRONG'),
('G-B-203-1', 'B-203', 'G-01', 'Tầng 2', 'DANG_O'),
('G-B-203-2', 'B-203', 'G-02', 'Tầng 1', 'DANG_O'),
('G-B-203-3', 'B-203', 'G-03', 'Tầng 2', 'DANG_O'),
('G-B-203-4', 'B-203', 'G-04', 'Tầng 1', 'GIU_CHO'),
('G-B-203-5', 'B-203', 'G-05', 'Tầng 2', 'TRONG'),
('G-B-203-6', 'B-203', 'G-06', 'Tầng 1', 'TRONG'),
('G-C-101-1', 'C-101', 'G-01', 'Tầng 2', 'DANG_O'),
('G-C-101-2', 'C-101', 'G-02', 'Tầng 1', 'GIU_CHO'),
('G-C-102-1', 'C-102', 'G-01', 'Tầng 2', 'DANG_O'),
('G-C-102-2', 'C-102', 'G-02', 'Tầng 1', 'DANG_O'),
('G-C-201-1', 'C-201', 'G-01', 'Tầng 2', 'DANG_O'),
('G-C-201-2', 'C-201', 'G-02', 'Tầng 1', 'DANG_O'),
('G-C-202-1', 'C-202', 'G-01', 'Tầng 2', 'DANG_O'),
('G-C-202-2', 'C-202', 'G-02', 'Tầng 1', 'GIU_CHO'),
('G-D-101-1', 'D-101', 'G-01', 'Tầng 2', 'DANG_O'),
('G-D-101-2', 'D-101', 'G-02', 'Tầng 1', 'DANG_O'),
('G-D-101-3', 'D-101', 'G-03', 'Tầng 2', 'DANG_O'),
('G-D-101-4', 'D-101', 'G-04', 'Tầng 1', 'GIU_CHO'),
('G-D-102-1', 'D-102', 'G-01', 'Tầng 2', 'DANG_O'),
('G-D-102-2', 'D-102', 'G-02', 'Tầng 1', 'DANG_O'),
('G-D-102-3', 'D-102', 'G-03', 'Tầng 2', 'TRONG'),
('G-D-102-4', 'D-102', 'G-04', 'Tầng 1', 'TRONG');

INSERT INTO SINH_VIEN (MaSinhVien, MaTaiKhoan, HoTen, NgaySinh, GioiTinh, CCCD, Email, SoDienThoai, Lop, Khoa, QueQuan, MaPhongHienTai, TrangThai) VALUES
('21110001', 5, 'Trần Hữu Đạt', '2002-07-10', 'NAM', '079203841739', 'sv001@student.ktx.edu.vn', '0949714664', '21110B', 'Công Nghệ Phần Mềm', 'Đồng Nai', 'A-102', 'DANG_O'),
('21110002', 6, 'Lê Thu Linh', '2005-08-08', 'NU', '079200630787', 'sv002@student.ktx.edu.vn', '0926456681', '21110C', 'Hệ Thống Thông Tin', 'Bình Dương', 'B-103', 'DANG_O'),
('21110003', 7, 'Phạm Hoàng Huy', '2003-08-15', 'NAM', '079201903897', 'sv003@student.ktx.edu.vn', '0964728935', '21110D', 'Khoa Học Máy Tính', 'Bà Rịa - Vũng Tàu', 'A-101', 'DANG_O'),
('21110004', 8, 'Hoàng Hồng Mai', '2005-11-03', 'NU', '079200547500', 'sv004@student.ktx.edu.vn', '0940391159', '21110E', 'Kỹ Thuật Máy Tính', 'Long An', 'B-102', 'DANG_O'),
('21110005', 9, 'Huỳnh Quốc Nam', '2003-12-27', 'NAM', '079207679630', 'sv005@student.ktx.edu.vn', '0994462375', '21110A', 'An Toàn Thông Tin', 'Tiền Giang', 'A-103', 'DANG_O'),
('21110006', 10, 'Phan Phương Hương', '2003-11-12', 'NU', '079204213482', 'sv006@student.ktx.edu.vn', '0963804153', '21110B', 'Công Nghệ Thông Tin', 'Cần Thơ', 'B-101', 'DANG_O'),
('21110007', 11, 'Vũ Thành Bảo', '2002-03-19', 'NAM', '079205079173', 'sv007@student.ktx.edu.vn', '0910961884', '21110C', 'Công Nghệ Phần Mềm', 'Lâm Đồng', 'A-102', 'DANG_O'),
('21110008', 12, 'Võ Bảo Hà', '2004-11-10', 'NU', '079204195343', 'sv008@student.ktx.edu.vn', '0925064835', '21110D', 'Hệ Thống Thông Tin', 'Bình Thuận', 'B-103', 'DANG_O'),
('21110009', 13, 'Đặng Công Dũng', '2003-01-01', 'NAM', '079200057836', 'sv009@student.ktx.edu.vn', '0996968607', '21110E', 'Khoa Học Máy Tính', 'Khánh Hòa', 'A-101', 'DANG_O'),
('21110010', 14, 'Bùi Thị Trâm', '2005-11-20', 'NU', '079203692225', 'sv010@student.ktx.edu.vn', '0947223374', '21110A', 'Kỹ Thuật Máy Tính', 'Đà Nẵng', 'B-102', 'DANG_O'),
('21110011', 15, 'Đỗ Hữu Phong', '2005-03-18', 'NAM', '079204015714', 'sv011@student.ktx.edu.vn', '0944365948', '21110B', 'An Toàn Thông Tin', 'Bến Tre', 'A-103', 'DANG_O'),
('21110012', 16, 'Hồ Thu Quyên', '2005-07-10', 'NU', '079203288220', 'sv012@student.ktx.edu.vn', '0959892300', '21110C', 'Công Nghệ Thông Tin', 'TP. Hồ Chí Minh', 'B-101', 'DANG_O'),
('21110013', 17, 'Ngô Hoàng Khôi', '2005-07-10', 'NAM', '079208348350', 'sv013@student.ktx.edu.vn', '0928692061', '21110D', 'Công Nghệ Phần Mềm', 'Đồng Nai', 'A-102', 'DANG_O'),
('21110014', 18, 'Dương Hồng Yến', '2002-05-27', 'NU', '079204135456', 'sv014@student.ktx.edu.vn', '0984104833', '21110E', 'Hệ Thống Thông Tin', 'Bình Dương', 'B-103', 'DANG_O'),
('21110015', 19, 'Nguyễn Quốc Phúc', '2005-09-23', 'NAM', '079205408687', 'sv015@student.ktx.edu.vn', '0926707386', '21110A', 'Khoa Học Máy Tính', 'Bà Rịa - Vũng Tàu', 'A-101', 'DANG_O'),
('21110016', 20, 'Trần Phương Trang', '2004-01-15', 'NU', '079205331899', 'sv016@student.ktx.edu.vn', '0965386525', '21110B', 'Kỹ Thuật Máy Tính', 'Long An', 'B-102', 'DANG_O'),
('21110017', 21, 'Lê Thành Khoa', '2002-07-23', 'NAM', '079209639258', 'sv017@student.ktx.edu.vn', '0981533791', '21110C', 'An Toàn Thông Tin', 'Tiền Giang', 'A-103', 'DANG_O'),
('21110018', 22, 'Phạm Bảo Lan', '2004-11-18', 'NU', '079207725268', 'sv018@student.ktx.edu.vn', '0933522951', '21110D', 'Công Nghệ Thông Tin', 'Cần Thơ', 'B-101', 'DANG_O'),
('21110019', 23, 'Hoàng Công Tùng', '2002-02-05', 'NAM', '079204935428', 'sv019@student.ktx.edu.vn', '0940348655', '21110E', 'Công Nghệ Phần Mềm', 'Lâm Đồng', 'A-102', 'DANG_O'),
('21110020', 24, 'Huỳnh Thị Thảo', '2005-01-03', 'NU', '079202990224', 'sv020@student.ktx.edu.vn', '0981340059', '21110A', 'Hệ Thống Thông Tin', 'Bình Thuận', 'B-103', 'DANG_O'),
('21110021', 25, 'Phan Hữu Việt', '2003-11-20', 'NAM', '079200528781', 'sv021@student.ktx.edu.vn', '0984323679', '21110B', 'Khoa Học Máy Tính', 'Khánh Hòa', 'A-101', 'DANG_O'),
('21110022', 26, 'Vũ Thu Nhi', '2004-02-20', 'NU', '079201215286', 'sv022@student.ktx.edu.vn', '0939464434', '21110C', 'Kỹ Thuật Máy Tính', 'Đà Nẵng', 'B-102', 'DANG_O'),
('21110023', 27, 'Võ Hoàng Long', '2002-10-02', 'NAM', '079209449674', 'sv023@student.ktx.edu.vn', '0972740255', '21110D', 'An Toàn Thông Tin', 'Bến Tre', 'A-103', 'DANG_O'),
('21110024', 28, 'Đặng Hồng Vy', '2004-02-12', 'NU', '079206900147', 'sv024@student.ktx.edu.vn', '0938908262', '21110E', 'Công Nghệ Thông Tin', 'TP. Hồ Chí Minh', 'B-101', 'DANG_O'),
('21110025', 29, 'Bùi Quốc Hải', '2004-09-01', 'NAM', '079206584322', 'sv025@student.ktx.edu.vn', '0937914738', '21110A', 'Công Nghệ Phần Mềm', 'Đồng Nai', 'A-102', 'DANG_O'),
('21110026', 30, 'Đỗ Phương Hạnh', '2002-09-08', 'NU', '079200792215', 'sv026@student.ktx.edu.vn', '0996923685', '21110B', 'Hệ Thống Thông Tin', 'Bình Dương', 'B-103', 'DANG_O'),
('21110027', 31, 'Hồ Thành Quân', '2004-07-03', 'NAM', '079209411155', 'sv027@student.ktx.edu.vn', '0946471669', '21110C', 'Khoa Học Máy Tính', 'Bà Rịa - Vũng Tàu', 'A-101', 'DANG_O'),
('21110028', 32, 'Ngô Bảo Tú', '2005-01-10', 'NU', '079208878640', 'sv028@student.ktx.edu.vn', '0963416568', '21110D', 'Kỹ Thuật Máy Tính', 'Long An', 'B-102', 'DANG_O'),
('21110029', 33, 'Dương Công Thịnh', '2002-09-03', 'NAM', '079203128088', 'sv029@student.ktx.edu.vn', '0970722586', '21110E', 'An Toàn Thông Tin', 'Tiền Giang', 'A-103', 'DANG_O'),
('21110030', 34, 'Nguyễn Thị Hoa', '2004-07-23', 'NU', '079208021734', 'sv030@student.ktx.edu.vn', '0943374197', '21110A', 'Công Nghệ Thông Tin', 'Cần Thơ', 'B-101', 'DANG_O'),
('21110031', 35, 'Trần Hữu Đạt', '2005-08-15', 'NAM', '079201350704', 'sv031@student.ktx.edu.vn', '0955694237', '21110B', 'Công Nghệ Phần Mềm', 'Lâm Đồng', 'A-102', 'DANG_O'),
('21110032', 36, 'Lê Thu Linh', '2003-05-23', 'NU', '079204161861', 'sv032@student.ktx.edu.vn', '0961855246', '21110C', 'Hệ Thống Thông Tin', 'Bình Thuận', 'B-103', 'DANG_O'),
('21110033', 37, 'Phạm Hoàng Huy', '2002-09-04', 'NAM', '079206419800', 'sv033@student.ktx.edu.vn', '0998677250', '21110D', 'Khoa Học Máy Tính', 'Khánh Hòa', 'A-101', 'DANG_O'),
('21110034', 38, 'Hoàng Hồng Mai', '2003-02-15', 'NU', '079200191975', 'sv034@student.ktx.edu.vn', '0962228824', '21110E', 'Kỹ Thuật Máy Tính', 'Đà Nẵng', 'B-102', 'DANG_O'),
('21110035', 39, 'Huỳnh Quốc Nam', '2004-06-12', 'NAM', '079200568220', 'sv035@student.ktx.edu.vn', '0969949385', '21110A', 'An Toàn Thông Tin', 'Bến Tre', 'A-103', 'DANG_O'),
('21110036', 40, 'Phan Phương Hương', '2002-05-03', 'NU', '079203038850', 'sv036@student.ktx.edu.vn', '0913172692', '21110B', 'Công Nghệ Thông Tin', 'TP. Hồ Chí Minh', 'B-101', 'DANG_O'),
('21110037', 41, 'Vũ Thành Bảo', '2005-07-22', 'NAM', '079202483194', 'sv037@student.ktx.edu.vn', '0952734917', '21110C', 'Công Nghệ Phần Mềm', 'Đồng Nai', 'A-102', 'DANG_O'),
('21110038', 42, 'Võ Bảo Hà', '2004-07-06', 'NU', '079203146314', 'sv038@student.ktx.edu.vn', '0983197542', '21110D', 'Hệ Thống Thông Tin', 'Bình Dương', 'B-103', 'DANG_O'),
('21110039', 43, 'Đặng Công Dũng', '2002-06-10', 'NAM', '079203707795', 'sv039@student.ktx.edu.vn', '0980121774', '21110E', 'Khoa Học Máy Tính', 'Bà Rịa - Vũng Tàu', 'A-101', 'DANG_O'),
('21110040', 44, 'Bùi Thị Trâm', '2005-07-14', 'NU', '079201472394', 'sv040@student.ktx.edu.vn', '0927506013', '21110A', 'Kỹ Thuật Máy Tính', 'Long An', 'B-102', 'DANG_O'),
('21110041', 45, 'Đỗ Hữu Phong', '2003-12-19', 'NAM', '079200207302', 'sv041@student.ktx.edu.vn', '0967025445', '21110B', 'An Toàn Thông Tin', 'Tiền Giang', NULL, 'CHO_DUYET'),
('21110042', 46, 'Hồ Thu Quyên', '2005-11-19', 'NU', '079200786284', 'sv042@student.ktx.edu.vn', '0971564503', '21110C', 'Công Nghệ Thông Tin', 'Cần Thơ', NULL, 'CHO_DUYET'),
('21110043', 47, 'Ngô Hoàng Khôi', '2002-07-02', 'NAM', '079208573077', 'sv043@student.ktx.edu.vn', '0969927692', '21110D', 'Công Nghệ Phần Mềm', 'Lâm Đồng', NULL, 'CHO_DUYET'),
('21110044', 48, 'Dương Hồng Yến', '2005-04-08', 'NU', '079209835685', 'sv044@student.ktx.edu.vn', '0924602596', '21110E', 'Hệ Thống Thông Tin', 'Bình Thuận', NULL, 'CHO_DUYET'),
('21110045', 49, 'Nguyễn Quốc Phúc', '2004-01-17', 'NAM', '079208493764', 'sv045@student.ktx.edu.vn', '0975338304', '21110A', 'Khoa Học Máy Tính', 'Khánh Hòa', NULL, 'CHO_DUYET'),
('21110046', 50, 'Trần Phương Trang', '2002-10-03', 'NU', '079200824930', 'sv046@student.ktx.edu.vn', '0997439033', '21110B', 'Kỹ Thuật Máy Tính', 'Đà Nẵng', NULL, 'CHO_DUYET'),
('21110047', 51, 'Lê Thành Khoa', '2002-09-08', 'NAM', '079208549259', 'sv047@student.ktx.edu.vn', '0961209949', '21110C', 'An Toàn Thông Tin', 'Bến Tre', NULL, 'CHO_DUYET'),
('21110048', 52, 'Phạm Bảo Lan', '2003-11-08', 'NU', '079208046122', 'sv048@student.ktx.edu.vn', '0979859308', '21110D', 'Công Nghệ Thông Tin', 'TP. Hồ Chí Minh', NULL, 'CHO_DUYET'),
('21110049', 53, 'Hoàng Công Tùng', '2005-08-26', 'NAM', '079202331517', 'sv049@student.ktx.edu.vn', '0986131391', '21110E', 'Công Nghệ Phần Mềm', 'Đồng Nai', NULL, 'CHO_DUYET'),
('21110050', 54, 'Huỳnh Thị Thảo', '2002-05-24', 'NU', '079202321545', 'sv050@student.ktx.edu.vn', '0942117940', '21110A', 'Hệ Thống Thông Tin', 'Bình Dương', NULL, 'CHO_DUYET'),
('21110051', 55, 'Phan Hữu Việt', '2002-06-05', 'NAM', '079201268038', 'sv051@student.ktx.edu.vn', '0982547538', '21110B', 'Khoa Học Máy Tính', 'Bà Rịa - Vũng Tàu', NULL, 'CHO_DUYET'),
('21110052', 56, 'Vũ Thu Nhi', '2003-06-16', 'NU', '079205735006', 'sv052@student.ktx.edu.vn', '0948919603', '21110C', 'Kỹ Thuật Máy Tính', 'Long An', NULL, 'CHO_DUYET'),
('21110053', 57, 'Võ Hoàng Long', '2003-02-10', 'NAM', '079204911719', 'sv053@student.ktx.edu.vn', '0934565575', '21110D', 'An Toàn Thông Tin', 'Tiền Giang', NULL, 'CHO_DUYET'),
('21110054', 58, 'Đặng Hồng Vy', '2003-06-15', 'NU', '079207600554', 'sv054@student.ktx.edu.vn', '0990233835', '21110E', 'Công Nghệ Thông Tin', 'Cần Thơ', NULL, 'CHO_DUYET'),
('21110055', 59, 'Bùi Quốc Hải', '2005-06-04', 'NAM', '079208200748', 'sv055@student.ktx.edu.vn', '0925745103', '21110A', 'Công Nghệ Phần Mềm', 'Lâm Đồng', NULL, 'CHO_DUYET');

INSERT INTO DON_GIA_DICH_VU (MaDonGia, LoaiDichVu, TenDichVu, DonViTinh, DonGia, NgayApDung, TrangThai) VALUES
('DG-DIEN', 'DIEN', 'Điện sinh hoạt KTX', 'kWh', 3500, '2026-01-01', 'ACTIVE'),
('DG-NUOC', 'NUOC', 'Nước sinh hoạt', 'm3', 15000, '2026-01-01', 'ACTIVE'),
('DG-NET', 'INTERNET', 'Mạng Internet tốc độ cao', 'Phòng/Tháng', 100000, '2026-01-01', 'ACTIVE'),
('DG-XE-MAY', 'GUI_XE', 'Phí gửi xe máy', 'Xe/Tháng', 80000, '2026-01-01', 'ACTIVE'),
('DG-RAC', 'KHAC', 'Vệ sinh môi trường', 'Phòng/Tháng', 50000, '2026-01-01', 'ACTIVE');

INSERT INTO CHI_SO_DIEN_NUOC (MaPhong, KyThang, DienCu, DienMoi, NuocCu, NuocMoi, NgayGhi, NguoiGhi) VALUES
('A-101', '2026-08', 182, 234, 24, 33, NOW(), 'kythuat1'),
('A-102', '2026-08', 124, 185, 50, 59, NOW(), 'kythuat1'),
('A-103', '2026-08', 184, 243, 41, 50, NOW(), 'kythuat1'),
('A-201', '2026-08', 165, 232, 42, 52, NOW(), 'kythuat1'),
('A-202', '2026-08', 187, 242, 39, 48, NOW(), 'kythuat1'),
('A-203', '2026-08', 196, 272, 22, 41, NOW(), 'kythuat1'),
('A-301', '2026-08', 146, 198, 35, 49, NOW(), 'kythuat1'),
('A-302', '2026-08', 172, 228, 45, 60, NOW(), 'kythuat1'),
('B-101', '2026-08', 189, 276, 24, 36, NOW(), 'kythuat1'),
('B-102', '2026-08', 194, 246, 32, 52, NOW(), 'kythuat1'),
('B-103', '2026-08', 117, 196, 35, 45, NOW(), 'kythuat1'),
('B-201', '2026-08', 160, 216, 44, 55, NOW(), 'kythuat1'),
('B-202', '2026-08', 123, 189, 37, 46, NOW(), 'kythuat1'),
('B-203', '2026-08', 115, 204, 37, 57, NOW(), 'kythuat1'),
('C-101', '2026-08', 172, 213, 22, 42, NOW(), 'kythuat1'),
('C-102', '2026-08', 102, 182, 40, 52, NOW(), 'kythuat1'),
('C-201', '2026-08', 101, 148, 42, 62, NOW(), 'kythuat1'),
('C-202', '2026-08', 132, 180, 27, 42, NOW(), 'kythuat1'),
('D-101', '2026-08', 143, 207, 43, 55, NOW(), 'kythuat1'),
('D-102', '2026-08', 100, 161, 35, 53, NOW(), 'kythuat1'),
('A-101', '2026-09', 193, 280, 49, 58, NOW(), 'kythuat1'),
('A-102', '2026-09', 147, 234, 33, 46, NOW(), 'kythuat1'),
('A-103', '2026-09', 114, 192, 39, 59, NOW(), 'kythuat1'),
('A-201', '2026-09', 176, 242, 35, 51, NOW(), 'kythuat1'),
('A-202', '2026-09', 129, 192, 48, 68, NOW(), 'kythuat1'),
('A-203', '2026-09', 159, 236, 44, 54, NOW(), 'kythuat1'),
('A-301', '2026-09', 179, 252, 44, 58, NOW(), 'kythuat1'),
('A-302', '2026-09', 182, 251, 45, 58, NOW(), 'kythuat1'),
('B-101', '2026-09', 114, 169, 21, 37, NOW(), 'kythuat1'),
('B-102', '2026-09', 103, 146, 34, 54, NOW(), 'kythuat1'),
('B-103', '2026-09', 162, 214, 24, 40, NOW(), 'kythuat1'),
('B-201', '2026-09', 114, 198, 41, 51, NOW(), 'kythuat1'),
('B-202', '2026-09', 185, 229, 31, 43, NOW(), 'kythuat1'),
('B-203', '2026-09', 100, 145, 23, 42, NOW(), 'kythuat1'),
('C-101', '2026-09', 143, 201, 42, 54, NOW(), 'kythuat1'),
('C-102', '2026-09', 186, 257, 24, 32, NOW(), 'kythuat1'),
('C-201', '2026-09', 116, 200, 25, 42, NOW(), 'kythuat1'),
('C-202', '2026-09', 200, 253, 24, 41, NOW(), 'kythuat1'),
('D-101', '2026-09', 119, 193, 37, 54, NOW(), 'kythuat1'),
('D-102', '2026-09', 178, 266, 45, 54, NOW(), 'kythuat1'),
('A-101', '2026-10', 158, 245, 34, 51, NOW(), 'kythuat1'),
('A-102', '2026-10', 150, 208, 40, 60, NOW(), 'kythuat1'),
('A-103', '2026-10', 168, 210, 31, 45, NOW(), 'kythuat1'),
('A-201', '2026-10', 162, 227, 30, 49, NOW(), 'kythuat1'),
('A-202', '2026-10', 167, 227, 46, 54, NOW(), 'kythuat1'),
('A-203', '2026-10', 143, 231, 26, 38, NOW(), 'kythuat1'),
('A-301', '2026-10', 144, 208, 46, 55, NOW(), 'kythuat1'),
('A-302', '2026-10', 149, 208, 45, 55, NOW(), 'kythuat1'),
('B-101', '2026-10', 120, 166, 36, 46, NOW(), 'kythuat1'),
('B-102', '2026-10', 186, 258, 36, 56, NOW(), 'kythuat1'),
('B-103', '2026-10', 107, 171, 42, 61, NOW(), 'kythuat1'),
('B-201', '2026-10', 169, 216, 41, 56, NOW(), 'kythuat1'),
('B-202', '2026-10', 165, 236, 37, 54, NOW(), 'kythuat1'),
('B-203', '2026-10', 170, 220, 44, 52, NOW(), 'kythuat1'),
('C-101', '2026-10', 129, 188, 31, 48, NOW(), 'kythuat1'),
('C-102', '2026-10', 131, 176, 22, 31, NOW(), 'kythuat1'),
('C-201', '2026-10', 162, 204, 26, 45, NOW(), 'kythuat1'),
('C-202', '2026-10', 134, 198, 37, 54, NOW(), 'kythuat1'),
('D-101', '2026-10', 194, 255, 35, 52, NOW(), 'kythuat1'),
('D-102', '2026-10', 197, 252, 20, 33, NOW(), 'kythuat1');

INSERT INTO HOA_DON (MaHoaDon, MaPhong, KyThang, TienPhong, TienDien, TienNuoc, TienDichVuKhac, GiamGia, TongTien, DaThanhToan, HanThanhToan, TrangThai, NgayPhatHanh) VALUES
(1, 'A-101', '2026-08', 600000, 182000, 135000, 0, 0, 917000, 917000, '2026-08-25', 'DA_TT', NOW()),
(2, 'A-102', '2026-08', 600000, 213500, 135000, 0, 0, 948500, 948500, '2026-08-25', 'DA_TT', NOW()),
(3, 'A-103', '2026-08', 600000, 206500, 135000, 0, 0, 941500, 941500, '2026-08-25', 'DA_TT', NOW()),
(4, 'A-201', '2026-08', 600000, 234500, 150000, 0, 0, 984500, 984500, '2026-08-25', 'DA_TT', NOW()),
(5, 'A-202', '2026-08', 600000, 192500, 135000, 0, 0, 927500, 927500, '2026-08-25', 'DA_TT', NOW()),
(6, 'A-203', '2026-08', 600000, 266000, 285000, 0, 0, 1151000, 1151000, '2026-08-25', 'DA_TT', NOW()),
(7, 'A-301', '2026-08', 600000, 182000, 210000, 0, 0, 992000, 992000, '2026-08-25', 'DA_TT', NOW()),
(8, 'A-302', '2026-08', 600000, 196000, 225000, 0, 0, 1021000, 1021000, '2026-08-25', 'DA_TT', NOW()),
(9, 'B-101', '2026-08', 450000, 304500, 180000, 0, 0, 934500, 934500, '2026-08-25', 'DA_TT', NOW()),
(10, 'B-102', '2026-08', 450000, 182000, 300000, 0, 0, 932000, 932000, '2026-08-25', 'DA_TT', NOW()),
(11, 'B-103', '2026-08', 450000, 276500, 150000, 0, 0, 876500, 876500, '2026-08-25', 'DA_TT', NOW()),
(12, 'B-201', '2026-08', 450000, 196000, 165000, 0, 0, 811000, 811000, '2026-08-25', 'DA_TT', NOW()),
(13, 'B-202', '2026-08', 450000, 231000, 135000, 0, 0, 816000, 816000, '2026-08-25', 'DA_TT', NOW()),
(14, 'B-203', '2026-08', 450000, 311500, 300000, 0, 0, 1061500, 1061500, '2026-08-25', 'DA_TT', NOW()),
(15, 'C-101', '2026-08', 1200000, 143500, 300000, 0, 0, 1643500, 1643500, '2026-08-25', 'DA_TT', NOW()),
(16, 'C-102', '2026-08', 1200000, 280000, 180000, 0, 0, 1660000, 1660000, '2026-08-25', 'DA_TT', NOW()),
(17, 'C-201', '2026-08', 1200000, 164500, 300000, 0, 0, 1664500, 1664500, '2026-08-25', 'DA_TT', NOW()),
(18, 'C-202', '2026-08', 1200000, 168000, 225000, 0, 0, 1593000, 1593000, '2026-08-25', 'DA_TT', NOW()),
(19, 'D-101', '2026-08', 1200000, 224000, 180000, 0, 0, 1604000, 1604000, '2026-08-25', 'DA_TT', NOW()),
(20, 'D-102', '2026-08', 1200000, 213500, 270000, 0, 0, 1683500, 1683500, '2026-08-25', 'DA_TT', NOW()),
(21, 'A-101', '2026-09', 600000, 304500, 135000, 0, 0, 1039500, 1039500, '2026-09-25', 'DA_TT', NOW()),
(22, 'A-102', '2026-09', 600000, 304500, 195000, 0, 0, 1099500, 1099500, '2026-09-25', 'DA_TT', NOW()),
(23, 'A-103', '2026-09', 600000, 273000, 300000, 0, 0, 1173000, 1173000, '2026-09-25', 'DA_TT', NOW()),
(24, 'A-201', '2026-09', 600000, 231000, 240000, 0, 0, 1071000, 1071000, '2026-09-25', 'DA_TT', NOW()),
(25, 'A-202', '2026-09', 600000, 220500, 300000, 0, 0, 1120500, 1120500, '2026-09-25', 'DA_TT', NOW()),
(26, 'A-203', '2026-09', 600000, 269500, 150000, 0, 0, 1019500, 1019500, '2026-09-25', 'DA_TT', NOW()),
(27, 'A-301', '2026-09', 600000, 255500, 210000, 0, 0, 1065500, 1065500, '2026-09-25', 'DA_TT', NOW()),
(28, 'A-302', '2026-09', 600000, 241500, 195000, 0, 0, 1036500, 1036500, '2026-09-25', 'DA_TT', NOW()),
(29, 'B-101', '2026-09', 450000, 192500, 240000, 0, 0, 882500, 882500, '2026-09-25', 'DA_TT', NOW()),
(30, 'B-102', '2026-09', 450000, 150500, 300000, 0, 0, 900500, 900500, '2026-09-25', 'DA_TT', NOW()),
(31, 'B-103', '2026-09', 450000, 182000, 240000, 0, 0, 872000, 872000, '2026-09-25', 'DA_TT', NOW()),
(32, 'B-201', '2026-09', 450000, 294000, 150000, 0, 0, 894000, 894000, '2026-09-25', 'DA_TT', NOW()),
(33, 'B-202', '2026-09', 450000, 154000, 180000, 0, 0, 784000, 784000, '2026-09-25', 'DA_TT', NOW()),
(34, 'B-203', '2026-09', 450000, 157500, 285000, 0, 0, 892500, 892500, '2026-09-25', 'DA_TT', NOW()),
(35, 'C-101', '2026-09', 1200000, 203000, 180000, 0, 0, 1583000, 1583000, '2026-09-25', 'DA_TT', NOW()),
(36, 'C-102', '2026-09', 1200000, 248500, 120000, 0, 0, 1568500, 1568500, '2026-09-25', 'DA_TT', NOW()),
(37, 'C-201', '2026-09', 1200000, 294000, 255000, 0, 0, 1749000, 1749000, '2026-09-25', 'DA_TT', NOW()),
(38, 'C-202', '2026-09', 1200000, 185500, 255000, 0, 0, 1640500, 1640500, '2026-09-25', 'DA_TT', NOW()),
(39, 'D-101', '2026-09', 1200000, 259000, 255000, 0, 0, 1714000, 1714000, '2026-09-25', 'DA_TT', NOW()),
(40, 'D-102', '2026-09', 1200000, 308000, 135000, 0, 0, 1643000, 1643000, '2026-09-25', 'DA_TT', NOW()),
(41, 'A-101', '2026-10', 600000, 304500, 255000, 0, 0, 1159500, 0, '2026-10-25', 'CHUA_TT', NOW()),
(42, 'A-102', '2026-10', 600000, 203000, 300000, 0, 0, 1103000, 1103000, '2026-10-25', 'DA_TT', NOW()),
(43, 'A-103', '2026-10', 600000, 147000, 210000, 0, 0, 957000, 478500, '2026-10-25', 'TT_MOT_PHAN', NOW()),
(44, 'A-201', '2026-10', 600000, 227500, 285000, 0, 0, 1112500, 1112500, '2026-10-25', 'DA_TT', NOW()),
(45, 'A-202', '2026-10', 600000, 210000, 120000, 0, 0, 930000, 930000, '2026-10-25', 'DA_TT', NOW()),
(46, 'A-203', '2026-10', 600000, 308000, 180000, 0, 0, 1088000, 544000, '2026-10-25', 'TT_MOT_PHAN', NOW()),
(47, 'A-301', '2026-10', 600000, 224000, 135000, 0, 0, 959000, 479500, '2026-10-25', 'TT_MOT_PHAN', NOW()),
(48, 'A-302', '2026-10', 600000, 206500, 150000, 0, 0, 956500, 956500, '2026-10-25', 'DA_TT', NOW()),
(49, 'B-101', '2026-10', 450000, 161000, 150000, 0, 0, 761000, 0, '2026-10-25', 'CHUA_TT', NOW()),
(50, 'B-102', '2026-10', 450000, 252000, 300000, 0, 0, 1002000, 1002000, '2026-10-25', 'DA_TT', NOW()),
(51, 'B-103', '2026-10', 450000, 224000, 285000, 0, 0, 959000, 959000, '2026-10-25', 'DA_TT', NOW()),
(52, 'B-201', '2026-10', 450000, 164500, 225000, 0, 0, 839500, 419750, '2026-10-25', 'TT_MOT_PHAN', NOW()),
(53, 'B-202', '2026-10', 450000, 248500, 255000, 0, 0, 953500, 476750, '2026-10-25', 'TT_MOT_PHAN', NOW()),
(54, 'B-203', '2026-10', 450000, 175000, 120000, 0, 0, 745000, 0, '2026-10-25', 'CHUA_TT', NOW()),
(55, 'C-101', '2026-10', 1200000, 206500, 255000, 0, 0, 1661500, 0, '2026-10-25', 'CHUA_TT', NOW()),
(56, 'C-102', '2026-10', 1200000, 157500, 135000, 0, 0, 1492500, 1492500, '2026-10-25', 'DA_TT', NOW()),
(57, 'C-201', '2026-10', 1200000, 147000, 285000, 0, 0, 1632000, 1632000, '2026-10-25', 'DA_TT', NOW()),
(58, 'C-202', '2026-10', 1200000, 224000, 255000, 0, 0, 1679000, 1679000, '2026-10-25', 'DA_TT', NOW()),
(59, 'D-101', '2026-10', 1200000, 213500, 255000, 0, 0, 1668500, 834250, '2026-10-25', 'TT_MOT_PHAN', NOW()),
(60, 'D-102', '2026-10', 1200000, 192500, 195000, 0, 0, 1587500, 1587500, '2026-10-25', 'DA_TT', NOW());

INSERT INTO CHI_TIET_HOA_DON (MaChiTiet, MaHoaDon, MaDonGia, TenMuc, SoLuong, DonGia, ThanhTien) VALUES
(1, 1, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(2, 1, 'DG-DIEN', 'Điện (52 kWh)', 52, 3500, 182000),
(3, 1, 'DG-NUOC', 'Nước (9 m3)', 9, 15000, 135000),
(4, 2, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(5, 2, 'DG-DIEN', 'Điện (61 kWh)', 61, 3500, 213500),
(6, 2, 'DG-NUOC', 'Nước (9 m3)', 9, 15000, 135000),
(7, 3, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(8, 3, 'DG-DIEN', 'Điện (59 kWh)', 59, 3500, 206500),
(9, 3, 'DG-NUOC', 'Nước (9 m3)', 9, 15000, 135000),
(10, 4, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(11, 4, 'DG-DIEN', 'Điện (67 kWh)', 67, 3500, 234500),
(12, 4, 'DG-NUOC', 'Nước (10 m3)', 10, 15000, 150000),
(13, 5, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(14, 5, 'DG-DIEN', 'Điện (55 kWh)', 55, 3500, 192500),
(15, 5, 'DG-NUOC', 'Nước (9 m3)', 9, 15000, 135000),
(16, 6, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(17, 6, 'DG-DIEN', 'Điện (76 kWh)', 76, 3500, 266000),
(18, 6, 'DG-NUOC', 'Nước (19 m3)', 19, 15000, 285000),
(19, 7, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(20, 7, 'DG-DIEN', 'Điện (52 kWh)', 52, 3500, 182000),
(21, 7, 'DG-NUOC', 'Nước (14 m3)', 14, 15000, 210000),
(22, 8, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(23, 8, 'DG-DIEN', 'Điện (56 kWh)', 56, 3500, 196000),
(24, 8, 'DG-NUOC', 'Nước (15 m3)', 15, 15000, 225000),
(25, 9, NULL, 'Tiền phòng tiêu chuẩn', 1, 450000, 450000),
(26, 9, 'DG-DIEN', 'Điện (87 kWh)', 87, 3500, 304500),
(27, 9, 'DG-NUOC', 'Nước (12 m3)', 12, 15000, 180000),
(28, 10, NULL, 'Tiền phòng tiêu chuẩn', 1, 450000, 450000),
(29, 10, 'DG-DIEN', 'Điện (52 kWh)', 52, 3500, 182000),
(30, 10, 'DG-NUOC', 'Nước (20 m3)', 20, 15000, 300000),
(31, 11, NULL, 'Tiền phòng tiêu chuẩn', 1, 450000, 450000),
(32, 11, 'DG-DIEN', 'Điện (79 kWh)', 79, 3500, 276500),
(33, 11, 'DG-NUOC', 'Nước (10 m3)', 10, 15000, 150000),
(34, 12, NULL, 'Tiền phòng tiêu chuẩn', 1, 450000, 450000),
(35, 12, 'DG-DIEN', 'Điện (56 kWh)', 56, 3500, 196000),
(36, 12, 'DG-NUOC', 'Nước (11 m3)', 11, 15000, 165000),
(37, 13, NULL, 'Tiền phòng tiêu chuẩn', 1, 450000, 450000),
(38, 13, 'DG-DIEN', 'Điện (66 kWh)', 66, 3500, 231000),
(39, 13, 'DG-NUOC', 'Nước (9 m3)', 9, 15000, 135000),
(40, 14, NULL, 'Tiền phòng tiêu chuẩn', 1, 450000, 450000),
(41, 14, 'DG-DIEN', 'Điện (89 kWh)', 89, 3500, 311500),
(42, 14, 'DG-NUOC', 'Nước (20 m3)', 20, 15000, 300000),
(43, 15, NULL, 'Tiền phòng tiêu chuẩn', 1, 1200000, 1200000),
(44, 15, 'DG-DIEN', 'Điện (41 kWh)', 41, 3500, 143500),
(45, 15, 'DG-NUOC', 'Nước (20 m3)', 20, 15000, 300000),
(46, 16, NULL, 'Tiền phòng tiêu chuẩn', 1, 1200000, 1200000),
(47, 16, 'DG-DIEN', 'Điện (80 kWh)', 80, 3500, 280000),
(48, 16, 'DG-NUOC', 'Nước (12 m3)', 12, 15000, 180000),
(49, 17, NULL, 'Tiền phòng tiêu chuẩn', 1, 1200000, 1200000),
(50, 17, 'DG-DIEN', 'Điện (47 kWh)', 47, 3500, 164500),
(51, 17, 'DG-NUOC', 'Nước (20 m3)', 20, 15000, 300000),
(52, 18, NULL, 'Tiền phòng tiêu chuẩn', 1, 1200000, 1200000),
(53, 18, 'DG-DIEN', 'Điện (48 kWh)', 48, 3500, 168000),
(54, 18, 'DG-NUOC', 'Nước (15 m3)', 15, 15000, 225000),
(55, 19, NULL, 'Tiền phòng tiêu chuẩn', 1, 1200000, 1200000),
(56, 19, 'DG-DIEN', 'Điện (64 kWh)', 64, 3500, 224000),
(57, 19, 'DG-NUOC', 'Nước (12 m3)', 12, 15000, 180000),
(58, 20, NULL, 'Tiền phòng tiêu chuẩn', 1, 1200000, 1200000),
(59, 20, 'DG-DIEN', 'Điện (61 kWh)', 61, 3500, 213500),
(60, 20, 'DG-NUOC', 'Nước (18 m3)', 18, 15000, 270000),
(61, 21, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(62, 21, 'DG-DIEN', 'Điện (87 kWh)', 87, 3500, 304500),
(63, 21, 'DG-NUOC', 'Nước (9 m3)', 9, 15000, 135000),
(64, 22, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(65, 22, 'DG-DIEN', 'Điện (87 kWh)', 87, 3500, 304500),
(66, 22, 'DG-NUOC', 'Nước (13 m3)', 13, 15000, 195000),
(67, 23, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(68, 23, 'DG-DIEN', 'Điện (78 kWh)', 78, 3500, 273000),
(69, 23, 'DG-NUOC', 'Nước (20 m3)', 20, 15000, 300000),
(70, 24, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(71, 24, 'DG-DIEN', 'Điện (66 kWh)', 66, 3500, 231000),
(72, 24, 'DG-NUOC', 'Nước (16 m3)', 16, 15000, 240000),
(73, 25, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(74, 25, 'DG-DIEN', 'Điện (63 kWh)', 63, 3500, 220500),
(75, 25, 'DG-NUOC', 'Nước (20 m3)', 20, 15000, 300000),
(76, 26, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(77, 26, 'DG-DIEN', 'Điện (77 kWh)', 77, 3500, 269500),
(78, 26, 'DG-NUOC', 'Nước (10 m3)', 10, 15000, 150000),
(79, 27, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(80, 27, 'DG-DIEN', 'Điện (73 kWh)', 73, 3500, 255500),
(81, 27, 'DG-NUOC', 'Nước (14 m3)', 14, 15000, 210000),
(82, 28, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(83, 28, 'DG-DIEN', 'Điện (69 kWh)', 69, 3500, 241500),
(84, 28, 'DG-NUOC', 'Nước (13 m3)', 13, 15000, 195000),
(85, 29, NULL, 'Tiền phòng tiêu chuẩn', 1, 450000, 450000),
(86, 29, 'DG-DIEN', 'Điện (55 kWh)', 55, 3500, 192500),
(87, 29, 'DG-NUOC', 'Nước (16 m3)', 16, 15000, 240000),
(88, 30, NULL, 'Tiền phòng tiêu chuẩn', 1, 450000, 450000),
(89, 30, 'DG-DIEN', 'Điện (43 kWh)', 43, 3500, 150500),
(90, 30, 'DG-NUOC', 'Nước (20 m3)', 20, 15000, 300000),
(91, 31, NULL, 'Tiền phòng tiêu chuẩn', 1, 450000, 450000),
(92, 31, 'DG-DIEN', 'Điện (52 kWh)', 52, 3500, 182000),
(93, 31, 'DG-NUOC', 'Nước (16 m3)', 16, 15000, 240000),
(94, 32, NULL, 'Tiền phòng tiêu chuẩn', 1, 450000, 450000),
(95, 32, 'DG-DIEN', 'Điện (84 kWh)', 84, 3500, 294000),
(96, 32, 'DG-NUOC', 'Nước (10 m3)', 10, 15000, 150000),
(97, 33, NULL, 'Tiền phòng tiêu chuẩn', 1, 450000, 450000),
(98, 33, 'DG-DIEN', 'Điện (44 kWh)', 44, 3500, 154000),
(99, 33, 'DG-NUOC', 'Nước (12 m3)', 12, 15000, 180000),
(100, 34, NULL, 'Tiền phòng tiêu chuẩn', 1, 450000, 450000),
(101, 34, 'DG-DIEN', 'Điện (45 kWh)', 45, 3500, 157500),
(102, 34, 'DG-NUOC', 'Nước (19 m3)', 19, 15000, 285000),
(103, 35, NULL, 'Tiền phòng tiêu chuẩn', 1, 1200000, 1200000),
(104, 35, 'DG-DIEN', 'Điện (58 kWh)', 58, 3500, 203000),
(105, 35, 'DG-NUOC', 'Nước (12 m3)', 12, 15000, 180000),
(106, 36, NULL, 'Tiền phòng tiêu chuẩn', 1, 1200000, 1200000),
(107, 36, 'DG-DIEN', 'Điện (71 kWh)', 71, 3500, 248500),
(108, 36, 'DG-NUOC', 'Nước (8 m3)', 8, 15000, 120000),
(109, 37, NULL, 'Tiền phòng tiêu chuẩn', 1, 1200000, 1200000),
(110, 37, 'DG-DIEN', 'Điện (84 kWh)', 84, 3500, 294000),
(111, 37, 'DG-NUOC', 'Nước (17 m3)', 17, 15000, 255000),
(112, 38, NULL, 'Tiền phòng tiêu chuẩn', 1, 1200000, 1200000),
(113, 38, 'DG-DIEN', 'Điện (53 kWh)', 53, 3500, 185500),
(114, 38, 'DG-NUOC', 'Nước (17 m3)', 17, 15000, 255000),
(115, 39, NULL, 'Tiền phòng tiêu chuẩn', 1, 1200000, 1200000),
(116, 39, 'DG-DIEN', 'Điện (74 kWh)', 74, 3500, 259000),
(117, 39, 'DG-NUOC', 'Nước (17 m3)', 17, 15000, 255000),
(118, 40, NULL, 'Tiền phòng tiêu chuẩn', 1, 1200000, 1200000),
(119, 40, 'DG-DIEN', 'Điện (88 kWh)', 88, 3500, 308000),
(120, 40, 'DG-NUOC', 'Nước (9 m3)', 9, 15000, 135000),
(121, 41, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(122, 41, 'DG-DIEN', 'Điện (87 kWh)', 87, 3500, 304500),
(123, 41, 'DG-NUOC', 'Nước (17 m3)', 17, 15000, 255000),
(124, 42, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(125, 42, 'DG-DIEN', 'Điện (58 kWh)', 58, 3500, 203000),
(126, 42, 'DG-NUOC', 'Nước (20 m3)', 20, 15000, 300000),
(127, 43, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(128, 43, 'DG-DIEN', 'Điện (42 kWh)', 42, 3500, 147000),
(129, 43, 'DG-NUOC', 'Nước (14 m3)', 14, 15000, 210000),
(130, 44, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(131, 44, 'DG-DIEN', 'Điện (65 kWh)', 65, 3500, 227500),
(132, 44, 'DG-NUOC', 'Nước (19 m3)', 19, 15000, 285000),
(133, 45, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(134, 45, 'DG-DIEN', 'Điện (60 kWh)', 60, 3500, 210000),
(135, 45, 'DG-NUOC', 'Nước (8 m3)', 8, 15000, 120000),
(136, 46, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(137, 46, 'DG-DIEN', 'Điện (88 kWh)', 88, 3500, 308000),
(138, 46, 'DG-NUOC', 'Nước (12 m3)', 12, 15000, 180000),
(139, 47, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(140, 47, 'DG-DIEN', 'Điện (64 kWh)', 64, 3500, 224000),
(141, 47, 'DG-NUOC', 'Nước (9 m3)', 9, 15000, 135000),
(142, 48, NULL, 'Tiền phòng tiêu chuẩn', 1, 600000, 600000),
(143, 48, 'DG-DIEN', 'Điện (59 kWh)', 59, 3500, 206500),
(144, 48, 'DG-NUOC', 'Nước (10 m3)', 10, 15000, 150000),
(145, 49, NULL, 'Tiền phòng tiêu chuẩn', 1, 450000, 450000),
(146, 49, 'DG-DIEN', 'Điện (46 kWh)', 46, 3500, 161000),
(147, 49, 'DG-NUOC', 'Nước (10 m3)', 10, 15000, 150000),
(148, 50, NULL, 'Tiền phòng tiêu chuẩn', 1, 450000, 450000),
(149, 50, 'DG-DIEN', 'Điện (72 kWh)', 72, 3500, 252000),
(150, 50, 'DG-NUOC', 'Nước (20 m3)', 20, 15000, 300000),
(151, 51, NULL, 'Tiền phòng tiêu chuẩn', 1, 450000, 450000),
(152, 51, 'DG-DIEN', 'Điện (64 kWh)', 64, 3500, 224000),
(153, 51, 'DG-NUOC', 'Nước (19 m3)', 19, 15000, 285000),
(154, 52, NULL, 'Tiền phòng tiêu chuẩn', 1, 450000, 450000),
(155, 52, 'DG-DIEN', 'Điện (47 kWh)', 47, 3500, 164500),
(156, 52, 'DG-NUOC', 'Nước (15 m3)', 15, 15000, 225000),
(157, 53, NULL, 'Tiền phòng tiêu chuẩn', 1, 450000, 450000),
(158, 53, 'DG-DIEN', 'Điện (71 kWh)', 71, 3500, 248500),
(159, 53, 'DG-NUOC', 'Nước (17 m3)', 17, 15000, 255000),
(160, 54, NULL, 'Tiền phòng tiêu chuẩn', 1, 450000, 450000),
(161, 54, 'DG-DIEN', 'Điện (50 kWh)', 50, 3500, 175000),
(162, 54, 'DG-NUOC', 'Nước (8 m3)', 8, 15000, 120000),
(163, 55, NULL, 'Tiền phòng tiêu chuẩn', 1, 1200000, 1200000),
(164, 55, 'DG-DIEN', 'Điện (59 kWh)', 59, 3500, 206500),
(165, 55, 'DG-NUOC', 'Nước (17 m3)', 17, 15000, 255000),
(166, 56, NULL, 'Tiền phòng tiêu chuẩn', 1, 1200000, 1200000),
(167, 56, 'DG-DIEN', 'Điện (45 kWh)', 45, 3500, 157500),
(168, 56, 'DG-NUOC', 'Nước (9 m3)', 9, 15000, 135000),
(169, 57, NULL, 'Tiền phòng tiêu chuẩn', 1, 1200000, 1200000),
(170, 57, 'DG-DIEN', 'Điện (42 kWh)', 42, 3500, 147000),
(171, 57, 'DG-NUOC', 'Nước (19 m3)', 19, 15000, 285000),
(172, 58, NULL, 'Tiền phòng tiêu chuẩn', 1, 1200000, 1200000),
(173, 58, 'DG-DIEN', 'Điện (64 kWh)', 64, 3500, 224000),
(174, 58, 'DG-NUOC', 'Nước (17 m3)', 17, 15000, 255000),
(175, 59, NULL, 'Tiền phòng tiêu chuẩn', 1, 1200000, 1200000),
(176, 59, 'DG-DIEN', 'Điện (61 kWh)', 61, 3500, 213500),
(177, 59, 'DG-NUOC', 'Nước (17 m3)', 17, 15000, 255000),
(178, 60, NULL, 'Tiền phòng tiêu chuẩn', 1, 1200000, 1200000),
(179, 60, 'DG-DIEN', 'Điện (55 kWh)', 55, 3500, 192500),
(180, 60, 'DG-NUOC', 'Nước (13 m3)', 13, 15000, 195000);

INSERT INTO VAT_TU (MaVatTu, TenVatTu, DonViTinh, SoLuongTon, DonGiaNhap, GhiChu) VALUES
('VT-BONG-DEN-18W', 'Bóng đèn LED Rạng Đông 18W', 'Bóng', 50, 45000, 'Vật tư tiêu chuẩn KTX'),
('VT-BONG-TUYP', 'Bóng đèn tuýp LED 1m2 20W', 'Bóng', 40, 65000, 'Vật tư tiêu chuẩn KTX'),
('VT-VOI-XIT', 'Vòi xịt vệ sinh inox 304', 'Bộ', 35, 85000, 'Vật tư tiêu chuẩn KTX'),
('VT-VOI-SEN', 'Vòi hoa sen tăng áp', 'Bộ', 25, 120000, 'Vật tư tiêu chuẩn KTX'),
('VT-KHOA-TAY-GAT', 'Ổ khóa cửa tay gạt Hafele', 'Bộ', 20, 250000, 'Vật tư tiêu chuẩn KTX'),
('VT-KHOA-TREO', 'Ổ khóa bấm Việt Tiệp 60mm', 'Cái', 45, 75000, 'Vật tư tiêu chuẩn KTX'),
('VT-TU-QUAT', 'Tụ quạt trần Vinawind 2uF', 'Cái', 50, 30000, 'Vật tư tiêu chuẩn KTX'),
('VT-CANH-QUAT', 'Cánh quạt trần 1.4m', 'Cái', 15, 110000, 'Vật tư tiêu chuẩn KTX'),
('VT-CB-16A', 'Aptomat chống giật Panasonic 16A', 'Cái', 30, 185000, 'Vật tư tiêu chuẩn KTX'),
('VT-OCAM-DULIEU', 'Ổ cắm điện đôi âm tường Sino', 'Bộ', 60, 42000, 'Vật tư tiêu chuẩn KTX'),
('VT-VAN-NUOC', 'Van khóa nước đồng phi 21', 'Cái', 40, 55000, 'Vật tư tiêu chuẩn KTX'),
('VT-ONG-MEM', 'Dây cấp nước mềm inox 40cm', 'Sợi', 55, 38000, 'Vật tư tiêu chuẩn KTX'),
('VT-GHE-HOCTAP', 'Ghế gấp học tập khung thép', 'Cái', 20, 195000, 'Vật tư tiêu chuẩn KTX'),
('VT-BAN-HOC', 'Mặt bàn học sinh vân gỗ 1m', 'Cái', 10, 320000, 'Vật tư tiêu chuẩn KTX'),
('VT-DEM-KTX', 'Đệm PE gấp 3 sinh viên 1m x 2m', 'Tấm', 25, 280000, 'Vật tư tiêu chuẩn KTX'),
('VT-SON-TUONG', 'Sơn nước nội thất trắng Maxilite 5L', 'Thùng', 12, 240000, 'Vật tư tiêu chuẩn KTX'),
('VT-GIO-GIA-TREO', 'Giá treo khăn inox phòng tắm', 'Cái', 30, 65000, 'Vật tư tiêu chuẩn KTX'),
('VT-VOI-LAVABO', 'Vòi rửa mặt lavabo lạnh inox', 'Cái', 18, 145000, 'Vật tư tiêu chuẩn KTX'),
('VT-BONG-DEN-DOWN', 'Đèn LED âm trần Downlight 9W', 'Bóng', 40, 55000, 'Vật tư tiêu chuẩn KTX'),
('VT-CHUYEN-DOI-NGUON', 'Bộ đổi nguồn Adapter Router 12V-2A', 'Cái', 20, 80000, 'Vật tư tiêu chuẩn KTX');

INSERT INTO YEU_CAU_SUA_CHUA (MaYeuCau, MaPhong, MaSinhVien, TieuDe, MoTaChiTiet, MucDoUuTien, TrangThai, NgayTao, NgayHoanThanh, GhiChuKyThuat) VALUES
(1, 'A-102', '21110001', 'Kẹt ổ khóa cửa chính', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng A-102', 'THAP', 'HOAN_THANH', NOW(), NOW(), 'Đã hoàn tất'),
(2, 'A-103', '21110002', 'Rò rỉ nước ở vòi xịt WC', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng A-103', 'KHAN_CAP', 'HOAN_THANH', NOW(), NOW(), 'Đã hoàn tất'),
(3, 'A-201', '21110003', 'Quạt trần quay yếu có tiếng kêu', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng A-201', 'KHAN_CAP', 'HOAN_THANH', NOW(), NOW(), 'Đã hoàn tất'),
(4, 'A-202', '21110004', 'Aptomat tự nhảy khi cắm máy sấy', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng A-202', 'KHAN_CAP', 'HOAN_THANH', NOW(), NOW(), 'Đã hoàn tất'),
(5, 'A-203', '21110005', 'Ổ cắm điện bàn học bị lỏng', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng A-203', 'TRUNG_BINH', 'HOAN_THANH', NOW(), NOW(), 'Đã hoàn tất'),
(6, 'A-301', '21110006', 'Vỡ gương phòng tắm', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng A-301', 'CAO', 'HOAN_THANH', NOW(), NOW(), 'Đã hoàn tất'),
(7, 'A-302', '21110007', 'Bản lề tủ cá nhân bị bung', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng A-302', 'KHAN_CAP', 'HOAN_THANH', NOW(), NOW(), 'Đã hoàn tất'),
(8, 'B-101', '21110008', 'Nước chảy yếu ở lavabo', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng B-101', 'CAO', 'HOAN_THANH', NOW(), NOW(), 'Đã hoàn tất'),
(9, 'B-102', '21110009', 'Cửa sổ ban công bị kẹt chốt', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng B-102', 'THAP', 'HOAN_THANH', NOW(), NOW(), 'Đã hoàn tất'),
(10, 'B-103', '21110010', 'Bóng tuýp phòng ngủ nhấp nháy', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng B-103', 'CAO', 'HOAN_THANH', NOW(), NOW(), 'Đã hoàn tất'),
(11, 'B-201', '21110011', 'Van cấp nước bồn cầu không ngắt', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng B-201', 'THAP', 'HOAN_THANH', NOW(), NOW(), 'Đã hoàn tất'),
(12, 'B-202', '21110012', 'Hỏng bóng đèn phòng tắm', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng B-202', 'KHAN_CAP', 'HOAN_THANH', NOW(), NOW(), 'Đã hoàn tất'),
(13, 'B-203', '21110013', 'Kẹt ổ khóa cửa chính', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng B-203', 'KHAN_CAP', 'HOAN_THANH', NOW(), NOW(), 'Đã hoàn tất'),
(14, 'C-101', '21110014', 'Rò rỉ nước ở vòi xịt WC', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng C-101', 'TRUNG_BINH', 'HOAN_THANH', NOW(), NOW(), 'Đã hoàn tất'),
(15, 'C-102', '21110015', 'Quạt trần quay yếu có tiếng kêu', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng C-102', 'THAP', 'HOAN_THANH', NOW(), NOW(), 'Đã hoàn tất'),
(16, 'C-201', '21110016', 'Aptomat tự nhảy khi cắm máy sấy', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng C-201', 'THAP', 'DANG_XU_LY', NOW(), NULL, NULL),
(17, 'C-202', '21110017', 'Ổ cắm điện bàn học bị lỏng', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng C-202', 'CAO', 'DANG_XU_LY', NOW(), NULL, NULL),
(18, 'D-101', '21110018', 'Vỡ gương phòng tắm', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng D-101', 'TRUNG_BINH', 'DANG_XU_LY', NOW(), NULL, NULL),
(19, 'D-102', '21110019', 'Bản lề tủ cá nhân bị bung', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng D-102', 'CAO', 'DANG_XU_LY', NOW(), NULL, NULL),
(20, 'A-101', '21110020', 'Nước chảy yếu ở lavabo', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng A-101', 'CAO', 'DANG_XU_LY', NOW(), NULL, NULL),
(21, 'A-102', '21110021', 'Cửa sổ ban công bị kẹt chốt', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng A-102', 'KHAN_CAP', 'DANG_XU_LY', NOW(), NULL, NULL),
(22, 'A-103', '21110022', 'Bóng tuýp phòng ngủ nhấp nháy', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng A-103', 'THAP', 'DANG_XU_LY', NOW(), NULL, NULL),
(23, 'A-201', '21110023', 'Van cấp nước bồn cầu không ngắt', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng A-201', 'THAP', 'MOI', NOW(), NULL, NULL),
(24, 'A-202', '21110024', 'Hỏng bóng đèn phòng tắm', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng A-202', 'CAO', 'MOI', NOW(), NULL, NULL),
(25, 'A-203', '21110025', 'Kẹt ổ khóa cửa chính', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng A-203', 'CAO', 'MOI', NOW(), NULL, NULL),
(26, 'A-301', '21110026', 'Rò rỉ nước ở vòi xịt WC', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng A-301', 'CAO', 'MOI', NOW(), NULL, NULL),
(27, 'A-302', '21110027', 'Quạt trần quay yếu có tiếng kêu', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng A-302', 'CAO', 'MOI', NOW(), NULL, NULL),
(28, 'B-101', '21110028', 'Aptomat tự nhảy khi cắm máy sấy', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng B-101', 'KHAN_CAP', 'MOI', NOW(), NULL, NULL),
(29, 'B-102', '21110029', 'Ổ cắm điện bàn học bị lỏng', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng B-102', 'TRUNG_BINH', 'MOI', NOW(), NULL, NULL),
(30, 'B-103', '21110030', 'Vỡ gương phòng tắm', 'Yêu cầu kiểm tra và xử lý thiết bị tại phòng B-103', 'TRUNG_BINH', 'MOI', NOW(), NULL, NULL);

INSERT INTO RA_VAO (MaRaVao, MaSinhVien, LoaiSuKien, ThoiGian, BaoVeGhiNhan, GhiChu) VALUES
(1, '21110001', 'VAO', '2026-09-30 21:52:38', 'baove1', 'Quét thẻ sinh viên tự động'),
(2, '21110002', 'RA', '2026-09-30 11:51:09', 'baove1', 'Quét thẻ sinh viên tự động'),
(3, '21110003', 'VAO', '2026-09-30 08:10:05', 'baove1', 'Quét thẻ sinh viên tự động'),
(4, '21110004', 'RA', '2026-09-30 21:59:12', 'baove1', 'Quét thẻ sinh viên tự động'),
(5, '21110005', 'VAO', '2026-09-30 18:00:51', 'baove1', 'Quét thẻ sinh viên tự động'),
(6, '21110006', 'RA', '2026-09-30 12:01:47', 'baove1', 'Quét thẻ sinh viên tự động'),
(7, '21110007', 'VAO', '2026-09-30 08:25:24', 'baove1', 'Quét thẻ sinh viên tự động'),
(8, '21110008', 'RA', '2026-09-30 07:25:26', 'baove1', 'Quét thẻ sinh viên tự động'),
(9, '21110009', 'VAO', '2026-09-30 07:01:20', 'baove1', 'Quét thẻ sinh viên tự động'),
(10, '21110010', 'RA', '2026-09-30 22:40:11', 'baove1', 'Quét thẻ sinh viên tự động'),
(11, '21110011', 'VAO', '2026-09-30 08:46:01', 'baove1', 'Quét thẻ sinh viên tự động'),
(12, '21110012', 'RA', '2026-09-30 21:46:45', 'baove1', 'Quét thẻ sinh viên tự động'),
(13, '21110013', 'VAO', '2026-09-30 18:01:57', 'baove1', 'Quét thẻ sinh viên tự động'),
(14, '21110014', 'RA', '2026-09-30 23:50:21', 'baove1', 'Quét thẻ sinh viên tự động'),
(15, '21110015', 'VAO', '2026-09-30 21:32:49', 'baove1', 'Quét thẻ sinh viên tự động'),
(16, '21110016', 'RA', '2026-09-30 23:43:37', 'baove1', 'Quét thẻ sinh viên tự động'),
(17, '21110017', 'VAO', '2026-09-30 21:00:35', 'baove1', 'Quét thẻ sinh viên tự động'),
(18, '21110018', 'RA', '2026-09-30 23:07:57', 'baove1', 'Quét thẻ sinh viên tự động'),
(19, '21110019', 'VAO', '2026-09-30 08:16:08', 'baove1', 'Quét thẻ sinh viên tự động'),
(20, '21110020', 'RA', '2026-09-30 17:13:44', 'baove1', 'Quét thẻ sinh viên tự động'),
(21, '21110021', 'VAO', '2026-09-30 21:13:24', 'baove1', 'Quét thẻ sinh viên tự động'),
(22, '21110022', 'RA', '2026-09-30 08:42:20', 'baove1', 'Quét thẻ sinh viên tự động'),
(23, '21110023', 'VAO', '2026-09-30 12:18:16', 'baove1', 'Quét thẻ sinh viên tự động'),
(24, '21110024', 'RA', '2026-09-30 17:38:41', 'baove1', 'Quét thẻ sinh viên tự động'),
(25, '21110025', 'VAO', '2026-09-30 23:38:47', 'baove1', 'Quét thẻ sinh viên tự động'),
(26, '21110026', 'RA', '2026-09-30 23:17:28', 'baove1', 'Quét thẻ sinh viên tự động'),
(27, '21110027', 'VAO', '2026-09-30 22:46:44', 'baove1', 'Quét thẻ sinh viên tự động'),
(28, '21110028', 'RA', '2026-09-30 21:13:31', 'baove1', 'Quét thẻ sinh viên tự động'),
(29, '21110029', 'VAO', '2026-09-30 08:56:32', 'baove1', 'Quét thẻ sinh viên tự động'),
(30, '21110030', 'RA', '2026-09-30 21:25:31', 'baove1', 'Quét thẻ sinh viên tự động'),
(31, '21110031', 'VAO', '2026-09-30 21:17:39', 'baove1', 'Quét thẻ sinh viên tự động'),
(32, '21110032', 'RA', '2026-09-30 18:20:03', 'baove1', 'Quét thẻ sinh viên tự động'),
(33, '21110033', 'VAO', '2026-09-30 08:35:36', 'baove1', 'Quét thẻ sinh viên tự động'),
(34, '21110034', 'RA', '2026-09-30 11:57:41', 'baove1', 'Quét thẻ sinh viên tự động'),
(35, '21110035', 'VAO', '2026-09-30 17:41:27', 'baove1', 'Quét thẻ sinh viên tự động'),
(36, '21110036', 'RA', '2026-09-30 08:39:05', 'baove1', 'Quét thẻ sinh viên tự động'),
(37, '21110037', 'VAO', '2026-09-30 18:21:15', 'baove1', 'Quét thẻ sinh viên tự động'),
(38, '21110038', 'RA', '2026-09-30 18:47:19', 'baove1', 'Quét thẻ sinh viên tự động'),
(39, '21110039', 'VAO', '2026-09-30 11:24:46', 'baove1', 'Quét thẻ sinh viên tự động'),
(40, '21110040', 'RA', '2026-09-30 07:14:59', 'baove1', 'Quét thẻ sinh viên tự động'),
(41, '21110041', 'VAO', '2026-09-30 17:42:12', 'baove1', 'Quét thẻ sinh viên tự động'),
(42, '21110042', 'RA', '2026-09-30 17:30:27', 'baove1', 'Quét thẻ sinh viên tự động'),
(43, '21110043', 'VAO', '2026-09-30 08:13:46', 'baove1', 'Quét thẻ sinh viên tự động'),
(44, '21110044', 'RA', '2026-09-30 12:16:05', 'baove1', 'Quét thẻ sinh viên tự động'),
(45, '21110045', 'VAO', '2026-09-30 23:30:37', 'baove1', 'Quét thẻ sinh viên tự động'),
(46, '21110046', 'RA', '2026-09-30 17:55:16', 'baove1', 'Quét thẻ sinh viên tự động'),
(47, '21110047', 'VAO', '2026-09-30 22:18:49', 'baove1', 'Quét thẻ sinh viên tự động'),
(48, '21110048', 'RA', '2026-09-30 22:33:20', 'baove1', 'Quét thẻ sinh viên tự động'),
(49, '21110049', 'VAO', '2026-09-30 21:34:46', 'baove1', 'Quét thẻ sinh viên tự động'),
(50, '21110050', 'RA', '2026-09-30 23:45:15', 'baove1', 'Quét thẻ sinh viên tự động'),
(51, '21110051', 'VAO', '2026-09-30 08:09:34', 'baove1', 'Quét thẻ sinh viên tự động'),
(52, '21110052', 'RA', '2026-09-30 11:18:31', 'baove1', 'Quét thẻ sinh viên tự động'),
(53, '21110053', 'VAO', '2026-09-30 18:46:16', 'baove1', 'Quét thẻ sinh viên tự động'),
(54, '21110054', 'RA', '2026-09-30 12:29:20', 'baove1', 'Quét thẻ sinh viên tự động'),
(55, '21110055', 'VAO', '2026-09-30 07:03:14', 'baove1', 'Quét thẻ sinh viên tự động');

INSERT INTO VI_PHAM (MaViPham, MaSinhVien, MaRaVao, NoiDungViPham, MucDo, HinhThucXuLy, NgayViPham, TrangThai) VALUES
(1, '21110001', 3, 'Về KTX muộn sau 23h không xin phép', 'LOW', 'Nhắc nhở và ghi biên bản', '2026-09-30 23:15:00', 'DA_XU_LY'),
(2, '21110002', 6, 'Về KTX muộn sau 23h không xin phép', 'LOW', 'Nhắc nhở và ghi biên bản', '2026-09-30 23:15:00', 'DA_XU_LY'),
(3, '21110003', 9, 'Về KTX muộn sau 23h không xin phép', 'LOW', 'Nhắc nhở và ghi biên bản', '2026-09-30 23:15:00', 'DA_XU_LY'),
(4, '21110004', 12, 'Về KTX muộn sau 23h không xin phép', 'LOW', 'Nhắc nhở và ghi biên bản', '2026-09-30 23:15:00', 'DA_XU_LY'),
(5, '21110005', 15, 'Về KTX muộn sau 23h không xin phép', 'LOW', 'Nhắc nhở và ghi biên bản', '2026-09-30 23:15:00', 'DA_XU_LY'),
(6, '21110006', 18, 'Về KTX muộn sau 23h không xin phép', 'LOW', 'Nhắc nhở và ghi biên bản', '2026-09-30 23:15:00', 'DA_XU_LY'),
(7, '21110007', 21, 'Về KTX muộn sau 23h không xin phép', 'LOW', 'Nhắc nhở và ghi biên bản', '2026-09-30 23:15:00', 'DA_XU_LY'),
(8, '21110008', 24, 'Về KTX muộn sau 23h không xin phép', 'LOW', 'Nhắc nhở và ghi biên bản', '2026-09-30 23:15:00', 'DA_XU_LY'),
(9, '21110009', 27, 'Về KTX muộn sau 23h không xin phép', 'LOW', 'Nhắc nhở và ghi biên bản', '2026-09-30 23:15:00', 'DA_XU_LY'),
(10, '21110010', 30, 'Về KTX muộn sau 23h không xin phép', 'LOW', 'Nhắc nhở và ghi biên bản', '2026-09-30 23:15:00', 'DA_XU_LY'),
(11, '21110011', 33, 'Về KTX muộn sau 23h không xin phép', 'LOW', 'Nhắc nhở và ghi biên bản', '2026-09-30 23:15:00', 'DA_XU_LY'),
(12, '21110012', 36, 'Về KTX muộn sau 23h không xin phép', 'LOW', 'Nhắc nhở và ghi biên bản', '2026-09-30 23:15:00', 'DA_XU_LY'),
(13, '21110013', 39, 'Về KTX muộn sau 23h không xin phép', 'LOW', 'Nhắc nhở và ghi biên bản', '2026-09-30 23:15:00', 'DA_XU_LY'),
(14, '21110014', 42, 'Về KTX muộn sau 23h không xin phép', 'LOW', 'Nhắc nhở và ghi biên bản', '2026-09-30 23:15:00', 'DA_XU_LY'),
(15, '21110015', 45, 'Về KTX muộn sau 23h không xin phép', 'LOW', 'Nhắc nhở và ghi biên bản', '2026-09-30 23:15:00', 'DA_XU_LY');

