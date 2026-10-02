package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "XUAT_VAT_TU")
public class XuatVatTu implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaXuat")
    private Integer maXuat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaVatTu", nullable = false)
    private VatTu vatTu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaYeuCau")
    private YeuCauSuaChua yeuCauSuaChua;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @Column(name = "NgayXuat", insertable = false, updatable = false)
    private LocalDateTime ngayXuat;

    @Column(name = "NguoiXuat", length = 50)
    private String nguoiXuat;

    @Column(name = "GhiChu", columnDefinition = "TEXT")
    private String ghiChu;

    public XuatVatTu() {}

    public Integer getMaXuat() { return maXuat; }
    public void setMaXuat(Integer maXuat) { this.maXuat = maXuat; }

    public VatTu getVatTu() { return vatTu; }
    public void setVatTu(VatTu vatTu) { this.vatTu = vatTu; }

    public YeuCauSuaChua getYeuCauSuaChua() { return yeuCauSuaChua; }
    public void setYeuCauSuaChua(YeuCauSuaChua yeuCauSuaChua) { this.yeuCauSuaChua = yeuCauSuaChua; }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }

    public LocalDateTime getNgayXuat() { return ngayXuat; }
    public void setNgayXuat(LocalDateTime ngayXuat) { this.ngayXuat = ngayXuat; }

    public String getNguoiXuat() { return nguoiXuat; }
    public void setNguoiXuat(String nguoiXuat) { this.nguoiXuat = nguoiXuat; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
