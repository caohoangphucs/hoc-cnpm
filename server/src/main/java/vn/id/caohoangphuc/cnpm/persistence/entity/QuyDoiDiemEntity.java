package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.quy_doi_diem; business writes are handled by service-layer code. */
@Entity
@Immutable
@Table(name = "quy_doi_diem", schema = "public")
public class QuyDoiDiemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maquydoi", nullable = false)
    private Integer maQuyDoi;

    @Column(name = "masinhvien", nullable = false, length = 20)
    private String maSinhVien;

    @Column(name = "mahocky", nullable = false, length = 20)
    private String maHocKy;

    @Column(name = "diemcongichtieu", nullable = false)
    private Integer diemCongIchTieu;

    @Column(name = "diemrenluyennhan", nullable = false)
    private Integer diemRenLuyenNhan;

    @Column(name = "ngayquydoi", nullable = true)
    private java.time.LocalDateTime ngayQuyDoi;

    @Column(name = "trangthai", nullable = true, length = 20)
    private String trangThai;

    protected QuyDoiDiemEntity() {}

    public Integer getMaQuyDoi() { return maQuyDoi; }
    public String getMaSinhVien() { return maSinhVien; }
    public String getMaHocKy() { return maHocKy; }
    public Integer getDiemCongIchTieu() { return diemCongIchTieu; }
    public Integer getDiemRenLuyenNhan() { return diemRenLuyenNhan; }
    public java.time.LocalDateTime getNgayQuyDoi() { return ngayQuyDoi; }
    public String getTrangThai() { return trangThai; }
}
