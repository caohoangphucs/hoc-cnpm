package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "CHI_SO_DIEN_NUOC", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"MaPhong", "KyThang"})
})
public class ChiSoDienNuoc implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaChiSo")
    private Integer maChiSo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaPhong", nullable = false)
    private Phong phong;

    @Column(name = "KyThang", nullable = false, length = 7)
    private String kyThang;

    @Column(name = "DienCu", nullable = false)
    private Integer dienCu = 0;

    @Column(name = "DienMoi", nullable = false)
    private Integer dienMoi = 0;

    @Column(name = "NuocCu", nullable = false)
    private Integer nuocCu = 0;

    @Column(name = "NuocMoi", nullable = false)
    private Integer nuocMoi = 0;

    @Column(name = "NgayGhi", insertable = false, updatable = false)
    private LocalDateTime ngayGhi;

    @Column(name = "NguoiGhi", length = 50)
    private String nguoiGhi;

    public ChiSoDienNuoc() {}

    public Integer getMaChiSo() { return maChiSo; }
    public void setMaChiSo(Integer maChiSo) { this.maChiSo = maChiSo; }

    public Phong getPhong() { return phong; }
    public void setPhong(Phong phong) { this.phong = phong; }

    public String getKyThang() { return kyThang; }
    public void setKyThang(String kyThang) { this.kyThang = kyThang; }

    public Integer getDienCu() { return dienCu; }
    public void setDienCu(Integer dienCu) { this.dienCu = dienCu; }

    public Integer getDienMoi() { return dienMoi; }
    public void setDienMoi(Integer dienMoi) { this.dienMoi = dienMoi; }

    public Integer getNuocCu() { return nuocCu; }
    public void setNuocCu(Integer nuocCu) { this.nuocCu = nuocCu; }

    public Integer getNuocMoi() { return nuocMoi; }
    public void setNuocMoi(Integer nuocMoi) { this.nuocMoi = nuocMoi; }

    public LocalDateTime getNgayGhi() { return ngayGhi; }
    public void setNgayGhi(LocalDateTime ngayGhi) { this.ngayGhi = ngayGhi; }

    public String getNguoiGhi() { return nguoiGhi; }
    public void setNguoiGhi(String nguoiGhi) { this.nguoiGhi = nguoiGhi; }
}
