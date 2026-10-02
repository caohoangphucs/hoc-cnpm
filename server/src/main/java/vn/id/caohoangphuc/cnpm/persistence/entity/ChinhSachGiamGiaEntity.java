package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.chinh_sach_giam_gia; business writes belong to stored procedures. */
@Entity
@Immutable
@Table(name = "chinh_sach_giam_gia", schema = "public")
public class ChinhSachGiamGiaEntity {
    @Id
    @Column(name = "machinhsach", nullable = false, length = 20)
    private String maChinhSach;

    @Column(name = "tenchinhsach", nullable = false, length = 150)
    private String tenChinhSach;

    @Column(name = "tylegiam", nullable = false, precision = 5, scale = 2)
    private java.math.BigDecimal tyLeGiam;

    @Column(name = "dieukienapdung", nullable = true, columnDefinition = "text")
    private String dieuKienApDung;

    @Column(name = "ngaybatdau", nullable = false)
    private java.time.LocalDate ngayBatDau;

    @Column(name = "ngayketthuc", nullable = true)
    private java.time.LocalDate ngayKetThuc;

    @Column(name = "trangthai", nullable = true, length = 20)
    private String trangThai;

    protected ChinhSachGiamGiaEntity() {}

    public String getMaChinhSach() { return maChinhSach; }
    public String getTenChinhSach() { return tenChinhSach; }
    public java.math.BigDecimal getTyLeGiam() { return tyLeGiam; }
    public String getDieuKienApDung() { return dieuKienApDung; }
    public java.time.LocalDate getNgayBatDau() { return ngayBatDau; }
    public java.time.LocalDate getNgayKetThuc() { return ngayKetThuc; }
    public String getTrangThai() { return trangThai; }
}
