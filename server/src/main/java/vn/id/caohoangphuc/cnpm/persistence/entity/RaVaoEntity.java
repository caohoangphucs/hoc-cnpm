package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.ra_vao; business writes belong to stored procedures. */
@Entity
@Immutable
@Table(name = "ra_vao", schema = "public")
public class RaVaoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maravao", nullable = false)
    private Integer maRaVao;

    @Column(name = "masinhvien", nullable = false, length = 20)
    private String maSinhVien;

    @Column(name = "loaisukien", nullable = false, length = 10)
    private String loaiSuKien;

    @Column(name = "thoigian", nullable = true)
    private java.time.LocalDateTime thoiGian;

    @Column(name = "baoveghinhan", nullable = true, length = 50)
    private String baoVeGhiNhan;

    @Column(name = "ghichu", nullable = true, length = 255)
    private String ghiChu;

    protected RaVaoEntity() {}

    public Integer getMaRaVao() { return maRaVao; }
    public String getMaSinhVien() { return maSinhVien; }
    public String getLoaiSuKien() { return loaiSuKien; }
    public java.time.LocalDateTime getThoiGian() { return thoiGian; }
    public String getBaoVeGhiNhan() { return baoVeGhiNhan; }
    public String getGhiChu() { return ghiChu; }
}
