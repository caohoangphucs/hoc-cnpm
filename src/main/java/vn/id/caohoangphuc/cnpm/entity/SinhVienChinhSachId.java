package vn.id.caohoangphuc.cnpm.entity;

import java.io.Serializable;
import java.util.Objects;

public class SinhVienChinhSachId implements Serializable {
    private String sinhVien;
    private String chinhSachGiamGia;

    public SinhVienChinhSachId() {}

    public SinhVienChinhSachId(String sinhVien, String chinhSachGiamGia) {
        this.sinhVien = sinhVien;
        this.chinhSachGiamGia = chinhSachGiamGia;
    }

    public String getSinhVien() { return sinhVien; }
    public void setSinhVien(String sinhVien) { this.sinhVien = sinhVien; }

    public String getChinhSachGiamGia() { return chinhSachGiamGia; }
    public void setChinhSachGiamGia(String chinhSachGiamGia) { this.chinhSachGiamGia = chinhSachGiamGia; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SinhVienChinhSachId that = (SinhVienChinhSachId) o;
        return Objects.equals(sinhVien, that.sinhVien) && Objects.equals(chinhSachGiamGia, that.chinhSachGiamGia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sinhVien, chinhSachGiamGia);
    }
}
