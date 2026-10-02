package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "YEU_CAU_SUA_CHUA")
public class YeuCauSuaChua implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaYeuCau")
    private Integer maYeuCau;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaPhong", nullable = false)
    private Phong phong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSinhVien", nullable = false)
    private SinhVien sinhVien;

    @Column(name = "TieuDe", nullable = false, length = 255)
    private String tieuDe;

    @Column(name = "MoTaChiTiet", nullable = false, columnDefinition = "TEXT")
    private String moTaChiTiet;

    @Column(name = "MucDoUuTien", length = 20)
    private String mucDoUuTien = "TRUNG_BINH";

    @Column(name = "TrangThai", length = 20)
    private String trangThai = "MOI";

    @Column(name = "NgayTao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "NgayHoanThanh")
    private LocalDateTime ngayHoanThanh;

    @Column(name = "GhiChuKyThuat", columnDefinition = "TEXT")
    private String ghiChuKyThuat;

    public YeuCauSuaChua() {}

    public Integer getMaYeuCau() { return maYeuCau; }
    public void setMaYeuCau(Integer maYeuCau) { this.maYeuCau = maYeuCau; }

    public Phong getPhong() { return phong; }
    public void setPhong(Phong phong) { this.phong = phong; }

    public SinhVien getSinhVien() { return sinhVien; }
    public void setSinhVien(SinhVien sinhVien) { this.sinhVien = sinhVien; }

    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }

    public String getMoTaChiTiet() { return moTaChiTiet; }
    public void setMoTaChiTiet(String moTaChiTiet) { this.moTaChiTiet = moTaChiTiet; }

    public String getMucDoUuTien() { return mucDoUuTien; }
    public void setMucDoUuTien(String mucDoUuTien) { this.mucDoUuTien = mucDoUuTien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    public LocalDateTime getNgayHoanThanh() { return ngayHoanThanh; }
    public void setNgayHoanThanh(LocalDateTime ngayHoanThanh) { this.ngayHoanThanh = ngayHoanThanh; }

    public String getGhiChuKyThuat() { return ghiChuKyThuat; }
    public void setGhiChuKyThuat(String ghiChuKyThuat) { this.ghiChuKyThuat = ghiChuKyThuat; }
}
