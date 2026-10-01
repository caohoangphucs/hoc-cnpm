package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.hoc_ky; business writes belong to stored procedures. */
@Entity
@Immutable
@Table(name = "hoc_ky", schema = "public")
public class HocKyEntity {
    @Id
    @Column(name = "mahocky", nullable = false, length = 20)
    private String maHocKy;

    @Column(name = "tenhocky", nullable = false, length = 100)
    private String tenHocKy;

    @Column(name = "ngaybatdau", nullable = false)
    private java.time.LocalDate ngayBatDau;

    @Column(name = "ngayketthuc", nullable = false)
    private java.time.LocalDate ngayKetThuc;

    @Column(name = "trangthai", nullable = true, length = 20)
    private String trangThai;

    protected HocKyEntity() {}

    public String getMaHocKy() { return maHocKy; }
    public String getTenHocKy() { return tenHocKy; }
    public java.time.LocalDate getNgayBatDau() { return ngayBatDau; }
    public java.time.LocalDate getNgayKetThuc() { return ngayKetThuc; }
    public String getTrangThai() { return trangThai; }
}
