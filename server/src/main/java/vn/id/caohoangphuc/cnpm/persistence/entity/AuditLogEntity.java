package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.audit_log; business writes belong to stored procedures. */
@Entity
@Immutable
@Table(name = "audit_log", schema = "public")
public class AuditLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "malog", nullable = false)
    private Integer maLog;

    @Column(name = "tenbang", nullable = false, length = 50)
    private String tenBang;

    @Column(name = "hanhdong", nullable = false, length = 20)
    private String hanhDong;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "dulieucu", nullable = true, columnDefinition = "jsonb")
    private java.util.Map<String, Object> duLieuCu;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "dulieumoi", nullable = true, columnDefinition = "jsonb")
    private java.util.Map<String, Object> duLieuMoi;

    @Column(name = "nguoithuchien", nullable = true, length = 50)
    private String nguoiThucHien;

    @Column(name = "thoigian", nullable = true)
    private java.time.LocalDateTime thoiGian;

    protected AuditLogEntity() {}

    public Integer getMaLog() { return maLog; }
    public String getTenBang() { return tenBang; }
    public String getHanhDong() { return hanhDong; }
    public java.util.Map<String, Object> getDuLieuCu() { return duLieuCu; }
    public java.util.Map<String, Object> getDuLieuMoi() { return duLieuMoi; }
    public String getNguoiThucHien() { return nguoiThucHien; }
    public java.time.LocalDateTime getThoiGian() { return thoiGian; }
}
