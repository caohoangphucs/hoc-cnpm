package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "LUU_TRU")
public class LuuTru implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaLuuTru")
    private Integer maLuuTru;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDangKy", unique = true)
    private DangKyPhong dangKyPhong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSinhVien", nullable = false)
    private SinhVien sinhVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaGiuong", nullable = false)
    private Giuong giuong;

    @Column(name = "NgayBatDau", nullable = false)
    private LocalDate ngayBatDau;

    @Column(name = "NgayKetThuc")
    private LocalDate ngayKetThuc;

    @Column(name = "TrangThai", length = 20)
    private String trangThai = "DANG_O";

    public LuuTru() {}

    public Integer getMaLuuTru() { return maLuuTru; }
    public void setMaLuuTru(Integer maLuuTru) { this.maLuuTru = maLuuTru; }

    public DangKyPhong getDangKyPhong() { return dangKyPhong; }
    public void setDangKyPhong(DangKyPhong dangKyPhong) { this.dangKyPhong = dangKyPhong; }

    public SinhVien getSinhVien() { return sinhVien; }
    public void setSinhVien(SinhVien sinhVien) { this.sinhVien = sinhVien; }

    public Giuong getGiuong() { return giuong; }
    public void setGiuong(Giuong giuong) { this.giuong = giuong; }

    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDate ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public LocalDate getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(LocalDate ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
