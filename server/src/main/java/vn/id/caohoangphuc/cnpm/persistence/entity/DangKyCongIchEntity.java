package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.dang_ky_cong_ich; business writes belong to stored procedures. */
@Entity
@Immutable
@Table(name = "dang_ky_cong_ich", schema = "public")
public class DangKyCongIchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "madangkycongich", nullable = false)
    private Integer maDangKyCongIch;

    @Column(name = "mahoatdong", nullable = false, length = 20)
    private String maHoatDong;

    @Column(name = "masinhvien", nullable = false, length = 20)
    private String maSinhVien;

    @Column(name = "ngaydangky", nullable = true)
    private java.time.LocalDateTime ngayDangKy;

    @Column(name = "maqrtoken", nullable = true, length = 100)
    private String maQRToken;

    @Column(name = "trangthai", nullable = true, length = 20)
    private String trangThai;

    @Column(name = "nguoixacnhan", nullable = true, length = 50)
    private String nguoiXacNhan;

    @Column(name = "ngayxacnhan", nullable = true)
    private java.time.LocalDateTime ngayXacNhan;

    protected DangKyCongIchEntity() {}

    public Integer getMaDangKyCongIch() { return maDangKyCongIch; }
    public String getMaHoatDong() { return maHoatDong; }
    public String getMaSinhVien() { return maSinhVien; }
    public java.time.LocalDateTime getNgayDangKy() { return ngayDangKy; }
    public String getMaQRToken() { return maQRToken; }
    public String getTrangThai() { return trangThai; }
    public String getNguoiXacNhan() { return nguoiXacNhan; }
    public java.time.LocalDateTime getNgayXacNhan() { return ngayXacNhan; }
}
