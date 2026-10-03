package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.giuong; business writes belong to stored procedures. */
@Entity
@Immutable
@Table(name = "giuong", schema = "public")
public class GiuongEntity {
    @Id
    @Column(name = "magiuong", nullable = false, length = 20)
    private String maGiuong;

    @Column(name = "maphong", nullable = false, length = 20)
    private String maPhong;

    @Column(name = "sogiuong", nullable = false, length = 20)
    private String soGiuong;

    @Column(name = "vitri", nullable = true, length = 50)
    private String viTri;

    @Column(name = "trangthai", nullable = true, length = 20)
    private String trangThai;

    protected GiuongEntity() {}

    public String getMaGiuong() { return maGiuong; }
    public String getMaPhong() { return maPhong; }
    public String getSoGiuong() { return soGiuong; }
    public String getViTri() { return viTri; }
    public String getTrangThai() { return trangThai; }
}
