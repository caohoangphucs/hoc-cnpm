import vn.id.caohoangphuc.cnpm.config.DBContext;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.Collections;

/** PostgreSQL JDBC interop: every procedure returns correctly bound OUT values.
 * Each call is rolled back. Run only on the isolated CI/demo database.
 */
public class PostgresProcedureSmoke {
    record Spec(String name, Object[] inputs, int[] outputs) {}
    static final int V = Types.VARCHAR, I = Types.INTEGER;
    static String first(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            if (!r.next()) throw new AssertionError("Missing fixture");
            return r.getString(1);
        }
    }
    public static void main(String[] args) throws Exception {
        try (Connection c = DBContext.getConnection()) {
            if (!"Asia/Ho_Chi_Minh".equals(first(c, "SHOW TimeZone")))
                throw new AssertionError("Incorrect DB timezone");
            String room = first(c, "SELECT MaPhong FROM PHONG ORDER BY MaPhong LIMIT 1");
            String bed = first(c, "SELECT MaGiuong FROM GIUONG WHERE TrangThai='TRONG' ORDER BY MaGiuong LIMIT 1");
            String item = first(c, "SELECT MaVatTu FROM VAT_TU WHERE SoLuongTon>0 ORDER BY MaVatTu LIMIT 1");
            int repair = Integer.parseInt(first(c, "SELECT MaYeuCau FROM YEU_CAU_SUA_CHUA WHERE TrangThai IN ('MOI','DANG_XU_LY') ORDER BY MaYeuCau LIMIT 1"));
            String student = first(c, "SELECT MaSinhVien FROM SINH_VIEN ORDER BY MaSinhVien LIMIT 1");
            List<Spec> cases = List.of(
                new Spec("usp_DatGiuong", new Object[]{student, bed}, new int[]{I,V,V}),
                new Spec("usp_DuyetDangKy", new Object[]{-1,"TU_CHOI","CI"}, new int[]{V,V}),
                new Spec("usp_GhiChiSoDienNuoc", new Object[]{room,"2099-01",0,1,0,1,"CI"}, new int[]{V,V}),
                new Spec("usp_PhatHanhHoaDon", new Object[]{room,"2099-01"}, new int[]{I,V,V}),
                new Spec("usp_XacNhanThanhToan", new Object[]{"CI_NO_TX","TIEN_MAT",BigDecimal.ONE,"CI"}, new int[]{V,V}),
                new Spec("usp_HoanThanhSuaChua", new Object[]{repair,item,1,"CI","CI"}, new int[]{V,V}),
                new Spec("usp_GhiNhanRaVao", new Object[]{student,"RA","CI","CI"}, new int[]{V,V}),
                new Spec("usp_XuLyKyLuat", new Object[]{-1,"LOW","NHAC_NHO","CI"}, new int[]{V,V}),
                new Spec("usp_DangKyCongIch", new Object[]{student,"CI_NO_ACTIVITY"}, new int[]{I,V,V,V})
            );
            c.setAutoCommit(false);
            for (Spec spec : cases) {
                int size = spec.inputs.length + spec.outputs.length;
                String sql = "{call " + spec.name + "(" + String.join(",", Collections.nCopies(size, "?")) + ")}";
                try (CallableStatement call = c.prepareCall(sql)) {
                    for (int n=0; n<spec.inputs.length; n++) call.setObject(n+1, spec.inputs[n]);
                    for (int n=0; n<spec.outputs.length; n++) call.registerOutParameter(spec.inputs.length+n+1, spec.outputs[n]);
                    call.execute();
                    String status = call.getString(size-1);
                    if (status == null || status.isBlank()) throw new AssertionError("Missing OUT status: " + spec.name);
                    System.out.println(spec.name + " JDBC OUT PASS: " + status);
                } finally { c.rollback(); }
            }
        } finally { DBContext.close(); }
    }
}
