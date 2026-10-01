package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "DANG_KY_CONG_ICH", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"MaHoatDong", "MaSinhVien"})
})
public class DangKyCongIch implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDangKyCongIch")
    private Integer maDangKyCongIch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaHoatDong", nullable = false)
    private HoatDong hoatDong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSinhVien", nullable = false)
    private SinhVien sinhVien;

    @Column(name = "NgayDangKy", insertable = false, updatable = false)
    private LocalDateTime ngayDangKy;

    @Column(name = "MaQRToken", unique = true, length = 100)
    private String maQRToken;

    @Column(name = "TrangThai", length = 20)
    private String trangThai = "REGISTERED";

    @Column(name = "NguoiXacNhan", length = 50)
    private String nguoiXacNhan;

    @Column(name = "NgayXacNhan")
    private LocalDateTime ngayXacNhan;

    public DangKyCongIch() {}

    public Integer getMaDangKyCongIch() { return maDangKyCongIch; }
    public void setMaDangKyCongIch(Integer maDangKyCongIch) { this.maDangKyCongIch = maDangKyCongIch; }

    public HoatDong getHoatDong() { return hoatDong; }
    public void setHoatDong(HoatDong hoatDong) { this.hoatDong = hoatDong; }

    public SinhVien getSinhVien() { return sinhVien; }
    public void setSinhVien(SinhVien sinhVien) { this.sinhVien = sinhVien; }

    public LocalDateTime getNgayDangKy() { return ngayDangKy; }
    public void setNgayDangKy(LocalDateTime ngayDangKy) { this.ngayDangKy = ngayDangKy; }

    public String getMaQRToken() { return maQRToken; }
    public void setMaQRToken(String maQRToken) { this.maQRToken = maQRToken; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getNguoiXacNhan() { return nguoiXacNhan; }
    public void setNguoiXacNhan(String nguoiXacNhan) { this.nguoiXacNhan = nguoiXacNhan; }

    public LocalDateTime getNgayXacNhan() { return ngayXacNhan; }
    public void setNgayXacNhan(LocalDateTime ngayXacNhan) { this.ngayXacNhan = ngayXacNhan; }
}
