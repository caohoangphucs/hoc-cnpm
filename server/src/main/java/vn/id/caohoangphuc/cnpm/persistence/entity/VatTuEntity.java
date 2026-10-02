package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.vat_tu; business writes are handled by service-layer code. */
@Entity
@Immutable
@Table(name = "vat_tu", schema = "public")
public class VatTuEntity {
    @Id
    @Column(name = "mavattu", nullable = false, length = 20)
    private String maVatTu;

    @Column(name = "tenvattu", nullable = false, length = 100)
    private String tenVatTu;

    @Column(name = "donvitinh", nullable = false, length = 20)
    private String donViTinh;

    @Column(name = "soluongton", nullable = false)
    private Integer soLuongTon;

    @Column(name = "dongianhap", nullable = false, precision = 15, scale = 0)
    private java.math.BigDecimal donGiaNhap;

    @Column(name = "ghichu", nullable = true, columnDefinition = "text")
    private String ghiChu;

    protected VatTuEntity() {}

    public String getMaVatTu() { return maVatTu; }
    public String getTenVatTu() { return tenVatTu; }
    public String getDonViTinh() { return donViTinh; }
    public Integer getSoLuongTon() { return soLuongTon; }
    public java.math.BigDecimal getDonGiaNhap() { return donGiaNhap; }
    public String getGhiChu() { return ghiChu; }
}
