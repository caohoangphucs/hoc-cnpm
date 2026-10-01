package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "VI_PHAM")
public class ViPham implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaViPham")
    private Integer maViPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSinhVien", nullable = false)
    private SinhVien sinhVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaRaVao")
    private RaVao raVao;

    @Column(name = "NoiDungViPham", nullable = false, columnDefinition = "TEXT")
    private String noiDungViPham;

    @Column(name = "MucDo", length = 20)
    private String mucDo = "LOW";

    @Column(name = "HinhThucXuLy", length = 255)
    private String hinhThucXuLy;

    @Column(name = "NgayViPham", insertable = false, updatable = false)
    private LocalDateTime ngayViPham;

    @Column(name = "TrangThai", length = 20)
    private String trangThai = "CHUA_XU_LY";

    public ViPham() {}

    public Integer getMaViPham() { return maViPham; }
    public void setMaViPham(Integer maViPham) { this.maViPham = maViPham; }

    public SinhVien getSinhVien() { return sinhVien; }
    public void setSinhVien(SinhVien sinhVien) { this.sinhVien = sinhVien; }

    public RaVao getRaVao() { return raVao; }
    public void setRaVao(RaVao raVao) { this.raVao = raVao; }

    public String getNoiDungViPham() { return noiDungViPham; }
    public void setNoiDungViPham(String noiDungViPham) { this.noiDungViPham = noiDungViPham; }

    public String getMucDo() { return mucDo; }
    public void setMucDo(String mucDo) { this.mucDo = mucDo; }

    public String getHinhThucXuLy() { return hinhThucXuLy; }
    public void setHinhThucXuLy(String hinhThucXuLy) { this.hinhThucXuLy = hinhThucXuLy; }

    public LocalDateTime getNgayViPham() { return ngayViPham; }
    public void setNgayViPham(LocalDateTime ngayViPham) { this.ngayViPham = ngayViPham; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
