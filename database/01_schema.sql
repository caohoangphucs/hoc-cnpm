-- =========================================================================
-- HE THONG QUAN LY KY TUC XA (KTX) - CONG NGHE PHAN MEM (CNPM)
-- SCHEMA, TABLES & INDEXES (POSTGRESQL 16)
-- =========================================================================

-- Remove routines from the previous schema; business rules now live in Java services.
DROP ROUTINE IF EXISTS usp_DangNhap(VARCHAR) CASCADE;
DROP ROUTINE IF EXISTS usp_DatGiuong(VARCHAR, VARCHAR) CASCADE;
DROP ROUTINE IF EXISTS usp_DuyetDangKy(INT, VARCHAR, TEXT) CASCADE;
DROP ROUTINE IF EXISTS usp_GhiChiSoDienNuoc(VARCHAR, VARCHAR, INT, INT, INT, INT, VARCHAR) CASCADE;
DROP ROUTINE IF EXISTS usp_PhatHanhHoaDon(VARCHAR, VARCHAR) CASCADE;
DROP ROUTINE IF EXISTS usp_XacNhanThanhToan(VARCHAR, VARCHAR, DECIMAL, VARCHAR) CASCADE;
DROP ROUTINE IF EXISTS usp_HoanThanhSuaChua(INT, VARCHAR, INT, VARCHAR, TEXT) CASCADE;
DROP ROUTINE IF EXISTS usp_GhiNhanRaVao(VARCHAR, VARCHAR, VARCHAR, VARCHAR) CASCADE;
DROP ROUTINE IF EXISTS usp_XuLyKyLuat(INT, VARCHAR, VARCHAR, VARCHAR) CASCADE;
DROP ROUTINE IF EXISTS usp_DangKyCongIch(VARCHAR, VARCHAR) CASCADE;

DROP TABLE IF EXISTS svc_room_price CASCADE;
DROP TABLE IF EXISTS svc_repair_files CASCADE;
DROP TABLE IF EXISTS svc_certificate CASCADE;
DROP TABLE IF EXISTS svc_conversion CASCADE;
DROP TABLE IF EXISTS svc_appeal CASCADE;
DROP TABLE IF EXISTS svc_discipline_decision CASCADE;
DROP TABLE IF EXISTS svc_visitor CASCADE;
DROP TABLE IF EXISTS svc_gate_event CASCADE;
DROP TABLE IF EXISTS svc_card CASCADE;
DROP TABLE IF EXISTS svc_purchase_request CASCADE;
DROP TABLE IF EXISTS svc_repair_history CASCADE;
DROP TABLE IF EXISTS svc_repair_assignment CASCADE;
DROP TABLE IF EXISTS svc_payment_callback CASCADE;
DROP TABLE IF EXISTS svc_payment_allocation CASCADE;
DROP TABLE IF EXISTS svc_payment_session CASCADE;
DROP TABLE IF EXISTS svc_room_extra CASCADE;
DROP TABLE IF EXISTS svc_invoice_snapshot CASCADE;
DROP TABLE IF EXISTS svc_invoice_draft CASCADE;
DROP TABLE IF EXISTS svc_outbox CASCADE;
DROP TABLE IF EXISTS svc_idempotency CASCADE;
DROP TABLE IF EXISTS svc_contract CASCADE;
DROP TABLE IF EXISTS svc_profile CASCADE;
DROP TABLE IF EXISTS svc_file CASCADE;
DROP TABLE IF EXISTS QUY_DOI_DIEM CASCADE;
DROP TABLE IF EXISTS SO_DIEM_CONG_ICH CASCADE;
DROP TABLE IF EXISTS DANG_KY_CONG_ICH CASCADE;
DROP TABLE IF EXISTS HOAT_DONG CASCADE;
DROP TABLE IF EXISTS DOT_DANG_KY CASCADE;
DROP TABLE IF EXISTS HOC_KY CASCADE;
DROP TABLE IF EXISTS AUDIT_LOG CASCADE;
DROP TABLE IF EXISTS XUAT_VAT_TU CASCADE;
DROP TABLE IF EXISTS YEU_CAU_SUA_CHUA CASCADE;
DROP TABLE IF EXISTS VAT_TU CASCADE;
DROP TABLE IF EXISTS VI_PHAM CASCADE;
DROP TABLE IF EXISTS RA_VAO CASCADE;
DROP TABLE IF EXISTS THANH_TOAN CASCADE;
DROP TABLE IF EXISTS CHI_TIET_HOA_DON CASCADE;
DROP TABLE IF EXISTS HOA_DON CASCADE;
DROP TABLE IF EXISTS CHI_SO_DIEN_NUOC CASCADE;
DROP TABLE IF EXISTS DON_GIA_DICH_VU CASCADE;
DROP TABLE IF EXISTS LUU_TRU CASCADE;
DROP TABLE IF EXISTS DANG_KY_PHONG CASCADE;
DROP TABLE IF EXISTS GIUONG CASCADE;
DROP TABLE IF EXISTS PHONG CASCADE;
DROP TABLE IF EXISTS LOAI_PHONG CASCADE;
DROP TABLE IF EXISTS TOA_NHA CASCADE;
DROP TABLE IF EXISTS SINH_VIEN_CHINH_SACH CASCADE;
DROP TABLE IF EXISTS CHINH_SACH_GIAM_GIA CASCADE;
DROP TABLE IF EXISTS SINH_VIEN CASCADE;
DROP TABLE IF EXISTS TAI_KHOAN CASCADE;

