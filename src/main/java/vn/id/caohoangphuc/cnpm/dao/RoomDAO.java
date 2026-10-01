package vn.id.caohoangphuc.cnpm.dao;

import vn.id.caohoangphuc.cnpm.config.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomDAO {

    public static List<Map<String, Object>> getAvailableRooms() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_PhongConCho ORDER BY MaToaNha, SoPhong";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> r = new HashMap<>();
                r.put("roomId", rs.getString("MaPhong"));
                r.put("buildingId", rs.getString("MaToaNha"));
                r.put("buildingName", rs.getString("TenToa"));
                r.put("roomNumber", rs.getString("SoPhong"));
                r.put("floor", rs.getInt("Tang"));
                r.put("capacity", rs.getInt("SucChua"));
                r.put("occupied", rs.getInt("DangO"));
                r.put("available", rs.getInt("SoChoTrong"));
                r.put("price", rs.getDouble("GiaPhongCoBan"));
                r.put("status", rs.getString("TrangThaiPhong"));
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Map<String, Object>> getBedsByRoom(String roomId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM GIUONG WHERE MaPhong = ? ORDER BY SoGiuong";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> b = new HashMap<>();
                    b.put("bedId", rs.getString("MaGiuong"));
                    b.put("roomId", rs.getString("MaPhong"));
                    b.put("bedNumber", rs.getString("SoGiuong"));
                    b.put("position", rs.getString("ViTri"));
                    b.put("status", rs.getString("TrangThai"));
                    list.add(b);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Map<String, Object> bookBed(String studentId, String bedId) {
        Map<String, Object> res = new HashMap<>();
        String sql = "{CALL usp_DatGiuong(?, ?, ?, ?, ?)}";
        try (Connection conn = DBContext.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, studentId);
            cs.setString(2, bedId);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.registerOutParameter(4, Types.VARCHAR);
            cs.registerOutParameter(5, Types.VARCHAR);

            cs.execute();
            res.put("registrationId", cs.getInt(3));
            res.put("status", cs.getString(4));
            res.put("message", cs.getString(5));
        } catch (Exception e) {
            e.printStackTrace();
            res.put("status", "ERROR");
            res.put("message", e.getMessage());
        }
        return res;
    }

    public static List<Map<String, Object>> getPendingRegistrations() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT dk.*, sv.HoTen, sv.Email, sv.SoDienThoai, g.MaPhong, g.SoGiuong " +
                     "FROM DANG_KY_PHONG dk " +
                     "JOIN SINH_VIEN sv ON dk.MaSinhVien = sv.MaSinhVien " +
                     "JOIN GIUONG g ON dk.MaGiuong = g.MaGiuong " +
                     "WHERE dk.TrangThai = 'CHO_DUYET' ORDER BY dk.NgayDangKy DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> r = new HashMap<>();
                r.put("id", rs.getInt("MaDangKy"));
                r.put("studentId", rs.getString("MaSinhVien"));
                r.put("studentName", rs.getString("HoTen"));
                r.put("email", rs.getString("Email"));
                r.put("phone", rs.getString("SoDienThoai"));
                r.put("bedId", rs.getString("MaGiuong"));
                r.put("roomId", rs.getString("MaPhong"));
                r.put("bedNumber", rs.getString("SoGiuong"));
                r.put("createdAt", rs.getTimestamp("NgayDangKy"));
                r.put("expireAt", rs.getTimestamp("NgayHetHan"));
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Map<String, Object> approveRegistration(int registrationId, String decision, String note) {
        Map<String, Object> res = new HashMap<>();
        String sql = "{CALL usp_DuyetDangKy(?, ?, ?, ?, ?)}";
        try (Connection conn = DBContext.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, registrationId);
            cs.setString(2, decision);
            cs.setString(3, note);
            cs.registerOutParameter(4, Types.VARCHAR);
            cs.registerOutParameter(5, Types.VARCHAR);

            cs.execute();
            res.put("status", cs.getString(4));
            res.put("message", cs.getString(5));
        } catch (Exception e) {
            e.printStackTrace();
            res.put("status", "ERROR");
            res.put("message", e.getMessage());
        }
        return res;
    }
}
