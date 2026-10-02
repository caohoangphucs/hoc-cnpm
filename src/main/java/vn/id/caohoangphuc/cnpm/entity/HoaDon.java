package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "HOA_DON", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"MaPhong", "KyThang"})
})
public class HoaDon implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHoaDon")
    private Integer maHoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaPhong", nullable = false)
    private Phong phong;

    @Column(name = "KyThang", nullable = false, length = 7)
    private String kyThang;

    @Column(name = "TienPhong", nullable = false, precision = 15, scale = 0)
    private BigDecimal tienPhong = BigDecimal.ZERO;

    @Column(name = "TienDien", nullable = false, precision = 15, scale = 0)
    private BigDecimal tienDien = BigDecimal.ZERO;

    @Column(name = "TienNuoc", nullable = false, precision = 15, scale = 0)
    private BigDecimal tienNuoc = BigDecimal.ZERO;

    @Column(name = "TienDichVuKhac", nullable = false, precision = 15, scale = 0)
    private BigDecimal tienDichVuKhac = BigDecimal.ZERO;

    @Column(name = "GiamGia", nullable = false, precision = 15, scale = 0)
    private BigDecimal giamGia = BigDecimal.ZERO;

    @Column(name = "TongTien", nullable = false, precision = 15, scale = 0)
    private BigDecimal tongTien = BigDecimal.ZERO;

    @Column(name = "DaThanhToan", nullable = false, precision = 15, scale = 0)
    private BigDecimal daThanhToan = BigDecimal.ZERO;

    @Column(name = "HanThanhToan", nullable = false)
    private LocalDate hanThanhToan;

    @Column(name = "TrangThai", length = 20)
    private String trangThai = "CHUA_TT";

    @Column(name = "NgayPhatHanh", insertable = false, updatable = false)
    private LocalDateTime ngayPhatHanh;

    public HoaDon() {}

    public Integer getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(Integer maHoaDon) { this.maHoaDon = maHoaDon; }

    public Phong getPhong() { return phong; }
    public void setPhong(Phong phong) { this.phong = phong; }

    public String getKyThang() { return kyThang; }
    public void setKyThang(String kyThang) { this.kyThang = kyThang; }

    public BigDecimal getTienPhong() { return tienPhong; }
    public void setTienPhong(BigDecimal tienPhong) { this.tienPhong = tienPhong; }

    public BigDecimal getTienDien() { return tienDien; }
    public void setTienDien(BigDecimal tienDien) { this.tienDien = tienDien; }

    public BigDecimal getTienNuoc() { return tienNuoc; }
    public void setTienNuoc(BigDecimal tienNuoc) { this.tienNuoc = tienNuoc; }

    public BigDecimal getTienDichVuKhac() { return tienDichVuKhac; }
    public void setTienDichVuKhac(BigDecimal tienDichVuKhac) { this.tienDichVuKhac = tienDichVuKhac; }

    public BigDecimal getGiamGia() { return giamGia; }
    public void setGiamGia(BigDecimal giamGia) { this.giamGia = giamGia; }

    public BigDecimal getTongTien() { return tongTien; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }

    public BigDecimal getDaThanhToan() { return daThanhToan; }
    public void setDaThanhToan(BigDecimal daThanhToan) { this.daThanhToan = daThanhToan; }

    public LocalDate getHanThanhToan() { return hanThanhToan; }
    public void setHanThanhToan(LocalDate hanThanhToan) { this.hanThanhToan = hanThanhToan; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getNgayPhatHanh() { return ngayPhatHanh; }
    public void setNgayPhatHanh(LocalDateTime ngayPhatHanh) { this.ngayPhatHanh = ngayPhatHanh; }
}
