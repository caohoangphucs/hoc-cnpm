package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.chi_tiet_hoa_don; business writes belong to stored procedures. */
@Entity
@Immutable
@Table(name = "chi_tiet_hoa_don", schema = "public")
public class ChiTietHoaDonEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "machitiet", nullable = false)
    private Integer maChiTiet;

    @Column(name = "mahoadon", nullable = false)
    private Integer maHoaDon;

    @Column(name = "madongia", nullable = true, length = 20)
    private String maDonGia;

    @Column(name = "tenmuc", nullable = false, length = 100)
    private String tenMuc;

    @Column(name = "soluong", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal soLuong;

    @Column(name = "dongia", nullable = false, precision = 15, scale = 0)
    private java.math.BigDecimal donGia;

    @Column(name = "thanhtien", nullable = false, precision = 15, scale = 0)
    private java.math.BigDecimal thanhTien;

    protected ChiTietHoaDonEntity() {}

    public Integer getMaChiTiet() { return maChiTiet; }
    public Integer getMaHoaDon() { return maHoaDon; }
    public String getMaDonGia() { return maDonGia; }
    public String getTenMuc() { return tenMuc; }
    public java.math.BigDecimal getSoLuong() { return soLuong; }
    public java.math.BigDecimal getDonGia() { return donGia; }
    public java.math.BigDecimal getThanhTien() { return thanhTien; }
}
