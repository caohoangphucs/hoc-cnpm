package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "GIUONG")
public class Giuong implements Serializable {

    @Id
    @Column(name = "MaGiuong", length = 20)
    private String maGiuong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaPhong", nullable = false)
    private Phong phong;

    @Column(name = "SoGiuong", nullable = false, length = 20)
    private String soGiuong;

    @Column(name = "ViTri", length = 50)
    private String viTri = "Tang 1";

    @Column(name = "TrangThai", length = 20)
    private String trangThai = "TRONG";

    public Giuong() {}

    public String getMaGiuong() { return maGiuong; }
    public void setMaGiuong(String maGiuong) { this.maGiuong = maGiuong; }

    public Phong getPhong() { return phong; }
    public void setPhong(Phong phong) { this.phong = phong; }

    public String getSoGiuong() { return soGiuong; }
    public void setSoGiuong(String soGiuong) { this.soGiuong = soGiuong; }

    public String getViTri() { return viTri; }
    public void setViTri(String viTri) { this.viTri = viTri; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
