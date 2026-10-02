package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "DOT_DANG_KY")
public class DotDangKy implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDot")
    private Integer maDot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaHocKy", nullable = false)
    private HocKy hocKy;

    @Column(name = "TenDot", nullable = false, length = 100)
    private String tenDot;

    @Column(name = "ThoiGianMo", nullable = false)
    private LocalDateTime thoiGianMo;

    @Column(name = "ThoiGianDong", nullable = false)
    private LocalDateTime thoiGianDong;

    @Column(name = "TrangThai", length = 20)
    private String trangThai = "ACTIVE";

    public DotDangKy() {}

    public Integer getMaDot() { return maDot; }
    public void setMaDot(Integer maDot) { this.maDot = maDot; }

    public HocKy getHocKy() { return hocKy; }
    public void setHocKy(HocKy hocKy) { this.hocKy = hocKy; }

    public String getTenDot() { return tenDot; }
    public void setTenDot(String tenDot) { this.tenDot = tenDot; }

    public LocalDateTime getThoiGianMo() { return thoiGianMo; }
    public void setThoiGianMo(LocalDateTime thoiGianMo) { this.thoiGianMo = thoiGianMo; }

    public LocalDateTime getThoiGianDong() { return thoiGianDong; }
    public void setThoiGianDong(LocalDateTime thoiGianDong) { this.thoiGianDong = thoiGianDong; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
