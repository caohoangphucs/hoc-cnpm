package vn.id.caohoangphuc.cnpm.persistence.auth;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;

public class AccountCredentialsRepository {
    private final JdbcTemplate jdbc;

    public AccountCredentialsRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** Loads login details with a parameterized query; password matching stays in Spring Security. */
    public Optional<AccountCredentials> findByUsername(String username) {
        var rows = jdbc.query(
            "SELECT tk.MaTaiKhoan, tk.TenDangNhap, tk.MatKhauHash, tk.HoTen, tk.Email,"
                + " tk.VaiTro, tk.TrangThai, sv.MaSinhVien, sv.MaPhongHienTai"
                + " FROM TAI_KHOAN tk LEFT JOIN SINH_VIEN sv ON sv.MaTaiKhoan=tk.MaTaiKhoan"
                + " WHERE tk.TenDangNhap=?",
            (rs, row) -> new AccountCredentials(
                rs.getInt("MaTaiKhoan"),
                rs.getString("TenDangNhap"),
                rs.getString("MatKhauHash"),
                rs.getString("HoTen"),
                rs.getString("Email"),
                rs.getString("VaiTro"),
                rs.getString("TrangThai"),
                rs.getString("MaSinhVien"),
                rs.getString("MaPhongHienTai")),
            username);
        if (rows.size() > 1) throw new IllegalStateException("Duplicate username result");
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }
}
