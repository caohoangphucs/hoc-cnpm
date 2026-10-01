package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "LOAI_PHONG")
public class LoaiPhong implements Serializable {

    @Id
    @Column(name = "MaLoaiPhong", length = 20)
    private String maLoaiPhong;

    @Column(name = "TenLoaiPhong", nullable = false, length = 100)
    private String tenLoaiPhong;

    @Column(name = "SucChuaTieuChuan", nullable = false)
    private Integer sucChuaTieuChuan = 4;

    @Column(name = "DonGiaThang", nullable = false, precision = 15, scale = 0)
    private BigDecimal donGiaThang = BigDecimal.valueOf(500000);

    @Column(name = "MoTa", columnDefinition = "TEXT")
    private String moTa;

    public LoaiPhong() {}

    public String getMaLoaiPhong() { return maLoaiPhong; }
    public void setMaLoaiPhong(String maLoaiPhong) { this.maLoaiPhong = maLoaiPhong; }

    public String getTenLoaiPhong() { return tenLoaiPhong; }
    public void setTenLoaiPhong(String tenLoaiPhong) { this.tenLoaiPhong = tenLoaiPhong; }

    public Integer getSucChuaTieuChuan() { return sucChuaTieuChuan; }
    public void setSucChuaTieuChuan(Integer sucChuaTieuChuan) { this.sucChuaTieuChuan = sucChuaTieuChuan; }

    public BigDecimal getDonGiaThang() { return donGiaThang; }
    public void setDonGiaThang(BigDecimal donGiaThang) { this.donGiaThang = donGiaThang; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
}
