package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "RA_VAO")
public class RaVao implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaRaVao")
    private Integer maRaVao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSinhVien", nullable = false)
    private SinhVien sinhVien;

    @Column(name = "LoaiSuKien", nullable = false, length = 10)
    private String loaiSuKien;

    @Column(name = "ThoiGian", insertable = false, updatable = false)
    private LocalDateTime thoiGian;

    @Column(name = "BaoVeGhiNhan", length = 50)
    private String baoVeGhiNhan;

    @Column(name = "GhiChu", length = 255)
    private String ghiChu;

    public RaVao() {}

    public Integer getMaRaVao() { return maRaVao; }
    public void setMaRaVao(Integer maRaVao) { this.maRaVao = maRaVao; }

    public SinhVien getSinhVien() { return sinhVien; }
    public void setSinhVien(SinhVien sinhVien) { this.sinhVien = sinhVien; }

    public String getLoaiSuKien() { return loaiSuKien; }
    public void setLoaiSuKien(String loaiSuKien) { this.loaiSuKien = loaiSuKien; }

    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }

    public String getBaoVeGhiNhan() { return baoVeGhiNhan; }
    public void setBaoVeGhiNhan(String baoVeGhiNhan) { this.baoVeGhiNhan = baoVeGhiNhan; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
