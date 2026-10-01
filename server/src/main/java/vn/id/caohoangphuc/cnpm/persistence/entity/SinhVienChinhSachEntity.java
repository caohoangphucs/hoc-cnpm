package vn.id.caohoangphuc.cnpm.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Read model of public.sinh_vien_chinh_sach; business writes belong to stored procedures. */
@Entity
@Immutable
@Table(name = "sinh_vien_chinh_sach", schema = "public")
@IdClass(SinhVienChinhSachId.class)
public class SinhVienChinhSachEntity {
    @Id
    @Column(name = "masinhvien", nullable = false, length = 20)
    private String maSinhVien;

    @Id
    @Column(name = "machinhsach", nullable = false, length = 20)
    private String maChinhSach;

    @Column(name = "ngayxacnhan", nullable = true)
    private java.time.LocalDate ngayXacNhan;

    @Column(name = "nguoixacnhan", nullable = true, length = 50)
    private String nguoiXacNhan;

    protected SinhVienChinhSachEntity() {}

    public String getMaSinhVien() { return maSinhVien; }
    public String getMaChinhSach() { return maChinhSach; }
    public java.time.LocalDate getNgayXacNhan() { return ngayXacNhan; }
    public String getNguoiXacNhan() { return nguoiXacNhan; }
}
