package vn.id.caohoangphuc.cnpm.dao;

import vn.id.caohoangphuc.cnpm.config.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityDAO {

    public static List<Map<String, Object>> getEntryLogs() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT rv.*, sv.HoTen, sv.MaPhongHienTai FROM RA_VAO rv " +
                     "JOIN SINH_VIEN sv ON rv.MaSinhVien = sv.MaSinhVien ORDER BY rv.ThoiGian DESC LIMIT 50";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> r = new HashMap<>();
                r.put("id", rs.getInt("MaRaVao"));
                r.put("studentId", rs.getString("MaSinhVien"));
                r.put("studentName", rs.getString("HoTen"));
                r.put("room", rs.getString("MaPhongHienTai"));
                r.put("type", rs.getString("LoaiSuKien"));
                r.put("time", rs.getTimestamp("ThoiGian"));
                r.put("guard", rs.getString("BaoVeGhiNhan"));
                r.put("note", rs.getString("GhiChu"));
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Map<String, Object>> getViolations() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT vp.*, sv.HoTen, sv.MaPhongHienTai FROM VI_PHAM vp " +
                     "JOIN SINH_VIEN sv ON vp.MaSinhVien = sv.MaSinhVien ORDER BY vp.NgayViPham DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> v = new HashMap<>();
                v.put("id", rs.getInt("MaViPham"));
                v.put("studentId", rs.getString("MaSinhVien"));
                v.put("studentName", rs.getString("HoTen"));
                v.put("room", rs.getString("MaPhongHienTai"));
                v.put("content", rs.getString("NoiDungViPham"));
                v.put("level", rs.getString("MucDo"));
                v.put("action", rs.getString("HinhThucXuLy"));
                v.put("time", rs.getTimestamp("NgayViPham"));
                v.put("status", rs.getString("TrangThai"));
                list.add(v);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Map<String, Object> recordEntry(String studentId, String type, String guard, String note) {
        Map<String, Object> res = new HashMap<>();
        String sql = "{CALL usp_GhiNhanRaVao(?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DBContext.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, studentId);
            cs.setString(2, type);
            cs.setString(3, guard);
            cs.setString(4, note);
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
