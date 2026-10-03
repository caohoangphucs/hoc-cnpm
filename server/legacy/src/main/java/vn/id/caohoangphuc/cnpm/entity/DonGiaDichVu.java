package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "DON_GIA_DICH_VU")
public class DonGiaDichVu implements Serializable {

    @Id
    @Column(name = "MaDonGia", length = 20)
    private String maDonGia;

    @Column(name = "LoaiDichVu", nullable = false, length = 20)
    private String loaiDichVu;

    @Column(name = "TenDichVu", nullable = false, length = 100)
    private String tenDichVu;

    @Column(name = "DonViTinh", nullable = false, length = 20)
    private String donViTinh;

    @Column(name = "DonGia", nullable = false, precision = 15, scale = 0)
    private BigDecimal donGia;

    @Column(name = "NgayApDung", nullable = false)
    private LocalDate ngayApDung;

    @Column(name = "TrangThai", length = 20)
    private String trangThai = "ACTIVE";

    public DonGiaDichVu() {}

    public String getMaDonGia() { return maDonGia; }
    public void setMaDonGia(String maDonGia) { this.maDonGia = maDonGia; }

    public String getLoaiDichVu() { return loaiDichVu; }
    public void setLoaiDichVu(String loaiDichVu) { this.loaiDichVu = loaiDichVu; }

    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public LocalDate getNgayApDung() { return ngayApDung; }
    public void setNgayApDung(LocalDate ngayApDung) { this.ngayApDung = ngayApDung; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
