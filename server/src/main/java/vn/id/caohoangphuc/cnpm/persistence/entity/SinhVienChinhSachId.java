package vn.id.caohoangphuc.cnpm.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

public class SinhVienChinhSachId implements Serializable {
    private static final long serialVersionUID = 1L;
    private String maSinhVien;
    private String maChinhSach;
    public SinhVienChinhSachId() {}
    public SinhVienChinhSachId(String maSinhVien, String maChinhSach) {
        this.maSinhVien = maSinhVien; this.maChinhSach = maChinhSach;
    }
    public String getMaSinhVien() { return maSinhVien; }
    public String getMaChinhSach() { return maChinhSach; }
    @Override public boolean equals(Object other) {
        return other instanceof SinhVienChinhSachId id
            && Objects.equals(maSinhVien, id.maSinhVien) && Objects.equals(maChinhSach, id.maChinhSach);
    }
    @Override public int hashCode() { return Objects.hash(maSinhVien, maChinhSach); }
}