-- 1. TAI_KHOAN
CREATE TABLE TAI_KHOAN (
    MaTaiKhoan SERIAL PRIMARY KEY,
    TenDangNhap VARCHAR(50) NOT NULL UNIQUE,
    MatKhauHash VARCHAR(255) NOT NULL,
    HoTen VARCHAR(100) NOT NULL,
    Email VARCHAR(100) NOT NULL UNIQUE,
    SoDienThoai VARCHAR(20),
    VaiTro VARCHAR(20) NOT NULL CHECK (VaiTro IN ('ADMIN', 'KETOAN', 'KYTHUAT', 'BAOVE', 'SINHVIEN')),
    TrangThai VARCHAR(20) DEFAULT 'ACTIVE' CHECK (TrangThai IN ('ACTIVE', 'LOCKED', 'DISABLED')),
    BiMatTOTP TEXT,
    NgayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. TOA_NHA
CREATE TABLE TOA_NHA (
    MaToaNha VARCHAR(20) PRIMARY KEY,
    TenToa VARCHAR(100) NOT NULL,
    SoTang INT NOT NULL DEFAULT 1,
    GioiTinhApDung VARCHAR(20) DEFAULT 'TAT_CA' CHECK (GioiTinhApDung IN ('NAM', 'NU', 'TAT_CA')),
    GhiChu VARCHAR(255)
);

-- 3. LOAI_PHONG
CREATE TABLE LOAI_PHONG (
    MaLoaiPhong VARCHAR(20) PRIMARY KEY,
    TenLoaiPhong VARCHAR(100) NOT NULL,
    SucChuaTieuChuan INT NOT NULL DEFAULT 4,
    DonGiaThang DECIMAL(15, 0) NOT NULL DEFAULT 500000,
    MoTa TEXT
);

-- 4. PHONG
CREATE TABLE PHONG (
    MaPhong VARCHAR(20) PRIMARY KEY,
    MaToaNha VARCHAR(20) NOT NULL REFERENCES TOA_NHA(MaToaNha) ON DELETE CASCADE,
    MaLoaiPhong VARCHAR(20) REFERENCES LOAI_PHONG(MaLoaiPhong) ON DELETE SET NULL,
    SoPhong VARCHAR(20) NOT NULL,
    Tang INT NOT NULL DEFAULT 1,
    SucChua INT NOT NULL DEFAULT 4,
    DangO INT NOT NULL DEFAULT 0,
    GiaPhongCoBan DECIMAL(15, 0) NOT NULL DEFAULT 500000,
    TrangThai VARCHAR(20) DEFAULT 'TRONG' CHECK (TrangThai IN ('TRONG', 'CON_CHO', 'DAY', 'DANG_SUA', 'KHOA')),
    CONSTRAINT UQ_Phong_Toa_SoPhong UNIQUE (MaToaNha, SoPhong)
);

-- 5. GIUONG
CREATE TABLE GIUONG (
    MaGiuong VARCHAR(20) PRIMARY KEY,
    MaPhong VARCHAR(20) NOT NULL REFERENCES PHONG(MaPhong) ON DELETE CASCADE,
    SoGiuong VARCHAR(20) NOT NULL,
    ViTri VARCHAR(50) DEFAULT 'Tang 1',
    TrangThai VARCHAR(20) DEFAULT 'TRONG' CHECK (TrangThai IN ('TRONG', 'GIU_CHO', 'DANG_O', 'KHOA')),
    CONSTRAINT UQ_Giuong_Phong_SoGiuong UNIQUE (MaPhong, SoGiuong)
);

-- 6. SINH_VIEN
CREATE TABLE SINH_VIEN (
    MaSinhVien VARCHAR(20) PRIMARY KEY,
    MaTaiKhoan INT UNIQUE REFERENCES TAI_KHOAN(MaTaiKhoan) ON DELETE SET NULL,
    HoTen VARCHAR(100) NOT NULL,
    NgaySinh DATE,
    GioiTinh VARCHAR(10) NOT NULL CHECK (GioiTinh IN ('NAM', 'NU')),
    CCCD VARCHAR(20) NOT NULL UNIQUE,
    Email VARCHAR(100) NOT NULL UNIQUE,
    SoDienThoai VARCHAR(20) NOT NULL,
    Lop VARCHAR(50),
    Khoa VARCHAR(100),
    QueQuan VARCHAR(255),
    MaPhongHienTai VARCHAR(20) REFERENCES PHONG(MaPhong) ON DELETE SET NULL,
    TrangThai VARCHAR(20) DEFAULT 'CHO_DUYET' CHECK (TrangThai IN ('DANG_O', 'DA_ROI_KTX', 'CHO_DUYET'))
);

-- 7. DANG_KY_PHONG
CREATE TABLE DANG_KY_PHONG (
    MaDangKy SERIAL PRIMARY KEY,
    MaSinhVien VARCHAR(20) NOT NULL REFERENCES SINH_VIEN(MaSinhVien) ON DELETE CASCADE,
    MaGiuong VARCHAR(20) NOT NULL REFERENCES GIUONG(MaGiuong) ON DELETE CASCADE,
    NgayDangKy TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    NgayHetHan TIMESTAMP NOT NULL,
    TrangThai VARCHAR(20) DEFAULT 'CHO_DUYET' CHECK (TrangThai IN ('CHO_DUYET', 'DA_DUYET', 'TU_CHOI', 'HUY')),
    GhiChuLyDo TEXT
);

-- 8. LUU_TRU
CREATE TABLE LUU_TRU (
    MaLuuTru SERIAL PRIMARY KEY,
    MaDangKy INT UNIQUE REFERENCES DANG_KY_PHONG(MaDangKy) ON DELETE SET NULL,
    MaSinhVien VARCHAR(20) NOT NULL REFERENCES SINH_VIEN(MaSinhVien),
    MaGiuong VARCHAR(20) NOT NULL REFERENCES GIUONG(MaGiuong),
    NgayBatDau DATE NOT NULL,
    NgayKetThuc DATE,
    TrangThai VARCHAR(20) DEFAULT 'DANG_O' CHECK (TrangThai IN ('DANG_O', 'DA_TRA_PHONG'))
);

-- 9. DON_GIA_DICH_VU
CREATE TABLE DON_GIA_DICH_VU (
    MaDonGia VARCHAR(20) PRIMARY KEY,
    LoaiDichVu VARCHAR(20) NOT NULL CHECK (LoaiDichVu IN ('DIEN', 'NUOC', 'INTERNET', 'GUI_XE', 'KHAC')),
    TenDichVu VARCHAR(100) NOT NULL,
    DonViTinh VARCHAR(20) NOT NULL,
    DonGia DECIMAL(15, 0) NOT NULL,
    NgayApDung DATE NOT NULL,
    TrangThai VARCHAR(20) DEFAULT 'ACTIVE' CHECK (TrangThai IN ('ACTIVE', 'INACTIVE'))
);

-- 10. CHI_SO_DIEN_NUOC
CREATE TABLE CHI_SO_DIEN_NUOC (
    MaChiSo SERIAL PRIMARY KEY,
    MaPhong VARCHAR(20) NOT NULL REFERENCES PHONG(MaPhong) ON DELETE CASCADE,
    KyThang VARCHAR(7) NOT NULL,
    DienCu INT NOT NULL DEFAULT 0,
    DienMoi INT NOT NULL DEFAULT 0,
    NuocCu INT NOT NULL DEFAULT 0,
    NuocMoi INT NOT NULL DEFAULT 0,
    NgayGhi TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    NguoiGhi VARCHAR(50),
    CONSTRAINT UQ_CSDN_Phong_KyThang UNIQUE (MaPhong, KyThang)
);

-- 11. HOA_DON
CREATE TABLE HOA_DON (
    MaHoaDon SERIAL PRIMARY KEY,
    MaPhong VARCHAR(20) NOT NULL REFERENCES PHONG(MaPhong) ON DELETE CASCADE,
    KyThang VARCHAR(7) NOT NULL,
    TienPhong DECIMAL(15, 0) NOT NULL DEFAULT 0,
    TienDien DECIMAL(15, 0) NOT NULL DEFAULT 0,
    TienNuoc DECIMAL(15, 0) NOT NULL DEFAULT 0,
    TienDichVuKhac DECIMAL(15, 0) NOT NULL DEFAULT 0,
    GiamGia DECIMAL(15, 0) NOT NULL DEFAULT 0,
    TongTien DECIMAL(15, 0) NOT NULL DEFAULT 0,
    DaThanhToan DECIMAL(15, 0) NOT NULL DEFAULT 0,
    HanThanhToan DATE NOT NULL,
    TrangThai VARCHAR(20) DEFAULT 'CHUA_TT' CHECK (TrangThai IN ('CHUA_TT', 'TT_MOT_PHAN', 'DA_TT', 'QUA_HAN', 'HUY')),
    NgayPhatHanh TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT UQ_HoaDon_Phong_KyThang UNIQUE (MaPhong, KyThang)
);

-- 12. CHI_TIET_HOA_DON
CREATE TABLE CHI_TIET_HOA_DON (
    MaChiTiet SERIAL PRIMARY KEY,
    MaHoaDon INT NOT NULL REFERENCES HOA_DON(MaHoaDon) ON DELETE CASCADE,
    MaDonGia VARCHAR(20) REFERENCES DON_GIA_DICH_VU(MaDonGia) ON DELETE SET NULL,
    TenMuc VARCHAR(100) NOT NULL,
    SoLuong DECIMAL(10, 2) NOT NULL DEFAULT 1,
    DonGia DECIMAL(15, 0) NOT NULL,
    ThanhTien DECIMAL(15, 0) NOT NULL
);

-- 13. THANH_TOAN
CREATE TABLE THANH_TOAN (
    MaThanhToan SERIAL PRIMARY KEY,
    MaHoaDon INT NOT NULL REFERENCES HOA_DON(MaHoaDon),
    MaGiaoDich VARCHAR(100) NOT NULL UNIQUE,
    PhuongThuc VARCHAR(20) NOT NULL CHECK (PhuongThuc IN ('VNPAY', 'MOMO', 'VIETQR', 'TIEN_MAT')),
    SoTien DECIMAL(15, 0) NOT NULL,
    TrangThai VARCHAR(20) DEFAULT 'PENDING' CHECK (TrangThai IN ('PENDING', 'SUCCESS', 'FAILED')),
    NgayGiaoDich TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    NguoiXacNhan VARCHAR(50),
    GhiChu TEXT
);

-- 14. RA_VAO
CREATE TABLE RA_VAO (
    MaRaVao SERIAL PRIMARY KEY,
    MaSinhVien VARCHAR(20) NOT NULL REFERENCES SINH_VIEN(MaSinhVien) ON DELETE CASCADE,
    LoaiSuKien VARCHAR(10) NOT NULL CHECK (LoaiSuKien IN ('VAO', 'RA')),
    ThoiGian TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    BaoVeGhiNhan VARCHAR(50),
    GhiChu VARCHAR(255)
);

-- 15. VI_PHAM
CREATE TABLE VI_PHAM (
    MaViPham SERIAL PRIMARY KEY,
    MaSinhVien VARCHAR(20) NOT NULL REFERENCES SINH_VIEN(MaSinhVien) ON DELETE CASCADE,
    MaRaVao INT REFERENCES RA_VAO(MaRaVao) ON DELETE SET NULL,
    NoiDungViPham TEXT NOT NULL,
    MucDo VARCHAR(20) DEFAULT 'LOW' CHECK (MucDo IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    HinhThucXuLy VARCHAR(255),
    NgayViPham TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    TrangThai VARCHAR(20) DEFAULT 'CHUA_XU_LY' CHECK (TrangThai IN ('CHUA_XU_LY', 'DA_XU_LY'))
);

-- 16. VAT_TU
CREATE TABLE VAT_TU (
    MaVatTu VARCHAR(20) PRIMARY KEY,
    TenVatTu VARCHAR(100) NOT NULL,
    DonViTinh VARCHAR(20) NOT NULL,
    SoLuongTon INT NOT NULL DEFAULT 0,
    DonGiaNhap DECIMAL(15, 0) NOT NULL DEFAULT 0,
    GhiChu TEXT
);

-- 17. YEU_CAU_SUA_CHUA
CREATE TABLE YEU_CAU_SUA_CHUA (
    MaYeuCau SERIAL PRIMARY KEY,
    MaPhong VARCHAR(20) NOT NULL REFERENCES PHONG(MaPhong) ON DELETE CASCADE,
    MaSinhVien VARCHAR(20) NOT NULL REFERENCES SINH_VIEN(MaSinhVien) ON DELETE CASCADE,
    TieuDe VARCHAR(255) NOT NULL,
    MoTaChiTiet TEXT NOT NULL,
    MucDoUuTien VARCHAR(20) DEFAULT 'TRUNG_BINH' CHECK (MucDoUuTien IN ('THAP', 'TRUNG_BINH', 'CAO', 'KHAN_CAP')),
    TrangThai VARCHAR(20) DEFAULT 'MOI' CHECK (TrangThai IN ('MOI', 'DANG_XU_LY', 'HOAN_THANH', 'HUY')),
    NgayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    NgayHoanThanh TIMESTAMP NULL,
    GhiChuKyThuat TEXT
);

-- 18. XUAT_VAT_TU
CREATE TABLE XUAT_VAT_TU (
    MaXuat SERIAL PRIMARY KEY,
    MaVatTu VARCHAR(20) NOT NULL REFERENCES VAT_TU(MaVatTu),
    MaYeuCau INT REFERENCES YEU_CAU_SUA_CHUA(MaYeuCau) ON DELETE SET NULL,
    SoLuong INT NOT NULL,
    NgayXuat TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    NguoiXuat VARCHAR(50),
    GhiChu TEXT
);

-- 19. AUDIT_LOG
CREATE TABLE AUDIT_LOG (
    MaLog SERIAL PRIMARY KEY,
    TenBang VARCHAR(50) NOT NULL,
    HanhDong VARCHAR(20) NOT NULL CHECK (HanhDong IN ('INSERT', 'UPDATE', 'DELETE')),
    DuLieuCu JSONB NULL,
    DuLieuMoi JSONB NULL,
    NguoiThucHien VARCHAR(50) DEFAULT 'SYSTEM',
    ThoiGian TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- 20. HOC_KY & DOT_DANG_KY (Phục vụ UC-04, UC-12, UC-14, UC-30)
CREATE TABLE HOC_KY (
    MaHocKy VARCHAR(20) PRIMARY KEY,
    TenHocKy VARCHAR(100) NOT NULL,
    NgayBatDau DATE NOT NULL,
    NgayKetThuc DATE NOT NULL,
    TrangThai VARCHAR(20) DEFAULT 'ACTIVE' CHECK (TrangThai IN ('ACTIVE', 'CLOSED')),
    CONSTRAINT CHK_HocKy_Dates CHECK (NgayBatDau <= NgayKetThuc)
);

CREATE TABLE DOT_DANG_KY (
    MaDot SERIAL PRIMARY KEY,
    MaHocKy VARCHAR(20) NOT NULL REFERENCES HOC_KY(MaHocKy) ON DELETE CASCADE,
    TenDot VARCHAR(100) NOT NULL,
    ThoiGianMo TIMESTAMP NOT NULL,
    ThoiGianDong TIMESTAMP NOT NULL,
    TrangThai VARCHAR(20) DEFAULT 'ACTIVE' CHECK (TrangThai IN ('ACTIVE', 'CLOSED')),
    CONSTRAINT CHK_Dot_Dates CHECK (ThoiGianMo < ThoiGianDong)
);

-- 21. CHINH_SACH_GIAM_GIA & SINH_VIEN_CHINH_SACH (Phục vụ UC-17, REQ-008)
CREATE TABLE CHINH_SACH_GIAM_GIA (
    MaChinhSach VARCHAR(20) PRIMARY KEY,
    TenChinhSach VARCHAR(150) NOT NULL,
    TyLeGiam DECIMAL(5, 2) NOT NULL CHECK (TyLeGiam >= 0 AND TyLeGiam <= 100),
    DieuKienApDung TEXT,
    NgayBatDau DATE NOT NULL,
    NgayKetThuc DATE,
    TrangThai VARCHAR(20) DEFAULT 'DANG_AP_DUNG' CHECK (TrangThai IN ('DANG_AP_DUNG', 'HET_HAN'))
);

CREATE TABLE SINH_VIEN_CHINH_SACH (
    MaSinhVien VARCHAR(20) NOT NULL REFERENCES SINH_VIEN(MaSinhVien) ON DELETE CASCADE,
    MaChinhSach VARCHAR(20) NOT NULL REFERENCES CHINH_SACH_GIAM_GIA(MaChinhSach) ON DELETE RESTRICT,
    NgayXacNhan DATE DEFAULT CURRENT_DATE,
    NguoiXacNhan VARCHAR(50),
    PRIMARY KEY (MaSinhVien, MaChinhSach)
);

-- 22. HOAT_DONG & CONG_ICH (Phục vụ UC-30, REQ-017, REQ-029, REQ-038)
CREATE TABLE HOAT_DONG (
    MaHoatDong VARCHAR(20) PRIMARY KEY,
    MaHocKy VARCHAR(20) REFERENCES HOC_KY(MaHocKy) ON DELETE SET NULL,
    TieuDe VARCHAR(255) NOT NULL,
    MoTa TEXT,
    ThoiGianBatDau TIMESTAMP NOT NULL,
    ThoiGianKetThuc TIMESTAMP NOT NULL,
    DiaDiem VARCHAR(255),
    ChiTieu INT NOT NULL CHECK (ChiTieu > 0),
    DaDangKy INT NOT NULL DEFAULT 0 CHECK (DaDangKy >= 0 AND DaDangKy <= ChiTieu),
    DiemCongIch INT NOT NULL DEFAULT 10 CHECK (DiemCongIch > 0),
    TrangThai VARCHAR(20) DEFAULT 'MO_DANG_KY' CHECK (TrangThai IN ('MO_DANG_KY', 'DONG_DANG_KY', 'HOAN_THANH', 'HUY')),
    CONSTRAINT CHK_HoatDong_Time CHECK (ThoiGianBatDau < ThoiGianKetThuc)
);

CREATE TABLE DANG_KY_CONG_ICH (
    MaDangKyCongIch SERIAL PRIMARY KEY,
    MaHoatDong VARCHAR(20) NOT NULL REFERENCES HOAT_DONG(MaHoatDong) ON DELETE CASCADE,
    MaSinhVien VARCHAR(20) NOT NULL REFERENCES SINH_VIEN(MaSinhVien) ON DELETE CASCADE,
    NgayDangKy TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    MaQRToken VARCHAR(100) UNIQUE,
    TrangThai VARCHAR(20) DEFAULT 'REGISTERED' CHECK (TrangThai IN ('REGISTERED', 'ATTENDED', 'COMPLETED', 'CANCELLED')),
    NguoiXacNhan VARCHAR(50),
    NgayXacNhan TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 1 CHECK (version > 0)
);

CREATE TABLE SO_DIEM_CONG_ICH (
    MaGiaoDich SERIAL PRIMARY KEY,
    MaSinhVien VARCHAR(20) NOT NULL REFERENCES SINH_VIEN(MaSinhVien) ON DELETE CASCADE,
    MaHocKy VARCHAR(20) REFERENCES HOC_KY(MaHocKy) ON DELETE SET NULL,
    MaDangKyCongIch INT REFERENCES DANG_KY_CONG_ICH(MaDangKyCongIch) ON DELETE SET NULL,
    SoDiemThayDoi INT NOT NULL,
    LoaiGiaoDich VARCHAR(20) NOT NULL CHECK (LoaiGiaoDich IN ('CONG_DIEM', 'QUY_DOI', 'DIEU_CHINH')),
    GhiChu TEXT,
    ThoiGian TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE QUY_DOI_DIEM (
    MaQuyDoi SERIAL PRIMARY KEY,
    MaSinhVien VARCHAR(20) NOT NULL REFERENCES SINH_VIEN(MaSinhVien) ON DELETE CASCADE,
    MaHocKy VARCHAR(20) NOT NULL REFERENCES HOC_KY(MaHocKy),
    DiemCongIchTieu INT NOT NULL CHECK (DiemCongIchTieu > 0),
    DiemRenLuyenNhan INT NOT NULL CHECK (DiemRenLuyenNhan >= 1 AND DiemRenLuyenNhan <= 5),
    NgayQuyDoi TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    TrangThai VARCHAR(20) DEFAULT 'PENDING' CHECK (TrangThai IN ('PENDING', 'THANH_CONG', 'TU_CHOI'))
);

-- Service storage only. All new business rules are implemented in Java.
CREATE UNIQUE INDEX IF NOT EXISTS svc_active_student ON luu_tru(masinhvien) WHERE trangthai='DANG_O';
CREATE UNIQUE INDEX IF NOT EXISTS svc_active_bed ON luu_tru(magiuong) WHERE trangthai='DANG_O';
CREATE UNIQUE INDEX IF NOT EXISTS svc_pending_booking ON dang_ky_phong(masinhvien) WHERE trangthai='CHO_DUYET';
ALTER TABLE hoc_ky ADD COLUMN IF NOT EXISTS public_id bigint GENERATED BY DEFAULT AS IDENTITY;
CREATE UNIQUE INDEX IF NOT EXISTS svc_semester_id ON hoc_ky(public_id);
ALTER TABLE hoat_dong ADD COLUMN IF NOT EXISTS public_id bigint GENERATED BY DEFAULT AS IDENTITY;
CREATE UNIQUE INDEX IF NOT EXISTS svc_activity_id ON hoat_dong(public_id);
ALTER TABLE giuong ADD COLUMN IF NOT EXISTS version bigint NOT NULL DEFAULT 1 CHECK(version>0);
ALTER TABLE dang_ky_phong ADD COLUMN IF NOT EXISTS version bigint NOT NULL DEFAULT 1 CHECK(version>0);
ALTER TABLE yeu_cau_sua_chua ADD COLUMN IF NOT EXISTS version bigint NOT NULL DEFAULT 1 CHECK(version>0);
ALTER TABLE vi_pham ADD COLUMN IF NOT EXISTS version bigint NOT NULL DEFAULT 1 CHECK(version>0);
ALTER TABLE hoat_dong ADD COLUMN IF NOT EXISTS version bigint NOT NULL DEFAULT 1 CHECK(version>0);
ALTER TABLE dang_ky_cong_ich ADD COLUMN IF NOT EXISTS version bigint NOT NULL DEFAULT 1 CHECK(version>0);
ALTER TABLE dang_ky_cong_ich DROP CONSTRAINT IF EXISTS uq_sv_hoatdong;
CREATE UNIQUE INDEX IF NOT EXISTS svc_active_community ON dang_ky_cong_ich(mahoatdong,masinhvien) WHERE trangthai<>'CANCELLED';
CREATE UNIQUE INDEX IF NOT EXISTS svc_earned_once ON so_diem_cong_ich(madangkycongich) WHERE loaigiaodich='CONG_DIEM';
CREATE TABLE IF NOT EXISTS svc_file (
 id uuid PRIMARY KEY, owner_id int NOT NULL REFERENCES tai_khoan, kind text NOT NULL
 CHECK(kind IN ('CCCD_FRONT','CCCD_BACK','STUDENT_CARD','EVIDENCE','REPAIR','POLICY')),
 object_key text NOT NULL UNIQUE, mime text NOT NULL CHECK(mime IN ('image/jpeg','image/png')),
 size_bytes int NOT NULL CHECK(size_bytes BETWEEN 1 AND 5242880), sha256 text NOT NULL,
 key_id text NOT NULL, nonce bytea NOT NULL, tag bytea NOT NULL,
 state text NOT NULL CHECK(state IN ('DRAFT','ATTACHED','REJECTED')),
 scanned_at timestamptz NOT NULL, expires_at timestamptz NOT NULL,
 created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS svc_profile (
 booking_id int PRIMARY KEY REFERENCES dang_ky_phong, window_id int NOT NULL REFERENCES dot_dang_ky,
 file_ids jsonb NOT NULL, accepted_at timestamptz NOT NULL, state text NOT NULL CHECK(state IN ('VALID','REJECTED')),
 approved_by int REFERENCES tai_khoan
);
CREATE TABLE IF NOT EXISTS svc_contract (
 residency_id int PRIMARY KEY REFERENCES luu_tru, semester_id bigint NOT NULL,
 state text NOT NULL DEFAULT 'DRAFT' CHECK(state IN ('DRAFT','ACTIVE','ENDED')),
 start_date date NOT NULL, end_date date NOT NULL, CHECK(start_date<=end_date)
);
CREATE TABLE IF NOT EXISTS svc_idempotency (
 actor_id int NOT NULL REFERENCES tai_khoan, route text NOT NULL, key uuid NOT NULL,
 request_hash text NOT NULL, response jsonb NOT NULL, expires_at timestamptz NOT NULL,
 PRIMARY KEY(actor_id,route,key)
);
CREATE TABLE IF NOT EXISTS svc_outbox (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), event_type text NOT NULL, aggregate_id text NOT NULL,
 payload jsonb NOT NULL, dedupe_key text NOT NULL UNIQUE,
 state text NOT NULL DEFAULT 'NEW' CHECK(state IN ('NEW','LEASED','SENT','DEAD')),
 attempts int NOT NULL DEFAULT 0 CHECK(attempts>=0), next_attempt_at timestamptz NOT NULL DEFAULT now(),
 lease_until timestamptz, last_error text, created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS svc_outbox_pending ON svc_outbox(next_attempt_at) WHERE state IN ('NEW','LEASED');
CREATE TABLE IF NOT EXISTS svc_invoice_draft (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), actor_id int NOT NULL REFERENCES tai_khoan,
 month text NOT NULL CHECK(month~'^\d{4}-(0[1-9]|1[0-2])$'), version bigint NOT NULL DEFAULT 1,
 inputs jsonb NOT NULL, input_hash text NOT NULL, created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS svc_invoice_snapshot (
 invoice_id int PRIMARY KEY REFERENCES hoa_don, input_hash text NOT NULL, inputs jsonb NOT NULL
);
CREATE TABLE IF NOT EXISTS svc_room_extra (
 id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, room_id varchar(20) NOT NULL REFERENCES phong,
 month text NOT NULL, name text NOT NULL, amount numeric(15,0) NOT NULL CHECK(amount>=0),
 evidence_id uuid NOT NULL REFERENCES svc_file, approved_by int NOT NULL REFERENCES tai_khoan
);
CREATE TABLE IF NOT EXISTS svc_payment_session (
 id uuid PRIMARY KEY, actor_id int NOT NULL REFERENCES tai_khoan, gateway text NOT NULL CHECK(gateway IN ('VNPAY','MOMO')),
 provider_ref text NOT NULL UNIQUE, amount numeric(15,0) NOT NULL CHECK(amount>0),
 state text NOT NULL DEFAULT 'PENDING' CHECK(state IN ('PENDING','SUCCESS','FAILED','EXPIRED','NEEDS_REVIEW')),
 provider_transaction_id text, expires_at timestamptz NOT NULL, pay_url text,
 created_at timestamptz NOT NULL DEFAULT now(), UNIQUE(gateway,provider_transaction_id)
);
CREATE TABLE IF NOT EXISTS svc_payment_allocation (
 session_id uuid NOT NULL REFERENCES svc_payment_session, invoice_id int NOT NULL REFERENCES hoa_don,
 amount numeric(15,0) NOT NULL CHECK(amount>0), child_ref varchar(100) NOT NULL UNIQUE,
 PRIMARY KEY(session_id,invoice_id)
);
CREATE TABLE IF NOT EXISTS svc_payment_callback (
 gateway text NOT NULL, event_key text NOT NULL, session_id uuid NOT NULL REFERENCES svc_payment_session,
 payload_hash text NOT NULL, result jsonb NOT NULL, received_at timestamptz NOT NULL,
 PRIMARY KEY(gateway,event_key)
);
CREATE TABLE IF NOT EXISTS svc_repair_assignment (
 repair_id int PRIMARY KEY REFERENCES yeu_cau_sua_chua, technician_id int NOT NULL REFERENCES tai_khoan,
 scheduled_at timestamptz, warranty_until date, file_ids jsonb NOT NULL DEFAULT '[]'
);
CREATE TABLE IF NOT EXISTS svc_repair_history (
 id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, repair_id int NOT NULL REFERENCES yeu_cau_sua_chua,
 actor_id int NOT NULL REFERENCES tai_khoan, from_state text NOT NULL, to_state text NOT NULL,
 note text NOT NULL, created_at timestamptz NOT NULL
);
CREATE TABLE IF NOT EXISTS svc_purchase_request (
 id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, repair_id int NOT NULL REFERENCES yeu_cau_sua_chua,
 supply_id varchar(20) NOT NULL REFERENCES vat_tu, quantity int NOT NULL CHECK(quantity>0),
 state text NOT NULL DEFAULT 'NEW', request_id uuid NOT NULL, UNIQUE(repair_id,supply_id,request_id)
);
CREATE TABLE IF NOT EXISTS svc_card (
 id uuid PRIMARY KEY, student_id varchar(20) NOT NULL REFERENCES sinh_vien, token_hash text NOT NULL UNIQUE,
 valid_from timestamptz NOT NULL, valid_to timestamptz NOT NULL, state text NOT NULL CHECK(state IN ('ACTIVE','REVOKED')),
 CHECK(valid_from<valid_to)
);
CREATE TABLE IF NOT EXISTS svc_gate_event (
 event_id uuid PRIMARY KEY, entry_id int NOT NULL UNIQUE REFERENCES ra_vao, device_id text NOT NULL,
 payload_hash text NOT NULL, actor_id int NOT NULL REFERENCES tai_khoan,
 barrier_status text NOT NULL DEFAULT 'PENDING' CHECK(barrier_status IN ('PENDING','OPENED','FAILED'))
);
CREATE TABLE IF NOT EXISTS svc_visitor (
 id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, host_student_id varchar(20) NOT NULL REFERENCES sinh_vien,
 name_encrypted text NOT NULL, identity_encrypted text NOT NULL,
 valid_from timestamptz NOT NULL, valid_to timestamptz NOT NULL, state text NOT NULL DEFAULT 'PENDING'
 CHECK(state IN ('PENDING','APPROVED','REJECTED')), version bigint NOT NULL DEFAULT 1,
 approved_by int REFERENCES tai_khoan, checked_in_at timestamptz, checked_out_at timestamptz,
 CHECK(valid_from<valid_to)
);
CREATE TABLE IF NOT EXISTS svc_discipline_decision (
 id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, violation_id int NOT NULL REFERENCES vi_pham,
 semester_id bigint NOT NULL, revision int NOT NULL, actor_id int NOT NULL REFERENCES tai_khoan,
 decision text NOT NULL, reason text NOT NULL, evidence_id uuid NOT NULL REFERENCES svc_file,
 created_at timestamptz NOT NULL, UNIQUE(violation_id,revision)
);
CREATE TABLE IF NOT EXISTS svc_appeal (
 id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, decision_id bigint NOT NULL REFERENCES svc_discipline_decision,
 student_id varchar(20) NOT NULL REFERENCES sinh_vien, content text NOT NULL,
 state text NOT NULL DEFAULT 'OPEN' CHECK(state IN ('OPEN','ACCEPTED','REJECTED')),
 response text, handled_by int REFERENCES tai_khoan, version bigint NOT NULL DEFAULT 1
);
CREATE TABLE IF NOT EXISTS svc_conversion (
 id uuid PRIMARY KEY, legacy_id int NOT NULL UNIQUE REFERENCES quy_doi_diem,
 state text NOT NULL DEFAULT 'PENDING' CHECK(state IN ('PENDING','SUCCESS','REJECTED')),
 external_ref text, compensated_at timestamptz
);
CREATE TABLE IF NOT EXISTS svc_certificate (
 id uuid PRIMARY KEY, actor_id int NOT NULL REFERENCES tai_khoan, student_id varchar(20) NOT NULL REFERENCES sinh_vien,
 semester_id bigint NOT NULL, snapshot jsonb NOT NULL, token_hash text NOT NULL UNIQUE,
 state text NOT NULL DEFAULT 'PENDING' CHECK(state IN ('PENDING','SIGNED','FAILED','REVOKED')),
 object_key text, sha256 text, signed_at timestamptz, revoked_at timestamptz
);
CREATE TABLE IF NOT EXISTS svc_repair_files (
 repair_id int PRIMARY KEY REFERENCES yeu_cau_sua_chua, file_ids jsonb NOT NULL
);
CREATE TABLE IF NOT EXISTS svc_room_price (
 room_id varchar(20) NOT NULL REFERENCES phong, effective_from date NOT NULL,
 amount numeric(15,0) NOT NULL CHECK(amount>=0), PRIMARY KEY(room_id,effective_from)
);

-- VIEWS
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
    COALESCE(SUM(p.SucChua), 0) AS TongSucChua,
    COALESCE(SUM(p.DangO), 0) AS TongDangO,
    ROUND((COALESCE(SUM(p.DangO), 0) * 100.0 / NULLIF(SUM(p.SucChua), 0)), 2) AS TyLeLapDayPhanTram
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
