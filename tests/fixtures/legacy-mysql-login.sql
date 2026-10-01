-- Reproduce the production mismatch without copying production data.
ALTER DATABASE cnpm_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER IF NOT EXISTS 'cnpm_user'@'%' IDENTIFIED BY 'ci-only-password';
GRANT SELECT, EXECUTE ON cnpm_db.* TO 'cnpm_user'@'%';
USE cnpm_db;
CREATE TABLE TAI_KHOAN (
    MaTaiKhoan INT PRIMARY KEY, TenDangNhap VARCHAR(50), MatKhauHash VARCHAR(100),
    HoTen VARCHAR(100), Email VARCHAR(100), VaiTro VARCHAR(20), TrangThai VARCHAR(20)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE TABLE SINH_VIEN (MaTaiKhoan INT, MaSinhVien VARCHAR(20), MaPhongHienTai VARCHAR(20))
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
INSERT INTO TAI_KHOAN VALUES
    (1, 'admin', 'ci-fixture', 'Admin', 'admin@example.test', 'ADMIN', 'ACTIVE'),
    (2, 'sinhviên', 'ci-fixture', 'Sinh viên', 'sv@example.test', 'SINHVIEN', 'LOCKED');
INSERT INTO SINH_VIEN VALUES (2, 'SV001', 'P001');
DELIMITER $$
CREATE PROCEDURE usp_DangNhap(IN p_TenDangNhap VARCHAR(50))
BEGIN
    SELECT tk.MaTaiKhoan FROM TAI_KHOAN tk WHERE tk.TenDangNhap = p_TenDangNhap;
END$$
DELIMITER ;
