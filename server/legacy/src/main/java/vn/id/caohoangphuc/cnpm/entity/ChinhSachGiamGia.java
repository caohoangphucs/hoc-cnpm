package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "CHINH_SACH_GIAM_GIA")
public class ChinhSachGiamGia implements Serializable {

    @Id
    @Column(name = "MaChinhSach", length = 20)
    private String maChinhSach;

    @Column(name = "TenChinhSach", nullable = false, length = 150)
    private String tenChinhSach;

    @Column(name = "TyLeGiam", nullable = false, precision = 5, scale = 2)
    private BigDecimal tyLeGiam;

    @Column(name = "DieuKienApDung", columnDefinition = "TEXT")
    private String dieuKienApDung;

    @Column(name = "NgayBatDau", nullable = false)
    private LocalDate ngayBatDau;

    @Column(name = "NgayKetThuc")
    private LocalDate ngayKetThuc;

    @Column(name = "TrangThai", length = 20)
    private String trangThai = "DANG_AP_DUNG";

    public ChinhSachGiamGia() {}

    public String getMaChinhSach() { return maChinhSach; }
    public void setMaChinhSach(String maChinhSach) { this.maChinhSach = maChinhSach; }

    public String getTenChinhSach() { return tenChinhSach; }
    public void setTenChinhSach(String tenChinhSach) { this.tenChinhSach = tenChinhSach; }

    public BigDecimal getTyLeGiam() { return tyLeGiam; }
    public void setTyLeGiam(BigDecimal tyLeGiam) { this.tyLeGiam = tyLeGiam; }

    public String getDieuKienApDung() { return dieuKienApDung; }
    public void setDieuKienApDung(String dieuKienApDung) { this.dieuKienApDung = dieuKienApDung; }

    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDate ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public LocalDate getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(LocalDate ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
