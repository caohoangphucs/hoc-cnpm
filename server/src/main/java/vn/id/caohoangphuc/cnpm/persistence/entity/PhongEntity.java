package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.phong; business writes belong to stored procedures. */
@Entity
@Immutable
@Table(name = "phong", schema = "public")
public class PhongEntity {
    @Id
    @Column(name = "maphong", nullable = false, length = 20)
    private String maPhong;

    @Column(name = "matoanha", nullable = false, length = 20)
    private String maToaNha;

    @Column(name = "maloaiphong", nullable = true, length = 20)
    private String maLoaiPhong;

    @Column(name = "sophong", nullable = false, length = 20)
    private String soPhong;

    @Column(name = "tang", nullable = false)
    private Integer tang;

    @Column(name = "succhua", nullable = false)
    private Integer sucChua;

    @Column(name = "dango", nullable = false)
    private Integer dangO;

    @Column(name = "giaphongcoban", nullable = false, precision = 15, scale = 0)
    private java.math.BigDecimal giaPhongCoBan;

    @Column(name = "trangthai", nullable = true, length = 20)
    private String trangThai;

    protected PhongEntity() {}

    public String getMaPhong() { return maPhong; }
    public String getMaToaNha() { return maToaNha; }
    public String getMaLoaiPhong() { return maLoaiPhong; }
    public String getSoPhong() { return soPhong; }
    public Integer getTang() { return tang; }
    public Integer getSucChua() { return sucChua; }
    public Integer getDangO() { return dangO; }
    public java.math.BigDecimal getGiaPhongCoBan() { return giaPhongCoBan; }
    public String getTrangThai() { return trangThai; }
}
