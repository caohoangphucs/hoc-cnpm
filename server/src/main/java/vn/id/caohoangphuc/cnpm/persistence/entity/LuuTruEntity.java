package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.luu_tru; business writes belong to stored procedures. */
@Entity
@Immutable
@Table(name = "luu_tru", schema = "public")
public class LuuTruEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maluutru", nullable = false)
    private Integer maLuuTru;

    @Column(name = "madangky", nullable = true)
    private Integer maDangKy;

    @Column(name = "masinhvien", nullable = false, length = 20)
    private String maSinhVien;

    @Column(name = "magiuong", nullable = false, length = 20)
    private String maGiuong;

    @Column(name = "ngaybatdau", nullable = false)
    private java.time.LocalDate ngayBatDau;

    @Column(name = "ngayketthuc", nullable = true)
    private java.time.LocalDate ngayKetThuc;

    @Column(name = "trangthai", nullable = true, length = 20)
    private String trangThai;

    protected LuuTruEntity() {}

    public Integer getMaLuuTru() { return maLuuTru; }
    public Integer getMaDangKy() { return maDangKy; }
    public String getMaSinhVien() { return maSinhVien; }
    public String getMaGiuong() { return maGiuong; }
    public java.time.LocalDate getNgayBatDau() { return ngayBatDau; }
    public java.time.LocalDate getNgayKetThuc() { return ngayKetThuc; }
    public String getTrangThai() { return trangThai; }
}
