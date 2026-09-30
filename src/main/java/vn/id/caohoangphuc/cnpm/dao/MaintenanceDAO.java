package vn.id.caohoangphuc.cnpm.dao;

import vn.id.caohoangphuc.cnpm.config.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaintenanceDAO {

    public static List<Map<String, Object>> getSupplies() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM VAT_TU ORDER BY MaVatTu";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> v = new HashMap<>();
                v.put("id", rs.getString("MaVatTu"));
                v.put("name", rs.getString("TenVatTu"));
                v.put("unit", rs.getString("DonViTinh"));
                v.put("stock", rs.getInt("SoLuongTon"));
                v.put("price", rs.getDouble("DonGiaNhap"));
                list.add(v);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Map<String, Object>> getRepairRequests() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT yc.*, sv.HoTen AS TenSinhVien, sv.SoDienThoai FROM YEU_CAU_SUA_CHUA yc " +
                     "JOIN SINH_VIEN sv ON yc.MaSinhVien = sv.MaSinhVien ORDER BY yc.NgayTao DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> y = new HashMap<>();
                y.put("id", rs.getInt("MaYeuCau"));
                y.put("roomId", rs.getString("MaPhong"));
                y.put("studentId", rs.getString("MaSinhVien"));
                y.put("studentName", rs.getString("TenSinhVien"));
                y.put("phone", rs.getString("SoDienThoai"));
                y.put("title", rs.getString("TieuDe"));
                y.put("description", rs.getString("MoTaChiTiet"));
                y.put("priority", rs.getString("MucDoUuTien"));
                y.put("status", rs.getString("TrangThai"));
                y.put("createdAt", rs.getTimestamp("NgayTao"));
                y.put("completedAt", rs.getTimestamp("NgayHoanThanh"));
                list.add(y);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Map<String, Object> completeRepair(int reqId, String supplyId, int quantity, String worker, String note) {
        Map<String, Object> res = new HashMap<>();
        String sql = "{CALL usp_HoanThanhSuaChua(?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DBContext.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, reqId);
            cs.setString(2, supplyId);
            cs.setInt(3, quantity);
            cs.setString(4, worker);
            cs.setString(5, note);
            cs.registerOutParameter(6, Types.VARCHAR);
            cs.registerOutParameter(7, Types.VARCHAR);

            cs.execute();
            res.put("status", cs.getString(6));
            res.put("message", cs.getString(7));
        } catch (Exception e) {
            e.printStackTrace();
            res.put("status", "ERROR");
            res.put("message", e.getMessage());
        }
        return res;
    }
}
