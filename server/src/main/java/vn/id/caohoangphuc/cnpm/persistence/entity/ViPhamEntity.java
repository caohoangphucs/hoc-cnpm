package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.vi_pham; business writes belong to stored procedures. */
@Entity
@Immutable
@Table(name = "vi_pham", schema = "public")
public class ViPhamEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mavipham", nullable = false)
    private Integer maViPham;

    @Column(name = "masinhvien", nullable = false, length = 20)
    private String maSinhVien;

    @Column(name = "maravao", nullable = true)
    private Integer maRaVao;

    @Column(name = "noidungvipham", nullable = false, columnDefinition = "text")
    private String noiDungViPham;

    @Column(name = "mucdo", nullable = true, length = 20)
    private String mucDo;

    @Column(name = "hinhthucxuly", nullable = true, length = 255)
    private String hinhThucXuLy;

    @Column(name = "ngayvipham", nullable = true)
    private java.time.LocalDateTime ngayViPham;

    @Column(name = "trangthai", nullable = true, length = 20)
    private String trangThai;

    protected ViPhamEntity() {}

    public Integer getMaViPham() { return maViPham; }
    public String getMaSinhVien() { return maSinhVien; }
    public Integer getMaRaVao() { return maRaVao; }
    public String getNoiDungViPham() { return noiDungViPham; }
    public String getMucDo() { return mucDo; }
    public String getHinhThucXuLy() { return hinhThucXuLy; }
    public java.time.LocalDateTime getNgayViPham() { return ngayViPham; }
    public String getTrangThai() { return trangThai; }
}
