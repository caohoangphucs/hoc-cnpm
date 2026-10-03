package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "DANG_KY_PHONG")
public class DangKyPhong implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDangKy")
    private Integer maDangKy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSinhVien", nullable = false)
    private SinhVien sinhVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaGiuong", nullable = false)
    private Giuong giuong;

    @Column(name = "NgayDangKy", insertable = false, updatable = false)
    private LocalDateTime ngayDangKy;

    @Column(name = "NgayHetHan", nullable = false)
    private LocalDateTime ngayHetHan;

    @Column(name = "TrangThai", length = 20)
    private String trangThai = "CHO_DUYET";

    @Column(name = "GhiChuLyDo", columnDefinition = "TEXT")
    private String ghiChuLyDo;

    public DangKyPhong() {}

    public Integer getMaDangKy() { return maDangKy; }
    public void setMaDangKy(Integer maDangKy) { this.maDangKy = maDangKy; }

    public SinhVien getSinhVien() { return sinhVien; }
    public void setSinhVien(SinhVien sinhVien) { this.sinhVien = sinhVien; }

    public Giuong getGiuong() { return giuong; }
    public void setGiuong(Giuong giuong) { this.giuong = giuong; }

    public LocalDateTime getNgayDangKy() { return ngayDangKy; }
    public void setNgayDangKy(LocalDateTime ngayDangKy) { this.ngayDangKy = ngayDangKy; }

    public LocalDateTime getNgayHetHan() { return ngayHetHan; }
    public void setNgayHetHan(LocalDateTime ngayHetHan) { this.ngayHetHan = ngayHetHan; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChuLyDo() { return ghiChuLyDo; }
    public void setGhiChuLyDo(String ghiChuLyDo) { this.ghiChuLyDo = ghiChuLyDo; }
}
