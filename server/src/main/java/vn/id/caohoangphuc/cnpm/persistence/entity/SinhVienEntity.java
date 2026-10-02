package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.sinh_vien; business writes belong to stored procedures. */
@Entity
@Immutable
@Table(name = "sinh_vien", schema = "public")
public class SinhVienEntity {
    @Id
    @Column(name = "masinhvien", nullable = false, length = 20)
    private String maSinhVien;

    @Column(name = "mataikhoan", nullable = true)
    private Integer maTaiKhoan;

    @Column(name = "hoten", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "ngaysinh", nullable = true)
    private java.time.LocalDate ngaySinh;

    @Column(name = "gioitinh", nullable = false, length = 10)
    private String gioiTinh;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "cccd", nullable = false, length = 20)
    private String cCCD;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "sodienthoai", nullable = false, length = 20)
    private String soDienThoai;

    @Column(name = "lop", nullable = true, length = 50)
    private String lop;

    @Column(name = "khoa", nullable = true, length = 100)
    private String khoa;

    @Column(name = "quequan", nullable = true, length = 255)
    private String queQuan;

    @Column(name = "maphonghientai", nullable = true, length = 20)
    private String maPhongHienTai;

    @Column(name = "trangthai", nullable = true, length = 20)
    private String trangThai;

    protected SinhVienEntity() {}

    public String getMaSinhVien() { return maSinhVien; }
    public Integer getMaTaiKhoan() { return maTaiKhoan; }
    public String getHoTen() { return hoTen; }
    public java.time.LocalDate getNgaySinh() { return ngaySinh; }
    public String getGioiTinh() { return gioiTinh; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getCCCD() { return cCCD; }
    public String getEmail() { return email; }
    public String getSoDienThoai() { return soDienThoai; }
    public String getLop() { return lop; }
    public String getKhoa() { return khoa; }
    public String getQueQuan() { return queQuan; }
    public String getMaPhongHienTai() { return maPhongHienTai; }
    public String getTrangThai() { return trangThai; }
}
