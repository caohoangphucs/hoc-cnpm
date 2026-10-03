package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.chi_so_dien_nuoc; business writes belong to stored procedures. */
@Entity
@Immutable
@Table(name = "chi_so_dien_nuoc", schema = "public")
public class ChiSoDienNuocEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "machiso", nullable = false)
    private Integer maChiSo;

    @Column(name = "maphong", nullable = false, length = 20)
    private String maPhong;

    @Column(name = "kythang", nullable = false, length = 7)
    private String kyThang;

    @Column(name = "diencu", nullable = false)
    private Integer dienCu;

    @Column(name = "dienmoi", nullable = false)
    private Integer dienMoi;

    @Column(name = "nuoccu", nullable = false)
    private Integer nuocCu;

    @Column(name = "nuocmoi", nullable = false)
    private Integer nuocMoi;

    @Column(name = "ngayghi", nullable = true)
    private java.time.LocalDateTime ngayGhi;

    @Column(name = "nguoighi", nullable = true, length = 50)
    private String nguoiGhi;

    protected ChiSoDienNuocEntity() {}

    public Integer getMaChiSo() { return maChiSo; }
    public String getMaPhong() { return maPhong; }
    public String getKyThang() { return kyThang; }
    public Integer getDienCu() { return dienCu; }
    public Integer getDienMoi() { return dienMoi; }
    public Integer getNuocCu() { return nuocCu; }
    public Integer getNuocMoi() { return nuocMoi; }
    public java.time.LocalDateTime getNgayGhi() { return ngayGhi; }
    public String getNguoiGhi() { return nguoiGhi; }
}
