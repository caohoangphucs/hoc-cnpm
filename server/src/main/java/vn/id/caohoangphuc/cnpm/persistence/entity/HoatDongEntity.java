package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.hoat_dong; business writes belong to stored procedures. */
@Entity
@Immutable
@Table(name = "hoat_dong", schema = "public")
public class HoatDongEntity {
    @Id
    @Column(name = "mahoatdong", nullable = false, length = 20)
    private String maHoatDong;

    @Column(name = "mahocky", nullable = true, length = 20)
    private String maHocKy;

    @Column(name = "tieude", nullable = false, length = 255)
    private String tieuDe;

    @Column(name = "mota", nullable = true, columnDefinition = "text")
    private String moTa;

    @Column(name = "thoigianbatdau", nullable = false)
    private java.time.LocalDateTime thoiGianBatDau;

    @Column(name = "thoigianketthuc", nullable = false)
    private java.time.LocalDateTime thoiGianKetThuc;

    @Column(name = "diadiem", nullable = true, length = 255)
    private String diaDiem;

    @Column(name = "chitieu", nullable = false)
    private Integer chiTieu;

    @Column(name = "dadangky", nullable = false)
    private Integer daDangKy;

    @Column(name = "diemcongich", nullable = false)
    private Integer diemCongIch;

    @Column(name = "trangthai", nullable = true, length = 20)
    private String trangThai;

    protected HoatDongEntity() {}

    public String getMaHoatDong() { return maHoatDong; }
    public String getMaHocKy() { return maHocKy; }
    public String getTieuDe() { return tieuDe; }
    public String getMoTa() { return moTa; }
    public java.time.LocalDateTime getThoiGianBatDau() { return thoiGianBatDau; }
    public java.time.LocalDateTime getThoiGianKetThuc() { return thoiGianKetThuc; }
    public String getDiaDiem() { return diaDiem; }
    public Integer getChiTieu() { return chiTieu; }
    public Integer getDaDangKy() { return daDangKy; }
    public Integer getDiemCongIch() { return diemCongIch; }
    public String getTrangThai() { return trangThai; }
}
