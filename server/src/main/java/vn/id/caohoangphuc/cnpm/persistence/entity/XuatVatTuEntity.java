package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.xuat_vat_tu; business writes are handled by service-layer code. */
@Entity
@Immutable
@Table(name = "xuat_vat_tu", schema = "public")
public class XuatVatTuEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maxuat", nullable = false)
    private Integer maXuat;

    @Column(name = "mavattu", nullable = false, length = 20)
    private String maVatTu;

    @Column(name = "mayeucau", nullable = true)
    private Integer maYeuCau;

    @Column(name = "soluong", nullable = false)
    private Integer soLuong;

    @Column(name = "ngayxuat", nullable = true)
    private java.time.LocalDateTime ngayXuat;

    @Column(name = "nguoixuat", nullable = true, length = 50)
    private String nguoiXuat;

    @Column(name = "ghichu", nullable = true, columnDefinition = "text")
    private String ghiChu;

    protected XuatVatTuEntity() {}

    public Integer getMaXuat() { return maXuat; }
    public String getMaVatTu() { return maVatTu; }
    public Integer getMaYeuCau() { return maYeuCau; }
    public Integer getSoLuong() { return soLuong; }
    public java.time.LocalDateTime getNgayXuat() { return ngayXuat; }
    public String getNguoiXuat() { return nguoiXuat; }
    public String getGhiChu() { return ghiChu; }
}
