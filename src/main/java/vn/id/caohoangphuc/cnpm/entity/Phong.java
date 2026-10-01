package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "PHONG")
public class Phong implements Serializable {

    @Id
    @Column(name = "MaPhong", length = 20)
    private String maPhong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaToaNha", nullable = false)
    private ToaNha toaNha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaLoaiPhong")
    private LoaiPhong loaiPhong;

    @Column(name = "SoPhong", nullable = false, length = 20)
    private String soPhong;

    @Column(name = "Tang", nullable = false)
    private Integer tang = 1;

    @Column(name = "SucChua", nullable = false)
    private Integer sucChua = 4;

    @Column(name = "DangO", nullable = false)
    private Integer dangO = 0;

    @Column(name = "GiaPhongCoBan", nullable = false, precision = 15, scale = 0)
    private BigDecimal giaPhongCoBan = BigDecimal.valueOf(500000);

    @Column(name = "TrangThai", length = 20)
    private String trangThai = "TRONG";

    public Phong() {}

    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }

    public ToaNha getToaNha() { return toaNha; }
    public void setToaNha(ToaNha toaNha) { this.toaNha = toaNha; }

    public LoaiPhong getLoaiPhong() { return loaiPhong; }
    public void setLoaiPhong(LoaiPhong loaiPhong) { this.loaiPhong = loaiPhong; }

    public String getSoPhong() { return soPhong; }
    public void setSoPhong(String soPhong) { this.soPhong = soPhong; }

    public Integer getTang() { return tang; }
    public void setTang(Integer tang) { this.tang = tang; }

    public Integer getSucChua() { return sucChua; }
    public void setSucChua(Integer sucChua) { this.sucChua = sucChua; }

    public Integer getDangO() { return dangO; }
    public void setDangO(Integer dangO) { this.dangO = dangO; }

    public BigDecimal getGiaPhongCoBan() { return giaPhongCoBan; }
    public void setGiaPhongCoBan(BigDecimal giaPhongCoBan) { this.giaPhongCoBan = giaPhongCoBan; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
