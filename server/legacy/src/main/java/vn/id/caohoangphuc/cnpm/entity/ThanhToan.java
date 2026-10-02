package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "THANH_TOAN")
public class ThanhToan implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaThanhToan")
    private Integer maThanhToan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaHoaDon", nullable = false)
    private HoaDon hoaDon;

    @Column(name = "MaGiaoDich", nullable = false, unique = true, length = 100)
    private String maGiaoDich;

    @Column(name = "PhuongThuc", nullable = false, length = 20)
    private String phuongThuc;

    @Column(name = "SoTien", nullable = false, precision = 15, scale = 0)
    private BigDecimal soTien;

    @Column(name = "TrangThai", length = 20)
    private String trangThai = "PENDING";

    @Column(name = "NgayGiaoDich", insertable = false, updatable = false)
    private LocalDateTime ngayGiaoDich;

    @Column(name = "NguoiXacNhan", length = 50)
    private String nguoiXacNhan;

    @Column(name = "GhiChu", columnDefinition = "TEXT")
    private String ghiChu;

    public ThanhToan() {}

    public Integer getMaThanhToan() { return maThanhToan; }
    public void setMaThanhToan(Integer maThanhToan) { this.maThanhToan = maThanhToan; }

    public HoaDon getHoaDon() { return hoaDon; }
    public void setHoaDon(HoaDon hoaDon) { this.hoaDon = hoaDon; }

    public String getMaGiaoDich() { return maGiaoDich; }
    public void setMaGiaoDich(String maGiaoDich) { this.maGiaoDich = maGiaoDich; }

    public String getPhuongThuc() { return phuongThuc; }
    public void setPhuongThuc(String phuongThuc) { this.phuongThuc = phuongThuc; }

    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getNgayGiaoDich() { return ngayGiaoDich; }
    public void setNgayGiaoDich(LocalDateTime ngayGiaoDich) { this.ngayGiaoDich = ngayGiaoDich; }

    public String getNguoiXacNhan() { return nguoiXacNhan; }
    public void setNguoiXacNhan(String nguoiXacNhan) { this.nguoiXacNhan = nguoiXacNhan; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
