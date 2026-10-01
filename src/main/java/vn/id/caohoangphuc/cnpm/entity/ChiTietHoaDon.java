package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "CHI_TIET_HOA_DON")
public class ChiTietHoaDon implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaChiTiet")
    private Integer maChiTiet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaHoaDon", nullable = false)
    private HoaDon hoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDonGia")
    private DonGiaDichVu donGiaDichVu;

    @Column(name = "TenMuc", nullable = false, length = 100)
    private String tenMuc;

    @Column(name = "SoLuong", nullable = false, precision = 10, scale = 2)
    private BigDecimal soLuong = BigDecimal.ONE;

    @Column(name = "DonGia", nullable = false, precision = 15, scale = 0)
    private BigDecimal donGia;

    @Column(name = "ThanhTien", nullable = false, precision = 15, scale = 0)
    private BigDecimal thanhTien;

    public ChiTietHoaDon() {}

    public Integer getMaChiTiet() { return maChiTiet; }
    public void setMaChiTiet(Integer maChiTiet) { this.maChiTiet = maChiTiet; }

    public HoaDon getHoaDon() { return hoaDon; }
    public void setHoaDon(HoaDon hoaDon) { this.hoaDon = hoaDon; }

    public DonGiaDichVu getDonGiaDichVu() { return donGiaDichVu; }
    public void setDonGiaDichVu(DonGiaDichVu donGiaDichVu) { this.donGiaDichVu = donGiaDichVu; }

    public String getTenMuc() { return tenMuc; }
    public void setTenMuc(String tenMuc) { this.tenMuc = tenMuc; }

    public BigDecimal getSoLuong() { return soLuong; }
    public void setSoLuong(BigDecimal soLuong) { this.soLuong = soLuong; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public BigDecimal getThanhTien() { return thanhTien; }
    public void setThanhTien(BigDecimal thanhTien) { this.thanhTien = thanhTien; }
}
