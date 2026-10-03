package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.toa_nha; business writes are handled by service-layer code. */
@Entity
@Immutable
@Table(name = "toa_nha", schema = "public")
public class ToaNhaEntity {
    @Id
    @Column(name = "matoanha", nullable = false, length = 20)
    private String maToaNha;

    @Column(name = "tentoa", nullable = false, length = 100)
    private String tenToa;

    @Column(name = "sotang", nullable = false)
    private Integer soTang;

    @Column(name = "gioitinhapdung", nullable = true, length = 20)
    private String gioiTinhApDung;

    @Column(name = "ghichu", nullable = true, length = 255)
    private String ghiChu;

    protected ToaNhaEntity() {}

    public String getMaToaNha() { return maToaNha; }
    public String getTenToa() { return tenToa; }
    public Integer getSoTang() { return soTang; }
    public String getGioiTinhApDung() { return gioiTinhApDung; }
    public String getGhiChu() { return ghiChu; }
}
