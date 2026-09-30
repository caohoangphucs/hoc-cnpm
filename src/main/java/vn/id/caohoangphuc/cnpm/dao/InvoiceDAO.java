package vn.id.caohoangphuc.cnpm.dao;

import vn.id.caohoangphuc.cnpm.config.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoiceDAO {

    public static List<Map<String, Object>> getInvoicesByRoom(String roomId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM HOA_DON WHERE MaPhong = ? ORDER BY KyThang DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> h = new HashMap<>();
                    h.put("id", rs.getInt("MaHoaDon"));
                    h.put("roomId", rs.getString("MaPhong"));
                    h.put("month", rs.getString("KyThang"));
                    h.put("roomFee", rs.getDouble("TienPhong"));
                    h.put("electricFee", rs.getDouble("TienDien"));
                    h.put("waterFee", rs.getDouble("TienNuoc"));
                    h.put("totalAmount", rs.getDouble("TongTien"));
                    h.put("paidAmount", rs.getDouble("DaThanhToan"));
                    h.put("dueDate", rs.getDate("HanThanhToan"));
                    h.put("status", rs.getString("TrangThai"));
                    h.put("createdAt", rs.getTimestamp("NgayPhatHanh"));
                    list.add(h);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Map<String, Object>> getAllInvoices() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM HOA_DON ORDER BY KyThang DESC, MaPhong ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> h = new HashMap<>();
                h.put("id", rs.getInt("MaHoaDon"));
                h.put("roomId", rs.getString("MaPhong"));
                h.put("month", rs.getString("KyThang"));
                h.put("roomFee", rs.getDouble("TienPhong"));
                h.put("electricFee", rs.getDouble("TienDien"));
                h.put("waterFee", rs.getDouble("TienNuoc"));
                h.put("totalAmount", rs.getDouble("TongTien"));
                h.put("paidAmount", rs.getDouble("DaThanhToan"));
                h.put("dueDate", rs.getDate("HanThanhToan"));
                h.put("status", rs.getString("TrangThai"));
                list.add(h);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Map<String, Object> recordMeter(String roomId, String month, int oldE, int newE, int oldW, int newW, String recorder) {
        Map<String, Object> res = new HashMap<>();
        String sql = "{CALL usp_GhiChiSoDienNuoc(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DBContext.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, roomId);
            cs.setString(2, month);
            cs.setInt(3, oldE);
            cs.setInt(4, newE);
            cs.setInt(5, oldW);
            cs.setInt(6, newW);
            cs.setString(7, recorder);
            cs.registerOutParameter(8, Types.VARCHAR);
            cs.registerOutParameter(9, Types.VARCHAR);

            cs.execute();
            res.put("status", cs.getString(8));
            res.put("message", cs.getString(9));
        } catch (Exception e) {
            e.printStackTrace();
            res.put("status", "ERROR");
            res.put("message", e.getMessage());
        }
        return res;
    }

    public static Map<String, Object> generateInvoice(String roomId, String month) {
        Map<String, Object> res = new HashMap<>();
        String sql = "{CALL usp_PhatHanhHoaDon(?, ?, ?, ?, ?)}";
        try (Connection conn = DBContext.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, roomId);
            cs.setString(2, month);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.registerOutParameter(4, Types.VARCHAR);
            cs.registerOutParameter(5, Types.VARCHAR);

            cs.execute();
            res.put("invoiceId", cs.getInt(3));
            res.put("status", cs.getString(4));
            res.put("message", cs.getString(5));
        } catch (Exception e) {
            e.printStackTrace();
            res.put("status", "ERROR");
            res.put("message", e.getMessage());
        }
        return res;
    }

    public static Map<String, Object> confirmPayment(String txnRef, String method, double amount, String confirmedBy) {
        Map<String, Object> res = new HashMap<>();
        String sql = "{CALL usp_XacNhanThanhToan(?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DBContext.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, txnRef);
            cs.setString(2, method);
            cs.setDouble(3, amount);
            cs.setString(4, confirmedBy);
            cs.registerOutParameter(5, Types.VARCHAR);
            cs.registerOutParameter(6, Types.VARCHAR);

            cs.execute();
            res.put("status", cs.getString(5));
            res.put("message", cs.getString(6));
        } catch (Exception e) {
            e.printStackTrace();
            res.put("status", "ERROR");
            res.put("message", e.getMessage());
        }
        return res;
    }
}
