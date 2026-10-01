package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "SO_DIEM_CONG_ICH")
public class SoDiemCongIch implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaGiaoDich")
    private Integer maGiaoDich;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSinhVien", nullable = false)
    private SinhVien sinhVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaHocKy")
    private HocKy hocKy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDangKyCongIch")
    private DangKyCongIch dangKyCongIch;

    @Column(name = "SoDiemThayDoi", nullable = false)
    private Integer soDiemThayDoi;

    @Column(name = "LoaiGiaoDich", nullable = false, length = 20)
    private String loaiGiaoDich;

    @Column(name = "GhiChu", columnDefinition = "TEXT")
    private String ghiChu;

    @Column(name = "ThoiGian", insertable = false, updatable = false)
    private LocalDateTime thoiGian;

    public SoDiemCongIch() {}

    public Integer getMaGiaoDich() { return maGiaoDich; }
    public void setMaGiaoDich(Integer maGiaoDich) { this.maGiaoDich = maGiaoDich; }

    public SinhVien getSinhVien() { return sinhVien; }
    public void setSinhVien(SinhVien sinhVien) { this.sinhVien = sinhVien; }

    public HocKy getHocKy() { return hocKy; }
    public void setHocKy(HocKy hocKy) { this.hocKy = hocKy; }

    public DangKyCongIch getDangKyCongIch() { return dangKyCongIch; }
    public void setDangKyCongIch(DangKyCongIch dangKyCongIch) { this.dangKyCongIch = dangKyCongIch; }

    public Integer getSoDiemThayDoi() { return soDiemThayDoi; }
    public void setSoDiemThayDoi(Integer soDiemThayDoi) { this.soDiemThayDoi = soDiemThayDoi; }

    public String getLoaiGiaoDich() { return loaiGiaoDich; }
    public void setLoaiGiaoDich(String loaiGiaoDich) { this.loaiGiaoDich = loaiGiaoDich; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }
}
