package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "HOAT_DONG")
public class HoatDong implements Serializable {

    @Id
    @Column(name = "MaHoatDong", length = 20)
    private String maHoatDong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaHocKy")
    private HocKy hocKy;

    @Column(name = "TieuDe", nullable = false, length = 255)
    private String tieuDe;

    @Column(name = "MoTa", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "ThoiGianBatDau", nullable = false)
    private LocalDateTime thoiGianBatDau;

    @Column(name = "ThoiGianKetThuc", nullable = false)
    private LocalDateTime thoiGianKetThuc;

    @Column(name = "DiaDiem", length = 255)
    private String diaDiem;

    @Column(name = "ChiTieu", nullable = false)
    private Integer chiTieu;

    @Column(name = "DaDangKy", nullable = false)
    private Integer daDangKy = 0;

    @Column(name = "DiemCongIch", nullable = false)
    private Integer diemCongIch = 10;

    @Column(name = "TrangThai", length = 20)
    private String trangThai = "MO_DANG_KY";

    public HoatDong() {}

    public String getMaHoatDong() { return maHoatDong; }
    public void setMaHoatDong(String maHoatDong) { this.maHoatDong = maHoatDong; }

    public HocKy getHocKy() { return hocKy; }
    public void setHocKy(HocKy hocKy) { this.hocKy = hocKy; }

    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public LocalDateTime getThoiGianBatDau() { return thoiGianBatDau; }
    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) { this.thoiGianBatDau = thoiGianBatDau; }

    public LocalDateTime getThoiGianKetThuc() { return thoiGianKetThuc; }
    public void setThoiGianKetThuc(LocalDateTime thoiGianKetThuc) { this.thoiGianKetThuc = thoiGianKetThuc; }

    public String getDiaDiem() { return diaDiem; }
    public void setDiaDiem(String diaDiem) { this.diaDiem = diaDiem; }

    public Integer getChiTieu() { return chiTieu; }
    public void setChiTieu(Integer chiTieu) { this.chiTieu = chiTieu; }

    public Integer getDaDangKy() { return daDangKy; }
    public void setDaDangKy(Integer daDangKy) { this.daDangKy = daDangKy; }

    public Integer getDiemCongIch() { return diemCongIch; }
    public void setDiemCongIch(Integer diemCongIch) { this.diemCongIch = diemCongIch; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
