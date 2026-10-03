package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.so_diem_cong_ich; business writes are handled by service-layer code. */
@Entity
@Immutable
@Table(name = "so_diem_cong_ich", schema = "public")
public class SoDiemCongIchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "magiaodich", nullable = false)
    private Integer maGiaoDich;

    @Column(name = "masinhvien", nullable = false, length = 20)
    private String maSinhVien;

    @Column(name = "mahocky", nullable = true, length = 20)
    private String maHocKy;

    @Column(name = "madangkycongich", nullable = true)
    private Integer maDangKyCongIch;

    @Column(name = "sodiemthaydoi", nullable = false)
    private Integer soDiemThayDoi;

    @Column(name = "loaigiaodich", nullable = false, length = 20)
    private String loaiGiaoDich;

    @Column(name = "ghichu", nullable = true, columnDefinition = "text")
    private String ghiChu;

    @Column(name = "thoigian", nullable = true)
    private java.time.LocalDateTime thoiGian;

    protected SoDiemCongIchEntity() {}

    public Integer getMaGiaoDich() { return maGiaoDich; }
    public String getMaSinhVien() { return maSinhVien; }
    public String getMaHocKy() { return maHocKy; }
    public Integer getMaDangKyCongIch() { return maDangKyCongIch; }
    public Integer getSoDiemThayDoi() { return soDiemThayDoi; }
    public String getLoaiGiaoDich() { return loaiGiaoDich; }
    public String getGhiChu() { return ghiChu; }
    public java.time.LocalDateTime getThoiGian() { return thoiGian; }
}
