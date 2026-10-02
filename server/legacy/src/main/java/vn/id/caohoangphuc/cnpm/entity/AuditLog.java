package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "AUDIT_LOG")
public class AuditLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaLog")
    private Integer maLog;

    @Column(name = "TenBang", nullable = false, length = 50)
    private String tenBang;

    @Column(name = "HanhDong", nullable = false, length = 20)
    private String hanhDong;

    @Column(name = "DuLieuCu", columnDefinition = "jsonb")
    private String duLieuCu;

    @Column(name = "DuLieuMoi", columnDefinition = "jsonb")
    private String duLieuMoi;

    @Column(name = "NguoiThucHien", length = 50)
    private String nguoiThucHien = "SYSTEM";

    @Column(name = "ThoiGian", insertable = false, updatable = false)
    private LocalDateTime thoiGian;

    public AuditLog() {}

    public Integer getMaLog() { return maLog; }
    public void setMaLog(Integer maLog) { this.maLog = maLog; }

    public String getTenBang() { return tenBang; }
    public void setTenBang(String tenBang) { this.tenBang = tenBang; }

    public String getHanhDong() { return hanhDong; }
    public void setHanhDong(String hanhDong) { this.hanhDong = hanhDong; }

    public String getDuLieuCu() { return duLieuCu; }
    public void setDuLieuCu(String duLieuCu) { this.duLieuCu = duLieuCu; }

    public String getDuLieuMoi() { return duLieuMoi; }
    public void setDuLieuMoi(String duLieuMoi) { this.duLieuMoi = duLieuMoi; }

    public String getNguoiThucHien() { return nguoiThucHien; }
    public void setNguoiThucHien(String nguoiThucHien) { this.nguoiThucHien = nguoiThucHien; }

    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }
}
