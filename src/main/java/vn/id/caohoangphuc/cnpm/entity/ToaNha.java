package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "TOA_NHA")
public class ToaNha implements Serializable {

    @Id
    @Column(name = "MaToaNha", length = 20)
    private String maToaNha;

    @Column(name = "TenToa", nullable = false, length = 100)
    private String tenToa;

    @Column(name = "SoTang", nullable = false)
    private Integer soTang = 1;

    @Column(name = "GioiTinhApDung", length = 20)
    private String gioiTinhApDung = "TAT_CA";

    @Column(name = "GhiChu", length = 255)
    private String ghiChu;

    public ToaNha() {}

    public String getMaToaNha() { return maToaNha; }
    public void setMaToaNha(String maToaNha) { this.maToaNha = maToaNha; }

    public String getTenToa() { return tenToa; }
    public void setTenToa(String tenToa) { this.tenToa = tenToa; }

    public Integer getSoTang() { return soTang; }
    public void setSoTang(Integer soTang) { this.soTang = soTang; }

    public String getGioiTinhApDung() { return gioiTinhApDung; }
    public void setGioiTinhApDung(String gioiTinhApDung) { this.gioiTinhApDung = gioiTinhApDung; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
