package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.loai_phong; business writes are handled by service-layer code. */
@Entity
@Immutable
@Table(name = "loai_phong", schema = "public")
public class LoaiPhongEntity {
    @Id
    @Column(name = "maloaiphong", nullable = false, length = 20)
    private String maLoaiPhong;

    @Column(name = "tenloaiphong", nullable = false, length = 100)
    private String tenLoaiPhong;

    @Column(name = "succhuatieuchuan", nullable = false)
    private Integer sucChuaTieuChuan;

    @Column(name = "dongiathang", nullable = false, precision = 15, scale = 0)
    private java.math.BigDecimal donGiaThang;

    @Column(name = "mota", nullable = true, columnDefinition = "text")
    private String moTa;

    protected LoaiPhongEntity() {}

    public String getMaLoaiPhong() { return maLoaiPhong; }
    public String getTenLoaiPhong() { return tenLoaiPhong; }
    public Integer getSucChuaTieuChuan() { return sucChuaTieuChuan; }
    public java.math.BigDecimal getDonGiaThang() { return donGiaThang; }
    public String getMoTa() { return moTa; }
}
