package vn.id.caohoangphuc.cnpm.dao;

import vn.id.caohoangphuc.cnpm.config.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportDAO {

    public static List<Map<String, Object>> getOccupancyRate() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_TyLeLapDayPhong";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> r = new HashMap<>();
                r.put("buildingId", rs.getString("MaToaNha"));
                r.put("buildingName", rs.getString("TenToa"));
                r.put("totalRooms", rs.getInt("TongSoPhong"));
                r.put("totalCapacity", rs.getInt("TongSucChua"));
                r.put("totalOccupied", rs.getInt("TongDangO"));
                r.put("rate", rs.getDouble("TyLeLapDayPhanTram"));
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Map<String, Object>> getMonthlyRevenue() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_DoanhThuThang ORDER BY KyThang DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> r = new HashMap<>();
                r.put("month", rs.getString("KyThang"));
                r.put("totalInvoices", rs.getInt("TongHoaDon"));
                r.put("totalBilled", rs.getDouble("TongTienPhatHanh"));
                r.put("totalCollected", rs.getDouble("TongThucThu"));
                r.put("paidInvoices", rs.getInt("SoHoaDonDaThu"));
                r.put("debtAmount", rs.getDouble("CongNoConLai"));
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
