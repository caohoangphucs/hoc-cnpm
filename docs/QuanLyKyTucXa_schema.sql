
-- SCHEMA: Hệ thống Quản lý Ký túc xá (đã chuẩn hóa 3NF)
-- DBMS: PostgreSQL

-- PHẦN 1: ENUM TYPES
CREATE TYPE enum_trang_thai_phong AS ENUM ('TRONG', 'DA_CO_NGUOI', 'DANG_SUA');
CREATE TYPE enum_vai_tro_nv       AS ENUM ('ADMIN', 'KE_TOAN', 'KY_THUAT', 'BAO_VE', 'BAO_TRI');
CREATE TYPE enum_trang_thai_csgg  AS ENUM ('DANG_AP_DUNG', 'HET_HAN');
CREATE TYPE enum_trang_thai_hd    AS ENUM ('CHUA_TT', 'DA_TT', 'QUA_HAN');
CREATE TYPE enum_trang_thai_dv    AS ENUM ('DANG_AP_DUNG', 'NGUNG_AP_DUNG');
CREATE TYPE enum_phuong_thuc_tt   AS ENUM ('CHUYEN_KHOAN', 'VNPAY', 'MOMO');
CREATE TYPE enum_trang_thai_tt    AS ENUM ('CHO', 'THANH_CONG', 'THAT_BAI');


-- PHẦN 2: CREATE TABLE (PK, kiểu dữ liệu, NOT NULL, DEFAULT, CHECK)

-- 2.1 loai_phong
CREATE TABLE loai_phong (
    id      VARCHAR(36)     PRIMARY KEY,
    ten_loai VARCHAR(50)    NOT NULL UNIQUE,
    mo_ta   TEXT,
    gia     DECIMAL(12,2)   NOT NULL CHECK (gia > 0)
);

-- 2.2 phong
CREATE TABLE phong (
    id            VARCHAR(36)             PRIMARY KEY,
    loai_phong_id VARCHAR(36),                         -- NULL cho phép (SET NULL khi loai_phong bị xóa)
    so_phong      VARCHAR(10)             NOT NULL UNIQUE,
    tang          INT                     NOT NULL CHECK (tang >= 0),
    suc_chua      INT                     NOT NULL CHECK (suc_chua > 0),
    trang_thai    enum_trang_thai_phong   NOT NULL DEFAULT 'TRONG'
);

-- 2.3 nhan_vien (Single Table Inheritance)
CREATE TABLE nhan_vien (
    id          VARCHAR(36)     PRIMARY KEY,
    ho_ten      VARCHAR(100)    NOT NULL,
    username    VARCHAR(50)     NOT NULL UNIQUE,
    mat_khau    VARCHAR(255)    NOT NULL,
    vai_tro     enum_vai_tro_nv NOT NULL,
    trang_thai  BOOLEAN         NOT NULL DEFAULT TRUE
);

-- 2.4 chinh_sach_giam_gia
CREATE TABLE chinh_sach_giam_gia (
    id                  VARCHAR(36)             PRIMARY KEY,
    ten_chinh_sach      VARCHAR(150)            NOT NULL UNIQUE,
    ty_le_giam_gia      DECIMAL(5,2)            NOT NULL CHECK (ty_le_giam_gia BETWEEN 0 AND 100),
    dieu_kien_ap_dung   TEXT,
    ngay_bat_dau        DATE                    NOT NULL,
    ngay_ket_thuc       DATE                    NOT NULL,
    trang_thai          enum_trang_thai_csgg    NOT NULL DEFAULT 'DANG_AP_DUNG',
    CHECK (ngay_ket_thuc >= ngay_bat_dau)
);

-- 2.5 sinh_vien
CREATE TABLE sinh_vien (
    id          VARCHAR(36)     PRIMARY KEY,
    phong_id    VARCHAR(36),                        -- NULL nếu chưa được xếp phòng
    mssv        VARCHAR(15)     NOT NULL UNIQUE,
    ho_ten      VARCHAR(100)    NOT NULL,
    email       VARCHAR(150)    UNIQUE,
    sdt         VARCHAR(15),
    ngay_vao_o  DATE
);

-- 2.6 dich_vu
CREATE TABLE dich_vu (
    id                  VARCHAR(36)         PRIMARY KEY,
    ten_dich_vu         VARCHAR(100)        NOT NULL UNIQUE,
    don_gia_hien_tai    DECIMAL(12,2)       NOT NULL CHECK (don_gia_hien_tai >= 0),
    don_vi              VARCHAR(20),
    trang_thai          enum_trang_thai_dv  NOT NULL DEFAULT 'DANG_AP_DUNG'
);

