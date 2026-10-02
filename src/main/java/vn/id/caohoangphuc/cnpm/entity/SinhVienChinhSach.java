package vn.id.caohoangphuc.cnpm.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "SINH_VIEN_CHINH_SACH")
@IdClass(SinhVienChinhSachId.class)
public class SinhVienChinhSach implements Serializable {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSinhVien", nullable = false)
    private SinhVien sinhVien;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaChinhSach", nullable = false)
    private ChinhSachGiamGia chinhSachGiamGia;

    @Column(name = "NgayXacNhan")
    private LocalDate ngayXacNhan;

    @Column(name = "NguoiXacNhan", length = 50)
    private String nguoiXacNhan;

    public SinhVienChinhSach() {}

    public SinhVien getSinhVien() { return sinhVien; }
    public void setSinhVien(SinhVien sinhVien) { this.sinhVien = sinhVien; }

    public ChinhSachGiamGia getChinhSachGiamGia() { return chinhSachGiamGia; }
    public void setChinhSachGiamGia(ChinhSachGiamGia chinhSachGiamGia) { this.chinhSachGiamGia = chinhSachGiamGia; }

    public LocalDate getNgayXacNhan() { return ngayXacNhan; }
    public void setNgayXacNhan(LocalDate ngayXacNhan) { this.ngayXacNhan = ngayXacNhan; }

    public String getNguoiXacNhan() { return nguoiXacNhan; }
    public void setNguoiXacNhan(String nguoiXacNhan) { this.nguoiXacNhan = nguoiXacNhan; }
}
