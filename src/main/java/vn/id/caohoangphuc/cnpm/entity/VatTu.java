package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "VAT_TU")
public class VatTu implements Serializable {

    @Id
    @Column(name = "MaVatTu", length = 20)
    private String maVatTu;

    @Column(name = "TenVatTu", nullable = false, length = 100)
    private String tenVatTu;

    @Column(name = "DonViTinh", nullable = false, length = 20)
    private String donViTinh;

    @Column(name = "SoLuongTon", nullable = false)
    private Integer soLuongTon = 0;

    @Column(name = "DonGiaNhap", nullable = false, precision = 15, scale = 0)
    private BigDecimal donGiaNhap = BigDecimal.ZERO;

    @Column(name = "GhiChu", columnDefinition = "TEXT")
    private String ghiChu;

    public VatTu() {}

    public String getMaVatTu() { return maVatTu; }
    public void setMaVatTu(String maVatTu) { this.maVatTu = maVatTu; }

    public String getTenVatTu() { return tenVatTu; }
    public void setTenVatTu(String tenVatTu) { this.tenVatTu = tenVatTu; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public Integer getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(Integer soLuongTon) { this.soLuongTon = soLuongTon; }

    public BigDecimal getDonGiaNhap() { return donGiaNhap; }
    public void setDonGiaNhap(BigDecimal donGiaNhap) { this.donGiaNhap = donGiaNhap; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