-- 2.7 hoa_don
CREATE TABLE hoa_don (
    id                      VARCHAR(36)         PRIMARY KEY,
    sinh_vien_id            VARCHAR(36)         NOT NULL,
    phong_id                VARCHAR(36)         NOT NULL,   
    nhan_vien_id            VARCHAR(36)         NOT NULL,
    chinh_sach_giam_gia_id  VARCHAR(36),                   
    so_hoa_don              VARCHAR(20)         NOT NULL UNIQUE,
    ky_thanh_toan           VARCHAR(7)          NOT NULL,  
    ngay_tao                TIMESTAMP           NOT NULL DEFAULT now(),
    trang_thai              enum_trang_thai_hd  NOT NULL DEFAULT 'CHUA_TT',
    tong_tien_goc           DECIMAL(12,2)       NOT NULL CHECK (tong_tien_goc >= 0),
    so_tien_giam            DECIMAL(12,2)       NOT NULL DEFAULT 0 CHECK (so_tien_giam >= 0),
    tong_tien_thanh_toan    DECIMAL(12,2) GENERATED ALWAYS AS (tong_tien_goc - so_tien_giam) STORED,
    CHECK (so_tien_giam <= tong_tien_goc),
    CHECK (ky_thanh_toan ~ '^\d{4}-\d{2}$')
);

-- 2.8 chi_tiet_hoa_don
CREATE TABLE chi_tiet_hoa_don (
    id          VARCHAR(36)     PRIMARY KEY,
    hoa_don_id  VARCHAR(36)     NOT NULL,
    dich_vu_id  VARCHAR(36)     NOT NULL,
    so_luong    INT             NOT NULL CHECK (so_luong > 0),
    don_gia     DECIMAL(12,2)   NOT NULL CHECK (don_gia >= 0),  
    ghi_chu     VARCHAR(255),
    UNIQUE (hoa_don_id, dich_vu_id)  
);

-- 2.9 thanh_toan
CREATE TABLE thanh_toan (
    id          VARCHAR(36)         PRIMARY KEY,
    hoa_don_id  VARCHAR(36)         NOT NULL UNIQUE,  
    phuong_thuc enum_phuong_thuc_tt NOT NULL,
    so_tien     DECIMAL(12,2)       NOT NULL CHECK (so_tien > 0),
    trang_thai  enum_trang_thai_tt  NOT NULL DEFAULT 'CHO',
    thoi_gian   TIMESTAMP           NOT NULL DEFAULT now()
);

-- 2.10 thanh_toan_dien_tu 
CREATE TABLE thanh_toan_dien_tu (
    thanh_toan_id       VARCHAR(36)     PRIMARY KEY,
    ten_cong_thanh_toan VARCHAR(50)     NOT NULL,
    ma_giao_dich        VARCHAR(100)    NOT NULL UNIQUE
);


-- PHẦN 3: FOREIGN KEY CONSTRAINTS 

