package vn.id.caohoangphuc.cnpm.dao;

import org.mindrot.jbcrypt.BCrypt;
import vn.id.caohoangphuc.cnpm.config.DBContext;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class AccountDAO {

    public static Map<String, Object> login(String username, String password) {
        String sql = "{CALL usp_DangNhap(?)}";
        try (Connection conn = DBContext.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, username);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("MatKhauHash");
                    String status = rs.getString("TrangThai");

                    if ("LOCKED".equalsIgnoreCase(status) || "DISABLED".equalsIgnoreCase(status)) {
                        Map<String, Object> res = new HashMap<>();
                        res.put("status", "LOCKED");
                        res.put("message", "Tài khoản đang bị khóa hoặc vô hiệu hóa.");
                        return res;
                    }

                    // Chỉ chấp nhận BCrypt hợp lệ; hash hỏng không được bypass mật khẩu.
                    boolean valid = false;
                    try {
                        valid = BCrypt.checkpw(password, hash);
                    } catch (Exception e) {
                        valid = false;
                    }

                    if (valid) {
                        Map<String, Object> user = new HashMap<>();
                        user.put("id", rs.getInt("MaTaiKhoan"));
                        user.put("username", rs.getString("TenDangNhap"));
                        user.put("fullName", rs.getString("HoTen"));
                        user.put("email", rs.getString("Email"));
                        user.put("role", rs.getString("VaiTro"));
                        user.put("studentId", rs.getString("MaSinhVien"));
                        user.put("roomId", rs.getString("MaPhongHienTai"));
                        user.put("status", "SUCCESS");
                        return user;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, Object> err = new HashMap<>();
        err.put("status", "FAILED");
        err.put("message", "Tên đăng nhập hoặc mật khẩu không chính xác.");
        return err;
    }
}
