package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.hoa_don; business writes belong to stored procedures. */
@Entity
@Immutable
@Table(name = "hoa_don", schema = "public")
public class HoaDonEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mahoadon", nullable = false)
    private Integer maHoaDon;

    @Column(name = "maphong", nullable = false, length = 20)
    private String maPhong;

    @Column(name = "kythang", nullable = false, length = 7)
    private String kyThang;

    @Column(name = "tienphong", nullable = false, precision = 15, scale = 0)
    private java.math.BigDecimal tienPhong;

    @Column(name = "tiendien", nullable = false, precision = 15, scale = 0)
    private java.math.BigDecimal tienDien;

    @Column(name = "tiennuoc", nullable = false, precision = 15, scale = 0)
    private java.math.BigDecimal tienNuoc;

    @Column(name = "tiendichvukhac", nullable = false, precision = 15, scale = 0)
    private java.math.BigDecimal tienDichVuKhac;

    @Column(name = "giamgia", nullable = false, precision = 15, scale = 0)
    private java.math.BigDecimal giamGia;

    @Column(name = "tongtien", nullable = false, precision = 15, scale = 0)
    private java.math.BigDecimal tongTien;

    @Column(name = "dathanhtoan", nullable = false, precision = 15, scale = 0)
    private java.math.BigDecimal daThanhToan;

    @Column(name = "hanthanhtoan", nullable = false)
    private java.time.LocalDate hanThanhToan;

    @Column(name = "trangthai", nullable = true, length = 20)
    private String trangThai;

    @Column(name = "ngayphathanh", nullable = true)
    private java.time.LocalDateTime ngayPhatHanh;

    protected HoaDonEntity() {}

    public Integer getMaHoaDon() { return maHoaDon; }
    public String getMaPhong() { return maPhong; }
    public String getKyThang() { return kyThang; }
    public java.math.BigDecimal getTienPhong() { return tienPhong; }
    public java.math.BigDecimal getTienDien() { return tienDien; }
    public java.math.BigDecimal getTienNuoc() { return tienNuoc; }
    public java.math.BigDecimal getTienDichVuKhac() { return tienDichVuKhac; }
    public java.math.BigDecimal getGiamGia() { return giamGia; }
    public java.math.BigDecimal getTongTien() { return tongTien; }
    public java.math.BigDecimal getDaThanhToan() { return daThanhToan; }
    public java.time.LocalDate getHanThanhToan() { return hanThanhToan; }
    public String getTrangThai() { return trangThai; }
    public java.time.LocalDateTime getNgayPhatHanh() { return ngayPhatHanh; }
}
