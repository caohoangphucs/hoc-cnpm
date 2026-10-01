package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.tai_khoan; business writes belong to stored procedures. */
@Entity
@Immutable
@Table(name = "tai_khoan", schema = "public")
public class TaiKhoanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mataikhoan", nullable = false)
    private Integer maTaiKhoan;

    @Column(name = "tendangnhap", nullable = false, length = 50)
    private String tenDangNhap;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "matkhauhash", nullable = false, length = 255)
    private String matKhauHash;

    @Column(name = "hoten", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "sodienthoai", nullable = true, length = 20)
    private String soDienThoai;

    @Column(name = "vaitro", nullable = false, length = 20)
    private String vaiTro;

    @Column(name = "trangthai", nullable = true, length = 20)
    private String trangThai;

    @Column(name = "ngaytao", nullable = true)
    private java.time.LocalDateTime ngayTao;

    protected TaiKhoanEntity() {}

    public Integer getMaTaiKhoan() { return maTaiKhoan; }
    public String getTenDangNhap() { return tenDangNhap; }
    public String getMatKhauHash() { return matKhauHash; }
    public String getHoTen() { return hoTen; }
    public String getEmail() { return email; }
    public String getSoDienThoai() { return soDienThoai; }
    public String getVaiTro() { return vaiTro; }
    public String getTrangThai() { return trangThai; }
    public java.time.LocalDateTime getNgayTao() { return ngayTao; }
}
