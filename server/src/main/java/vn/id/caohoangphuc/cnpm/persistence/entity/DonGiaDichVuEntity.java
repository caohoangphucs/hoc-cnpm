package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.don_gia_dich_vu; business writes belong to stored procedures. */
@Entity
@Immutable
@Table(name = "don_gia_dich_vu", schema = "public")
public class DonGiaDichVuEntity {
    @Id
    @Column(name = "madongia", nullable = false, length = 20)
    private String maDonGia;

    @Column(name = "loaidichvu", nullable = false, length = 20)
    private String loaiDichVu;

    @Column(name = "tendichvu", nullable = false, length = 100)
    private String tenDichVu;

    @Column(name = "donvitinh", nullable = false, length = 20)
    private String donViTinh;

    @Column(name = "dongia", nullable = false, precision = 15, scale = 0)
    private java.math.BigDecimal donGia;

    @Column(name = "ngayapdung", nullable = false)
    private java.time.LocalDate ngayApDung;

    @Column(name = "trangthai", nullable = true, length = 20)
    private String trangThai;

    protected DonGiaDichVuEntity() {}

    public String getMaDonGia() { return maDonGia; }
    public String getLoaiDichVu() { return loaiDichVu; }
    public String getTenDichVu() { return tenDichVu; }
    public String getDonViTinh() { return donViTinh; }
    public java.math.BigDecimal getDonGia() { return donGia; }
    public java.time.LocalDate getNgayApDung() { return ngayApDung; }
    public String getTrangThai() { return trangThai; }
}
