package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.thanh_toan; business writes belong to stored procedures. */
@Entity
@Immutable
@Table(name = "thanh_toan", schema = "public")
public class ThanhToanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mathanhtoan", nullable = false)
    private Integer maThanhToan;

    @Column(name = "mahoadon", nullable = false)
    private Integer maHoaDon;

    @Column(name = "magiaodich", nullable = false, length = 100)
    private String maGiaoDich;

    @Column(name = "phuongthuc", nullable = false, length = 20)
    private String phuongThuc;

    @Column(name = "sotien", nullable = false, precision = 15, scale = 0)
    private java.math.BigDecimal soTien;

    @Column(name = "trangthai", nullable = true, length = 20)
    private String trangThai;

    @Column(name = "ngaygiaodich", nullable = true)
    private java.time.LocalDateTime ngayGiaoDich;

    @Column(name = "nguoixacnhan", nullable = true, length = 50)
    private String nguoiXacNhan;

    @Column(name = "ghichu", nullable = true, columnDefinition = "text")
    private String ghiChu;

    protected ThanhToanEntity() {}

    public Integer getMaThanhToan() { return maThanhToan; }
    public Integer getMaHoaDon() { return maHoaDon; }
    public String getMaGiaoDich() { return maGiaoDich; }
    public String getPhuongThuc() { return phuongThuc; }
    public java.math.BigDecimal getSoTien() { return soTien; }
    public String getTrangThai() { return trangThai; }
    public java.time.LocalDateTime getNgayGiaoDich() { return ngayGiaoDich; }
    public String getNguoiXacNhan() { return nguoiXacNhan; }
    public String getGhiChu() { return ghiChu; }
}
