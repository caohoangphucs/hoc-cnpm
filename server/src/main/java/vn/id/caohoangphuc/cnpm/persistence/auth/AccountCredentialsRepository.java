package vn.id.caohoangphuc.cnpm.persistence.auth;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AccountCredentialsRepository {
    private final JdbcTemplate jdbc;
    public AccountCredentialsRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public Optional<AccountCredentials> findByUsername(String username) {
        var rows = jdbc.query("SELECT * FROM usp_DangNhap(?)", (rs, row) ->
            new AccountCredentials(rs.getInt("MaTaiKhoan"), rs.getString("TenDangNhap"),
                rs.getString("MatKhauHash"), rs.getString("HoTen"), rs.getString("Email"),
                rs.getString("VaiTro"), rs.getString("TrangThai"), rs.getString("MaSinhVien"),
                rs.getString("MaPhongHienTai")), username);
        if (rows.size() > 1) throw new IllegalStateException("Duplicate username result");
        return rows.stream().findFirst();
    }
}