-- phong -> loai_phong
ALTER TABLE phong
    ADD CONSTRAINT fk_phong_loai_phong
    FOREIGN KEY (loai_phong_id) REFERENCES loai_phong(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- sinh_vien -> phong
ALTER TABLE sinh_vien
    ADD CONSTRAINT fk_sinhvien_phong
    FOREIGN KEY (phong_id) REFERENCES phong(id)
    ON DELETE SET NULL
	ON UPDATE CASCADE;

-- hoa_don -> sinh_vien
ALTER TABLE hoa_don
    ADD CONSTRAINT fk_hoadon_sinhvien
    FOREIGN KEY (sinh_vien_id) REFERENCES sinh_vien(id)
    ON DELETE RESTRICT      
    ON UPDATE CASCADE;

-- hoa_don -> phong 
ALTER TABLE hoa_don
    ADD CONSTRAINT fk_hoadon_phong
    FOREIGN KEY (phong_id) REFERENCES phong(id)
    ON DELETE RESTRICT    
    ON UPDATE CASCADE;

-- hoa_don -> nhan_vien
ALTER TABLE hoa_don
    ADD CONSTRAINT fk_hoadon_nhanvien
    FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(id)
    ON DELETE RESTRICT     
    ON UPDATE CASCADE;

-- hoa_don -> chinh_sach_giam_gia
ALTER TABLE hoa_don
    ADD CONSTRAINT fk_hoadon_csgg
    FOREIGN KEY (chinh_sach_giam_gia_id) REFERENCES chinh_sach_giam_gia(id)
    ON DELETE SET NULL
	ON UPDATE CASCADE;

-- chi_tiet_hoa_don -> hoa_don
ALTER TABLE chi_tiet_hoa_don
    ADD CONSTRAINT fk_ctdh_hoadon
    FOREIGN KEY (hoa_don_id) REFERENCES hoa_don(id)
    ON DELETE CASCADE       -- xóa hóa đơn thì xóa luôn chi tiết
    ON UPDATE CASCADE;

-- chi_tiet_hoa_don -> dich_vu
ALTER TABLE chi_tiet_hoa_don
    ADD CONSTRAINT fk_ctdh_dichvu
    FOREIGN KEY (dich_vu_id) REFERENCES dich_vu(id)
    ON DELETE RESTRICT     
    ON UPDATE CASCADE;

-- thanh_toan -> hoa_don
ALTER TABLE thanh_toan
    ADD CONSTRAINT fk_thanhtoan_hoadon
    FOREIGN KEY (hoa_don_id) REFERENCES hoa_don(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

-- thanh_toan_dien_tu -> thanh_toan (Joined Table Inheritance)
ALTER TABLE thanh_toan_dien_tu
    ADD CONSTRAINT fk_ttdt_thanhtoan
    FOREIGN KEY (thanh_toan_id) REFERENCES thanh_toan(id)
    ON DELETE CASCADE       
    ON UPDATE CASCADE;


-- PHẦN 4: VIEW thay thế cho các cột suy diễn

-- Thay thế phong.so_luong_hien_o
CREATE VIEW v_phong_si_so AS
SELECT
    p.id,
    p.so_phong,
    p.suc_chua,
    COUNT(sv.id) AS so_luong_hien_o
FROM phong p
LEFT JOIN sinh_vien sv ON sv.phong_id = p.id
GROUP BY p.id, p.so_phong, p.suc_chua;


-- PHẦN 5: TRIGGERS (ràng buộc liên bảng mà CHECK không xử lý được)

-- 5.1 Chặn sinh viên vượt sức chứa phòng
CREATE OR REPLACE FUNCTION check_suc_chua_phong()
RETURNS TRIGGER AS $$
DECLARE
    so_luong_hien_tai INT;
    suc_chua_toi_da   INT;
BEGIN
    SELECT COUNT(*) INTO so_luong_hien_tai
    FROM sinh_vien
    WHERE phong_id = NEW.phong_id;

    SELECT suc_chua INTO suc_chua_toi_da
    FROM phong
    WHERE id = NEW.phong_id;

    IF so_luong_hien_tai >= suc_chua_toi_da THEN
        RAISE EXCEPTION 'Phòng % đã đầy (%/%), không thể xếp thêm sinh viên',
            NEW.phong_id, so_luong_hien_tai, suc_chua_toi_da;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_suc_chua_phong
BEFORE INSERT OR UPDATE OF phong_id ON sinh_vien
FOR EACH ROW
WHEN (NEW.phong_id IS NOT NULL)
EXECUTE FUNCTION check_suc_chua_phong();

-- 5.2 Tự động đồng bộ phong.trang_thai khi sinh viên vào/rời phòng
CREATE OR REPLACE FUNCTION sync_trang_thai_phong()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'UPDATE' AND OLD.phong_id IS DISTINCT FROM NEW.phong_id)
       OR TG_OP = 'DELETE' THEN
        UPDATE phong SET trang_thai = 'TRONG'
        WHERE id = OLD.phong_id
          AND NOT EXISTS (SELECT 1 FROM sinh_vien WHERE phong_id = OLD.phong_id);
    END IF;

    IF TG_OP IN ('INSERT', 'UPDATE') AND NEW.phong_id IS NOT NULL THEN
        UPDATE phong SET trang_thai = 'DA_CO_NGUOI' WHERE id = NEW.phong_id;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sync_trang_thai_phong
AFTER INSERT OR UPDATE OF phong_id OR DELETE ON sinh_vien
FOR EACH ROW
EXECUTE FUNCTION sync_trang_thai_phong();

-- 5.3 Tự động chuyển hoa_don.trang_thai = 'DA_TT' khi thanh toán thành công
CREATE OR REPLACE FUNCTION sync_trang_thai_hoadon()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.trang_thai = 'THANH_CONG' THEN
        UPDATE hoa_don SET trang_thai = 'DA_TT' WHERE id = NEW.hoa_don_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sync_trang_thai_hoadon
AFTER INSERT OR UPDATE OF trang_thai ON thanh_toan
FOR EACH ROW
EXECUTE FUNCTION sync_trang_thai_hoadon();

-- BÀI TẬP 3: THIẾT KẾ INDEXES

-- Q1: Lấy hóa đơn của một sinh viên (FK: sinh_vien_id, kèm cột thời gian)
CREATE INDEX idx_hoadon_sinhvien_ngay
ON hoa_don (sinh_vien_id, ngay_tao DESC);

-- Q2: FK phong_id, kèm ky_thanh_toan + trang_thai
CREATE INDEX idx_hoadon_phong_ky_trangthai
ON hoa_don (phong_id, ky_thanh_toan, trang_thai);

-- FK nhan_vien_id 
CREATE INDEX idx_hoadon_nhanvien
ON hoa_don (nhan_vien_id);

-- FK chinh_sach_giam_gia_id 
CREATE INDEX idx_hoadon_csgg
ON hoa_don (chinh_sach_giam_gia_id);

-- Q3: FK phong_id của sinh_vien
CREATE INDEX idx_sinhvien_phong
ON sinh_vien (phong_id);

-- Q4: FK dich_vu_id của chi_tiet_hoa_don
CREATE INDEX idx_ctdh_dichvu
ON chi_tiet_hoa_don (dich_vu_id);

-- Q5: trang_thai + ngay_tao (thời gian đứng sau, phục vụ ORDER BY)
CREATE INDEX idx_hoadon_trangthai_ngay
ON hoa_don (trang_thai, ngay_tao);

-- Q6: thoi_gian nằm cuối composite, phục vụ ORDER BY DESC
CREATE INDEX idx_thanhtoan_phuongthuc_trangthai
ON thanh_toan (phuong_thuc, trang_thai, thoi_gian DESC);

-- Q7: ky_thanh_toan (không phải trạng thái nhưng cần cho GROUP BY)
CREATE INDEX idx_hoadon_kythanhtoan
ON hoa_don (ky_thanh_toan);

-- Q8: trang_thai của phong, kèm loai_phong_id
CREATE INDEX idx_phong_loaiphong_trangthai
ON phong (loai_phong_id, trang_thai);

-- Q9: trang_thai của phong, đứng độc lập
CREATE INDEX idx_phong_trangthai
ON phong (trang_thai);

-- Q10: trang_thai của thanh_toan, đứng độc lập
CREATE INDEX idx_thanhtoan_trangthai
ON thanh_toan (trang_thai);

-- Q1: idx_hoadon_sinhvien_ngay
EXPLAIN ANALYZE
SELECT * FROM hoa_don
WHERE sinh_vien_id = 'sv-001'
ORDER BY ngay_tao DESC;

-- Q2: idx_hoadon_phong_ky_trangthai
EXPLAIN ANALYZE
SELECT * FROM hoa_don
WHERE phong_id = 'phong-001'
  AND ky_thanh_toan = '2026-09'
  AND trang_thai = 'CHUA_TT';

-- Q3: idx_sinhvien_phong
EXPLAIN ANALYZE
SELECT * FROM sinh_vien
WHERE phong_id = 'phong-001';

-- Q4: idx_ctdh_dichvu
EXPLAIN ANALYZE
SELECT dich_vu_id, SUM(so_luong) AS tong_so_luong
FROM chi_tiet_hoa_don
GROUP BY dich_vu_id
ORDER BY tong_so_luong DESC;

-- Q5: idx_hoadon_trangthai_ngay
EXPLAIN ANALYZE
SELECT * FROM hoa_don
WHERE trang_thai = 'QUA_HAN'
ORDER BY ngay_tao;

-- Q6: idx_thanhtoan_phuongthuc_trangthai
EXPLAIN ANALYZE
SELECT * FROM thanh_toan
WHERE phuong_thuc = 'CHUYEN_KHOAN'
  AND trang_thai = 'THANH_CONG'
ORDER BY thoi_gian DESC;

-- Q7: idx_hoadon_kythanhtoan
EXPLAIN ANALYZE
SELECT ky_thanh_toan, SUM(tong_tien_thanh_toan) AS doanh_thu
FROM hoa_don
GROUP BY ky_thanh_toan;

-- Q8: idx_phong_loaiphong_trangthai
EXPLAIN ANALYZE
SELECT * FROM phong
WHERE loai_phong_id = 'lp-001'
  AND trang_thai = 'TRONG';

-- Q9: idx_phong_trangthai
EXPLAIN ANALYZE
SELECT * FROM phong
WHERE trang_thai = 'TRONG';

-- Q10: idx_thanhtoan_trangthai
EXPLAIN ANALYZE
SELECT * FROM thanh_toan
WHERE trang_thai = 'CHO';

-- test index không gắn với Q
EXPLAIN ANALYZE SELECT * FROM hoa_don WHERE nhan_vien_id = 'nv-001';
EXPLAIN ANALYZE SELECT * FROM hoa_don WHERE chinh_sach_giam_gia_id = 'csgg-001';




