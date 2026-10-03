package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.yeu_cau_sua_chua; business writes are handled by service-layer code. */
@Entity
@Immutable
@Table(name = "yeu_cau_sua_chua", schema = "public")
public class YeuCauSuaChuaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mayeucau", nullable = false)
    private Integer maYeuCau;

    @Column(name = "maphong", nullable = false, length = 20)
    private String maPhong;

    @Column(name = "masinhvien", nullable = false, length = 20)
    private String maSinhVien;

    @Column(name = "tieude", nullable = false, length = 255)
    private String tieuDe;

    @Column(name = "motachitiet", nullable = false, columnDefinition = "text")
    private String moTaChiTiet;

    @Column(name = "mucdouutien", nullable = true, length = 20)
    private String mucDoUuTien;

    @Column(name = "trangthai", nullable = true, length = 20)
    private String trangThai;

    @Column(name = "ngaytao", nullable = true)
    private java.time.LocalDateTime ngayTao;

    @Column(name = "ngayhoanthanh", nullable = true)
    private java.time.LocalDateTime ngayHoanThanh;

    @Column(name = "ghichukythuat", nullable = true, columnDefinition = "text")
    private String ghiChuKyThuat;

    protected YeuCauSuaChuaEntity() {}

    public Integer getMaYeuCau() { return maYeuCau; }
    public String getMaPhong() { return maPhong; }
    public String getMaSinhVien() { return maSinhVien; }
    public String getTieuDe() { return tieuDe; }
    public String getMoTaChiTiet() { return moTaChiTiet; }
    public String getMucDoUuTien() { return mucDoUuTien; }
    public String getTrangThai() { return trangThai; }
    public java.time.LocalDateTime getNgayTao() { return ngayTao; }
    public java.time.LocalDateTime getNgayHoanThanh() { return ngayHoanThanh; }
    public String getGhiChuKyThuat() { return ghiChuKyThuat; }
}
