-- Manual repair for the deployed legacy MySQL instance only.
-- PostgreSQL remains the canonical database. Do not put this file in database/.
-- Back up cnpm_db including routines before applying. No table/data changes.
USE cnpm_db;
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
DELIMITER $$
DROP PROCEDURE IF EXISTS usp_DangNhap$$
CREATE DEFINER=`cnpm_user`@`%` PROCEDURE usp_DangNhap(
    IN p_TenDangNhap VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
)
SQL SECURITY DEFINER
BEGIN
    SELECT tk.MaTaiKhoan, tk.TenDangNhap, tk.MatKhauHash,
           tk.HoTen, tk.Email, tk.VaiTro, tk.TrangThai,
           sv.MaSinhVien, sv.MaPhongHienTai
    FROM TAI_KHOAN tk
    LEFT JOIN SINH_VIEN sv ON tk.MaTaiKhoan = sv.MaTaiKhoan
    WHERE tk.TenDangNhap = p_TenDangNhap;
END$$
DELIMITER ;
