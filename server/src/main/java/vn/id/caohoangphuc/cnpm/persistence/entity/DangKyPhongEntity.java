package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.dang_ky_phong; business writes belong to stored procedures. */
@Entity
@Immutable
@Table(name = "dang_ky_phong", schema = "public")
public class DangKyPhongEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "madangky", nullable = false)
    private Integer maDangKy;

    @Column(name = "masinhvien", nullable = false, length = 20)
    private String maSinhVien;

    @Column(name = "magiuong", nullable = false, length = 20)
    private String maGiuong;

    @Column(name = "ngaydangky", nullable = true)
    private java.time.LocalDateTime ngayDangKy;

    @Column(name = "ngayhethan", nullable = false)
    private java.time.LocalDateTime ngayHetHan;

    @Column(name = "trangthai", nullable = true, length = 20)
    private String trangThai;

    @Column(name = "ghichulydo", nullable = true, columnDefinition = "text")
    private String ghiChuLyDo;

    protected DangKyPhongEntity() {}

    public Integer getMaDangKy() { return maDangKy; }
    public String getMaSinhVien() { return maSinhVien; }
    public String getMaGiuong() { return maGiuong; }
    public java.time.LocalDateTime getNgayDangKy() { return ngayDangKy; }
    public java.time.LocalDateTime getNgayHetHan() { return ngayHetHan; }
    public String getTrangThai() { return trangThai; }
    public String getGhiChuLyDo() { return ghiChuLyDo; }
}
