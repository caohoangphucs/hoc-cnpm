package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "QUY_DOI_DIEM")
public class QuyDoiDiem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaQuyDoi")
    private Integer maQuyDoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSinhVien", nullable = false)
    private SinhVien sinhVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaHocKy", nullable = false)
    private HocKy hocKy;

    @Column(name = "DiemCongIchTieu", nullable = false)
    private Integer diemCongIchTieu;

    @Column(name = "DiemRenLuyenNhan", nullable = false)
    private Integer diemRenLuyenNhan;

    @Column(name = "NgayQuyDoi", insertable = false, updatable = false)
    private LocalDateTime ngayQuyDoi;

    @Column(name = "TrangThai", length = 20)
    private String trangThai = "THANH_CONG";

    public QuyDoiDiem() {}

    public Integer getMaQuyDoi() { return maQuyDoi; }
    public void setMaQuyDoi(Integer maQuyDoi) { this.maQuyDoi = maQuyDoi; }

    public SinhVien getSinhVien() { return sinhVien; }
    public void setSinhVien(SinhVien sinhVien) { this.sinhVien = sinhVien; }

    public HocKy getHocKy() { return hocKy; }
    public void setHocKy(HocKy hocKy) { this.hocKy = hocKy; }

    public Integer getDiemCongIchTieu() { return diemCongIchTieu; }
    public void setDiemCongIchTieu(Integer diemCongIchTieu) { this.diemCongIchTieu = diemCongIchTieu; }

    public Integer getDiemRenLuyenNhan() { return diemRenLuyenNhan; }
    public void setDiemRenLuyenNhan(Integer diemRenLuyenNhan) { this.diemRenLuyenNhan = diemRenLuyenNhan; }

    public LocalDateTime getNgayQuyDoi() { return ngayQuyDoi; }
    public void setNgayQuyDoi(LocalDateTime ngayQuyDoi) { this.ngayQuyDoi = ngayQuyDoi; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
