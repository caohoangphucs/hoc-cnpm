package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "SINH_VIEN")
public class SinhVien implements Serializable {

    @Id
    @Column(name = "MaSinhVien", length = 20)
    private String maSinhVien;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaTaiKhoan", unique = true)
    private TaiKhoan taiKhoan;

    @Column(name = "HoTen", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "NgaySinh")
    private LocalDate ngaySinh;

    @Column(name = "GioiTinh", nullable = false, length = 10)
    private String gioiTinh;

    @Column(name = "CCCD", nullable = false, unique = true, length = 20)
    private String cccd;

    @Column(name = "Email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "SoDienThoai", nullable = false, length = 20)
    private String soDienThoai;

    @Column(name = "Lop", length = 50)
    private String lop;

    @Column(name = "Khoa", length = 100)
    private String khoa;

    @Column(name = "QueQuan", length = 255)
    private String queQuan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaPhongHienTai")
    private Phong phongHienTai;

    @Column(name = "TrangThai", length = 20)
    private String trangThai = "CHO_DUYET";

    public SinhVien() {}

    public String getMaSinhVien() { return maSinhVien; }
    public void setMaSinhVien(String maSinhVien) { this.maSinhVien = maSinhVien; }

    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public void setTaiKhoan(TaiKhoan taiKhoan) { this.taiKhoan = taiKhoan; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public String getLop() { return lop; }
    public void setLop(String lop) { this.lop = lop; }

    public String getKhoa() { return khoa; }
    public void setKhoa(String khoa) { this.khoa = khoa; }

    public String getQueQuan() { return queQuan; }
    public void setQueQuan(String queQuan) { this.queQuan = queQuan; }

    public Phong getPhongHienTai() { return phongHienTai; }
    public void setPhongHienTai(Phong phongHienTai) { this.phongHienTai = phongHienTai; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
