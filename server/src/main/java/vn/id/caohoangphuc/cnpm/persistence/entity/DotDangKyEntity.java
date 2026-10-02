package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.dot_dang_ky; business writes belong to stored procedures. */
@Entity
@Immutable
@Table(name = "dot_dang_ky", schema = "public")
public class DotDangKyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "madot", nullable = false)
    private Integer maDot;

    @Column(name = "mahocky", nullable = false, length = 20)
    private String maHocKy;

    @Column(name = "tendot", nullable = false, length = 100)
    private String tenDot;

    @Column(name = "thoigianmo", nullable = false)
    private java.time.LocalDateTime thoiGianMo;

    @Column(name = "thoigiandong", nullable = false)
    private java.time.LocalDateTime thoiGianDong;

    @Column(name = "trangthai", nullable = true, length = 20)
    private String trangThai;

    protected DotDangKyEntity() {}

    public Integer getMaDot() { return maDot; }
    public String getMaHocKy() { return maHocKy; }
    public String getTenDot() { return tenDot; }
    public java.time.LocalDateTime getThoiGianMo() { return thoiGianMo; }
    public java.time.LocalDateTime getThoiGianDong() { return thoiGianDong; }
    public String getTrangThai() { return trangThai; }
}
